plugins {
    id("org.springframework.boot")
}

extra["bloodbank.service"] = true

val minioVersion: String by project

dependencies {
    // Document service stores uploaded files in MinIO (S3-compatible).
    implementation("io.minio:minio:$minioVersion")
}
