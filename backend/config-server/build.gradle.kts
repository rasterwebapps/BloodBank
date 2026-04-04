plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":shared-libs:common-model"))
    implementation(project(":shared-libs:common-dto"))
    implementation(project(":shared-libs:common-events"))
    implementation(project(":shared-libs:common-exceptions"))
    implementation(project(":shared-libs:common-security"))

    implementation("org.springframework.cloud:spring-cloud-config-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
}
