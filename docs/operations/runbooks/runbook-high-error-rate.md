# Runbook: High Error Rate

**Last Updated**: 2026-04-22
**Severity**: P1 (error rate > 10%) / P2 (error rate 5–10%)
**Response Owner**: On-call Engineer (L2)
**Escalation**: See `docs/operations/on-call-guide.md`

---

## Trigger

This runbook is activated when:
- Grafana alert: `BloodBankHighErrorRate` fires (HTTP 5xx rate > 5% over any 5-minute window)
- PagerDuty pages on-call engineer with alert `ErrorRateHigh`
- Users report widespread failures in Slack `#bloodbank-ops-alerts`

---

## Impact Assessment

| Error Rate | Severity | User Impact |
|---|---|---|
| > 10% | P1 | Major feature degradation — many operations failing |
| 5–10% | P2 | Significant degradation — some workflows impaired |
| 1–5% | P3 | Elevated errors — monitor closely |
| 0.1–1% | Monitor | Normal baseline; investigate if sustained |

---

## Step 1 — Quantify and Locate the Errors (≤ 5 minutes)

```bash
# 1. Check overall error rate in Grafana
# Navigate to: Grafana → BloodBank Overview → Error Rate panel

# 2. Find which service has the highest error rate (Prometheus query)
# Run in Grafana Explore:
# sum by (service) (rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
# / sum by (service) (rate(http_server_requests_seconds_count[5m]))

# 3. Get recent error logs across all services
kubectl logs -n bloodbank-prod -l tier=backend --since=10m 2>/dev/null | \
  grep -E "ERROR|WARN|Exception|5[0-9][0-9]" | tail -100

# 4. Check specific service error logs
kubectl logs -n bloodbank-prod -l app=<SERVICE_NAME> --since=5m | \
  grep "ERROR" | tail -50
```

**Document:** Which service(s) are affected, which endpoints, what error messages.

---

## Step 2 — Classify the Error Type

### Type A — NullPointerException / Application Logic Error

**Symptoms:** `java.lang.NullPointerException`, `IllegalArgumentException`, `ClassCastException` in logs

```bash
# Get full stack traces
kubectl logs -n bloodbank-prod -l app=<SERVICE_NAME> --since=5m | \
  grep -A 20 "NullPointerException\|IllegalArgument\|ClassCast"
```

**Actions:**
1. Check if a recent deployment introduced the regression: `kubectl rollout history deployment/<SERVICE_NAME> -n bloodbank-prod`
2. If caused by recent deployment → roll back: `kubectl rollout undo deployment/<SERVICE_NAME> -n bloodbank-prod`
3. If not a deployment issue → escalate to development team with log evidence

### Type B — Database / Persistence Error

**Symptoms:** `JpaSystemException`, `DataIntegrityViolationException`, `HikariPool connection timeout`, `PSQLException`

```bash
# Check database health
curl -s https://api.bloodbank.example.com/<SERVICE_NAME>/actuator/health | \
  jq '.components.db'

# Check connection pool
curl -s https://api.bloodbank.example.com/<SERVICE_NAME>/actuator/metrics/hikaricp.connections.active | \
  jq '.measurements[0].value'
```

**Actions:** Follow `docs/operations/runbooks/runbook-database-issues.md`

### Type C — External Dependency Error (Keycloak, RabbitMQ, Redis)

**Symptoms:** `Connection refused`, `401 Unauthorized from Keycloak`, `RedisConnectionFailureException`, `AmqpConnectException`

```bash
# Check Keycloak
curl -s https://keycloak.bloodbank.example.com/health/ready | jq .status

# Check Redis
kubectl exec -it -n bloodbank-prod redis-0 -- redis-cli ping

# Check RabbitMQ
kubectl exec -it -n bloodbank-prod rabbitmq-0 -- \
  rabbitmq-diagnostics check_running
```

**Actions:**
- Keycloak down: restart Keycloak pod: `kubectl rollout restart deployment/keycloak -n bloodbank-prod`
- Redis down: follow cache reconnection procedure below
- RabbitMQ down: escalate immediately (data-loss risk)

### Type D — Circuit Breaker Open

**Symptoms:** `CallNotPermittedException`, HTTP 503 with `circuit-breaker` in response body

```bash
# Check circuit breaker states via Actuator
curl -s https://api.bloodbank.example.com/<SERVICE_NAME>/actuator/circuitbreakers | jq .

# Check which circuit breakers are OPEN
curl -s https://api.bloodbank.example.com/<SERVICE_NAME>/actuator/circuitbreakers | \
  jq '.circuitBreakers | to_entries[] | select(.value.state == "OPEN")'
```

**Actions:**
1. Identify which downstream service is causing circuit breaker to open
2. Investigate that downstream service first
3. Once downstream recovers, circuit breaker will transition to HALF_OPEN then CLOSED automatically
4. If needed, manually force-close circuit breaker via Actuator:

```bash
# Force circuit breaker to CLOSED (use cautiously)
curl -X POST https://api.bloodbank.example.com/<SERVICE_NAME>/actuator/circuitbreakers/<CB_NAME>/close
```

### Type E — Memory / Resource Exhaustion

**Symptoms:** `OutOfMemoryError`, `GC overhead limit exceeded`, pod being OOMKilled

```bash
# Check JVM heap
curl -s https://api.bloodbank.example.com/<SERVICE_NAME>/actuator/metrics/jvm.memory.used | jq .

# Check pod resource usage
kubectl top pods -n bloodbank-prod -l app=<SERVICE_NAME>

# Check for OOMKilled pods
kubectl get pods -n bloodbank-prod -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{range .status.containerStatuses[*]}{.lastState.terminated.reason}{"\n"}{end}{end}' | grep OOMKilled
```

**Actions:**
1. Immediately increase memory limit temporarily:
   ```bash
   kubectl set resources deployment/<SERVICE_NAME> -n bloodbank-prod \
     --limits=memory=3Gi --requests=memory=1.5Gi
   ```
2. Trigger a heap dump before restarting (if the pod is still alive):
   ```bash
   curl -X POST https://api.bloodbank.example.com/<SERVICE_NAME>/actuator/heapdump \
     -o /tmp/heapdump-$(date +%Y%m%d%H%M%S).hprof
   ```
3. Escalate to development team with the heap dump for analysis.

---

## Step 3 — Mitigate (≤ 15 minutes)

### If caused by recent deployment

```bash
# Roll back to previous version
kubectl rollout undo deployment/<SERVICE_NAME> -n bloodbank-prod
kubectl rollout status deployment/<SERVICE_NAME> -n bloodbank-prod --timeout=5m

# Verify error rate drops
# Watch Grafana: wait 2-3 minutes for rate to stabilise
```

### If caused by traffic spike

```bash
# Scale up replicas
kubectl scale deployment/<SERVICE_NAME> -n bloodbank-prod --replicas=6

# Check HPA status
kubectl get hpa -n bloodbank-prod

# If HPA is not scaling fast enough, manually override max replicas
kubectl patch hpa <SERVICE_NAME>-hpa -n bloodbank-prod \
  --patch '{"spec":{"maxReplicas":10}}'
```

### If Redis cache is unavailable (causing DB overload)

```bash
# Restart Redis
kubectl rollout restart statefulset/redis -n bloodbank-prod

# If cache won't start, temporarily disable cache-aside in service
# Set in ConfigMap: spring.cache.type=none
# Then restart the service (it will go directly to DB — may cause load spike)
kubectl edit configmap <SERVICE_NAME>-config -n bloodbank-prod
kubectl rollout restart deployment/<SERVICE_NAME> -n bloodbank-prod
```

---

## Step 4 — Monitor Recovery

```bash
# Watch error rate in real-time (Prometheus query for Grafana Explore)
# rate(http_server_requests_seconds_count{status=~"5..",service="<SERVICE_NAME>"}[1m])
# / rate(http_server_requests_seconds_count{service="<SERVICE_NAME>"}[1m])

# Check that error rate has dropped below 1%
# Wait at least 5 minutes of sustained low error rate before declaring resolved
```

---

## Step 5 — Escalation

| Condition | Action |
|---|---|
| Error rate > 10% and not decreasing after 10 minutes | Escalate to L3 immediately |
| Error rate caused by suspected data corruption | Follow `runbook-data-corruption.md` |
| Error rate caused by possible security incident | Follow `runbook-security-incident.md` |
| Unable to identify root cause within 20 minutes | Escalate to L3; consider rollback |

```bash
# Post status update to Slack
curl -X POST -H 'Content-type: application/json' \
  --data '{"text":"⚠️ [P2] High error rate on <SERVICE_NAME>: X%. Investigating. ETA update in 15min."}' \
  $SLACK_WEBHOOK_URL
```

---

## Step 6 — Verify and Close

- [ ] Error rate below 0.5% for 5 consecutive minutes
- [ ] No new P1/P2 alerts firing
- [ ] All service health checks returning `UP`
- [ ] Grafana dashboard shows normal baseline
- [ ] Post recovery update to Slack
- [ ] Open Post-Incident Review (P1/P2): see `docs/operations/incident-response.md`

---

## Reference

| Resource | URL |
|---|---|
| Grafana | `https://monitoring.bloodbank.example.com/grafana` |
| Loki logs | `https://monitoring.bloodbank.example.com/logs` |
| Jaeger tracing | `https://monitoring.bloodbank.example.com/tracing` |
| On-call guide | `docs/operations/on-call-guide.md` |
| Incident response | `docs/operations/incident-response.md` |
