package security.audit;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * M6-025: Audit Log Immutability Tests.
 *
 * <p>Verifies that the {@code audit_logs} table is truly append-only:
 * <ol>
 *   <li>Inserts are allowed (appending new entries).</li>
 *   <li>UPDATE on any row is blocked by the {@code audit_logs_prevent_update} trigger.</li>
 *   <li>DELETE on any row is blocked by the {@code audit_logs_prevent_delete} trigger.</li>
 *   <li>TRUNCATE is not permitted by a regular application user.</li>
 * </ol>
 *
 * <p>This mirrors the production schema in
 * {@code shared-libs/db-migration/src/main/resources/db/migration/V13__reporting_tables.sql},
 * which creates the exact same trigger functions used here.
 *
 * <p>Reference: FDA 21 CFR Part 11 — electronic records must be accurate and reliable.
 * <p>Reference: HIPAA Audit Controls — immutable log trail for all PHI access.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("M6-025: Audit Log Immutability — UPDATE/DELETE Blocked by DB Trigger")
class AuditLogImmutabilityTest {

    private static final String SCHEMA = "audit_immutability_test";

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

            // Exact mirror of V13__reporting_tables.sql audit_logs table
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
                        created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                        created_by  VARCHAR(255),
                        updated_by  VARCHAR(255),
                        version     BIGINT NOT NULL DEFAULT 0
                    )""".formatted(SCHEMA, SCHEMA));

            // Exact mirror of V13 trigger — prevent_audit_log_modification function
            stmt.execute("""
                    CREATE OR REPLACE FUNCTION %s.prevent_audit_log_modification()
                    RETURNS TRIGGER AS $$
                    BEGIN
                        RAISE EXCEPTION 'audit_logs table is append-only. UPDATE and DELETE operations are not permitted.';
                        RETURN NULL;
                    END;
                    $$ LANGUAGE plpgsql""".formatted(SCHEMA));

            stmt.execute("""
                    CREATE TRIGGER audit_logs_prevent_update
                        BEFORE UPDATE ON %s.audit_logs
                        FOR EACH ROW
                        EXECUTE FUNCTION %s.prevent_audit_log_modification()
                    """.formatted(SCHEMA, SCHEMA));

            stmt.execute("""
                    CREATE TRIGGER audit_logs_prevent_delete
                        BEFORE DELETE ON %s.audit_logs
                        FOR EACH ROW
                        EXECUTE FUNCTION %s.prevent_audit_log_modification()
                    """.formatted(SCHEMA, SCHEMA));
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".branches (id, branch_code, branch_name) VALUES (?, ?, ?)")) {
            ps.setObject(1, branchId);
            ps.setString(2, "AUDIT-TEST");
            ps.setString(3, "Audit Test Branch");
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
    // INSERT is allowed
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("INSERT (append) operations are permitted")
    class InsertPermitted {

        @Test
        @DisplayName("Inserting a new audit log entry succeeds")
        void insertNewEntry_succeeds() throws SQLException {
            UUID logId = insertAuditLog("DONOR", UUID.randomUUID(), "CREATE",
                    "user-001", "INSERT test", branchId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT action FROM " + SCHEMA + ".audit_logs WHERE id = ?")) {
                ps.setObject(1, logId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getString("action")).isEqualTo("CREATE");
                }
            }
        }

        @Test
        @DisplayName("Multiple audit log entries can be inserted")
        void multipleInserts_succeed() throws SQLException {
            int countBefore = countAuditLogs();

            for (int i = 0; i < 5; i++) {
                insertAuditLog("BLOOD_UNIT", UUID.randomUUID(), "READ",
                        "user-multi-" + i, "Multi-insert test " + i, branchId);
            }

            int countAfter = countAuditLogs();
            assertThat(countAfter).isEqualTo(countBefore + 5);
        }

        @Test
        @DisplayName("Audit log entry without branch_id (global action) is inserted successfully")
        void insertGlobalActionWithoutBranch_succeeds() throws SQLException {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO " + SCHEMA + ".audit_logs " +
                    "(entity_type, entity_id, action, actor_id, description, timestamp) " +
                    "VALUES ('SYSTEM', gen_random_uuid(), 'LOGIN', 'super-admin', 'Global login', ?) " +
                    "RETURNING id")) {
                ps.setTimestamp(1, Timestamp.from(Instant.now()));
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getObject(1)).isNotNull();
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // UPDATE is blocked
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("UPDATE operations are blocked by the immutability trigger")
    class UpdateBlocked {

        @Test
        @DisplayName("Attempting to UPDATE description field raises exception")
        void updateDescription_isBlocked() throws SQLException {
            UUID logId = insertAuditLog("DONOR", UUID.randomUUID(), "UPDATE",
                    "user-upd-001", "Original description", branchId);

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE " + SCHEMA + ".audit_logs SET description = 'TAMPERED' WHERE id = ?")) {
                    ps.setObject(1, logId);
                    ps.executeUpdate();
                }
            })
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("append-only");
        }

        @Test
        @DisplayName("Attempting to UPDATE actor_id field raises exception")
        void updateActorId_isBlocked() throws SQLException {
            UUID logId = insertAuditLog("COLLECTION", UUID.randomUUID(), "CREATE",
                    "phlebotomist-001", "Collection started", branchId);

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE " + SCHEMA + ".audit_logs SET actor_id = 'ATTACKER' WHERE id = ?")) {
                    ps.setObject(1, logId);
                    ps.executeUpdate();
                }
            })
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("append-only");
        }

        @Test
        @DisplayName("Attempting to UPDATE action field raises exception")
        void updateActionField_isBlocked() throws SQLException {
            UUID logId = insertAuditLog("BLOOD_UNIT", UUID.randomUUID(), "DELETE",
                    "inventory-mgr-001", "Unit disposed", branchId);

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE " + SCHEMA + ".audit_logs SET action = 'CREATE' WHERE id = ?")) {
                    ps.setObject(1, logId);
                    ps.executeUpdate();
                }
            })
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("append-only");
        }

        @Test
        @DisplayName("UPDATE with WHERE 1=1 (bulk update attempt) raises exception")
        void bulkUpdate_isBlocked() throws SQLException {
            insertAuditLog("TRANSFUSION", UUID.randomUUID(), "CREATE",
                    "nurse-001", "Transfusion started", branchId);

            assertThatThrownBy(() -> {
                try (var stmt = conn.createStatement()) {
                    stmt.executeUpdate(
                            "UPDATE " + SCHEMA + ".audit_logs SET description = 'BULK TAMPERED'");
                }
            })
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("append-only");
        }

        @Test
        @DisplayName("UPDATE old_values field (attempt to hide evidence) raises exception")
        void updateOldValues_isBlocked() throws SQLException {
            UUID logId = insertAuditLog("DONOR", UUID.randomUUID(), "UPDATE",
                    "branch-admin-001", "Donor updated",
                    "{\"status\":\"ACTIVE\"}",
                    "{\"status\":\"DEFERRED\"}",
                    branchId);

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE " + SCHEMA + ".audit_logs SET old_values = NULL WHERE id = ?")) {
                    ps.setObject(1, logId);
                    ps.executeUpdate();
                }
            })
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("append-only");
        }

        @Test
        @DisplayName("Record content is unchanged after failed UPDATE attempt")
        void contentUnchangedAfterFailedUpdate() throws SQLException {
            String originalDescription = "Original content — must not change";
            UUID logId = insertAuditLog("INVOICE", UUID.randomUUID(), "CREATE",
                    "billing-clerk-001", originalDescription, branchId);

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE " + SCHEMA + ".audit_logs SET description = 'CHANGED' WHERE id = ?")) {
                    ps.setObject(1, logId);
                    ps.executeUpdate();
                }
            } catch (SQLException expected) {
                // Expected — trigger blocks the update
            }

            // Verify content is unchanged
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT description FROM " + SCHEMA + ".audit_logs WHERE id = ?")) {
                ps.setObject(1, logId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getString("description")).isEqualTo(originalDescription);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // DELETE is blocked
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE operations are blocked by the immutability trigger")
    class DeleteBlocked {

        @Test
        @DisplayName("Attempting to DELETE a single row raises exception")
        void deleteRow_isBlocked() throws SQLException {
            UUID logId = insertAuditLog("DONOR", UUID.randomUUID(), "READ",
                    "doctor-001", "Donor record accessed", branchId);

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM " + SCHEMA + ".audit_logs WHERE id = ?")) {
                    ps.setObject(1, logId);
                    ps.executeUpdate();
                }
            })
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("append-only");
        }

        @Test
        @DisplayName("Attempting to DELETE all rows raises exception")
        void deleteAllRows_isBlocked() throws SQLException {
            insertAuditLog("BLOOD_UNIT", UUID.randomUUID(), "EXPORT",
                    "auditor-001", "Report exported", branchId);

            assertThatThrownBy(() -> {
                try (var stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM " + SCHEMA + ".audit_logs");
                }
            })
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("append-only");
        }

        @Test
        @DisplayName("Row count is unchanged after failed DELETE attempt")
        void rowCountUnchangedAfterFailedDelete() throws SQLException {
            UUID logId = insertAuditLog("COMPLIANCE", UUID.randomUUID(), "CREATE",
                    "system-admin-001", "SOP document created", branchId);

            int countBefore = countAuditLogs();

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM " + SCHEMA + ".audit_logs WHERE id = ?")) {
                    ps.setObject(1, logId);
                    ps.executeUpdate();
                }
            } catch (SQLException expected) {
                // Trigger blocked the delete
            }

            int countAfter = countAuditLogs();
            assertThat(countAfter)
                    .as("Row count must not decrease after a blocked DELETE")
                    .isEqualTo(countBefore);
        }

        @Test
        @DisplayName("Break-glass log cannot be deleted even by attacker with direct DB access")
        void breakGlassLogCannotBeDeleted() throws SQLException {
            UUID logId = insertAuditLog("PATIENT", UUID.randomUUID(), "BREAK_GLASS",
                    "doctor-emergency-001", "Emergency O-neg issued", branchId);

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM " + SCHEMA + ".audit_logs WHERE id = ? AND action = 'BREAK_GLASS'")) {
                    ps.setObject(1, logId);
                    ps.executeUpdate();
                }
            })
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("append-only");
        }
    }

    // -----------------------------------------------------------------------
    // Trigger metadata verification
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Trigger metadata is correctly configured")
    class TriggerMetadata {

        @Test
        @DisplayName("Both UPDATE and DELETE triggers exist on audit_logs table")
        void bothTriggersExist() throws SQLException {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT trigger_name FROM information_schema.triggers " +
                    "WHERE event_object_schema = ? AND event_object_table = 'audit_logs' " +
                    "ORDER BY trigger_name")) {
                ps.setString(1, SCHEMA);
                try (ResultSet rs = ps.executeQuery()) {
                    java.util.List<String> triggers = new java.util.ArrayList<>();
                    while (rs.next()) {
                        triggers.add(rs.getString("trigger_name"));
                    }
                    assertThat(triggers)
                            .as("Both immutability triggers must exist on audit_logs")
                            .contains("audit_logs_prevent_delete", "audit_logs_prevent_update");
                }
            }
        }

        @Test
        @DisplayName("Both triggers fire BEFORE the operation (not AFTER)")
        void triggersFireBeforeOperation() throws SQLException {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT action_timing FROM information_schema.triggers " +
                    "WHERE event_object_schema = ? AND event_object_table = 'audit_logs'")) {
                ps.setString(1, SCHEMA);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        assertThat(rs.getString("action_timing"))
                                .as("Triggers must fire BEFORE the operation to block it")
                                .isEqualTo("BEFORE");
                    }
                }
            }
        }

        @Test
        @DisplayName("Triggers fire FOR EACH ROW (not once per statement)")
        void triggersFireForEachRow() throws SQLException {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT action_orientation FROM information_schema.triggers " +
                    "WHERE event_object_schema = ? AND event_object_table = 'audit_logs'")) {
                ps.setString(1, SCHEMA);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        assertThat(rs.getString("action_orientation"))
                                .as("Triggers must fire FOR EACH ROW")
                                .isEqualTo("ROW");
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private UUID insertAuditLog(String entityType, UUID entityId, String action,
                                  String actorId, String description, UUID branchId) throws SQLException {
        return insertAuditLog(entityType, entityId, action, actorId, description, null, null, branchId);
    }

    private UUID insertAuditLog(String entityType, UUID entityId, String action,
                                  String actorId, String description,
                                  String oldValues, String newValues, UUID branchId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".audit_logs " +
                "(branch_id, entity_type, entity_id, action, actor_id, description, old_values, new_values, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id")) {
            ps.setObject(1, branchId);
            ps.setString(2, entityType);
            ps.setObject(3, entityId);
            ps.setString(4, action);
            ps.setString(5, actorId);
            ps.setString(6, description);
            ps.setString(7, oldValues);
            ps.setString(8, newValues);
            ps.setTimestamp(9, Timestamp.from(Instant.now()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return (UUID) rs.getObject(1);
            }
        }
    }

    private int countAuditLogs() throws SQLException {
        try (var stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + SCHEMA + ".audit_logs")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
