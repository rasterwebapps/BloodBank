# Runbook: Emergency Version Rollback

**Last Updated**: 2026-04-22
**Severity**: P1 (critical regression) / P2 (significant regression)
**Response Owner**: On-call Engineer (L2) initiates; L3 approves
**Escalation**: See `docs/operations/on-call-guide.md`

---

## Trigger

This runbook is activated when:
- A recent deployment has caused a P1 or P2 incident and the fix is not available within 15 minutes
- Error rate spiked immediately after a deployment (`kubectl rollout history` confirms recent change)
- A service is crash-looping after a deployment
- Clinical workflow is broken and the only mitigation is to revert to the previous version

---

## Decision Authority

| Scenario | Approval Required |
|---|---|
| Single service rollback (non-clinical) | L2 On-call Engineer |
| Single service rollback (clinical: donor, inventory, transfusion, lab) | L3 Technical Lead |
| Full release rollback (all services) | L3 Technical Lead + L4 Executive Sponsor |
| Database migration rollback | L3 Technical Lead + DBA (never done without L3) |

**Get verbal or Slack approval before proceeding. Log the approver's name and timestamp.**

---

## Pre-Rollback Checklist

Before rolling back, confirm:

- [ ] Identify the deployment that caused the issue (see Step 1)
- [ ] Confirm current error rate / impact (document in incident thread)
- [ ] Confirm rollback target version is known-good (see Step 2)
- [ ] Check if the problematic deployment included a database migration (see Step 3)
- [ ] Approval obtained from the correct authority
- [ ] Notify Slack channel: `#bloodbank-ops` of imminent rollback

---

## Step 1 — Identify the Problematic Deployment

```bash
# List recent rollout history for the affected service
kubectl rollout history deployment/<SERVICE_NAME> -n bloodbank-prod

# Get details of the last revision
kubectl rollout history deployment/<SERVICE_NAME> -n bloodbank-prod --revision=<N>

# Correlate with recent Kubernetes events
kubectl get events -n bloodbank-prod \
  --field-selector reason=ScalingReplicaSet \
  --sort-by='.lastTimestamp' | tail -20

# Check the image tag currently running
kubectl get deployment/<SERVICE_NAME> -n bloodbank-prod \
  -o jsonpath='{.spec.template.spec.containers[0].image}'
```

**Document:** Deployment timestamp, image tag before and after, who deployed.

---

## Step 2 — Identify the Rollback Target

```bash
# List available revisions (Kubernetes keeps last 10 by default)
kubectl rollout history deployment/<SERVICE_NAME> -n bloodbank-prod

# Check the image in the previous revision
kubectl rollout history deployment/<SERVICE_NAME> -n bloodbank-prod \
  --revision=<PREVIOUS_REVISION> \
  -o jsonpath='{.spec.template.spec.containers[0].image}'

# Verify the previous image is still available in the registry
docker pull bloodbank/<SERVICE_NAME>:<PREVIOUS_TAG>
```

---

## Step 3 — Check for Database Migrations

> ⚠️ If the problematic deployment included a Flyway migration, a Kubernetes rollback alone is NOT sufficient. A schema rollback is required, which is a DBA-level operation.

```bash
# Check if a new migration was applied since the last known-good state
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT version, description, installed_on, success
    FROM flyway_schema_history
    ORDER BY installed_rank DESC
    LIMIT 5;"
```

**If a migration was applied:**
1. Stop here — do NOT roll back the application without DBA involvement
2. Escalate to L3 Technical Lead + DBA immediately
3. The DBA must evaluate whether the migration can be reversed safely
4. A migration rollback requires a custom SQL script (Flyway Community Edition has no undo)
5. Consider taking the service offline rather than rolling back a schema change

**If no migration was applied:** proceed to Step 4.

---

## Step 4 — Execute the Rollback

### 4a. Single Service Rollback (Kubernetes Native)

```bash
# Roll back to the previous revision
kubectl rollout undo deployment/<SERVICE_NAME> -n bloodbank-prod

# Watch rollout progress
kubectl rollout status deployment/<SERVICE_NAME> -n bloodbank-prod --timeout=5m

# Verify the previous image is now running
kubectl get deployment/<SERVICE_NAME> -n bloodbank-prod \
  -o jsonpath='{.spec.template.spec.containers[0].image}'
```

### 4b. Rollback to a Specific Revision

```bash
# Roll back to a specific known-good revision number
kubectl rollout undo deployment/<SERVICE_NAME> -n bloodbank-prod \
  --to-revision=<REVISION_NUMBER>

# Watch rollout progress
kubectl rollout status deployment/<SERVICE_NAME> -n bloodbank-prod --timeout=5m
```

### 4c. Rollback Multiple Services (Full Release Rollback)

If the release included multiple services, roll back all affected services. Execute in reverse dependency order:

```bash
# Typical reverse order: start with consumers, then providers
SERVICES=(
  "compliance-service"
  "document-service"
  "reporting-service"
  "notification-service"
  "request-matching-service"
  "billing-service"
  "hospital-service"
  "transfusion-service"
  "branch-service"
  "lab-service"
  "inventory-service"
  "donor-service"
  "api-gateway"
)

for SVC in "${SERVICES[@]}"; do
  echo "Rolling back ${SVC}..."
  kubectl rollout undo deployment/${SVC} -n bloodbank-prod
done

# Wait for all rollouts to complete
for SVC in "${SERVICES[@]}"; do
  kubectl rollout status deployment/${SVC} -n bloodbank-prod --timeout=5m
  echo "${SVC}: $(kubectl rollout status deployment/${SVC} -n bloodbank-prod 2>&1 | tail -1)"
done
```

### 4d. Rollback via Helm (If Using Helm)

```bash
# List Helm release history
helm history bloodbank -n bloodbank-prod

# Roll back to previous release
helm rollback bloodbank <PREVIOUS_REVISION> -n bloodbank-prod --wait

# Verify
helm status bloodbank -n bloodbank-prod
```

### 4e. Rollback via CI/CD Pipeline (Preferred for Non-Emergency)

If the situation allows (P2 but not P1), use the CI/CD pipeline for a controlled rollback:

1. Revert the commit in Git: `git revert <COMMIT_SHA>`
2. Push to main branch
3. Jenkins pipeline will build and deploy the reverted code
4. This provides a clean audit trail and passes all pre-deployment checks

---

## Step 5 — Verify Rollback Success

```bash
# 1. Check pod status
kubectl get pods -n bloodbank-prod -l app=<SERVICE_NAME>

# 2. Check health endpoint
curl -s https://api.bloodbank.example.com/<SERVICE_NAME>/actuator/health | jq .status

# 3. Verify error rate is decreasing in Grafana
# Wait 2-3 minutes for metrics to stabilise

# 4. Run smoke tests on key endpoints
# Collection workflow
curl -s -H "Authorization: Bearer ${TEST_TOKEN}" \
  https://api.bloodbank.example.com/api/v1/collections?branchId=${TEST_BRANCH_ID} | \
  jq '.success'

# Blood unit availability
curl -s -H "Authorization: Bearer ${TEST_TOKEN}" \
  https://api.bloodbank.example.com/api/v1/inventory/units?status=AVAILABLE | \
  jq '.success'

# 5. Confirm image version is the expected rollback target
kubectl get deployment/<SERVICE_NAME> -n bloodbank-prod \
  -o jsonpath='{.spec.template.spec.containers[0].image}'
```

---

## Step 6 — Post-Rollback Actions

```bash
# Notify Slack that rollback is complete
curl -X POST -H 'Content-type: application/json' \
  --data '{"text":"✅ Rollback complete for <SERVICE_NAME>. Running version: <VERSION>. Monitoring in progress."}' \
  $SLACK_WEBHOOK_URL
```

After rollback:

1. **Monitor for 30 minutes**: confirm the issue is fully resolved
2. **Re-enable blocked deployments**: notify the development team that the release is rolled back
3. **Incident log**: document the timeline, rollback version, and approvals
4. **Root cause**: the failing deployment must be analysed by the development team before re-deployment
5. **Post-Incident Review** within 24 hours: `docs/operations/incident-response.md`
6. **Re-deployment gate**: the fix must go through the standard review + staging process before another production deployment attempt

---

## Rollback of Feature Flags (Soft Rollback)

For features controlled by feature flags, a rollback can be done without redeploying:

```bash
# Disable a feature flag via ConfigMap
kubectl patch configmap feature-flags -n bloodbank-prod \
  --patch '{"data":{"feature.new-crossmatch-flow":"false"}}'

# Rolling restart not needed if the service reads flags dynamically
# Check application logs to confirm the flag is picked up
kubectl logs -n bloodbank-prod -l app=transfusion-service --since=2m | \
  grep "feature.new-crossmatch-flow"
```

---

## Reference

| Resource | URL |
|---|---|
| Jenkins pipeline | `https://jenkins.bloodbank.example.com` |
| Container registry | `https://registry.bloodbank.example.com` |
| Kubernetes dashboard | `https://k8s.bloodbank.example.com` |
| Grafana | `https://monitoring.bloodbank.example.com/grafana` |
| On-call guide | `docs/operations/on-call-guide.md` |
| Incident response | `docs/operations/incident-response.md` |
