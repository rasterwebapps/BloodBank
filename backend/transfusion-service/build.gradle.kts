plugins {
    id("org.springframework.boot")
}

extra["bloodbank.service"] = true
extra["bloodbank.coverageExcludes"] = listOf(
    "**/event/TransfusionEventPublisher.class"
)
