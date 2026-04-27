// Spring Cloud Config Server. No JPA, no MapStruct, no Testcontainers.
plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-config-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Config server has minimal application code — disable strict coverage gate.
tasks.matching { it.name == "jacocoTestCoverageVerification" }.configureEach {
    enabled = false
}
