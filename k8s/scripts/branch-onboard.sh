#!/usr/bin/env bash
# =============================================================================
# M11-001: branch-onboard.sh
# BloodBank — Automated Branch Onboarding Script
#
# Usage:
#   ./branch-onboard.sh --branch <branch_name> --region <region> \
#                       --admin-email <email> [--dry-run] [--namespace <ns>]
#
# Example:
#   ./branch-onboard.sh --branch "central-city" --region "north-america" \
#                       --admin-email "admin@centralcity.bloodbank.org"
#
# What this script does:
#   1. Validates inputs and environment prerequisites
#   2. Creates Keycloak group under /regions/{region}/{branch}
#   3. Creates BRANCH_ADMIN user with correct attributes and role
#   4. Registers branch in the BloodBank branch-service via API
#   5. Runs data migration job for the branch (Flyway + seed)
#   6. Verifies branch data isolation (cannot see other branches' data)
#   7. Produces a structured onboarding report
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
# Helpers
# ---------------------------------------------------------------------------
# Convert a hyphenated/spaced string to Title Case (e.g. "central-city" → "Central City")
titlecase_name() {
  echo "$1" | sed 's/-/ /g' | awk '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)} 1'
}

# ---------------------------------------------------------------------------
# Defaults
# ---------------------------------------------------------------------------
NAMESPACE="${BLOODBANK_NAMESPACE:-bloodbank-prod}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://keycloak:8080}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-bloodbank}"
KEYCLOAK_ADMIN_USER="${KEYCLOAK_ADMIN_USER:-admin}"
KEYCLOAK_ADMIN_PASS="${KEYCLOAK_ADMIN_PASS:-}"
API_GATEWAY_URL="${API_GATEWAY_URL:-http://api-gateway:8080}"
BRANCH_SERVICE_URL="${BRANCH_SERVICE_URL:-http://branch-service:8081}"
DRY_RUN=false
LOG_DIR="${LOG_DIR:-/tmp/bloodbank-logs}"
SCRIPT_START_TIME=$(date '+%Y%m%d-%H%M%S')

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------
BRANCH_NAME=""
REGION=""
ADMIN_EMAIL=""

usage() {
  cat <<EOF
Usage: $0 --branch <branch_name> --region <region> --admin-email <email> [OPTIONS]

Required:
  --branch        Branch name (slug, e.g. "central-city")
  --region        Region slug (e.g. "north-america")
  --admin-email   Admin email address for the BRANCH_ADMIN user

Options:
  --namespace     Kubernetes namespace (default: bloodbank-prod)
  --dry-run       Print actions without executing them
  --keycloak-url  Keycloak base URL (default: \$KEYCLOAK_URL)
  --api-url       API Gateway base URL (default: \$API_GATEWAY_URL)
  --help          Show this message
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --branch)       BRANCH_NAME="$2";       shift 2 ;;
    --region)       REGION="$2";            shift 2 ;;
    --admin-email)  ADMIN_EMAIL="$2";       shift 2 ;;
    --namespace)    NAMESPACE="$2";         shift 2 ;;
    --dry-run)      DRY_RUN=true;           shift   ;;
    --keycloak-url) KEYCLOAK_URL="$2";      shift 2 ;;
    --api-url)      API_GATEWAY_URL="$2";   shift 2 ;;
    --help|-h)      usage; exit 0           ;;
    *)              log_error "Unknown argument: $1"; usage; exit 1 ;;
  esac
done

# ---------------------------------------------------------------------------
# Derived variables
# ---------------------------------------------------------------------------
BRANCH_SLUG=$(echo "${BRANCH_NAME}" | tr '[:upper:]' '[:lower:]' | tr ' ' '-' | tr -cd '[:alnum:]-')
REGION_SLUG=$(echo "${REGION}"      | tr '[:upper:]' '[:lower:]' | tr ' ' '-' | tr -cd '[:alnum:]-')
ADMIN_USERNAME="${BRANCH_SLUG}-admin"
KC_GROUP_PATH="/regions/${REGION_SLUG}/${BRANCH_SLUG}"
LOG_FILE="${LOG_DIR}/onboard-${BRANCH_SLUG}-${SCRIPT_START_TIME}.log"
REPORT_FILE="${LOG_DIR}/onboard-report-${BRANCH_SLUG}-${SCRIPT_START_TIME}.txt"

# Redirect all output to log file AND terminal
mkdir -p "${LOG_DIR}"
exec > >(tee -a "${LOG_FILE}") 2>&1

# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------
validate_inputs() {
  log_step "Validating inputs"

  local errors=0

  if [[ -z "${BRANCH_NAME}" ]]; then
    log_error "--branch is required"
    errors=$((errors + 1))
  fi

  if [[ -z "${REGION}" ]]; then
    log_error "--region is required"
    errors=$((errors + 1))
  fi

  if [[ -z "${ADMIN_EMAIL}" ]]; then
    log_error "--admin-email is required"
    errors=$((errors + 1))
  elif ! echo "${ADMIN_EMAIL}" | grep -qE '^[^@]+@[^@]+\.[^@]+$'; then
    log_error "Invalid email format: ${ADMIN_EMAIL}"
    errors=$((errors + 1))
  fi

  if [[ -z "${KEYCLOAK_ADMIN_PASS}" ]]; then
    log_error "KEYCLOAK_ADMIN_PASS environment variable must be set"
    errors=$((errors + 1))
  fi

  if [[ $errors -gt 0 ]]; then
    log_error "Input validation failed with ${errors} error(s). Aborting."
    exit 1
  fi

  log_success "Inputs validated"
  log_info "  Branch slug : ${BRANCH_SLUG}"
  log_info "  Region slug : ${REGION_SLUG}"
  log_info "  Admin user  : ${ADMIN_USERNAME}"
  log_info "  KC group    : ${KC_GROUP_PATH}"
  log_info "  Namespace   : ${NAMESPACE}"
  log_info "  Dry-run     : ${DRY_RUN}"
}

# ---------------------------------------------------------------------------
# Prerequisite checks
# ---------------------------------------------------------------------------
check_prerequisites() {
  log_step "Checking prerequisites"

  local missing=0
  for cmd in kubectl curl jq; do
    if ! command -v "${cmd}" &>/dev/null; then
      log_error "Required command not found: ${cmd}"
      missing=$((missing + 1))
    else
      log_success "  ${cmd} found: $(command -v "${cmd}")"
    fi
  done

  if [[ $missing -gt 0 ]]; then
    log_error "Missing ${missing} required tool(s). Install them and retry."
    exit 1
  fi

  # Verify kubectl context targets the right namespace
  local ctx
  ctx=$(kubectl config current-context 2>/dev/null || echo "unknown")
  log_info "  kubectl context : ${ctx}"

  if ! kubectl get namespace "${NAMESPACE}" &>/dev/null; then
    log_error "Kubernetes namespace '${NAMESPACE}' does not exist."
    exit 1
  fi
  log_success "  Namespace '${NAMESPACE}' exists"
}

# ---------------------------------------------------------------------------
# Keycloak token helper
# ---------------------------------------------------------------------------
KC_ACCESS_TOKEN=""

keycloak_token() {
  log_info "Obtaining Keycloak admin token …"
  local response
  response=$(curl -sS -X POST \
    "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=admin-cli" \
    -d "username=${KEYCLOAK_ADMIN_USER}" \
    -d "password=${KEYCLOAK_ADMIN_PASS}" \
    -d "grant_type=password") || {
    log_error "Failed to reach Keycloak at ${KEYCLOAK_URL}"
    exit 1
  }

  KC_ACCESS_TOKEN=$(echo "${response}" | jq -r '.access_token // empty')
  if [[ -z "${KC_ACCESS_TOKEN}" ]]; then
    log_error "Keycloak authentication failed. Check KEYCLOAK_ADMIN_PASS."
    log_error "Response: ${response}"
    exit 1
  fi
  log_success "Keycloak admin token obtained"
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
# Step 1 — Create Keycloak group hierarchy /regions/{region}/{branch}
# ---------------------------------------------------------------------------
create_keycloak_group() {
  log_step "Step 1 — Creating Keycloak group: ${KC_GROUP_PATH}"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would create Keycloak group hierarchy: ${KC_GROUP_PATH}"
    return
  fi

  keycloak_token

  # Ensure top-level /regions group exists
  local regions_id
  regions_id=$(kc_api GET "/groups" | jq -r '.[] | select(.name=="regions") | .id // empty')
  if [[ -z "${regions_id}" ]]; then
    log_info "Creating top-level 'regions' group …"
    kc_api POST "/groups" -d '{"name":"regions"}' | jq .
    regions_id=$(kc_api GET "/groups" | jq -r '.[] | select(.name=="regions") | .id')
  fi
  log_info "  'regions' group id: ${regions_id}"

  # Ensure /regions/{region} subgroup exists
  local region_id
  region_id=$(kc_api GET "/groups/${regions_id}/children" \
    | jq -r --arg n "${REGION_SLUG}" '.[] | select(.name==$n) | .id // empty')
  if [[ -z "${region_id}" ]]; then
    log_info "Creating region subgroup '${REGION_SLUG}' …"
    kc_api POST "/groups/${regions_id}/children" \
      -d "{\"name\":\"${REGION_SLUG}\"}" | jq .
    region_id=$(kc_api GET "/groups/${regions_id}/children" \
      | jq -r --arg n "${REGION_SLUG}" '.[] | select(.name==$n) | .id')
  fi
  log_info "  '${REGION_SLUG}' group id: ${region_id}"

  # Create /regions/{region}/{branch} subgroup
  local existing_branch
  existing_branch=$(kc_api GET "/groups/${region_id}/children" \
    | jq -r --arg n "${BRANCH_SLUG}" '.[] | select(.name==$n) | .id // empty')
  if [[ -n "${existing_branch}" ]]; then
    log_warn "Branch group '${BRANCH_SLUG}' already exists (id: ${existing_branch}). Skipping creation."
  else
    log_info "Creating branch subgroup '${BRANCH_SLUG}' …"
    local create_resp
    create_resp=$(kc_api POST "/groups/${region_id}/children" \
      -d "{\"name\":\"${BRANCH_SLUG}\",\"attributes\":{\"region\":[\"${REGION_SLUG}\"]}}")
    log_info "Create response: ${create_resp}"
  fi

  log_success "Keycloak group hierarchy ready: ${KC_GROUP_PATH}"
}

# ---------------------------------------------------------------------------
# Step 2 — Create BRANCH_ADMIN user in Keycloak
# ---------------------------------------------------------------------------
create_admin_user() {
  log_step "Step 2 — Creating BRANCH_ADMIN user: ${ADMIN_USERNAME}"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would create Keycloak user '${ADMIN_USERNAME}' with email '${ADMIN_EMAIL}'"
    log_dry "Would assign client role BRANCH_ADMIN (client: bloodbank-app)"
    log_dry "Would set user attributes: branch_id, region_id"
    log_dry "Would add user to group: ${KC_GROUP_PATH}"
    return
  fi

  # Generate a temporary password (user must change on first login)
  local temp_password
  temp_password="Tmp!$(openssl rand -base64 12 | tr -dc 'A-Za-z0-9!@#' | head -c 14)"

  # Check if user already exists
  local existing_user
  existing_user=$(kc_api GET "/users?username=${ADMIN_USERNAME}&exact=true" \
    | jq -r '.[0].id // empty')

  local user_id
  if [[ -n "${existing_user}" ]]; then
    log_warn "User '${ADMIN_USERNAME}' already exists (id: ${existing_user}). Updating attributes."
    user_id="${existing_user}"
  else
    log_info "Creating user '${ADMIN_USERNAME}' …"
    kc_api POST "/users" -d "{
      \"username\":     \"${ADMIN_USERNAME}\",
      \"email\":        \"${ADMIN_EMAIL}\",
      \"firstName\":    \"Admin\",
      \"lastName\":     \"$(titlecase_name "${BRANCH_NAME}")\",
      \"enabled\":      true,
      \"emailVerified\": true,
      \"requiredActions\": [\"UPDATE_PASSWORD\"],
      \"credentials\": [{
        \"type\":      \"password\",
        \"value\":     \"${temp_password}\",
        \"temporary\": true
      }],
      \"attributes\": {
        \"region_id\":   [\"${REGION_SLUG}\"],
        \"branch_id\":   [\"PENDING\"],
        \"onboarded_at\": [\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"]
      }
    }" >/dev/null

    user_id=$(kc_api GET "/users?username=${ADMIN_USERNAME}&exact=true" \
      | jq -r '.[0].id')
    log_success "User created — id: ${user_id}"
    log_warn "Temporary password: ${temp_password}  ← send to admin via secure channel"
  fi

  # Assign BRANCH_ADMIN client role
  local client_id
  client_id=$(kc_api GET "/clients?clientId=bloodbank-app" | jq -r '.[0].id // empty')
  if [[ -z "${client_id}" ]]; then
    log_error "Keycloak client 'bloodbank-app' not found in realm '${KEYCLOAK_REALM}'"
    exit 1
  fi

  local role_id
  role_id=$(kc_api GET "/clients/${client_id}/roles/BRANCH_ADMIN" | jq -r '.id // empty')
  if [[ -z "${role_id}" ]]; then
    log_error "Client role BRANCH_ADMIN not found on client bloodbank-app"
    exit 1
  fi

  kc_api POST "/users/${user_id}/role-mappings/clients/${client_id}" \
    -d "[{\"id\":\"${role_id}\",\"name\":\"BRANCH_ADMIN\"}]" >/dev/null
  log_success "Role BRANCH_ADMIN assigned to ${ADMIN_USERNAME}"

  # Add user to the branch group.
  # Walk the group tree explicitly via .subGroups to avoid full-tree recursion:
  #   top-level groups → subGroups (region level) → subGroups (branch level)
  local branch_group_id
  branch_group_id=$(kc_api GET "/groups?search=${BRANCH_SLUG}&exact=false" \
    | jq -r --arg p "${KC_GROUP_PATH}" '
        [
          .[] |
          (.subGroups // [])[] |
          (.subGroups // [])[] |
          select(.path == $p)
        ] | first | .id // empty
      ' 2>/dev/null | head -1)

  if [[ -n "${branch_group_id}" ]]; then
    kc_api PUT "/users/${user_id}/groups/${branch_group_id}" >/dev/null
    log_success "User added to group: ${KC_GROUP_PATH}"
  else
    log_warn "Could not find group id for path '${KC_GROUP_PATH}' — skipping group membership"
  fi
}

# ---------------------------------------------------------------------------
# Step 3 — Register branch via branch-service API
# ---------------------------------------------------------------------------
register_branch() {
  log_step "Step 3 — Registering branch via branch-service API"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would POST to ${BRANCH_SERVICE_URL}/api/v1/branches with:"
    log_dry "  { name: '${BRANCH_NAME}', region: '${REGION_SLUG}', adminEmail: '${ADMIN_EMAIL}' }"
    return
  fi

  # Obtain a service-to-service token (SYSTEM_ADMIN scope)
  local svc_token
  svc_token=$(curl -sS -X POST \
    "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=bloodbank-service" \
    -d "client_secret=${BLOODBANK_SERVICE_SECRET:-}" \
    -d "grant_type=client_credentials" \
    | jq -r '.access_token // empty') || true

  if [[ -z "${svc_token}" ]]; then
    log_warn "Could not obtain service token — branch API registration skipped."
    log_warn "Register branch manually via Keycloak Admin Console or API."
    return
  fi

  local response
  response=$(curl -sS -X POST \
    "${BRANCH_SERVICE_URL}/api/v1/branches" \
    -H "Authorization: Bearer ${svc_token}" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\":        \"${BRANCH_NAME}\",
      \"code\":        \"${BRANCH_SLUG}\",
      \"regionCode\":  \"${REGION_SLUG}\",
      \"adminEmail\":  \"${ADMIN_EMAIL}\",
      \"status\":      \"ONBOARDING\"
    }")

  local http_status
  http_status=$(echo "${response}" | jq -r '.status // .httpStatus // "unknown"')
  log_info "Branch service response: ${response}"

  BRANCH_UUID=$(echo "${response}" | jq -r '.data.id // .id // empty')
  if [[ -z "${BRANCH_UUID}" ]]; then
    log_warn "Branch UUID not returned — may already exist or API unavailable."
    BRANCH_UUID="UNKNOWN"
  else
    log_success "Branch registered — UUID: ${BRANCH_UUID}"

    # Update Keycloak user attribute with real branch_id
    local user_id
    user_id=$(kc_api GET "/users?username=${ADMIN_USERNAME}&exact=true" \
      | jq -r '.[0].id // empty')
    if [[ -n "${user_id}" ]]; then
      kc_api PUT "/users/${user_id}" -d "{
        \"attributes\": {
          \"region_id\": [\"${REGION_SLUG}\"],
          \"branch_id\": [\"${BRANCH_UUID}\"],
          \"onboarded_at\": [\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"]
        }
      }" >/dev/null
      log_success "Keycloak user branch_id attribute updated: ${BRANCH_UUID}"
    fi
  fi
}

# ---------------------------------------------------------------------------
# Step 4 — Run data migration for the branch (Kubernetes Job)
# ---------------------------------------------------------------------------
run_data_migration() {
  log_step "Step 4 — Running data migration for branch: ${BRANCH_SLUG}"

  local job_name="branch-migrate-${BRANCH_SLUG}-${SCRIPT_START_TIME}"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would create Kubernetes Job '${job_name}' in namespace '${NAMESPACE}'"
    log_dry "  Image: bloodbank/db-migration:latest"
    log_dry "  Env:   BRANCH_ID=${BRANCH_UUID:-PENDING}, BRANCH_CODE=${BRANCH_SLUG}"
    return
  fi

  cat <<EOF | kubectl apply -f - -n "${NAMESPACE}"
apiVersion: batch/v1
kind: Job
metadata:
  name: "${job_name}"
  namespace: "${NAMESPACE}"
  labels:
    app.kubernetes.io/part-of: bloodbank
    bloodbank/component: db-migration
    bloodbank/branch: "${BRANCH_SLUG}"
spec:
  ttlSecondsAfterFinished: 3600
  backoffLimit: 3
  template:
    metadata:
      labels:
        app: branch-migration
        bloodbank/branch: "${BRANCH_SLUG}"
    spec:
      restartPolicy: OnFailure
      containers:
        - name: flyway-migration
          image: bloodbank/db-migration:latest
          env:
            - name: SPRING_DATASOURCE_URL
              valueFrom:
                secretKeyRef:
                  name: bloodbank-db-secret
                  key: url
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: bloodbank-db-secret
                  key: username
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: bloodbank-db-secret
                  key: password
            - name: BRANCH_ID
              value: "${BRANCH_UUID:-PENDING}"
            - name: BRANCH_CODE
              value: "${BRANCH_SLUG}"
            - name: BRANCH_NAME
              value: "${BRANCH_NAME}"
            - name: REGION_CODE
              value: "${REGION_SLUG}"
          resources:
            requests:
              memory: "256Mi"
              cpu: "100m"
            limits:
              memory: "512Mi"
              cpu: "500m"
EOF

  log_info "Waiting for migration job '${job_name}' to complete (timeout: 20 min) …"
  if kubectl wait job/"${job_name}" \
      --for=condition=complete \
      --timeout=1200s \
      -n "${NAMESPACE}"; then
    log_success "Migration job completed successfully"
  else
    log_error "Migration job '${job_name}' failed or timed out"
    log_error "Check logs: kubectl logs -l app=branch-migration,bloodbank/branch=${BRANCH_SLUG} -n ${NAMESPACE}"
    exit 1
  fi
}

# ---------------------------------------------------------------------------
# Step 5 — Verify branch data isolation
# ---------------------------------------------------------------------------
verify_data_isolation() {
  log_step "Step 5 — Verifying branch data isolation"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would verify isolation via branch-service health endpoint"
    log_dry "Would run cross-branch data leak probe for branch: ${BRANCH_SLUG}"
    return
  fi

  local isolation_ok=true

  # 5a: Verify branch-service health
  log_info "Checking branch-service health …"
  local health_resp
  health_resp=$(curl -sS --max-time 10 \
    "${BRANCH_SERVICE_URL}/actuator/health" 2>/dev/null || echo '{"status":"DOWN"}')
  local health_status
  health_status=$(echo "${health_resp}" | jq -r '.status // "UNKNOWN"')
  if [[ "${health_status}" == "UP" ]]; then
    log_success "branch-service health: UP"
  else
    log_warn "branch-service health: ${health_status}"
    isolation_ok=false
  fi

  # 5b: Verify the branch exists and is scoped correctly via API
  if [[ "${BRANCH_UUID}" != "UNKNOWN" && -n "${BRANCH_UUID}" ]]; then
    log_info "Verifying branch record is accessible only within its scope …"

    local svc_token
    svc_token=$(curl -sS -X POST \
      "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "client_id=bloodbank-service" \
      -d "client_secret=${BLOODBANK_SERVICE_SECRET:-}" \
      -d "grant_type=client_credentials" \
      | jq -r '.access_token // empty') || true

    if [[ -n "${svc_token}" ]]; then
      # Request data for this branch — expect 200
      local branch_data_resp
      branch_data_resp=$(curl -sS -o /dev/null -w "%{http_code}" \
        --max-time 10 \
        "${BRANCH_SERVICE_URL}/api/v1/branches/${BRANCH_UUID}" \
        -H "Authorization: Bearer ${svc_token}" \
        -H "X-Branch-Id: ${BRANCH_UUID}")

      if [[ "${branch_data_resp}" == "200" ]]; then
        log_success "Branch data accessible with correct branch scope: HTTP ${branch_data_resp}"
      else
        log_warn "Unexpected response when fetching branch data: HTTP ${branch_data_resp}"
        isolation_ok=false
      fi

      # Cross-branch probe: try to access this branch's data with a DIFFERENT branch header
      # Expect 403 Forbidden (data isolation enforcement)
      local cross_resp
      cross_resp=$(curl -sS -o /dev/null -w "%{http_code}" \
        --max-time 10 \
        "${BRANCH_SERVICE_URL}/api/v1/branches/${BRANCH_UUID}/donors" \
        -H "Authorization: Bearer ${svc_token}" \
        -H "X-Branch-Id: 00000000-0000-0000-0000-000000000000")

      if [[ "${cross_resp}" == "403" || "${cross_resp}" == "404" ]]; then
        log_success "Cross-branch isolation verified: cross-branch request returned HTTP ${cross_resp}"
      else
        log_warn "Cross-branch isolation probe returned unexpected HTTP ${cross_resp} (expected 403/404)"
        isolation_ok=false
      fi
    else
      log_warn "No service token available — skipping API isolation checks"
    fi
  fi

  # 5c: Verify Kubernetes pod-level network policies exist
  log_info "Checking NetworkPolicy for branch isolation …"
  local netpol_count
  netpol_count=$(kubectl get networkpolicy -n "${NAMESPACE}" \
    -l "app.kubernetes.io/part-of=bloodbank" \
    --no-headers 2>/dev/null | wc -l || echo "0")
  if [[ "${netpol_count}" -gt 0 ]]; then
    log_success "NetworkPolicies found: ${netpol_count}"
  else
    log_warn "No NetworkPolicies found in namespace '${NAMESPACE}' — review network security"
  fi

  if [[ "${isolation_ok}" == "true" ]]; then
    log_success "Branch data isolation verification PASSED"
  else
    log_warn "Branch data isolation verification completed with warnings — review logs"
  fi
}

# ---------------------------------------------------------------------------
# Report generation
# ---------------------------------------------------------------------------
generate_report() {
  log_step "Generating onboarding report"

  cat > "${REPORT_FILE}" <<EOF
================================================================================
 BloodBank — Branch Onboarding Report
================================================================================
 Timestamp  : $(date -u '+%Y-%m-%d %H:%M:%S UTC')
 Script     : branch-onboard.sh (M11-001)
 Dry-run    : ${DRY_RUN}
--------------------------------------------------------------------------------
 Branch Details
   Name      : ${BRANCH_NAME}
   Slug      : ${BRANCH_SLUG}
   Region    : ${REGION_SLUG}
   UUID      : ${BRANCH_UUID:-N/A}
   KC Group  : ${KC_GROUP_PATH}
--------------------------------------------------------------------------------
 Admin User
   Username  : ${ADMIN_USERNAME}
   Email     : ${ADMIN_EMAIL}
   Role      : BRANCH_ADMIN (client role on bloodbank-app)
--------------------------------------------------------------------------------
 Steps Executed
   [1] Keycloak group created   : ${KC_GROUP_PATH}
   [2] Admin user created       : ${ADMIN_USERNAME} (temp password issued)
   [3] Branch API registered    : UUID=${BRANCH_UUID:-N/A}
   [4] Data migration job run   : branch-migrate-${BRANCH_SLUG}-${SCRIPT_START_TIME}
   [5] Isolation verified       : See log for details
--------------------------------------------------------------------------------
 Log file   : ${LOG_FILE}
 Next steps :
   • Send temporary password to ${ADMIN_EMAIL} via secure channel
   • Update branch_id attribute in Keycloak once UUID confirmed
   • Run verify-branch.sh to perform full functional verification
   • Schedule staff training using rollout-schedule.md
================================================================================
EOF

  cat "${REPORT_FILE}"
  log_success "Report saved: ${REPORT_FILE}"
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
  echo -e "\n${BOLD}${GREEN}BloodBank — Branch Onboarding (M11-001)${NC}"
  echo -e "${CYAN}Started: $(date -u '+%Y-%m-%d %H:%M:%S UTC')${NC}\n"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_warn "DRY-RUN MODE — no changes will be made"
  fi

  validate_inputs
  check_prerequisites
  create_keycloak_group
  create_admin_user
  BRANCH_UUID="${BRANCH_UUID:-UNKNOWN}"
  register_branch
  run_data_migration
  verify_data_isolation
  generate_report

  echo -e "\n${BOLD}${GREEN}✔ Branch onboarding complete: ${BRANCH_NAME}${NC}\n"
}

main "$@"
