# Stage 1: Build
FROM gradle:8-jdk21 AS builder
WORKDIR /workspace
COPY --chown=gradle:gradle . .
RUN gradle :backend:config-server:bootJar -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S bloodbank && adduser -S bloodbank -G bloodbank
WORKDIR /app
COPY --from=builder /workspace/backend/config-server/build/libs/*.jar app.jar
# Copy config-repo for native profile
COPY --from=builder /workspace/config-repo /config-repo
RUN apk add --no-cache wget
USER bloodbank
EXPOSE 8888
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8888/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
