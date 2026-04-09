plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    testImplementation(project(":shared-libs:common-events"))

    testImplementation("org.springframework.boot:spring-boot-starter-amqp")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Integration tests do not produce a bootable JAR
tasks.getByName("jar") {
    enabled = true
}

// Disable JaCoCo coverage verification — these are cross-service integration tests
tasks.jacocoTestCoverageVerification {
    enabled = false
}
