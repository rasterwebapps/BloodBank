plugins {
    `java-library`
    id("org.flywaydb.flyway") version "10.21.0"
}

dependencies {
    // These two are needed by the Flyway Gradle plugin's own classloader (separate from runtimeOnly)
    flyway("org.postgresql:postgresql")
    flyway("org.flywaydb:flyway-database-postgresql")

    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

flyway {
    url = "jdbc:postgresql://localhost:5432/bloodbank_db"
    user = "bloodbank"
    password = "bloodbank"
    schemas = arrayOf("public")
    locations = arrayOf("classpath:db/migration")
}
