#!/usr/bin/env bash
# =============================================================================
# dns-switch.sh
# BloodBank — DNS Cutover Script
#
# Usage:
#   DNS_PROVIDER=route53  HOSTED_ZONE_ID=Z1234567890 ./dns-switch.sh
#   DNS_PROVIDER=cloudflare CLOUDFLARE_ZONE_ID=abc123   ./dns-switch.sh
#
# What this script does:
#   1. Retrieves the current load-balancer IP/hostname from Kubernetes
#   2. Updates DNS records for all production domains
#   3. Polls for DNS propagation (timeout: 30 minutes)
#   4. Runs health checks after propagation is confirmed
#   5. Rolls back DNS records if health checks fail
#   6. Generates a cutover report
#
# Environment variables:
#   DNS_PROVIDER        — "route53" or "cloudflare" (default: route53)
#   NAMESPACE           — Kubernetes namespace (default: bloodbank-prod)
#   HOSTED_ZONE_ID      — Route53 hosted zone ID (required for route53)
#   CLOUDFLARE_ZONE_ID  — Cloudflare zone ID     (required for cloudflare)
#   CLOUDFLARE_API_TOKEN— Cloudflare API token   (required for cloudflare)
#   AWS_REGION          — AWS region for Route53 (default: us-east-1)
#   DNS_TTL             — TTL in seconds for new records (default: 60)
#   PROPAGATION_TIMEOUT — Max seconds to wait for DNS propagation (default: 1800)
#   DRY_RUN             — Set "true" to simulate without actual DNS changes
#   REPORTS_DIR         — Output dir for reports (default: reports/dns)
# =============================================================================
set -euo pipefail

# ---------------------------------------------------------------------------
# Colour helpers
# ---------------------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC}   $(date '+%Y-%m-%d %H:%M:%S') $*"; }
log_success() { echo -e "${GREEN}[OK]${NC}     $(date '+%Y-%m-%d %H:%M:%S') $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}   $(date '+%Y-%m-%d %H:%M:%S') $*"; }
log_error()   { echo -e "${RED}[ERROR]${NC}  $(date '+%Y-%m-%d %H:%M:%S') $*" >&2; }
log_step()    { echo -e "\n${BOLD}${CYAN}=== $* ===${NC}"; }
log_dns()     { echo -e "${BOLD}[DNS]${NC}     $(date '+%Y-%m-%d %H:%M:%S') $*"; }

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
DNS_PROVIDER="${DNS_PROVIDER:-route53}"
NAMESPACE="${NAMESPACE:-bloodbank-prod}"
HOSTED_ZONE_ID="${HOSTED_ZONE_ID:-}"
CLOUDFLARE_ZONE_ID="${CLOUDFLARE_ZONE_ID:-}"
CLOUDFLARE_API_TOKEN="${CLOUDFLARE_API_TOKEN:-}"
AWS_REGION="${AWS_REGION:-us-east-1}"
DNS_TTL="${DNS_TTL:-60}"
PROPAGATION_TIMEOUT="${PROPAGATION_TIMEOUT:-1800}"  # 30 minutes
DRY_RUN="${DRY_RUN:-false}"
REPORTS_DIR="${REPORTS_DIR:-reports/dns}"
CUTOVER_TIMESTAMP="$(date '+%Y%m%d_%H%M%S')"
REPORT_FILE="${REPORTS_DIR}/dns-cutover-${CUTOVER_TIMESTAMP}.txt"
REPORT_JSON="${REPORTS_DIR}/dns-cutover-${CUTOVER_TIMESTAMP}.json"

# Production domains to update
DOMAINS=(
  "bloodbank.example.com"
  "api.bloodbank.example.com"
  "admin.bloodbank.example.com"
)

# Kubernetes service that exposes the load balancer
LB_SERVICE="api-gateway"   # or the ingress-nginx service in ingress-nginx namespace

# Tracking
LB_IP=""
LB_HOSTNAME=""
OLD_DNS_IPS=()
CUTOVER_STATUS="PENDING"
ROLLBACK_PERFORMED=false

# ---------------------------------------------------------------------------
# Preflight checks
# ---------------------------------------------------------------------------
preflight_checks() {
  log_step "Preflight Checks"

  if ! command -v kubectl &>/dev/null; then
    log_error "kubectl is required but not found"
    exit 1
  fi

  if ! command -v dig &>/dev/null && ! command -v nslookup &>/dev/null; then
    log_error "dig (or nslookup) is required for DNS propagation checks"
    exit 1
  fi

  if ! command -v curl &>/dev/null; then
    log_error "curl is required for health checks"
    exit 1
  fi

  case "${DNS_PROVIDER}" in
    route53)
      if ! command -v aws &>/dev/null; then
        log_error "aws CLI is required for Route53 DNS updates"
        exit 1
      fi
      if [[ -z "${HOSTED_ZONE_ID}" ]]; then
        log_error "HOSTED_ZONE_ID must be set for DNS_PROVIDER=route53"
        exit 1
      fi
      log_info "DNS provider: Route53 (zone: ${HOSTED_ZONE_ID})"
      ;;
    cloudflare)
      if [[ -z "${CLOUDFLARE_ZONE_ID}" || -z "${CLOUDFLARE_API_TOKEN}" ]]; then
        log_error "CLOUDFLARE_ZONE_ID and CLOUDFLARE_API_TOKEN must be set for DNS_PROVIDER=cloudflare"
        exit 1
      fi
      log_info "DNS provider: Cloudflare (zone: ${CLOUDFLARE_ZONE_ID})"
      ;;
    *)
      log_error "Unsupported DNS_PROVIDER='${DNS_PROVIDER}'. Use 'route53' or 'cloudflare'."
      exit 1
      ;;
  esac

  mkdir -p "${REPORTS_DIR}"
  log_success "Preflight checks passed"
}

# ---------------------------------------------------------------------------
# Step 1: Get load-balancer IP/hostname from Kubernetes
# ---------------------------------------------------------------------------
get_lb_address() {
  log_step "Step 1: Retrieve Load-Balancer Address from Kubernetes"

  if [[ "${DRY_RUN}" == "true" ]]; then
    LB_IP="203.0.113.42"           # RFC 5737 documentation IP
    LB_HOSTNAME="lb.example.com"
    log_info "[DRY-RUN] Using placeholder LB_IP=${LB_IP}"
    return 0
  fi

  # Try ingress-nginx service first (most common setup)
  LB_HOSTNAME=$(kubectl get service ingress-nginx-controller \
    --namespace=ingress-nginx \
    -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")

  LB_IP=$(kubectl get service ingress-nginx-controller \
    --namespace=ingress-nginx \
    -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")

  # Fall back to the api-gateway service in the app namespace
  if [[ -z "${LB_IP}" && -z "${LB_HOSTNAME}" ]]; then
    LB_HOSTNAME=$(kubectl get service "${LB_SERVICE}" \
      --namespace="${NAMESPACE}" \
      -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")

    LB_IP=$(kubectl get service "${LB_SERVICE}" \
      --namespace="${NAMESPACE}" \
      -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
  fi

  if [[ -z "${LB_IP}" && -z "${LB_HOSTNAME}" ]]; then
    log_error "Could not determine load-balancer IP or hostname from Kubernetes"
    log_error "Ensure the LoadBalancer service has an external IP assigned"
    exit 1
  fi

  # If we only got a hostname (AWS ALB/NLB), resolve it to an IP for health checks
  if [[ -z "${LB_IP}" && -n "${LB_HOSTNAME}" ]]; then
    LB_IP=$(dig +short "${LB_HOSTNAME}" | head -1 || echo "")
    log_info "LB hostname: ${LB_HOSTNAME} → resolved IP: ${LB_IP}"
  fi

  log_success "Load-balancer address: IP=${LB_IP}  HOSTNAME=${LB_HOSTNAME}"
}

# ---------------------------------------------------------------------------
# Step 2a: Update DNS via Route 53
# ---------------------------------------------------------------------------
update_route53() {
  local domain="$1"
  local new_ip="$2"
  local new_hostname="$3"

  log_dns "Updating Route53 record for ${domain}..."

  # Determine record type: A for IP, CNAME for hostname
  local record_type="A"
  local record_value="${new_ip}"
  if [[ -z "${new_ip}" && -n "${new_hostname}" ]]; then
    record_type="CNAME"
    record_value="${new_hostname}"
  fi

  local change_batch
  change_batch=$(cat <<EOF
{
  "Changes": [
    {
      "Action": "UPSERT",
      "ResourceRecordSet": {
        "Name": "${domain}.",
        "Type": "${record_type}",
        "TTL": ${DNS_TTL},
        "ResourceRecords": [
          { "Value": "${record_value}" }
        ]
      }
    }
  ]
}
EOF
)

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_info "[DRY-RUN] Would run: aws route53 change-resource-record-sets"
    log_info "[DRY-RUN] Change batch: ${change_batch}"
    return 0
  fi

  local change_id
  change_id=$(aws route53 change-resource-record-sets \
    --hosted-zone-id "${HOSTED_ZONE_ID}" \
    --change-batch "${change_batch}" \
    --region "${AWS_REGION}" \
    --query 'ChangeInfo.Id' \
    --output text)

  log_info "Route53 change submitted: ${change_id}"

  # Wait for Route53 to sync the change
  log_info "Waiting for Route53 change to propagate to their nameservers..."
  aws route53 wait resource-record-sets-changed \
    --id "${change_id}" \
    --region "${AWS_REGION}" || log_warn "Route53 wait timed out — change may still propagate"

  log_success "Route53 record updated: ${domain} → ${record_value}"
}

# ---------------------------------------------------------------------------
# Step 2b: Update DNS via Cloudflare API
# ---------------------------------------------------------------------------
update_cloudflare() {
  local domain="$1"
  local new_ip="$2"
  local new_hostname="$3"

  log_dns "Updating Cloudflare record for ${domain}..."

  local record_type="A"
  local record_value="${new_ip}"
  if [[ -z "${new_ip}" && -n "${new_hostname}" ]]; then
    record_type="CNAME"
    record_value="${new_hostname}"
  fi

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_info "[DRY-RUN] Would UPSERT Cloudflare ${record_type} record: ${domain} → ${record_value}"
    return 0
  fi

  # Find existing record ID
  local existing_id
  existing_id=$(curl -sf \
    --request GET \
    --url "https://api.cloudflare.com/client/v4/zones/${CLOUDFLARE_ZONE_ID}/dns_records?name=${domain}&type=${record_type}" \
    --header "Authorization: Bearer ${CLOUDFLARE_API_TOKEN}" \
    --header "Content-Type: application/json" \
    | python3 -c "import json,sys; data=json.load(sys.stdin); print(data['result'][0]['id'] if data['result'] else '')" 2>/dev/null || echo "")

  local payload="{\"type\":\"${record_type}\",\"name\":\"${domain}\",\"content\":\"${record_value}\",\"ttl\":${DNS_TTL},\"proxied\":true}"

  if [[ -n "${existing_id}" ]]; then
    # Update existing record
    curl -sf \
      --request PUT \
      --url "https://api.cloudflare.com/client/v4/zones/${CLOUDFLARE_ZONE_ID}/dns_records/${existing_id}" \
      --header "Authorization: Bearer ${CLOUDFLARE_API_TOKEN}" \
      --header "Content-Type: application/json" \
      --data "${payload}" > /dev/null
    log_success "Cloudflare record updated (id=${existing_id}): ${domain} → ${record_value}"
  else
    # Create new record
    curl -sf \
      --request POST \
      --url "https://api.cloudflare.com/client/v4/zones/${CLOUDFLARE_ZONE_ID}/dns_records" \
      --header "Authorization: Bearer ${CLOUDFLARE_API_TOKEN}" \
      --header "Content-Type: application/json" \
      --data "${payload}" > /dev/null
    log_success "Cloudflare record created: ${domain} → ${record_value}"
  fi
}

# ---------------------------------------------------------------------------
# Step 2: Update all DNS records
# ---------------------------------------------------------------------------
update_dns_records() {
  log_step "Step 2: Update DNS Records (${DNS_PROVIDER})"

  for domain in "${DOMAINS[@]}"; do
    # Save old IP for rollback
    local old_ip
    old_ip=$(dig +short "${domain}" | head -1 2>/dev/null || echo "")
    OLD_DNS_IPS+=("${old_ip:-unknown}")
    log_info "Current DNS for ${domain}: ${old_ip:-<not set>}"

    case "${DNS_PROVIDER}" in
      route53)    update_route53    "${domain}" "${LB_IP}" "${LB_HOSTNAME}" ;;
      cloudflare) update_cloudflare "${domain}" "${LB_IP}" "${LB_HOSTNAME}" ;;
    esac
  done

  log_success "All DNS records submitted for update"
}

# ---------------------------------------------------------------------------
# Step 3: Wait for DNS propagation
# ---------------------------------------------------------------------------
wait_for_propagation() {
  log_step "Step 3: Waiting for DNS Propagation (timeout: ${PROPAGATION_TIMEOUT}s)"

  local expected_ip="${LB_IP}"
  local elapsed=0
  local poll_interval=30
  local all_propagated=false

  while [[ "${elapsed}" -lt "${PROPAGATION_TIMEOUT}" ]]; do
    local all_ok=true

    for domain in "${DOMAINS[@]}"; do
      local resolved_ip
      resolved_ip=$(dig +short "${domain}" @8.8.8.8 | head -1 2>/dev/null || echo "")

      if [[ "${resolved_ip}" == "${expected_ip}" ]]; then
        log_info "  ${domain} → ${resolved_ip} ✓"
      else
        log_info "  ${domain} → ${resolved_ip:-<not resolved>} (expected: ${expected_ip})"
        all_ok=false
      fi
    done

    if [[ "${all_ok}" == "true" ]]; then
      all_propagated=true
      log_success "DNS propagation confirmed for all ${#DOMAINS[@]} domains!"
      break
    fi

    log_info "DNS not yet fully propagated — waiting ${poll_interval}s (${elapsed}s / ${PROPAGATION_TIMEOUT}s elapsed)"
    sleep "${poll_interval}"
    elapsed=$((elapsed + poll_interval))
  done

  if [[ "${all_propagated}" == "false" ]]; then
    log_warn "DNS propagation timed out after ${PROPAGATION_TIMEOUT}s"
    log_warn "Some public resolvers may still be serving old records — proceeding with health checks"
  fi
}

# ---------------------------------------------------------------------------
# Step 4: Health checks after propagation
# ---------------------------------------------------------------------------
run_health_checks() {
  log_step "Step 4: Post-Cutover Health Checks"

  local failed_checks=0

  for domain in "${DOMAINS[@]}"; do
    local url="https://${domain}/actuator/health"
    # Frontend domain uses a different health path
    if [[ "${domain}" == "bloodbank.example.com" ]]; then
      url="https://${domain}/health"
    fi

    log_info "Health check: ${url}"

    local http_code
    if [[ "${DRY_RUN}" == "true" ]]; then
      log_success "  [DRY-RUN] ${domain} — simulated 200 OK"
      continue
    fi

    http_code=$(curl -sk \
      --connect-timeout 10 \
      --max-time 15 \
      --resolve "${domain}:443:${LB_IP}" \
      -o /dev/null \
      -w "%{http_code}" \
      "${url}" 2>/dev/null || echo "000")

    if [[ "${http_code}" =~ ^(200|204)$ ]]; then
      log_success "  ${domain} — HTTP ${http_code} ✓"
    else
      log_error "  ${domain} — HTTP ${http_code} ✗"
      failed_checks=$((failed_checks + 1))
    fi
  done

  if [[ "${failed_checks}" -gt 0 ]]; then
    log_error "${failed_checks} health check(s) failed after DNS cutover"
    return 1
  fi

  log_success "All health checks passed"
  return 0
}

# ---------------------------------------------------------------------------
# Rollback: Restore old DNS records if health checks fail
# ---------------------------------------------------------------------------
rollback_dns() {
  log_step "ROLLBACK: Restoring Previous DNS Records"
  ROLLBACK_PERFORMED=true

  for i in "${!DOMAINS[@]}"; do
    local domain="${DOMAINS[${i}]}"
    local old_ip="${OLD_DNS_IPS[${i}]:-}"

    if [[ -z "${old_ip}" || "${old_ip}" == "unknown" ]]; then
      log_warn "No previous IP recorded for ${domain} — cannot roll back"
      continue
    fi

    log_dns "Rolling back ${domain} → ${old_ip}"

    case "${DNS_PROVIDER}" in
      route53)    update_route53    "${domain}" "${old_ip}" "" ;;
      cloudflare) update_cloudflare "${domain}" "${old_ip}" "" ;;
    esac
  done

  log_warn "Rollback complete — DNS records restored to previous values"
  log_warn "Waiting 60s for rollback propagation before reporting..."
  sleep 60
}

# ---------------------------------------------------------------------------
# Generate cutover report
# ---------------------------------------------------------------------------
generate_report() {
  log_step "Generating DNS Cutover Report"

  {
    echo "================================================================"
    echo " BloodBank — DNS Cutover Report"
    echo " Timestamp    : $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
    echo " DNS Provider : ${DNS_PROVIDER}"
    echo " Namespace    : ${NAMESPACE}"
    echo " Dry Run      : ${DRY_RUN}"
    echo " LB IP        : ${LB_IP}"
    echo " LB Hostname  : ${LB_HOSTNAME}"
    echo " TTL          : ${DNS_TTL}s"
    echo " Status       : ${CUTOVER_STATUS}"
    echo " Rollback     : ${ROLLBACK_PERFORMED}"
    echo "================================================================"
    echo ""
    echo "DOMAINS UPDATED:"
    for i in "${!DOMAINS[@]}"; do
      local domain="${DOMAINS[${i}]}"
      local old_ip="${OLD_DNS_IPS[${i}]:-unknown}"
      printf "  %-40s  %s → %s\n" "${domain}" "${old_ip}" "${LB_IP}"
    done
    echo ""
    echo "================================================================"
    if [[ "${CUTOVER_STATUS}" == "SUCCESS" ]]; then
      echo " ✓  DNS CUTOVER SUCCESSFUL"
    elif [[ "${ROLLBACK_PERFORMED}" == "true" ]]; then
      echo " ⚠  DNS CUTOVER FAILED — ROLLED BACK TO PREVIOUS RECORDS"
    else
      echo " ✗  DNS CUTOVER FAILED"
    fi
    echo "================================================================"
  } | tee "${REPORT_FILE}"

  # JSON report
  {
    echo "{"
    echo "  \"timestamp\": \"$(date -u '+%Y-%m-%dT%H:%M:%SZ')\","
    echo "  \"dns_provider\": \"${DNS_PROVIDER}\","
    echo "  \"namespace\": \"${NAMESPACE}\","
    echo "  \"dry_run\": ${DRY_RUN},"
    echo "  \"lb_ip\": \"${LB_IP}\","
    echo "  \"lb_hostname\": \"${LB_HOSTNAME}\","
    echo "  \"ttl_seconds\": ${DNS_TTL},"
    echo "  \"status\": \"${CUTOVER_STATUS}\","
    echo "  \"rollback_performed\": ${ROLLBACK_PERFORMED},"
    echo "  \"domains\": ["
    for i in "${!DOMAINS[@]}"; do
      local sep=","
      [[ $((i + 1)) -eq ${#DOMAINS[@]} ]] && sep=""
      printf "    {\"domain\": \"%s\", \"old_ip\": \"%s\", \"new_ip\": \"%s\"}%s\n" \
        "${DOMAINS[${i}]}" "${OLD_DNS_IPS[${i}]:-unknown}" "${LB_IP}" "${sep}"
    done
    echo "  ]"
    echo "}"
  } > "${REPORT_JSON}"

  log_info "Text report : ${REPORT_FILE}"
  log_info "JSON report : ${REPORT_JSON}"
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
  echo -e "${BOLD}${CYAN}"
  local _pad=39  # banner inner width: 62 total - 6 prefix (║     ) - 2 suffix ( ║) - 15 label
  echo "╔════════════════════════════════════════════════════════════╗"
  echo "║     BloodBank — DNS Cutover Script                         ║"
  echo "║     DNS_PROVIDER : ${DNS_PROVIDER}$(printf '%*s' $((_pad - ${#DNS_PROVIDER})) '') ║"
  echo "║     NAMESPACE    : ${NAMESPACE}$(printf '%*s' $((_pad - ${#NAMESPACE})) '') ║"
  echo "║     DRY_RUN      : ${DRY_RUN}$(printf '%*s' $((_pad - ${#DRY_RUN})) '') ║"
  echo "╚════════════════════════════════════════════════════════════╝"
  echo -e "${NC}"

  preflight_checks
  get_lb_address
  update_dns_records
  wait_for_propagation

  if run_health_checks; then
    CUTOVER_STATUS="SUCCESS"
    log_success "DNS cutover completed successfully!"
  else
    log_error "Health checks failed after DNS cutover — initiating rollback"
    rollback_dns
    CUTOVER_STATUS="FAILED_ROLLED_BACK"
    generate_report
    exit 1
  fi

  generate_report
}

main "$@"
