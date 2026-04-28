// API Gateway is reactive (WebFlux) — it deliberately does NOT opt in to the
// JPA-microservice convention from the root build, so we declare its full
// dependency set here.
plugins {
    id("org.springframework.boot")
}

val resilience4jVersion: String by project

dependencies {
    // Spring Cloud Gateway (reactive)
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

    // Security: JWT validation against Keycloak
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Reactive Redis for rate-limiting / caching
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Operational essentials
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")

    // Shared cross-cutting libs (security configs, exception handlers, DTOs)
    implementation(project(":shared-libs:common-dto"))
    implementation(project(":shared-libs:common-exceptions"))
    implementation(project(":shared-libs:common-security"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Coverage gate: gateway is mostly routing config and filters — keep reporting
// on but do not enforce the 80 % rule (no domain logic here).
tasks.matching { it.name == "jacocoTestCoverageVerification" }.configureEach {
    enabled = false
}
