# =============================================================================
# Flyway migration image — runs once before any application service starts.
# Pinned to the same Flyway version used by the Spring Boot runtime
# (see gradle.properties → flywayVersion) to avoid checksum drift.
# =============================================================================
FROM flyway/flyway:10.21.0-alpine

# Copy migrations from the shared-libs module
COPY src/main/resources/db/migration /flyway/sql

# Drop privileges (the base image ships a `flyway` user)
USER flyway

# Production-grade defaults; the connection details are injected at runtime via
# the SPRING_DATASOURCE_URL / DB_USERNAME / DB_PASSWORD env vars (set by
# docker-compose, the k8s Job, or the CI pipeline).
ENTRYPOINT ["sh", "-c", "exec flyway \
    -url=\"${SPRING_DATASOURCE_URL}\" \
    -user=\"${DB_USERNAME}\" \
    -password=\"${DB_PASSWORD}\" \
    -locations=\"filesystem:/flyway/sql\" \
    -schemas=\"${FLYWAY_SCHEMAS:-public}\" \
    -baselineOnMigrate=true \
    -baselineVersion=0 \
    -validateOnMigrate=true \
    -cleanDisabled=true \
    -connectRetries=${FLYWAY_CONNECT_RETRIES:-30} \
    migrate"]
