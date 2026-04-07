plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-config-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Config Server doesn't need JPA, MapStruct, or Testcontainers
configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-data-jpa")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-validation")
        exclude(group = "org.mapstruct", module = "mapstruct")
        exclude(group = "org.mapstruct", module = "mapstruct-processor")
        exclude(group = "org.testcontainers", module = "junit-jupiter")
        exclude(group = "org.testcontainers", module = "postgresql")
    }
}
