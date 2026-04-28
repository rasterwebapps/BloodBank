plugins {
    id("org.springframework.boot")
}

extra["bloodbank.service"] = true
extra["bloodbank.coverageExcludes"] = listOf(
    "**/event/DomainEventListener.class"
)

dependencies {
    // Notification service additionally sends transactional emails.
    implementation("org.springframework.boot:spring-boot-starter-mail")
}
