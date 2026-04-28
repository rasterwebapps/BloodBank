// =============================================================================
// BloodBank — Root build script
//
// Strategy:
//   • Apply ONLY universal concerns (Java 21 toolchain, Spring BOMs, Jacoco,
//     test conventions) to every subproject.
//   • Apply the heavyweight "JPA + REST microservice" dependency stack ONLY to
//     subprojects that opt-in via the `bloodbank.service` extra property in
//     their own build.gradle.kts.
//
// Rationale:
//   The previous root script forced JPA, Web, MapStruct, Testcontainers onto
//   every subproject — including reactive `api-gateway`, `config-server`, and
//   pure-Java `shared-libs:*` — which then had to manually `exclude(...)` them.
//   That coupling produced confusing classpaths and made one mis-applied dep
//   break unrelated modules. We now invert the model: opt-in, not opt-out.
// =============================================================================

plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.4.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "com.bloodbank"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
    }

    // ─── BOMs (versions resolved here once for the whole repo) ──────────────
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
            mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
        }
    }

    // ─── Compiler defaults (apply to every subproject) ──────────────────────
    // `-parameters` keeps method parameter names in bytecode, which is required
    // by Spring (DI, MVC), MapStruct, and Jackson — production-grade default.
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf(
            "-parameters",
            "-Amapstruct.defaultComponentModel=spring",
            "-Amapstruct.unmappedTargetPolicy=IGNORE"
        ))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        // Surface failed tests in CI logs without sifting through HTML reports
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStackTraces = true
            showCauses = true
        }
        // Reasonable defaults for Testcontainers-based suites
        systemProperty("file.encoding", "UTF-8")
        maxHeapSize = "1g"
    }

    // ─── Jacoco (every module gets reporting; coverage gate only on services) ─
    extensions.configure<JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }

    tasks.withType<JacocoReport>().configureEach {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

// =============================================================================
// "Service" convention — applied only to subprojects that set
//     extra["bloodbank.service"] = true
// in their own build.gradle.kts. This is every Spring Boot microservice that
// uses JPA + REST + RabbitMQ + Redis + OAuth2 (12 of the 14 services).
//
// `api-gateway` (reactive WebFlux) and `config-server` (Spring Cloud Config)
// have different runtime profiles and opt OUT of this convention.
// =============================================================================
subprojects {
    afterEvaluate {
        val isService = (extra.properties["bloodbank.service"] as? Boolean) == true
        if (!isService) return@afterEvaluate

        val mapstructVersion = property("mapstructVersion") as String
        val springdocVersion = property("springdocVersion") as String
        val resilience4jVersion = property("resilience4jVersion") as String

        dependencies {
            // ─── Shared libraries (every service uses them) ─────────────────
            "implementation"(project(":shared-libs:common-model"))
            "implementation"(project(":shared-libs:common-dto"))
            "implementation"(project(":shared-libs:common-events"))
            "implementation"(project(":shared-libs:common-exceptions"))
            "implementation"(project(":shared-libs:common-security"))

            // ─── Spring Boot starters ───────────────────────────────────────
            "implementation"("org.springframework.boot:spring-boot-starter-web")
            "implementation"("org.springframework.boot:spring-boot-starter-validation")
            "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
            "implementation"("org.springframework.boot:spring-boot-starter-security")
            "implementation"("org.springframework.boot:spring-boot-starter-actuator")
            "implementation"("org.springframework.boot:spring-boot-starter-data-redis")
            "implementation"("org.springframework.boot:spring-boot-starter-amqp")
            "implementation"("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

            // ─── Spring Cloud Config client (services pull config on boot) ──
            "implementation"("org.springframework.cloud:spring-cloud-starter-config")
            "implementation"("org.springframework.cloud:spring-cloud-starter-bootstrap")

            // ─── Resilience & observability (production essentials) ─────────
            "implementation"("io.github.resilience4j:resilience4j-spring-boot3:$resilience4jVersion")
            "implementation"("io.micrometer:micrometer-registry-prometheus")
            "implementation"("io.micrometer:micrometer-tracing-bridge-otel")

            // ─── OpenAPI / SpringDoc ────────────────────────────────────────
            "implementation"("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

            // ─── MapStruct ──────────────────────────────────────────────────
            "implementation"("org.mapstruct:mapstruct:$mapstructVersion")
            "annotationProcessor"("org.mapstruct:mapstruct-processor:$mapstructVersion")

            // ─── Runtime ────────────────────────────────────────────────────
            "runtimeOnly"("org.postgresql:postgresql")

            // ─── Test ───────────────────────────────────────────────────────
            "testImplementation"("org.springframework.boot:spring-boot-starter-test")
            "testImplementation"("org.springframework.security:spring-security-test")
            "testImplementation"("org.testcontainers:junit-jupiter")
            "testImplementation"("org.testcontainers:postgresql")
            "testImplementation"("org.testcontainers:rabbitmq")
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        }

        // ─── Coverage gate (services only). Default exclusions cover the
        // boilerplate + generated code that has no business logic. Each service
        // can extend the list via the `bloodbank.coverageExcludes` extra.
        val defaultExcludes = listOf(
            "**/*Application.class",
            "**/config/**",
            "**/dto/**",
            "**/entity/**",
            "**/enums/**",
            "**/mapper/*Impl.class",
            "**/mapper/*Impl\$*.class"
        )
        @Suppress("UNCHECKED_CAST")
        val extraExcludes = (extra.properties["bloodbank.coverageExcludes"] as? List<String>) ?: emptyList()
        val coverageExcludes = defaultExcludes + extraExcludes

        tasks.named<JacocoReport>("jacocoTestReport") {
            classDirectories.setFrom(files(classDirectories.files.map {
                fileTree(it) { exclude(coverageExcludes) }
            }))
        }

        tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
            classDirectories.setFrom(files(classDirectories.files.map {
                fileTree(it) { exclude(coverageExcludes) }
            }))
            violationRules {
                rule {
                    limit { minimum = "0.80".toBigDecimal() }
                }
            }
        }

        tasks.named("check") {
            dependsOn(tasks.named("jacocoTestCoverageVerification"))
        }
    }
}
