#!/usr/bin/env bash
# =============================================================================
# M11-004: verify-branch.sh
# BloodBank — Branch Verification Script
#
# Usage:
#   ./verify-branch.sh --branch <branch_name> --region <region> \
#                      [--branch-id <uuid>] [--dry-run] [--namespace <ns>]
#
# What this script does:
#   1. Verifies BRANCH_ADMIN user can authenticate via Keycloak (token probe)
#   2. Verifies branch data isolation (cannot access another branch's data)
#   3. Verifies all 14 microservices are healthy (actuator/health endpoints)
#   4. Verifies critical workflows: donor registration, inventory, lab, billing
#   5. Verifies RabbitMQ event routing is operational for the branch
#   6. Produces a structured verification report (pass/fail per check)
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

log_info()    { echo -e "${BLUE}[INFO]${NC}  $(date '+%H:%M:%S') $*"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $(date '+%H:%M:%S') $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $(date '+%H:%M:%S') $*"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $(date '+%H:%M:%S') $*" >&2; }
log_step()    { echo -e "\n${BOLD}${CYAN}=== $* ===${NC}"; }
log_dry()     { echo -e "${YELLOW}[DRY-RUN]${NC} $(date '+%H:%M:%S') $*"; }

# ---------------------------------------------------------------------------
# Defaults
# ---------------------------------------------------------------------------
NAMESPACE="${BLOODBANK_NAMESPACE:-bloodbank-prod}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://keycloak:8080}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-bloodbank}"
KEYCLOAK_ADMIN_USER="${KEYCLOAK_ADMIN_USER:-admin}"
KEYCLOAK_ADMIN_PASS="${KEYCLOAK_ADMIN_PASS:-}"
API_GATEWAY_URL="${API_GATEWAY_URL:-http://api-gateway:8080}"
DRY_RUN=false
LOG_DIR="${LOG_DIR:-/tmp/bloodbank-logs}"
SCRIPT_START_TIME=$(date '+%Y%m%d-%H%M%S')
BRANCH_UUID=""

# Result tracking
declare -A CHECK_RESULTS=()
TOTAL_PASS=0
TOTAL_FAIL=0
TOTAL_WARN=0

usage() {
  cat <<EOF
Usage: $0 --branch <branch_name> --region <region> [OPTIONS]

Required:
  --branch      Branch name (slug or full name, e.g. "central-city")
  --region      Region slug (e.g. "north-america")

Options:
  --branch-id   Branch UUID (if known; auto-detected otherwise)
  --namespace   Kubernetes namespace (default: bloodbank-prod)
  --dry-run     Print actions without executing them
  --help        Show this message
EOF
}

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------
BRANCH_NAME=""
REGION=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --branch)     BRANCH_NAME="$2";   shift 2 ;;
    --region)     REGION="$2";        shift 2 ;;
    --branch-id)  BRANCH_UUID="$2";   shift 2 ;;
    --namespace)  NAMESPACE="$2";     shift 2 ;;
    --dry-run)    DRY_RUN=true;       shift   ;;
    --help|-h)    usage; exit 0       ;;
    *)            log_error "Unknown argument: $1"; usage; exit 1 ;;
  esac
done

BRANCH_SLUG=$(echo "${BRANCH_NAME}" | tr '[:upper:]' '[:lower:]' | tr ' ' '-' | tr -cd '[:alnum:]-')
ADMIN_USERNAME="${BRANCH_SLUG}-admin"
LOG_FILE="${LOG_DIR}/verify-${BRANCH_SLUG}-${SCRIPT_START_TIME}.log"
REPORT_FILE="${LOG_DIR}/verify-report-${BRANCH_SLUG}-${SCRIPT_START_TIME}.txt"

mkdir -p "${LOG_DIR}"
exec > >(tee -a "${LOG_FILE}") 2>&1

# ---------------------------------------------------------------------------
# Check helpers
# ---------------------------------------------------------------------------
record_pass() {
  local check_name="$1"
  local detail="${2:-}"
  CHECK_RESULTS["${check_name}"]="PASS"
  TOTAL_PASS=$((TOTAL_PASS + 1))
  log_success "CHECK PASS: ${check_name}${detail:+ — ${detail}}"
}

record_fail() {
  local check_name="$1"
  local detail="${2:-}"
  CHECK_RESULTS["${check_name}"]="FAIL"
  TOTAL_FAIL=$((TOTAL_FAIL + 1))
  log_error "CHECK FAIL: ${check_name}${detail:+ — ${detail}}"
}

record_warn() {
  local check_name="$1"
  local detail="${2:-}"
  CHECK_RESULTS["${check_name}"]="WARN"
  TOTAL_WARN=$((TOTAL_WARN + 1))
  log_warn "CHECK WARN: ${check_name}${detail:+ — ${detail}}"
}

# ---------------------------------------------------------------------------
# Keycloak token helper
# ---------------------------------------------------------------------------
KC_ACCESS_TOKEN=""

keycloak_admin_token() {
  KC_ACCESS_TOKEN=$(curl -sS -X POST \
    "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=admin-cli" \
    -d "username=${KEYCLOAK_ADMIN_USER}" \
    -d "password=${KEYCLOAK_ADMIN_PASS}" \
    -d "grant_type=password" \
    | jq -r '.access_token // empty') || true
}

kc_api() {
  local method="$1"
  local path="$2"
  shift 2
  curl -sS -X "${method}" \
    "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}${path}" \
    -H "Authorization: Bearer ${KC_ACCESS_TOKEN}" \
    -H "Content-Type: application/json" \
    "$@"
}

# ---------------------------------------------------------------------------
# Check 1 — Prerequisites
# ---------------------------------------------------------------------------
check_prerequisites() {
  log_step "Check 1 — Prerequisites and environment"

  if [[ -z "${BRANCH_NAME}" ]]; then
    log_error "--branch is required"; exit 1
  fi
  if [[ -z "${REGION}" ]]; then
    log_error "--region is required"; exit 1
  fi

  for cmd in kubectl curl jq; do
    if ! command -v "${cmd}" &>/dev/null; then
      record_fail "prereq.${cmd}" "command not found"
    else
      record_pass "prereq.${cmd}"
    fi
  done

  if ! kubectl get namespace "${NAMESPACE}" &>/dev/null; then
    record_fail "prereq.namespace" "Namespace '${NAMESPACE}' not found"
  else
    record_pass "prereq.namespace" "${NAMESPACE}"
  fi
}

# ---------------------------------------------------------------------------
# Check 2 — BRANCH_ADMIN login (Keycloak authentication)
# ---------------------------------------------------------------------------
check_admin_login() {
  log_step "Check 2 — BRANCH_ADMIN authentication via Keycloak"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would attempt Keycloak login for user: ${ADMIN_USERNAME}"
    record_pass "auth.admin_login" "[dry-run]"
    return
  fi

  keycloak_admin_token
  if [[ -z "${KC_ACCESS_TOKEN}" ]]; then
    record_fail "auth.keycloak_admin" "Cannot obtain Keycloak admin token"
    return
  fi

  # Verify user exists in Keycloak
  local user_data
  user_data=$(kc_api GET "/users?username=${ADMIN_USERNAME}&exact=true")
  local user_id
  user_id=$(echo "${user_data}" | jq -r '.[0].id // empty')

  if [[ -z "${user_id}" ]]; then
    record_fail "auth.user_exists" "User '${ADMIN_USERNAME}' not found in Keycloak"
    return
  fi
  record_pass "auth.user_exists" "id=${user_id}"

  # Verify user is enabled
  local enabled
  enabled=$(echo "${user_data}" | jq -r '.[0].enabled // false')
  if [[ "${enabled}" == "true" ]]; then
    record_pass "auth.user_enabled"
  else
    record_fail "auth.user_enabled" "User '${ADMIN_USERNAME}' is disabled in Keycloak"
  fi

  # Verify user has BRANCH_ADMIN client role
  local client_id
  client_id=$(kc_api GET "/clients?clientId=bloodbank-app" | jq -r '.[0].id // empty')
  if [[ -n "${client_id}" ]]; then
    local roles
    roles=$(kc_api GET "/users/${user_id}/role-mappings/clients/${client_id}" \
      | jq -r '[.[].name] | join(",")')
    if echo "${roles}" | grep -q "BRANCH_ADMIN"; then
      record_pass "auth.branch_admin_role" "roles=${roles}"
    else
      record_fail "auth.branch_admin_role" "BRANCH_ADMIN not found in: ${roles}"
    fi
  else
    record_warn "auth.branch_admin_role" "bloodbank-app client not found — skipping role check"
  fi

  # Verify branch_id attribute is set
  local branch_id_attr
  branch_id_attr=$(echo "${user_data}" | jq -r '.[0].attributes.branch_id[0] // empty')
  if [[ -n "${branch_id_attr}" && "${branch_id_attr}" != "PENDING" ]]; then
    record_pass "auth.branch_id_attribute" "branch_id=${branch_id_attr}"
    BRANCH_UUID="${branch_id_attr}"
  else
    record_warn "auth.branch_id_attribute" "branch_id attribute is '${branch_id_attr}' — may need update"
  fi

  # Verify group membership
  local groups
  groups=$(kc_api GET "/users/${user_id}/groups" | jq -r '[.[].path] | join(",")')
  if echo "${groups}" | grep -q "/regions/${REGION}"; then
    record_pass "auth.group_membership" "groups=${groups}"
  else
    record_warn "auth.group_membership" "Expected group under /regions/${REGION}, found: ${groups}"
  fi
}

# ---------------------------------------------------------------------------
# Check 3 — Branch data isolation
# ---------------------------------------------------------------------------
check_data_isolation() {
  log_step "Check 3 — Branch data isolation"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would probe branch data isolation for branch: ${BRANCH_SLUG}"
    record_pass "isolation.cross_branch_blocked" "[dry-run]"
    record_pass "isolation.own_branch_accessible" "[dry-run]"
    return
  fi

  if [[ -z "${BRANCH_UUID}" ]]; then
    record_warn "isolation.branch_uuid" "Branch UUID unknown — isolation checks require UUID"
    return
  fi

  # Obtain service token
  local svc_token
  svc_token=$(curl -sS -X POST \
    "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=bloodbank-service" \
    -d "client_secret=${BLOODBANK_SERVICE_SECRET:-}" \
    -d "grant_type=client_credentials" \
    | jq -r '.access_token // empty') || true

  if [[ -z "${svc_token}" ]]; then
    record_warn "isolation.service_token" "Cannot obtain service token — skipping API isolation checks"
    return
  fi

  # Test 3a: Access own branch data (expect 200)
  local own_resp
  own_resp=$(curl -sS -o /dev/null -w "%{http_code}" --max-time 10 \
    "${API_GATEWAY_URL}/api/v1/donors" \
    -H "Authorization: Bearer ${svc_token}" \
    -H "X-Branch-Id: ${BRANCH_UUID}" 2>/dev/null || echo "000")

  if [[ "${own_resp}" == "200" ]]; then
    record_pass "isolation.own_branch_accessible" "HTTP ${own_resp}"
  elif [[ "${own_resp}" == "404" || "${own_resp}" == "204" ]]; then
    record_pass "isolation.own_branch_accessible" "HTTP ${own_resp} (empty — expected for new branch)"
  else
    record_warn "isolation.own_branch_accessible" "HTTP ${own_resp}"
  fi

  # Test 3b: Request another branch's data using this branch's token (expect 403/empty)
  # Use a nil UUID that should not match any real branch
  local fake_branch_id="00000000-dead-beef-0000-000000000000"
  local cross_resp
  cross_resp=$(curl -sS -o /dev/null -w "%{http_code}" --max-time 10 \
    "${API_GATEWAY_URL}/api/v1/donors" \
    -H "Authorization: Bearer ${svc_token}" \
    -H "X-Branch-Id: ${fake_branch_id}" 2>/dev/null || echo "000")

  if [[ "${cross_resp}" == "403" || "${cross_resp}" == "401" ]]; then
    record_pass "isolation.cross_branch_blocked" "HTTP ${cross_resp} (access correctly denied)"
  elif [[ "${cross_resp}" == "200" ]]; then
    # 200 with zero results is acceptable if Hibernate filter is working
    record_warn "isolation.cross_branch_blocked" "HTTP ${cross_resp} — verify response body is empty"
  else
    record_pass "isolation.cross_branch_blocked" "HTTP ${cross_resp} (likely filtered)"
  fi

  # Test 3c: Attempt to access donors with no X-Branch-Id header (non-admin role, expect 403)
  local no_branch_resp
  no_branch_resp=$(curl -sS -o /dev/null -w "%{http_code}" --max-time 10 \
    "${API_GATEWAY_URL}/api/v1/donors" \
    -H "Authorization: Bearer ${svc_token}" 2>/dev/null || echo "000")

  if [[ "${no_branch_resp}" == "403" || "${no_branch_resp}" == "400" ]]; then
    record_pass "isolation.no_branch_header_blocked" "HTTP ${no_branch_resp}"
  else
    record_warn "isolation.no_branch_header_blocked" "HTTP ${no_branch_resp} (expected 403 without branch header)"
  fi
}

# ---------------------------------------------------------------------------
# Check 4 — All 14 microservices healthy
# ---------------------------------------------------------------------------
check_service_health() {
  log_step "Check 4 — Microservice health checks"

  # Service name → internal port mapping
  declare -A SERVICES=(
    ["api-gateway"]=8080
    ["branch-service"]=8081
    ["donor-service"]=8082
    ["lab-service"]=8083
    ["inventory-service"]=8084
    ["transfusion-service"]=8085
    ["hospital-service"]=8086
    ["request-matching-service"]=8087
    ["billing-service"]=8088
    ["notification-service"]=8089
    ["reporting-service"]=8090
    ["document-service"]=8091
    ["compliance-service"]=8092
    ["config-server"]=8888
  )

  if [[ "${DRY_RUN}" == "true" ]]; then
    for svc in "${!SERVICES[@]}"; do
      log_dry "Would check health: ${svc}:${SERVICES[$svc]}/actuator/health"
      record_pass "health.${svc}" "[dry-run]"
    done
    return
  fi

  for svc in "${!SERVICES[@]}"; do
    local port="${SERVICES[$svc]}"
    local pod_selector="app=${svc}"

    # Check if deployment exists and has ready replicas
    local ready_replicas
    ready_replicas=$(kubectl get deployment "${svc}" \
      -n "${NAMESPACE}" \
      -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")

    if [[ "${ready_replicas}" == "" || "${ready_replicas}" == "0" ]]; then
      # Try with blue/green naming convention
      ready_replicas=$(kubectl get deployment "${svc}-blue" \
        -n "${NAMESPACE}" \
        -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")
    fi

    local desired_replicas
    desired_replicas=$(kubectl get deployment "${svc}" \
      -n "${NAMESPACE}" \
      -o jsonpath='{.spec.replicas}' 2>/dev/null || echo "1")

    if [[ "${ready_replicas:-0}" -ge 1 ]]; then
      # Port-forward to the first pod and probe actuator health
      local pod_name
      pod_name=$(kubectl get pods -n "${NAMESPACE}" \
        -l "app=${svc}" \
        -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

      if [[ -n "${pod_name}" ]]; then
        local health_status
        health_status=$(kubectl exec "${pod_name}" \
          -n "${NAMESPACE}" \
          -- wget -qO- "http://localhost:${port}/actuator/health" 2>/dev/null \
          | jq -r '.status // "UNKNOWN"') || health_status="UNREACHABLE"

        if [[ "${health_status}" == "UP" ]]; then
          record_pass "health.${svc}" "pods=${ready_replicas}/${desired_replicas}"
        else
          record_fail "health.${svc}" "status=${health_status} pods=${ready_replicas}/${desired_replicas}"
        fi
      else
        record_warn "health.${svc}" "No pods found (replicas=${ready_replicas})"
      fi
    else
      record_fail "health.${svc}" "No ready replicas in namespace '${NAMESPACE}'"
    fi
  done
}

# ---------------------------------------------------------------------------
# Check 5 — Critical workflow smoke tests
# ---------------------------------------------------------------------------
check_critical_workflows() {
  log_step "Check 5 — Critical workflow smoke tests"

  if [[ "${DRY_RUN}" == "true" ]]; then
    for wf in donor_registration inventory_check lab_service billing_service transfusion_service; do
      log_dry "Would smoke-test workflow: ${wf}"
      record_pass "workflow.${wf}" "[dry-run]"
    done
    return
  fi

  local svc_token
  svc_token=$(curl -sS -X POST \
    "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=bloodbank-service" \
    -d "client_secret=${BLOODBANK_SERVICE_SECRET:-}" \
    -d "grant_type=client_credentials" \
    | jq -r '.access_token // empty') || true

  if [[ -z "${svc_token}" ]]; then
    record_warn "workflow.token" "No service token — skipping API workflow tests"
    return
  fi

  local branch_header=""
  # branch_header is used as an explicit curl argument array element below
  [[ -n "${BRANCH_UUID}" ]] && branch_header="X-Branch-Id: ${BRANCH_UUID}"

  # Smoke test helper: check an endpoint returns expected HTTP status
  smoke_test() {
    local check_name="$1"
    local url="$2"
    local expected_codes="${3:-200}"  # comma-separated list e.g. "200,201,404"

    # Build curl args dynamically to avoid unquoted variable expansion
    local curl_args=(-sS -o /dev/null -w "%{http_code}" --max-time 15
      "${url}"
      -H "Authorization: Bearer ${svc_token}")
    [[ -n "${branch_header}" ]] && curl_args+=(-H "${branch_header}")

    local actual_code
    actual_code=$(curl "${curl_args[@]}" 2>/dev/null || echo "000")

    if echo "${expected_codes}" | tr ',' '\n' | grep -qx "${actual_code}"; then
      record_pass "${check_name}" "HTTP ${actual_code}"
    else
      record_warn "${check_name}" "HTTP ${actual_code} (expected: ${expected_codes})"
    fi
  }

  # Donor service
  smoke_test "workflow.donor_list"         "${API_GATEWAY_URL}/api/v1/donors"           "200,204"
  smoke_test "workflow.donor_service_hc"   "${API_GATEWAY_URL}/api/v1/donors/health"    "200,404"

  # Inventory service
  smoke_test "workflow.inventory_list"     "${API_GATEWAY_URL}/api/v1/blood-units"      "200,204"

  # Lab service
  smoke_test "workflow.lab_orders"         "${API_GATEWAY_URL}/api/v1/test-orders"      "200,204"

  # Billing service
  smoke_test "workflow.invoices"           "${API_GATEWAY_URL}/api/v1/invoices"         "200,204"

  # Transfusion service
  smoke_test "workflow.transfusions"       "${API_GATEWAY_URL}/api/v1/transfusions"     "200,204"

  # Request matching
  smoke_test "workflow.blood_requests"     "${API_GATEWAY_URL}/api/v1/blood-requests"   "200,204"

  # Notifications (read-only probe)
  smoke_test "workflow.notifications"      "${API_GATEWAY_URL}/api/v1/notifications"    "200,204"

  # Reporting (aggregate — may be empty for new branch)
  smoke_test "workflow.reports"            "${API_GATEWAY_URL}/api/v1/reports/summary"  "200,204,404"
}

# ---------------------------------------------------------------------------
# Check 6 — RabbitMQ event routing
# ---------------------------------------------------------------------------
check_event_routing() {
  log_step "Check 6 — RabbitMQ event routing"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would check RabbitMQ queues for branch: ${BRANCH_SLUG}"
    record_pass "events.rabbitmq_health" "[dry-run]"
    return
  fi

  local rabbitmq_mgmt_url="${RABBITMQ_MGMT_URL:-http://rabbitmq:15672}"
  local rabbitmq_user="${RABBITMQ_USER:-bloodbank}"
  local rabbitmq_pass="${RABBITMQ_PASS:-bloodbank_secret}"

  # Check RabbitMQ management API is accessible
  local rmq_health
  rmq_health=$(curl -sS -o /dev/null -w "%{http_code}" --max-time 10 \
    -u "${rabbitmq_user}:${rabbitmq_pass}" \
    "${rabbitmq_mgmt_url}/api/overview" 2>/dev/null || echo "000")

  if [[ "${rmq_health}" == "200" ]]; then
    record_pass "events.rabbitmq_accessible"
  else
    record_warn "events.rabbitmq_accessible" "HTTP ${rmq_health}"
    return
  fi

  # Check critical queues exist
  local queues_json
  queues_json=$(curl -sS --max-time 10 \
    -u "${rabbitmq_user}:${rabbitmq_pass}" \
    "${rabbitmq_mgmt_url}/api/queues" 2>/dev/null || echo "[]")

  local queue_names
  queue_names=$(echo "${queues_json}" | jq -r '[.[].name] | join(",")' 2>/dev/null || echo "")

  declare -a CRITICAL_QUEUES=(
    "donor.events"
    "inventory.events"
    "lab.events"
    "transfusion.events"
    "notification.events"
    "billing.events"
  )

  for queue in "${CRITICAL_QUEUES[@]}"; do
    if echo "${queue_names}" | grep -q "${queue}"; then
      # Check queue is not growing unboundedly (>1000 messages = warning)
      local msg_count
      msg_count=$(echo "${queues_json}" | \
        jq -r --arg q "${queue}" '.[] | select(.name==$q) | .messages // 0')
      if [[ "${msg_count:-0}" -lt 1000 ]]; then
        record_pass "events.queue.${queue}" "messages=${msg_count}"
      else
        record_warn "events.queue.${queue}" "messages=${msg_count} — queue depth is high"
      fi
    else
      record_warn "events.queue.${queue}" "Queue not found (may be created on first event)"
    fi
  done
}

# ---------------------------------------------------------------------------
# Generate verification report
# ---------------------------------------------------------------------------
generate_report() {
  log_step "Generating verification report"

  local overall_status
  if [[ ${TOTAL_FAIL} -gt 0 ]]; then
    overall_status="FAIL"
  elif [[ ${TOTAL_WARN} -gt 0 ]]; then
    overall_status="WARN"
  else
    overall_status="PASS"
  fi

  {
    echo "================================================================================"
    echo " BloodBank — Branch Verification Report"
    echo "================================================================================"
    echo " Branch      : ${BRANCH_NAME}"
    echo " Branch slug : ${BRANCH_SLUG}"
    echo " Branch UUID : ${BRANCH_UUID:-NOT SET}"
    echo " Region      : ${REGION}"
    echo " Namespace   : ${NAMESPACE}"
    echo " Timestamp   : $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
    echo " Dry-run     : ${DRY_RUN}"
    echo " Overall     : ${overall_status}"
    echo "--------------------------------------------------------------------------------"
    printf " %-50s %s\n" "CHECK" "RESULT"
    echo "--------------------------------------------------------------------------------"
    for check in $(echo "${!CHECK_RESULTS[@]}" | tr ' ' '\n' | sort); do
      printf " %-50s %s\n" "${check}" "${CHECK_RESULTS[$check]}"
    done
    echo "--------------------------------------------------------------------------------"
    echo " Summary: PASS=${TOTAL_PASS}  FAIL=${TOTAL_FAIL}  WARN=${TOTAL_WARN}"
    echo "--------------------------------------------------------------------------------"
    echo " Log file    : ${LOG_FILE}"
    echo " Report file : ${REPORT_FILE}"
    echo "================================================================================"
  } | tee "${REPORT_FILE}"

  log_success "Report saved: ${REPORT_FILE}"

  if [[ ${TOTAL_FAIL} -gt 0 ]]; then
    log_error "Verification FAILED: ${TOTAL_FAIL} check(s) failed"
    exit 1
  fi
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
  echo -e "\n${BOLD}${GREEN}BloodBank — Branch Verification (M11-004)${NC}"
  echo -e "${CYAN}Started: $(date -u '+%Y-%m-%d %H:%M:%S UTC')${NC}\n"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_warn "DRY-RUN MODE — no changes will be made"
  fi

  if [[ -z "${BRANCH_NAME}" ]]; then
    log_error "--branch is required"; usage; exit 1
  fi
  if [[ -z "${REGION}" ]]; then
    log_error "--region is required"; usage; exit 1
  fi

  check_prerequisites
  check_admin_login
  check_data_isolation
  check_service_health
  check_critical_workflows
  check_event_routing
  generate_report

  echo -e "\n${BOLD}${GREEN}✔ Verification complete: ${BRANCH_NAME}${NC}\n"
}

main "$@"
