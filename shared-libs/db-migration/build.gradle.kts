// ─────────────────────────────────────────────────────────────────────────────
// Flyway Gradle plugin runs in its own isolated classloader. Starting with the
// Flyway 10.x line, Postgres support is no longer bundled in `flyway-core` and
// the JDBC driver must be on the *plugin* classpath. The recommended way to do
// this in Kotlin DSL is via `buildscript { … }` rather than a `flyway` config.
// ─────────────────────────────────────────────────────────────────────────────
buildscript {
    val flywayVersion: String by project
    val postgresqlVersion: String by project
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:$flywayVersion")
        classpath("org.postgresql:postgresql:$postgresqlVersion")
    }
}

plugins {
    `java-library`
    id("org.flywaydb.flyway") version "10.21.0"
}

val flywayVersion: String by project
val postgresqlVersion: String by project

dependencies {
    api("org.flywaydb:flyway-core:$flywayVersion")
    api("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")

    // Spring Boot autoconfigure (provides FlywayMigrationStrategy hook + transitively spring-context).
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ─── Flyway task configuration ──────────────────────────────────────────────
// Values can be overridden at the command line, e.g.:
//   ./gradlew :shared-libs:db-migration:flywayMigrate \
//       -Pflyway.url=jdbc:postgresql://prod-host:5432/bloodbank_db \
//       -Pflyway.user=… -Pflyway.password=…
// or via environment variables (used by the Docker image / k8s Job).
flyway {
    url = (findProperty("flyway.url") as String?)
        ?: System.getenv("SPRING_DATASOURCE_URL")
        ?: "jdbc:postgresql://localhost:5432/bloodbank_db"
    user = (findProperty("flyway.user") as String?)
        ?: System.getenv("DB_USERNAME")
        ?: "bloodbank"
    password = (findProperty("flyway.password") as String?)
        ?: System.getenv("DB_PASSWORD")
        ?: "bloodbank"
    schemas = arrayOf("public")
    locations = arrayOf("classpath:db/migration")
    // Production-grade defaults
    baselineOnMigrate = true
    baselineVersion = "0"
    validateOnMigrate = true
    cleanDisabled = true             // safety: never accidentally wipe a real DB
    outOfOrder = false
    table = "flyway_schema_history"
}

// Skip Jacoco coverage threshold for this module — it ships SQL, not Java logic.
tasks.named("check") {
    setDependsOn(dependsOn.filterNot {
        (it as? TaskProvider<*>)?.name == "jacocoTestCoverageVerification"
    })
}
tasks.matching { it.name == "jacocoTestCoverageVerification" }.configureEach {
    enabled = false
}
