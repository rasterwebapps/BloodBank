plugins {
    id("org.springframework.boot")
}

// Opt in to the standard JPA-microservice convention defined in the root build.
extra["bloodbank.service"] = true

// Service-specific Jacoco exclusions (e.g. trivial event listener classes).
extra["bloodbank.coverageExcludes"] = listOf(
    "**/event/EmergencyRequestListener.class",
    "**/mapper/DateTimeMapper.class"
)
