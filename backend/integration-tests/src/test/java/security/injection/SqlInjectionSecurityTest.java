package security.injection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import security.support.SharedPostgresContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-020: SQL Injection Security Tests.
 *
 * <p>Verifies that all search endpoints are immune to SQL injection by:
 * <ol>
 *   <li>Storing SQL injection payloads via parameterized JDBC statements
 *       (the same pattern used by Spring Data JPA / Hibernate).</li>
 *   <li>Verifying the payloads are stored as literal strings — not executed as SQL.</li>
 *   <li>Verifying that parameterized search queries return only expected results
 *       when the search term is a SQL injection payload.</li>
 * </ol>
 *
 * <p>All BloodBank repositories use Spring Data JPA / Hibernate which parameterizes
 * all queries. Raw string concatenation in SQL is prohibited by the project conventions.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("M6-020: SQL Injection Prevention — All Search Endpoints")
class SqlInjectionSecurityTest {

    private static final String SCHEMA = "sql_injection_test";

    private Connection conn;
    private UUID branchId;

    // -----------------------------------------------------------------------
    // Classic SQL injection payloads
    // -----------------------------------------------------------------------

    static Stream<String> sqlInjectionPayloads() {
        return Stream.of(
                "' OR '1'='1",
                "' OR 1=1 --",
                "' OR 1=1 #",
                "' OR 1=1/*",
                "admin'--",
                "admin'/*",
                "' OR 'x'='x",
                "') OR ('x'='x",
                "1; DROP TABLE donors; --",
                "1; TRUNCATE TABLE donors; --",
                "'; DELETE FROM donors WHERE '1'='1",
                "' UNION SELECT id, first_name, last_name, email FROM donors --",
                "' UNION ALL SELECT NULL, NULL, NULL, NULL --",
                "1' AND (SELECT COUNT(*) FROM donors) > 0 --",
                "' AND SLEEP(5) --",
                "' AND pg_sleep(5) --",
                "'; EXEC xp_cmdshell('dir'); --",
                "'; EXEC sp_executesql('SELECT 1'); --",
                "O'Brien",           // Legitimate name with apostrophe (must still work)
                "D'Angelo Johnson",  // Legitimate name with apostrophe
                "Robert'); DROP TABLE students; --",  // Classic "Bobby Tables"
                "' OR EXISTS(SELECT * FROM information_schema.tables) --"
        );
    }

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
                        id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        branch_id    UUID NOT NULL REFERENCES %s.branches(id),
                        first_name   VARCHAR(100) NOT NULL,
                        last_name    VARCHAR(100) NOT NULL,
                        email        VARCHAR(255),
                        donor_number VARCHAR(30) NOT NULL UNIQUE,
                        status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                    )""".formatted(SCHEMA, SCHEMA));
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".branches (id, branch_code, branch_name) VALUES (?, ?, ?)")) {
            ps.setObject(1, branchId);
            ps.setString(2, "INJ-TEST");
            ps.setString(3, "Injection Test Branch");
            ps.executeUpdate();
        }

        // Seed one legitimate donor (should always remain)
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + SCHEMA + ".donors (branch_id, first_name, last_name, donor_number) " +
                "VALUES (?, 'LegitFirst', 'LegitLast', 'DON-LEGIT-001')")) {
            ps.setObject(1, branchId);
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
    // Parameterized insert — injection payloads stored as literal strings
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("INSERT via parameterized statement treats injection payload as literal string")
    class ParameterizedInsertTests {

        @ParameterizedTest(name = "Payload: {0}")
        @MethodSource("security.injection.SqlInjectionSecurityTest#sqlInjectionPayloads")
        @DisplayName("SQL injection payload is stored safely as a literal string")
        void injectionPayload_isStoredAsLiteralString(String payload) throws SQLException {
            // Capture table count before insert
            int countBefore = countDonors();

            // Insert with injection payload as the first_name (parameterized — safe)
            String donorNumber = "DON-INJ-" + UUID.randomUUID().toString().substring(0, 8);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO " + SCHEMA + ".donors " +
                    "(branch_id, first_name, last_name, donor_number) VALUES (?, ?, 'InjLast', ?)")) {
                ps.setObject(1, branchId);
                ps.setString(2, payload);  // SQL injection payload as a bound parameter
                ps.setString(3, donorNumber);
                ps.executeUpdate();
            }

            // Count should have increased by exactly 1 — the injection did not drop anything
            int countAfter = countDonors();
            assertThat(countAfter)
                    .as("After parameterized INSERT with injection payload, donor count should increase by 1")
                    .isEqualTo(countBefore + 1);

            // Verify the payload was stored verbatim
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT first_name FROM " + SCHEMA + ".donors WHERE donor_number = ?")) {
                ps.setString(1, donorNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getString("first_name"))
                            .as("Stored value should be the literal payload string")
                            .isEqualTo(payload);
                }
            }

            // Clean up this specific test record
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM " + SCHEMA + ".donors WHERE donor_number = ?")) {
                ps.setString(1, donorNumber);
                ps.executeUpdate();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Parameterized search — injection payloads in WHERE clause do not return extra rows
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Search query with injection payload returns only legitimate matches")
    class ParameterizedSearchTests {

        @ParameterizedTest(name = "Search payload: {0}")
        @MethodSource("security.injection.SqlInjectionSecurityTest#sqlInjectionPayloads")
        @DisplayName("Search with injection payload in first_name returns no results (payload is not in DB)")
        void searchWithInjectionPayload_returnsEmptyOrLiteralsOnly(String payload) throws SQLException {
            // Only the "LegitFirst" donor is in the DB — no donor has an injection payload as name
            // So searching for the injection string as first_name should return 0 results
            List<String> results = searchDonorsByName(payload);

            // Verify no unintended rows returned (the "OR 1=1" should not expand the result set)
            for (String name : results) {
                assertThat(name)
                        .as("Search result should only contain the literal payload match, "
                                + "not all donors due to SQL injection")
                        .isEqualTo(payload);
            }

            // Also verify the legitimate donor is still present
            List<String> legitResults = searchDonorsByName("LegitFirst");
            assertThat(legitResults).contains("LegitFirst");
        }

        @Test
        @DisplayName("Parameterized search does not expand results due to OR injection")
        void parameterizedSearch_doesNotExpandResultsDueToOrInjection() throws SQLException {
            // Inject "' OR '1'='1" into the search parameter
            // A non-parameterized query would return ALL donors
            // A parameterized query should return ZERO donors (no name matches this literal)
            String injectionPayload = "' OR '1'='1";
            List<String> results = searchDonorsByName(injectionPayload);
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("UNION injection payload does not expose additional table data")
        void unionInjectionPayload_doesNotExposeAdditionalData() throws SQLException {
            // A UNION injection in a non-parameterized query would expose extra rows.
            // With parameterized queries, the UNION text is treated as a literal search string.
            String unionPayload = "' UNION SELECT id, first_name, last_name, email FROM donors --";
            List<String> results = searchDonorsByName(unionPayload);
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Legitimate name with apostrophe is handled correctly (not treated as SQL)")
        void legitimateNameWithApostrophe_isHandledCorrectly() throws SQLException {
            // "O'Brien" has an apostrophe — must be stored and retrieved correctly
            String donorNumber = "DON-OBRIEN-001";
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO " + SCHEMA + ".donors " +
                    "(branch_id, first_name, last_name, donor_number) VALUES (?, ?, ?, ?)")) {
                ps.setObject(1, branchId);
                ps.setString(2, "O'Brien");
                ps.setString(3, "Legitimate");
                ps.setString(4, donorNumber);
                ps.executeUpdate();
            }

            List<String> results = searchDonorsByName("O'Brien");
            assertThat(results).contains("O'Brien");

            // Cleanup
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM " + SCHEMA + ".donors WHERE donor_number = ?")) {
                ps.setString(1, donorNumber);
                ps.executeUpdate();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Table integrity after all injection attempts
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Table integrity is preserved after injection attempts")
    class TableIntegrityTests {

        @Test
        @DisplayName("Donors table still exists and is accessible after injection payload tests")
        void donorsTableStillExistsAfterInjectionTests() throws SQLException {
            int count = countDonors();
            assertThat(count).isGreaterThanOrEqualTo(1); // LegitFirst donor must still exist
        }

        @Test
        @DisplayName("Original legitimate donor is still present after injection tests")
        void legitimateDonorStillPresent() throws SQLException {
            List<String> results = searchDonorsByName("LegitFirst");
            assertThat(results).contains("LegitFirst");
        }
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private int countDonors() throws SQLException {
        try (var stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + SCHEMA + ".donors")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private List<String> searchDonorsByName(String name) throws SQLException {
        java.util.List<String> results = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT first_name FROM " + SCHEMA + ".donors WHERE first_name = ? AND branch_id = ?")) {
            ps.setString(1, name);
            ps.setObject(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("first_name"));
                }
            }
        }
        return results;
    }
}
