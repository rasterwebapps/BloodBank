# Runbook: Service Health Check Failed

**Last Updated**: 2026-04-22
**Severity**: P1 (service fully down) / P2 (service degraded)
**Response Owner**: On-call Engineer (L2)
**Escalation**: See `docs/operations/on-call-guide.md`

---

## Trigger

This runbook is activated when:
- A Grafana alert fires: `BloodBankServiceDown` or `BloodBankServiceDegraded`
- PagerDuty pages on-call engineer
- `GET /{service}/actuator/health` returns non-200 for > 2 consecutive minutes
- Kubernetes liveness probe fails, causing pod restart loop

---

## Impact Assessment

| Service Down | Severity | Impact |
|---|---|---|
| api-gateway | P1 | All users locked out — entire system inaccessible |
| donor-service | P1 | Cannot register donors or perform collections |
| inventory-service | P1 | Cannot issue blood units |
| transfusion-service | P1 | Cannot perform crossmatch or issue blood for transfusion |
| lab-service | P2 | Testing pipeline blocked; delays transfusion eligibility |
| billing-service | P2 | Billing paused; clinical operations continue |
| notification-service | P3 | Notifications delayed; clinical operations unaffected |
| reporting-service | P3 | Reports unavailable; clinical operations unaffected |

---

## Step 1 — Confirm the Incident (≤ 5 minutes)

```bash
# 1. Check pod status across all namespaces
kubectl get pods -n bloodbank-prod --sort-by=.status.phase

# 2. Check which service is unhealthy
for SVC in api-gateway donor-service inventory-service lab-service branch-service \
           transfusion-service hospital-service billing-service \
           request-matching-service notification-service reporting-service \
           document-service compliance-service config-server; do
  STATUS=$(kubectl get pods -n bloodbank-prod -l app=${SVC} \
    -o jsonpath='{.items[0].status.containerStatuses[0].ready}' 2>/dev/null)
  echo "${SVC}: ready=${STATUS}"
done

# 3. Check the health endpoint directly
SERVICE_URL="https://api.bloodbank.example.com"
curl -s "${SERVICE_URL}/${SVC}/actuator/health" | jq .
```

**Document:** Which service is affected, how many pods are failing, since when.

---

## Step 2 — Check Logs (≤ 5 minutes)

```bash
# Get recent logs from failing pod
kubectl logs -n bloodbank-prod -l app=<SERVICE_NAME> \
  --previous --tail=200

# Check events for the pod
kubectl describe pod -n bloodbank-prod <POD_NAME>

# Check if it's a crash loop
kubectl get pod -n bloodbank-prod <POD_NAME> -o jsonpath='{.status.containerStatuses[0].restartCount}'
```

**Look for:**
- `OutOfMemoryError` → memory issue (Step 4a)
- `Connection refused` to DB/Redis/RabbitMQ → dependency issue (Step 4b)
- `Could not connect to Keycloak` → auth service issue (Step 4c)
- Application startup exception → config or code issue (Step 4d)

---

## Step 3 — Attempt Automatic Recovery (≤ 5 minutes)

```bash
# 3a. Rolling restart of the affected deployment
kubectl rollout restart deployment/<SERVICE_NAME> -n bloodbank-prod

# 3b. Watch rollout status
kubectl rollout status deployment/<SERVICE_NAME> -n bloodbank-prod --timeout=5m

# 3c. If rollout hangs, check resource constraints
kubectl top pods -n bloodbank-prod -l app=<SERVICE_NAME>
kubectl describe deployment/<SERVICE_NAME> -n bloodbank-prod
```

If the service recovers → go to **Step 7 (Verify and Close)**.
If the service does not recover → continue to Step 4.

---

## Step 4 — Root Cause Diagnosis

### 4a. Out of Memory (OOM)

```bash
# Check memory limits
kubectl get deployment/<SERVICE_NAME> -n bloodbank-prod \
  -o jsonpath='{.spec.template.spec.containers[0].resources}'

# Check node memory
kubectl top nodes

# Temporary fix: increase memory limit
kubectl set resources deployment/<SERVICE_NAME> -n bloodbank-prod \
  --limits=memory=2Gi --requests=memory=1Gi
```

**Long-term:** raise memory limits in Helm values and redeploy.

### 4b. Dependency Unavailable (DB/Redis/RabbitMQ)

```bash
# Check PostgreSQL connectivity from within cluster
kubectl run -it --rm debug --image=postgres:17 --restart=Never -n bloodbank-prod \
  -- psql -h postgresql.bloodbank-prod.svc.cluster.local -U bloodbank -d bloodbank_db -c "SELECT 1;"

# Check Redis
kubectl run -it --rm debug --image=redis:7 --restart=Never -n bloodbank-prod \
  -- redis-cli -h redis.bloodbank-prod.svc.cluster.local ping

# Check RabbitMQ
kubectl run -it --rm debug --image=curlimages/curl --restart=Never -n bloodbank-prod \
  -- curl -u bloodbank:password http://rabbitmq.bloodbank-prod.svc.cluster.local:15672/api/overview
```

If dependencies are down → follow:
- DB issues: `docs/operations/runbooks/runbook-database-issues.md`
- Proceed to Step 5 if dependency is restored.

### 4c. Keycloak Unreachable

```bash
# Check Keycloak pod
kubectl get pods -n bloodbank-prod -l app=keycloak

# Test token endpoint
curl -s https://keycloak.bloodbank.example.com/realms/bloodbank/.well-known/openid-configuration | jq .issuer
```

Restart Keycloak pod if needed:
```bash
kubectl rollout restart deployment/keycloak -n bloodbank-prod
```

### 4d. Configuration or Code Issue

```bash
# Check config-server is serving correct config
curl -s "http://config-server.bloodbank-prod.svc.cluster.local:8888/<SERVICE_NAME>/prod" | jq .

# If config is wrong, check ConfigMap
kubectl get configmap -n bloodbank-prod <SERVICE_NAME>-config -o yaml

# Roll back to previous known-good image
kubectl rollout undo deployment/<SERVICE_NAME> -n bloodbank-prod
kubectl rollout status deployment/<SERVICE_NAME> -n bloodbank-prod --timeout=5m
```

For full rollback to previous release: `docs/operations/runbooks/runbook-rollback.md`

---

## Step 5 — Scale Up / Pod Replacement

If pods are OOMKilled or evicted, manually scale to ensure coverage:

```bash
# Scale up to increase pod count
kubectl scale deployment/<SERVICE_NAME> -n bloodbank-prod --replicas=5

# Cordon unhealthy node if node-level issue
kubectl cordon <NODE_NAME>
kubectl drain <NODE_NAME> --ignore-daemonsets --delete-emptydir-data

# Verify new pods start successfully
kubectl get pods -n bloodbank-prod -l app=<SERVICE_NAME> -w
```

---

## Step 6 — Escalation Decision

| Condition | Action |
|---|---|
| Service restored within 15 minutes | No escalation required; document in incident log |
| P1 service down > 15 minutes | Escalate to L3 (Project Manager + Technical Lead) |
| P1 service down > 60 minutes | Escalate to L4 (Executive Sponsor) |
| Data loss suspected | Follow `runbook-data-corruption.md` immediately |
| Rolling back release | Follow `runbook-rollback.md` |

```bash
# Notify Slack
curl -X POST -H 'Content-type: application/json' \
  --data '{"text":"🔴 [P1] <SERVICE_NAME> is DOWN. On-call investigating. ETA: TBD"}' \
  $SLACK_WEBHOOK_URL
```

---

## Step 7 — Verify Recovery and Close

```bash
# 1. All pods healthy
kubectl get pods -n bloodbank-prod -l app=<SERVICE_NAME>

# 2. Health endpoint returns UP
curl -s https://api.bloodbank.example.com/<SERVICE_NAME>/actuator/health | jq .status

# 3. Error rate back to normal in Grafana (< 0.1%)
# 4. No new alerts in last 5 minutes

# 5. Resolve PagerDuty incident
# 6. Post recovery message to Slack
curl -X POST -H 'Content-type: application/json' \
  --data '{"text":"✅ [RESOLVED] <SERVICE_NAME> is UP. Duration: Xmin. PIR scheduled."}' \
  $SLACK_WEBHOOK_URL
```

**Close the incident** and schedule a Post-Incident Review within 24 hours (P1) or 72 hours (P2). Template: `docs/operations/incident-response.md`.

---

## Reference

| Resource | URL |
|---|---|
| Grafana dashboard | `https://monitoring.bloodbank.example.com/grafana` |
| Kubernetes dashboard | `https://k8s.bloodbank.example.com` |
| PagerDuty | `https://bloodbank.pagerduty.com` |
| Incident response | `docs/operations/incident-response.md` |
| On-call guide | `docs/operations/on-call-guide.md` |
