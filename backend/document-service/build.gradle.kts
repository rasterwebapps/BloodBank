plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":shared-libs:common-model"))
    implementation(project(":shared-libs:common-dto"))
    implementation(project(":shared-libs:common-events"))
    implementation(project(":shared-libs:common-exceptions"))
    implementation(project(":shared-libs:common-security"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdocVersion")}")
    implementation("org.mapstruct:mapstruct:${property("mapstructVersion")}")
    implementation("io.minio:minio:8.6.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:rabbitmq")
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
            excludes = listOf(
                "com.bloodbank.documentservice.DocumentServiceApplication",
                "com.bloodbank.documentservice.config.*",
                "com.bloodbank.documentservice.entity.*",
                "com.bloodbank.documentservice.enums.*",
                "com.bloodbank.documentservice.dto.*",
                "com.bloodbank.documentservice.mapper.*"
            )
        }
    }
}
