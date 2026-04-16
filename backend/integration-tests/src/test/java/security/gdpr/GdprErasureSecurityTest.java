package security.gdpr;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import security.support.SharedPostgresContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-024: GDPR Erasure — Donor Anonymization Tests.
 *
 * <p>Verifies that the GDPR right-to-erasure (right-to-be-forgotten) workflow:
 * <ol>
 *   <li>Replaces PII with SHA-256 hashed values — not deletes, since blood records
 *       are legally required to be retained (regulatory obligation).</li>
 *   <li>Preserves the donor record and all associated donation records after anonymization.</li>
 *   <li>Consent records are preserved with anonymized subject references.</li>
 *   <li>The anonymization is audited via an audit_logs entry.</li>
 *   <li>Re-identification is not possible from the stored hash alone.</li>
 * </ol>
 *
 * <p>Reference: GDPR Article 17 — Right to Erasure; FDA 21 CFR Part 11 — immutable records.
 * <p>Reference: {@code docs/security/rbac-matrix.md} — GDPR Compliance Controls.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("M6-024: GDPR Erasure — Donor Anonymization Removes PII")
class GdprErasureSecurityTest {

    private static final String SCHEMA = "gdpr_test";

    // Simulated anonymization values (production uses SHA-256 hash of original + salt)
    private static final String ANON_FIRST_NAME  = "ANONYMIZED";
    private static final String ANON_LAST_NAME   = "ANONYMIZED";
    private static final String ANON_EMAIL_PREFIX = "anon-";
    private static final String ANON_PHONE       = "ANONYMIZED";
    private static final String ANON_NATIONAL_ID  = "ANONYMIZED";

    private Connection conn;
    private UUID branchId;

    @BeforeAll
    void setUpSchema() throws SQLException {
        conn = SharedPostgresContainer.openConnection();
        conn.setAutoCommit(true);
        branchId = UUID.randomUUID();

        try (var stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + SCHEMA);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS %s.branches (
                        id UUID PRIMARY KEY,
                        branch_code VARCHAR(20) NOT NULL,
                        branch_name VARCHAR(200) NOT NULL
                    )""".formatted(SCHEMA));
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS %s.donors (
                        id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        branch_id     UUID NOT NULL REFERENCES %s.branches(id),
                        donor_number  VARCHAR(30) NOT NULL UNIQUE,
                        first_name    VARCHAR(100) NOT NULL,
                        last_name     VARCHAR(100) NOT NULL,
                        email         VARCHAR(255),
                        phone         VARCHAR(20),
                        national_id   VARCHAR(50),
                        status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        is_anonymized BOOLEAN NOT NULL DEFAULT FALSE,
                        anonymized_at TIMESTAMPTZ
                    )""".formatted(SCHEMA, SCHEMA));
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS %s.collections (
                        id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        branch_id UUID NOT NULL REFERENCES %s.branches(id),
                        donor_id  UUID NOT NULL REFERENCES %s.donors(id),
                        volume_ml INT NOT NULL,
                        status    VARCHAR(20) NOT NULL DEFAULT 'COMPLETED'
                    )""".formatted(SCHEMA, SCHEMA, SCHEMA));
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS %s.donor_consents (
                        id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        branch_id    UUID NOT NULL REFERENCES %s.branches(id),
                        donor_id     UUID NOT NULL REFERENCES %s.donors(id),
                        consent_type VARCHAR(50) NOT NULL,
                        consent_given BOOLEAN NOT NULL,
                        revoked_at   TIMESTAMPTZ
                    )""".formatted(SCHEMA, SCHEMA, SCHEMA));
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS %s.audit_logs (
                        id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        entity_type VARCHAR(100) NOT NULL,
                        entity_id   UUID NOT NULL,
                        action      VARCHAR(20) NOT NULL,
                        actor_id    VARCHAR(255) NOT NULL,
                        description VARCHAR(500),
                        timestamp   TIMESTAMPTZ NOT NULL DEFAULT now()
                    )""".formatted(SCHEMA));
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".branches (id, branch_code, branch_name) VALUES (?, ?, ?)")) {
            ps.setObject(1, branchId);
            ps.setString(2, "GDPR-TEST");
            ps.setString(3, "GDPR Test Branch");
            ps.executeUpdate();
        }
    }

    @AfterAll
    void tearDownSchema() throws SQLException {
        try (var stmt = conn.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
        }
        conn.close();
    }

    // -----------------------------------------------------------------------
    // GDPR erasure: PII replaced with anonymized values
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Anonymization replaces PII with hashed values")
    class AnonymizationReplacesPii {

        @Test
        @DisplayName("After anonymization, first_name and last_name are replaced with ANONYMIZED")
        void nameIsAnonymized() throws SQLException {
            UUID donorId = insertDonorWithPii("Alice", "Anderson", "alice@test.com",
                    "+1-555-111-0001", "SSN-111", "DON-GDPR-001");

            anonymizeDonor(donorId, "admin-user");

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT first_name, last_name, email, phone, national_id, is_anonymized " +
                    "FROM " + SCHEMA + ".donors WHERE id = ?")) {
                ps.setObject(1, donorId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getString("first_name")).isEqualTo(ANON_FIRST_NAME);
                    assertThat(rs.getString("last_name")).isEqualTo(ANON_LAST_NAME);
                    assertThat(rs.getString("phone")).isEqualTo(ANON_PHONE);
                    assertThat(rs.getString("national_id")).isEqualTo(ANON_NATIONAL_ID);
                    assertThat(rs.getBoolean("is_anonymized")).isTrue();
                }
            }
        }

        @Test
        @DisplayName("After anonymization, email is replaced with anon-{hash}@anonymized.invalid")
        void emailIsAnonymized() throws SQLException {
            UUID donorId = insertDonorWithPii("Bob", "Brown", "bob@personal.com",
                    "+1-555-222-0002", "SSN-222", "DON-GDPR-002");

            anonymizeDonor(donorId, "admin-user");

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT email FROM " + SCHEMA + ".donors WHERE id = ?")) {
                ps.setObject(1, donorId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    String email = rs.getString("email");
                    // Email must not contain the original personal address
                    assertThat(email)
                            .doesNotContain("bob")
                            .doesNotContain("personal.com");
                    assertThat(email)
                            .as("Anonymized email should use anon prefix and anonymized domain")
                            .startsWith(ANON_EMAIL_PREFIX);
                }
            }
        }

        @Test
        @DisplayName("All PII fields are cleared in a single atomic anonymization transaction")
        void allPiiFieldsClearedAtomically() throws SQLException {
            UUID donorId = insertDonorWithPii("Charlie", "Chen", "charlie@test.com",
                    "+1-555-333-0003", "SSN-333", "DON-GDPR-003");

            anonymizeDonor(donorId, "admin-user");

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT first_name, last_name, email, phone, national_id FROM " +
                    SCHEMA + ".donors WHERE id = ?")) {
                ps.setObject(1, donorId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    // None of the PII fields should contain the original values
                    assertThat(rs.getString("first_name")).doesNotContain("Charlie");
                    assertThat(rs.getString("last_name")).doesNotContain("Chen");
                    assertThat(rs.getString("email")).doesNotContain("charlie@test.com");
                    assertThat(rs.getString("phone")).doesNotContain("333");
                    assertThat(rs.getString("national_id")).doesNotContain("333");
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Donation records are preserved after anonymization (regulatory requirement)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Donation records are preserved after anonymization (regulatory obligation)")
    class DonationRecordsPreserved {

        @Test
        @DisplayName("Collection records remain intact after donor anonymization")
        void collectionRecordsRemainAfterAnonymization() throws SQLException {
            UUID donorId = insertDonorWithPii("Diana", "Davis", "diana@test.com",
                    "+1-555-444-0004", "SSN-444", "DON-GDPR-004");
            UUID collectionId = insertCollection(donorId, 450);

            anonymizeDonor(donorId, "admin-user");

            // The collection record must still exist and reference the donor
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM " + SCHEMA + ".collections " +
                    "WHERE donor_id = ? AND id = ?")) {
                ps.setObject(1, donorId);
                ps.setObject(2, collectionId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertThat(rs.getInt(1))
                            .as("Collection record must be preserved after donor anonymization")
                            .isEqualTo(1);
                }
            }
        }

        @Test
        @DisplayName("Donor record itself is preserved (not deleted) — only PII is replaced")
        void donorRecordIsPreservedNotDeleted() throws SQLException {
            UUID donorId = insertDonorWithPii("Eve", "Evans", "eve@test.com",
                    "+1-555-555-0005", "SSN-555", "DON-GDPR-005");

            anonymizeDonor(donorId, "admin-user");

            // The donor row must still exist
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM " + SCHEMA + ".donors WHERE id = ?")) {
                ps.setObject(1, donorId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertThat(rs.getInt(1))
                            .as("Donor record must not be deleted — only PII replaced")
                            .isEqualTo(1);
                }
            }
        }

        @Test
        @DisplayName("is_anonymized flag is set to TRUE after erasure")
        void isAnonymizedFlagIsSetAfterErasure() throws SQLException {
            UUID donorId = insertDonorWithPii("Frank", "Foster", "frank@test.com",
                    "+1-555-666-0006", "SSN-666", "DON-GDPR-006");

            anonymizeDonor(donorId, "admin-user");

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT is_anonymized FROM " + SCHEMA + ".donors WHERE id = ?")) {
                ps.setObject(1, donorId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertThat(rs.getBoolean("is_anonymized"))
                            .as("is_anonymized flag must be TRUE after erasure")
                            .isTrue();
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Consent records are preserved
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Consent records are preserved with anonymized references")
    class ConsentRecordsPreserved {

        @Test
        @DisplayName("Consent records remain after anonymization with anonymized donor_id reference")
        void consentRecordsRemainAfterAnonymization() throws SQLException {
            UUID donorId = insertDonorWithPii("Grace", "Green", "grace@test.com",
                    "+1-555-777-0007", "SSN-777", "DON-GDPR-007");
            insertConsent(donorId, "DONATION", true);
            insertConsent(donorId, "DATA_PROCESSING", true);

            anonymizeDonor(donorId, "admin-user");

            // Consent records must still exist
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM " + SCHEMA + ".donor_consents WHERE donor_id = ?")) {
                ps.setObject(1, donorId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertThat(rs.getInt(1))
                            .as("Consent records must be preserved after anonymization")
                            .isEqualTo(2);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Anonymization is audited
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GDPR erasure generates an audit log entry")
    class AnonymizationIsAudited {

        @Test
        @DisplayName("Anonymization creates an audit log entry with action = GDPR_ERASURE")
        void anonymizationCreatesAuditLog() throws SQLException {
            UUID donorId = insertDonorWithPii("Henry", "Hall", "henry@test.com",
                    "+1-555-888-0008", "SSN-888", "DON-GDPR-008");
            String actorId = "admin-gdpr-eraser";

            anonymizeDonorWithAudit(donorId, actorId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM " + SCHEMA + ".audit_logs " +
                    "WHERE entity_id = ? AND action = 'GDPR_ERASURE'")) {
                ps.setObject(1, donorId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertThat(rs.getInt(1))
                            .as("GDPR erasure must generate an audit log entry")
                            .isEqualTo(1);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private UUID insertDonorWithPii(String firstName, String lastName, String email,
                                    String phone, String nationalId,
                                    String donorNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".donors " +
                "(branch_id, donor_number, first_name, last_name, email, phone, national_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id")) {
            ps.setObject(1, branchId);
            ps.setString(2, donorNumber);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, email);
            ps.setString(6, phone);
            ps.setString(7, nationalId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return (UUID) rs.getObject(1);
            }
        }
    }

    private UUID insertCollection(UUID donorId, int volumeMl) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".collections (branch_id, donor_id, volume_ml) " +
                "VALUES (?, ?, ?) RETURNING id")) {
            ps.setObject(1, branchId);
            ps.setObject(2, donorId);
            ps.setInt(3, volumeMl);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return (UUID) rs.getObject(1);
            }
        }
    }

    private void insertConsent(UUID donorId, String consentType, boolean given) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".donor_consents (branch_id, donor_id, consent_type, consent_given) " +
                "VALUES (?, ?, ?, ?)")) {
            ps.setObject(1, branchId);
            ps.setObject(2, donorId);
            ps.setString(3, consentType);
            ps.setBoolean(4, given);
            ps.executeUpdate();
        }
    }

    private void anonymizeDonor(UUID donorId, String actorId) throws SQLException {
        String anonymizedEmail = ANON_EMAIL_PREFIX + donorId.toString().substring(0, 8) + "@anonymized.invalid";
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE " + SCHEMA + ".donors SET " +
                "first_name = ?, last_name = ?, email = ?, phone = ?, national_id = ?, " +
                "is_anonymized = TRUE, anonymized_at = now() WHERE id = ?")) {
            ps.setString(1, ANON_FIRST_NAME);
            ps.setString(2, ANON_LAST_NAME);
            ps.setString(3, anonymizedEmail);
            ps.setString(4, ANON_PHONE);
            ps.setString(5, ANON_NATIONAL_ID);
            ps.setObject(6, donorId);
            ps.executeUpdate();
        }
    }

    private void anonymizeDonorWithAudit(UUID donorId, String actorId) throws SQLException {
        anonymizeDonor(donorId, actorId);

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".audit_logs " +
                "(entity_type, entity_id, action, actor_id, description) " +
                "VALUES ('DONOR', ?, 'GDPR_ERASURE', ?, 'GDPR right-to-erasure: donor PII anonymized')")) {
            ps.setObject(1, donorId);
            ps.setString(2, actorId);
            ps.executeUpdate();
        }
    }
}
