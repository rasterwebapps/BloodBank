plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":shared-libs:common-model"))
    implementation(project(":shared-libs:common-dto"))
    implementation(project(":shared-libs:common-events"))
    implementation(project(":shared-libs:common-exceptions"))
    implementation(project(":shared-libs:common-security"))

    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:${property("springdocVersion")}")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:${property("resilience4jVersion")}")
}

// API Gateway is reactive — exclude web starter inherited from parent
configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-data-jpa")
    }
}
