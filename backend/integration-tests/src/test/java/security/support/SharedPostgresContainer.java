package security.support;

import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton PostgreSQL Testcontainer shared across all database-backed security tests.
 * Started once per JVM via the static initializer block, keeping test suite startup fast.
 */
public final class SharedPostgresContainer {

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> CONTAINER =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("bloodbank_security_test")
                    .withUsername("sectest")
                    .withPassword("sectest");

    static {
        CONTAINER.start();
    }

    private SharedPostgresContainer() {
        // utility class
    }

    public static PostgreSQLContainer<?> getInstance() {
        return CONTAINER;
    }

    public static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                CONTAINER.getJdbcUrl(),
                CONTAINER.getUsername(),
                CONTAINER.getPassword());
    }

    public static void execute(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
