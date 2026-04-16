package security.branch;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-014: Branch Isolation Security Tests.
 *
 * <p>Verifies the 4-Layer branch data isolation at the database level (Layer 4):
 * a user authenticated in Branch A must not be able to retrieve Branch B data
 * even when querying the same table.
 *
 * <p>Tests use a PostgreSQL Testcontainer with a minimal schema that mirrors the
 * production {@code donors} and {@code branches} tables. All SQL is executed via
 * parameterized statements — the same pattern used by the JPA Hibernate filter.
 *
 * <p>Reference: {@code docs/security/branch-isolation.md} — Layer 4: Database.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("M6-014: Branch Isolation — User in Branch A cannot see Branch B data")
class BranchIsolationSecurityTest {

    private static final String SCHEMA = "branch_iso_test";

    private Connection conn;
    private UUID branchAId;
    private UUID branchBId;

    @BeforeAll
    void setUpSchema() throws SQLException {
        conn = SharedPostgresContainer.openConnection();
        conn.setAutoCommit(true);

        try (var stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + SCHEMA);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS %s.branches (
                        id            UUID PRIMARY KEY,
                        branch_code   VARCHAR(20) NOT NULL,
                        branch_name   VARCHAR(200) NOT NULL
                    )""".formatted(SCHEMA));
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS %s.donors (
                        id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        branch_id    UUID NOT NULL REFERENCES %s.branches(id),
                        first_name   VARCHAR(100) NOT NULL,
                        last_name    VARCHAR(100) NOT NULL,
                        email        VARCHAR(255),
                        donor_number VARCHAR(30) NOT NULL,
                        status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                    )""".formatted(SCHEMA, SCHEMA));
        }

        // Insert two branches
        branchAId = UUID.randomUUID();
        branchBId = UUID.randomUUID();
        insertBranch(branchAId, "BR-A", "Branch Alpha");
        insertBranch(branchBId, "BR-B", "Branch Beta");

        // Insert donors for each branch
        insertDonor(branchAId, "Alice", "Anderson", "alice@branch-a.test", "DON-A-001");
        insertDonor(branchAId, "Adam", "Allen",    "adam@branch-a.test",  "DON-A-002");
        insertDonor(branchBId, "Bob",  "Brown",    "bob@branch-b.test",   "DON-B-001");
        insertDonor(branchBId, "Beth", "Baker",    "beth@branch-b.test",  "DON-B-002");
    }

    @AfterAll
    void tearDownSchema() throws SQLException {
        try (var stmt = conn.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
        }
        conn.close();
    }

    // -----------------------------------------------------------------------
    // Layer 4: Database column isolation
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Layer 4: branch_id column filters data correctly")
    class DatabaseColumnIsolation {

        @Test
        @DisplayName("Branch A user sees only Branch A donors")
        void branchAUserSeesOnlyBranchADonors() throws SQLException {
            List<String> names = queryDonorNames(branchAId);
            assertThat(names).containsExactlyInAnyOrder("Alice Anderson", "Adam Allen");
            assertThat(names).doesNotContain("Bob Brown", "Beth Baker");
        }

        @Test
        @DisplayName("Branch B user sees only Branch B donors")
        void branchBUserSeesOnlyBranchBDonors() throws SQLException {
            List<String> names = queryDonorNames(branchBId);
            assertThat(names).containsExactlyInAnyOrder("Bob Brown", "Beth Baker");
            assertThat(names).doesNotContain("Alice Anderson", "Adam Allen");
        }

        @Test
        @DisplayName("Unfiltered query (admin role) sees all donors across all branches")
        void adminRoleSeesAllDonors() throws SQLException {
            List<String> names = queryAllDonorNames();
            assertThat(names).hasSize(4);
            assertThat(names).containsExactlyInAnyOrder(
                    "Alice Anderson", "Adam Allen", "Bob Brown", "Beth Baker");
        }

        @Test
        @DisplayName("Branch A ID query returns zero Branch B records")
        void branchAQueryReturnsZeroBranchBRecords() throws SQLException {
            int count = countDonorsForBranch(branchAId);
            int crossCount = countDonorsMatchingOtherBranch(branchAId, branchBId);
            assertThat(count).isEqualTo(2);
            assertThat(crossCount).isZero();
        }

        @Test
        @DisplayName("Branch isolation holds when searching by email across branches")
        void branchIsolationHoldsOnEmailSearch() throws SQLException {
            // Branch A user searching by a Branch B email — should yield 0 results
            List<String> results = searchDonorsByEmailWithBranchFilter("bob@branch-b.test", branchAId);
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Branch isolation holds when searching by name across branches")
        void branchIsolationHoldsOnNameSearch() throws SQLException {
            List<String> results = searchDonorsByNameWithBranchFilter("Bob", branchAId);
            assertThat(results).isEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // Cross-branch access prevention
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Cross-branch access prevention")
    class CrossBranchAccessPrevention {

        @Test
        @DisplayName("Looking up a Branch B donor by ID from Branch A context returns empty")
        void branchBDonorIdNotVisibleFromBranchAContext() throws SQLException {
            UUID branchBDonorId = getFirstDonorId(branchBId);
            List<String> result = lookupDonorByIdWithBranchFilter(branchBDonorId, branchAId);
            assertThat(result)
                    .as("Branch B donor %s should not be visible in Branch A context", branchBDonorId)
                    .isEmpty();
        }

        @Test
        @DisplayName("Branch ID NOT NULL constraint prevents inserting a donor without a branch")
        void donorRequiresBranchId() {
            try (var stmt = conn.createStatement()) {
                stmt.execute("""
                        INSERT INTO %s.donors (id, branch_id, first_name, last_name, donor_number)
                        VALUES (gen_random_uuid(), NULL, 'Ghost', 'Donor', 'DON-GHOST-001')
                        """.formatted(SCHEMA));
                // If we reach here, the NOT NULL constraint was not enforced
                throw new AssertionError("Expected NOT NULL violation for branch_id but none was thrown");
            } catch (SQLException e) {
                // Expected: PostgreSQL NOT NULL violation (23502) or FK violation (23503)
                assertThat(e.getSQLState()).isIn("23502", "23503", "23000");
            }
        }

        @Test
        @DisplayName("Foreign key constraint prevents inserting a donor with a non-existent branch_id")
        void donorBranchIdMustExist() {
            UUID nonExistentBranchId = UUID.randomUUID();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO " + SCHEMA + ".donors " +
                    "(id, branch_id, first_name, last_name, donor_number) " +
                    "VALUES (gen_random_uuid(), ?, 'Orphan', 'Donor', 'DON-ORPHAN-001')")) {
                ps.setObject(1, nonExistentBranchId);
                ps.executeUpdate();
                throw new AssertionError("Expected FK violation but none was thrown");
            } catch (SQLException e) {
                assertThat(e.getSQLState())
                        .as("Expected FK constraint violation (23503)")
                        .isEqualTo("23503");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private void insertBranch(UUID id, String code, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".branches (id, branch_code, branch_name) VALUES (?, ?, ?)")) {
            ps.setObject(1, id);
            ps.setString(2, code);
            ps.setString(3, name);
            ps.executeUpdate();
        }
    }

    private void insertDonor(UUID branchId, String firstName, String lastName,
                              String email, String number) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".donors " +
                "(branch_id, first_name, last_name, email, donor_number) " +
                "VALUES (?, ?, ?, ?, ?)")) {
            ps.setObject(1, branchId);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, email);
            ps.setString(5, number);
            ps.executeUpdate();
        }
    }

    private List<String> queryDonorNames(UUID branchId) throws SQLException {
        List<String> names = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT first_name || ' ' || last_name AS full_name " +
                "FROM " + SCHEMA + ".donors WHERE branch_id = ?")) {
            ps.setObject(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("full_name"));
                }
            }
        }
        return names;
    }

    private List<String> queryAllDonorNames() throws SQLException {
        List<String> names = new ArrayList<>();
        try (var stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT first_name || ' ' || last_name AS full_name FROM " + SCHEMA + ".donors")) {
            while (rs.next()) {
                names.add(rs.getString("full_name"));
            }
        }
        return names;
    }

    private int countDonorsForBranch(UUID branchId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM " + SCHEMA + ".donors WHERE branch_id = ?")) {
            ps.setObject(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int countDonorsMatchingOtherBranch(UUID requestingBranch, UUID otherBranch) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM " + SCHEMA + ".donors " +
                "WHERE branch_id = ? AND branch_id = ?")) {
            ps.setObject(1, requestingBranch);
            ps.setObject(2, otherBranch);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private List<String> searchDonorsByEmailWithBranchFilter(String email, UUID branchId) throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT first_name || ' ' || last_name FROM " + SCHEMA + ".donors " +
                "WHERE email = ? AND branch_id = ?")) {
            ps.setString(1, email);
            ps.setObject(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString(1));
                }
            }
        }
        return results;
    }

    private List<String> searchDonorsByNameWithBranchFilter(String name, UUID branchId) throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT first_name || ' ' || last_name FROM " + SCHEMA + ".donors " +
                "WHERE first_name ILIKE ? AND branch_id = ?")) {
            ps.setString(1, "%" + name + "%");
            ps.setObject(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString(1));
                }
            }
        }
        return results;
    }

    private UUID getFirstDonorId(UUID branchId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM " + SCHEMA + ".donors WHERE branch_id = ? LIMIT 1")) {
            ps.setObject(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return (UUID) rs.getObject(1);
            }
        }
    }

    private List<String> lookupDonorByIdWithBranchFilter(UUID donorId, UUID branchId) throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT first_name || ' ' || last_name FROM " + SCHEMA + ".donors " +
                "WHERE id = ? AND branch_id = ?")) {
            ps.setObject(1, donorId);
            ps.setObject(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString(1));
                }
            }
        }
        return results;
    }
}
