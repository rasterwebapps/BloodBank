plugins {
    `java-library`
    id("org.flywaydb.flyway") version "10.21.0"
}

dependencies {
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
}

flyway {
    url = "jdbc:postgresql://localhost:5432/bloodbank_db"
    user = "bloodbank"
    password = "bloodbank"
    schemas = arrayOf("public")
    locations = arrayOf("classpath:db/migration")
}
