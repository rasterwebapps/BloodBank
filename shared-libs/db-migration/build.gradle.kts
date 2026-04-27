plugins {
    `java-library`
    id("org.flywaydb.flyway") version "10.21.0"
}

dependencies {
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // The Flyway Gradle plugin runs in its own isolated classloader and does NOT
    // inherit from the project's runtimeClasspath. The 'flyway' configuration is
    // the only way to put jars on the plugin's classpath.
    add("flyway", "org.postgresql:postgresql")
    add("flyway", "org.flywaydb:flyway-database-postgresql")

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
