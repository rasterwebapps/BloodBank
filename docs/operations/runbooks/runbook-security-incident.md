# Runbook: Security Incident

**Last Updated**: 2026-04-22
**Severity**: P1 (active breach or unauthorized access to PHI) / P2 (suspected or contained)
**Response Owner**: Security Lead (L3) + On-call Engineer (L2)
**Escalation**: Immediately escalate to L3 on any security indicator — do NOT attempt to investigate alone

> ⚠️ **IMPORTANT**: Do NOT attempt to remediate a security incident without involving the Security Lead. Incorrect actions can destroy forensic evidence, violate compliance obligations (HIPAA breach notification within 72 hours), or allow an attacker to escalate.

---

## Trigger

This runbook is activated when:
- WAF or SIEM alert: unauthorized access pattern detected
- Grafana alert: `BloodBankUnusualAuthFailures` (> 20 failed auth attempts in 5 minutes from one IP)
- Staff reports unexpected data access or account behaviour
- Penetration test or bug bounty report of critical vulnerability
- Unusual volume of data export or API calls outside normal patterns
- Any login from a known-malicious IP or geolocation not matching user profile

---

## IMMEDIATE ACTIONS (First 15 Minutes)

### 1. Engage the Security Lead

**Do this first — before any investigation or remediation.**

```
Phone: [Security Lead direct mobile — see PagerDuty contact list]
Slack: @security-lead in #bloodbank-security-incident
Email: security@bloodbank.example.com
```

Do NOT post details of the incident in public Slack channels.

### 2. Preserve Evidence

**Before making any changes to logs, systems, or configurations:**

```bash
# Export current application logs
kubectl logs -n bloodbank-prod -l tier=backend --since=60m > /tmp/incident-logs-$(date +%Y%m%d%H%M%S).txt

# Export audit logs from the database
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    COPY (
      SELECT * FROM audit_logs
      WHERE created_at >= now() - interval '2 hours'
      ORDER BY created_at DESC
    ) TO STDOUT WITH CSV HEADER;" > /tmp/audit-logs-$(date +%Y%m%d%H%M%S).csv

# Export Kubernetes events
kubectl get events -n bloodbank-prod --sort-by='.lastTimestamp' \
  > /tmp/k8s-events-$(date +%Y%m%d%H%M%S).txt

# Copy logs to secure storage immediately
aws s3 cp /tmp/incident-logs-*.txt s3://bloodbank-security-incidents/$(date +%Y%m%d)/
aws s3 cp /tmp/audit-logs-*.csv s3://bloodbank-security-incidents/$(date +%Y%m%d)/
```

**Never delete or modify logs during an incident. This is a compliance violation.**

### 3. Open the Incident

Create a private incident channel immediately:
- Slack: Create `#sec-incident-YYYYMMDD` (private channel)
- Invite: Security Lead, Technical Lead, Project Manager
- Do NOT invite non-essential personnel

---

## Step 1 — Identify What Is Happening (≤ 15 minutes)

### 1a. Check Authentication Logs

```bash
# Failed login attempts by IP
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT created_by_ip, COUNT(*) as attempts, MIN(created_at) as first_seen, MAX(created_at) as last_seen
    FROM audit_logs
    WHERE action = 'AUTH_FAILURE'
      AND created_at >= now() - interval '1 hour'
    GROUP BY created_by_ip
    ORDER BY attempts DESC
    LIMIT 20;"

# Successful logins from unusual locations
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT created_by, created_by_ip, COUNT(*) as sessions
    FROM audit_logs
    WHERE action = 'AUTH_SUCCESS'
      AND created_at >= now() - interval '1 hour'
    GROUP BY created_by, created_by_ip
    ORDER BY sessions DESC
    LIMIT 20;"
```

### 1b. Check for Unusual Data Access

```bash
# Large data exports or bulk reads
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT created_by, action, entity_type, COUNT(*) as ops
    FROM audit_logs
    WHERE created_at >= now() - interval '1 hour'
    GROUP BY created_by, action, entity_type
    HAVING COUNT(*) > 100
    ORDER BY ops DESC;"

# PHI access outside normal hours (adjust UTC offset as needed)
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT created_by, entity_type, COUNT(*) as accesses
    FROM audit_logs
    WHERE action IN ('READ', 'EXPORT')
      AND created_at BETWEEN now() - interval '12 hours' AND now()
      AND EXTRACT(HOUR FROM created_at) NOT BETWEEN 6 AND 22
    GROUP BY created_by, entity_type
    ORDER BY accesses DESC;"
```

### 1c. Check API Gateway Logs

```bash
# Look for scanning patterns (many 404s from one IP)
kubectl logs -n bloodbank-prod -l app=api-gateway --since=30m | \
  awk '{print $1}' | sort | uniq -c | sort -rn | head -20

# Look for JWT manipulation attempts
kubectl logs -n bloodbank-prod -l app=api-gateway --since=30m | \
  grep -E "invalid.*token|JWT.*exception|signature.*invalid" | head -50

# Look for unusual User-Agent strings (tools like sqlmap, nikto)
kubectl logs -n bloodbank-prod -l app=api-gateway --since=30m | \
  grep -iE "sqlmap|nikto|nessus|burp|metasploit|hydra|nmap" | head -20
```

---

## Step 2 — Classify the Incident

| Type | Indicators | Immediate Action |
|---|---|---|
| **Brute force / credential stuffing** | Many failed logins from one IP | Block IP at WAF/nginx (Step 3a) |
| **Account takeover** | Login from new IP + unusual activity | Lock account (Step 3b) |
| **PHI exfiltration** | Bulk data reads by one user | Lock account + isolate (Step 3b + 3c) |
| **SQL injection attempt** | WAF alerts, unusual query patterns | Block IP + check for successful injections |
| **Insider threat** | Legitimate user accessing data outside role | Lock account + escalate to L4 |
| **API key compromise** | Valid API calls from unexpected source | Rotate keys (Step 3d) |
| **Container escape / node compromise** | Unusual process in pod, unexpected outbound connections | Isolate node (Step 3e) |

---

## Step 3 — Containment

> **All containment actions require Security Lead approval first.**

### 3a. Block IP at WAF/Nginx

```bash
# Add IP block in nginx ingress (temporary)
kubectl edit configmap nginx-configuration -n ingress-nginx
# Add to data section:
# block-cidrs: "192.168.1.1/32"

# OR use cloud WAF rule (AWS WAF / Cloudflare)
# This must be done via the cloud console — do not automate blindly
```

### 3b. Lock Compromised User Account

```bash
# Disable user in Keycloak via Admin API
KEYCLOAK_URL="https://keycloak.bloodbank.example.com"
ADMIN_TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
  -d "client_id=admin-cli&username=${KEYCLOAK_ADMIN}&password=${KEYCLOAK_ADMIN_PASSWORD}&grant_type=password" | jq -r .access_token)

# Get user ID
USER_ID=$(curl -s -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  "${KEYCLOAK_URL}/admin/realms/bloodbank/users?username=<USERNAME>" | jq -r '.[0].id')

# Disable the account
curl -X PUT -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  "${KEYCLOAK_URL}/admin/realms/bloodbank/users/${USER_ID}" \
  -d '{"enabled": false}'

# Invalidate all active sessions
curl -X DELETE -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  "${KEYCLOAK_URL}/admin/realms/bloodbank/users/${USER_ID}/sessions"
```

### 3c. Isolate Affected Service (If Data Exfiltration in Progress)

```bash
# Apply network policy to block all external egress from the service
cat <<EOF | kubectl apply -f -
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: isolate-<SERVICE_NAME>
  namespace: bloodbank-prod
spec:
  podSelector:
    matchLabels:
      app: <SERVICE_NAME>
  policyTypes:
  - Egress
  egress: []
EOF
```

### 3d. Rotate API Keys / Secrets

```bash
# Rotate JWT signing key in Keycloak (this invalidates ALL active sessions)
# NOTE: This will log out all users — requires L4 approval
# Use Keycloak Admin UI: Realm Settings → Keys → Generate new RSA key

# Rotate database password
kubectl create secret generic postgresql-credentials \
  --from-literal=password=$(openssl rand -base64 32) \
  -n bloodbank-prod --dry-run=client -o yaml | kubectl apply -f -

# Rotate RabbitMQ credentials
kubectl rollout restart deployment -n bloodbank-prod
```

### 3e. Isolate Compromised Node

```bash
# Cordon and drain the node
kubectl cordon <NODE_NAME>
kubectl drain <NODE_NAME> --ignore-daemonsets --delete-emptydir-data

# Preserve node forensic data before replacing
# Contact cloud provider to take a snapshot of the node's disk
# Do NOT terminate the node until forensics are complete
```

---

## Step 4 — Notification and Compliance

### Internal Notification (within 1 hour)

Notify via the private incident Slack channel:
- Project Manager
- Technical Lead
- Executive Sponsor (if PHI involved or P1)
- Legal Counsel (if PHI involved)

### External Notifications — HIPAA / GDPR Deadlines

> ⚠️ These are legal obligations. Late notification is a regulatory violation.

| Regulation | Notification Requirement | Deadline |
|---|---|---|
| **HIPAA** | Notify affected individuals if PHI was accessed | 60 days from discovery |
| **HIPAA** | Notify HHS | 60 days from discovery |
| **GDPR** | Notify supervisory authority (if EU citizens' data) | **72 hours from discovery** |
| **GDPR** | Notify affected individuals (if high risk) | Without undue delay |

**Notification must be coordinated by Legal Counsel + Executive Sponsor — not by the engineering team.**

---

## Step 5 — Eradication and Recovery

Only after containment is confirmed and evidence is preserved:

1. Identify the root cause (vulnerability, misconfiguration, compromised credential)
2. Patch or remediate the vulnerability in a staging environment first
3. Deploy the fix via standard CI/CD pipeline
4. Remove any backdoors or persistence mechanisms installed by attacker
5. Reset all credentials that may have been exposed
6. Re-enable user accounts after credential reset (with MFA enforced)

---

## Step 6 — Post-Incident

1. **Forensic timeline**: document every known attacker action with timestamps
2. **Post-Incident Review** within 24 hours: `docs/operations/incident-response.md`
3. **Regulatory report** (if required): coordinate with Legal Counsel
4. **Security hardening**: implement all findings from the incident
5. **Penetration test**: schedule a re-test of affected systems within 30 days

---

## Reference

| Resource | URL |
|---|---|
| Keycloak Admin | `https://keycloak.bloodbank.example.com/admin` (VPN only) |
| SIEM / WAF console | _TBD — per cloud provider_ |
| Audit logs DB | `SELECT * FROM audit_logs WHERE ...` |
| HIPAA breach guidance | `docs/compliance/hipaa-validation.md` |
| GDPR breach guidance | `docs/compliance/gdpr-validation.md` |
| On-call guide | `docs/operations/on-call-guide.md` |
| Incident response | `docs/operations/incident-response.md` |
