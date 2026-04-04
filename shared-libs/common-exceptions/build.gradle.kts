plugins {
    `java-library`
}

dependencies {
    api(project(":shared-libs:common-dto"))
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")
}
