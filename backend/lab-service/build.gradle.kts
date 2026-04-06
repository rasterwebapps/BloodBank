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
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:rabbitmq")
}

tasks.jacocoTestCoverageVerification {
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/mapper/*Impl.class",
                    "**/mapper/*Impl\$*.class",
                    "**/config/*.class",
                    "**/enums/*.class",
                    "**/entity/*.class",
                    "**/dto/*.class",
                    "**/event/DonationCompletedListener.class",
                    "**/LabServiceApplication.class"
                )
            }
        })
    )
}

tasks.jacocoTestReport {
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/mapper/*Impl.class",
                    "**/mapper/*Impl\$*.class",
                    "**/config/*.class",
                    "**/enums/*.class",
                    "**/entity/*.class",
                    "**/dto/*.class",
                    "**/event/DonationCompletedListener.class",
                    "**/LabServiceApplication.class"
                )
            }
        })
    )
}
