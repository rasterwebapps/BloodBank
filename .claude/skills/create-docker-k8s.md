# Skill: Create Docker & Kubernetes Configuration

Generate Docker and Kubernetes configurations following BloodBank patterns.

## Dockerfile Template (Multi-Stage Build)

```dockerfile
# Stage 1: Build
FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/
COPY shared-libs/ shared-libs/
COPY backend/{service-name}/ backend/{service-name}/
RUN gradle :backend:{service-name}:bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/backend/{service-name}/build/libs/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

## Kubernetes Deployment Template

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {service-name}
  namespace: bloodbank-{env}
  labels:
    app: {service-name}
    version: "1.0.0"
spec:
  replicas: 2
  selector:
    matchLabels:
      app: {service-name}
  template:
    metadata:
      labels:
        app: {service-name}
    spec:
      serviceAccountName: {service-name}-sa
      containers:
        - name: {service-name}
          image: bloodbank/{service-name}:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: SPRING_DATASOURCE_URL
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: url
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: username
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: password
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
```

## Kubernetes Service Template

```yaml
apiVersion: v1
kind: Service
metadata:
  name: {service-name}
  namespace: bloodbank-{env}
spec:
  selector:
    app: {service-name}
  ports:
    - port: 8080
      targetPort: 8080
  type: ClusterIP
```

## HPA Template

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: {service-name}-hpa
  namespace: bloodbank-{env}
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: {service-name}
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

## Deployment Strategy

| Service | Strategy | Config |
|---|---|---|
| donor-service | Blue-Green | `strategy: { type: Recreate }` + service switch |
| inventory-service | Blue-Green | Same as above |
| lab-service | Blue-Green | Same as above |
| transfusion-service | Blue-Green | Same as above |
| api-gateway | Blue-Green | Same as above |
| request-matching-service | Canary | `maxSurge: 1, maxUnavailable: 0` |
| billing-service | Canary | Same as above |
| Others | Rolling Update | `maxSurge: 25%, maxUnavailable: 25%` |

## Validation

- [ ] Multi-stage Dockerfile (build + runtime)
- [ ] Non-root user in container
- [ ] Health checks configured
- [ ] Resource limits set
- [ ] Secrets referenced via K8s secrets (not hardcoded)
- [ ] HPA configured for production
- [ ] Readiness + liveness probes
