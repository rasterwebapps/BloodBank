plugins {
    id("org.springframework.boot")
}

extra["bloodbank.service"] = true
extra["bloodbank.coverageExcludes"] = listOf(
    "**/event/DonationCompletedListener.class",
    "**/event/TestResultAvailableListener.class",
    "**/event/UnitReleasedListener.class"
)
