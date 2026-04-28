// Cross-service integration tests. Pulls Spring Boot test infra + Testcontainers
// but does NOT package as a bootable JAR. No coverage gate here.
plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    testImplementation(project(":shared-libs:common-events"))
    testImplementation(project(":shared-libs:common-dto"))
    testImplementation(project(":shared-libs:common-exceptions"))

    testImplementation("org.springframework.boot:spring-boot-starter-amqp")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-oauth2-jose")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testRuntimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.matching { it.name == "jacocoTestCoverageVerification" }.configureEach {
    enabled = false
}
