package com.bloodbank.common.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class FlywayMigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("bloodbank_db")
            .withUsername("bloodbank")
            .withPassword("bloodbank");

    @Test
    void allMigrationsShouldRunSuccessfully() {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();

        MigrationInfoService info = flyway.info();
        MigrationInfo[] applied = info.applied();

        assertNotNull(applied);
        assertTrue(applied.length > 0, "At least one migration should have been applied");

        // Verify all migrations were successful (no failures or pending)
        for (MigrationInfo migration : applied) {
            assertNotNull(migration.getState(), "Migration state should not be null for: " + migration.getVersion());
            assertTrue(migration.getState().isApplied(),
                    "Migration " + migration.getVersion() + " should be applied but was: " + migration.getState());
        }

        // Verify no pending migrations
        MigrationInfo[] pending = info.pending();
        assertEquals(0, pending.length, "There should be no pending migrations");
    }

    @Test
    void shouldHaveExpectedNumberOfMigrations() {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();

        MigrationInfo[] applied = flyway.info().applied();
        assertEquals(20, applied.length, "Should have exactly 20 migrations (V1-V20)");
    }
}
