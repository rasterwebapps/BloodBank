FROM flyway/flyway:10-alpine
COPY src/main/resources/db/migration /flyway/sql
# Use shell form so environment variables are expanded at runtime
ENTRYPOINT flyway \
    -url="${SPRING_DATASOURCE_URL}" \
    -user="${DB_USERNAME}" \
    -password="${DB_PASSWORD}" \
    -locations="filesystem:/flyway/sql" \
    migrate
