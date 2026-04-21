#!/usr/bin/env bash
# =============================================================================
# M11-002: batch-rollout.sh
# BloodBank — Batch Branch Rollout Orchestrator
#
# Usage:
#   ./batch-rollout.sh --config <batch-config.json> [--dry-run] [--namespace <ns>]
#
# Batch config format (JSON):
#   {
#     "batch": {
#       "id":          "batch-1",
#       "week":        1,
#       "description": "North Region — Week 1 Rollout",
#       "contact":     "ops-team@bloodbank.org"
#     },
#     "branches": [
#       {
#         "name":         "Central City",
#         "region":       "north-america",
#         "admin_email":  "admin@centralcity.bloodbank.org",
#         "priority":     1
#       },
#       {
#         "name":         "East District",
#         "region":       "north-america",
#         "admin_email":  "admin@eastdistrict.bloodbank.org",
#         "priority":     2
#       }
#     ]
#   }
#
# What this script does:
#   1. Reads a JSON batch config file listing branches to roll out
#   2. For each branch (in priority order):
#      a. Calls branch-onboard.sh to provision Keycloak + data migration
#      b. Calls verify-branch.sh to run functional + isolation checks
#      c. Calls scaling-check.sh to validate HPA/DB/Redis/RabbitMQ health
#      d. Records pass/fail status
#   3. Generates a per-batch HTML + plain-text report
#   4. Exits non-zero if any branch failed; reports can be reviewed individually
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
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NAMESPACE="${BLOODBANK_NAMESPACE:-bloodbank-prod}"
DRY_RUN=false
LOG_DIR="${LOG_DIR:-/tmp/bloodbank-logs}"
SCRIPT_START_TIME=$(date '+%Y%m%d-%H%M%S')
BATCH_CONFIG=""
PAUSE_BETWEEN_BRANCHES="${PAUSE_BETWEEN_BRANCHES:-30}"  # seconds
STOP_ON_FAILURE="${STOP_ON_FAILURE:-false}"

# Track results
declare -a BRANCH_NAMES=()
declare -A ONBOARD_STATUS=()
declare -A VERIFY_STATUS=()
declare -A SCALING_STATUS=()
declare -A BRANCH_DURATION=()

usage() {
  cat <<EOF
Usage: $0 --config <batch-config.json> [OPTIONS]

Required:
  --config       Path to the batch configuration JSON file

Options:
  --namespace    Kubernetes namespace (default: bloodbank-prod)
  --dry-run      Print actions without executing them
  --stop-on-fail Stop processing remaining branches if one fails (default: false)
  --pause        Seconds to pause between branches (default: 30)
  --help         Show this message

Environment variables:
  KEYCLOAK_ADMIN_PASS       Keycloak admin password (required)
  BLOODBANK_SERVICE_SECRET  Service-to-service client secret (optional)
  BLOODBANK_NAMESPACE       Kubernetes namespace override
  LOG_DIR                   Log output directory (default: /tmp/bloodbank-logs)
EOF
}

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --config)        BATCH_CONFIG="$2";       shift 2 ;;
    --namespace)     NAMESPACE="$2";          shift 2 ;;
    --dry-run)       DRY_RUN=true;            shift   ;;
    --stop-on-fail)  STOP_ON_FAILURE=true;    shift   ;;
    --pause)         PAUSE_BETWEEN_BRANCHES="$2"; shift 2 ;;
    --help|-h)       usage; exit 0            ;;
    *)               log_error "Unknown argument: $1"; usage; exit 1 ;;
  esac
done

# ---------------------------------------------------------------------------
# Derived paths
# ---------------------------------------------------------------------------
BATCH_ID=""
BATCH_LOG_FILE=""

# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------
validate_inputs() {
  log_step "Validating batch configuration"

  if [[ -z "${BATCH_CONFIG}" ]]; then
    log_error "--config is required"
    usage
    exit 1
  fi

  if [[ ! -f "${BATCH_CONFIG}" ]]; then
    log_error "Batch config file not found: ${BATCH_CONFIG}"
    exit 1
  fi

  # Validate JSON
  if ! jq empty "${BATCH_CONFIG}" 2>/dev/null; then
    log_error "Batch config is not valid JSON: ${BATCH_CONFIG}"
    exit 1
  fi

  # Validate required fields
  local branch_count
  branch_count=$(jq '.branches | length' "${BATCH_CONFIG}")
  if [[ "${branch_count}" -eq 0 ]]; then
    log_error "No branches found in batch config"
    exit 1
  fi

  BATCH_ID=$(jq -r '.batch.id // "batch-unknown"' "${BATCH_CONFIG}")
  local batch_week
  batch_week=$(jq -r '.batch.week // "?"' "${BATCH_CONFIG}")
  local batch_desc
  batch_desc=$(jq -r '.batch.description // "No description"' "${BATCH_CONFIG}")

  mkdir -p "${LOG_DIR}"
  BATCH_LOG_FILE="${LOG_DIR}/batch-${BATCH_ID}-${SCRIPT_START_TIME}.log"

  # Redirect output to log file + terminal
  exec > >(tee -a "${BATCH_LOG_FILE}") 2>&1

  log_success "Batch config validated"
  log_info "  Batch ID    : ${BATCH_ID}"
  log_info "  Week        : ${batch_week}"
  log_info "  Description : ${batch_desc}"
  log_info "  Branches    : ${branch_count}"
  log_info "  Dry-run     : ${DRY_RUN}"
  log_info "  Stop on fail: ${STOP_ON_FAILURE}"
}

# ---------------------------------------------------------------------------
# Prerequisite checks
# ---------------------------------------------------------------------------
check_prerequisites() {
  log_step "Checking prerequisites"

  for cmd in kubectl curl jq; do
    if ! command -v "${cmd}" &>/dev/null; then
      log_error "Required command not found: ${cmd}"
      exit 1
    fi
    log_success "  ${cmd}: $(command -v "${cmd}")"
  done

  # Verify companion scripts are present and executable
  for script in branch-onboard.sh verify-branch.sh scaling-check.sh; do
    local script_path="${SCRIPT_DIR}/${script}"
    if [[ ! -f "${script_path}" ]]; then
      log_error "Required companion script not found: ${script_path}"
      exit 1
    fi
    if [[ ! -x "${script_path}" ]]; then
      log_warn "Script not executable, attempting chmod: ${script_path}"
      chmod +x "${script_path}"
    fi
    log_success "  ${script}: found"
  done

  if [[ -z "${KEYCLOAK_ADMIN_PASS:-}" ]]; then
    log_error "KEYCLOAK_ADMIN_PASS environment variable must be set"
    exit 1
  fi

  log_success "All prerequisites satisfied"
}

# ---------------------------------------------------------------------------
# Process a single branch
# ---------------------------------------------------------------------------
process_branch() {
  local idx="$1"
  local branch_json="$2"

  local branch_name region admin_email priority
  branch_name=$(echo "${branch_json}"  | jq -r '.name')
  region=$(echo "${branch_json}"       | jq -r '.region')
  admin_email=$(echo "${branch_json}"  | jq -r '.admin_email')
  priority=$(echo "${branch_json}"     | jq -r '.priority // 999')

  local branch_slug
  branch_slug=$(echo "${branch_name}" | tr '[:upper:]' '[:lower:]' | tr ' ' '-' | tr -cd '[:alnum:]-')

  BRANCH_NAMES+=("${branch_name}")
  ONBOARD_STATUS["${branch_name}"]="PENDING"
  VERIFY_STATUS["${branch_name}"]="PENDING"
  SCALING_STATUS["${branch_name}"]="PENDING"

  local branch_start_time
  branch_start_time=$(date +%s)

  echo ""
  echo -e "${BOLD}${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${BOLD}  Branch ${idx}: ${branch_name}${NC}"
  echo -e "${BOLD}  Region: ${region}  |  Admin: ${admin_email}  |  Priority: ${priority}${NC}"
  echo -e "${BOLD}${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

  # Build optional flags array to avoid word-splitting issues with unquoted variables
  local -a extra_flags=()
  [[ "${DRY_RUN}" == "true" ]] && extra_flags+=("--dry-run")

  # -----------------------------------------------------------------------
  # Phase A: Onboarding
  # -----------------------------------------------------------------------
  log_step "Phase A — Onboarding: ${branch_name}"
  local onboard_log="${LOG_DIR}/onboard-${branch_slug}-${SCRIPT_START_TIME}.log"

  if "${SCRIPT_DIR}/branch-onboard.sh" \
      --branch "${branch_name}" \
      --region "${region}" \
      --admin-email "${admin_email}" \
      --namespace "${NAMESPACE}" \
      "${extra_flags[@]+"${extra_flags[@]}"}" 2>&1 | tee "${onboard_log}"; then
    ONBOARD_STATUS["${branch_name}"]="PASS"
    log_success "Onboarding PASSED: ${branch_name}"
  else
    ONBOARD_STATUS["${branch_name}"]="FAIL"
    log_error "Onboarding FAILED: ${branch_name}  (see: ${onboard_log})"
    if [[ "${STOP_ON_FAILURE}" == "true" ]]; then
      log_error "STOP_ON_FAILURE=true — aborting batch"
      record_batch_summary
      exit 1
    fi
    local branch_end_time
    branch_end_time=$(date +%s)
    BRANCH_DURATION["${branch_name}"]=$((branch_end_time - branch_start_time))
    return
  fi

  # -----------------------------------------------------------------------
  # Phase B: Verification
  # -----------------------------------------------------------------------
  log_step "Phase B — Verification: ${branch_name}"
  local verify_log="${LOG_DIR}/verify-${branch_slug}-${SCRIPT_START_TIME}.log"

  if "${SCRIPT_DIR}/verify-branch.sh" \
      --branch "${branch_name}" \
      --region "${region}" \
      --namespace "${NAMESPACE}" \
      "${extra_flags[@]+"${extra_flags[@]}"}" 2>&1 | tee "${verify_log}"; then
    VERIFY_STATUS["${branch_name}"]="PASS"
    log_success "Verification PASSED: ${branch_name}"
  else
    VERIFY_STATUS["${branch_name}"]="FAIL"
    log_error "Verification FAILED: ${branch_name}  (see: ${verify_log})"
    if [[ "${STOP_ON_FAILURE}" == "true" ]]; then
      log_error "STOP_ON_FAILURE=true — aborting batch"
      record_batch_summary
      exit 1
    fi
  fi

  # -----------------------------------------------------------------------
  # Phase C: Scaling check
  # -----------------------------------------------------------------------
  log_step "Phase C — Scaling check after: ${branch_name}"
  local scaling_log="${LOG_DIR}/scaling-${SCRIPT_START_TIME}.log"

  if "${SCRIPT_DIR}/scaling-check.sh" \
      --namespace "${NAMESPACE}" \
      "${extra_flags[@]+"${extra_flags[@]}"}" 2>&1 | tee "${scaling_log}"; then
    SCALING_STATUS["${branch_name}"]="PASS"
    log_success "Scaling check PASSED"
  else
    SCALING_STATUS["${branch_name}"]="WARN"
    log_warn "Scaling check completed with warnings — review: ${scaling_log}"
  fi

  local branch_end_time
  branch_end_time=$(date +%s)
  BRANCH_DURATION["${branch_name}"]=$((branch_end_time - branch_start_time))

  log_success "Branch processing complete: ${branch_name} (${BRANCH_DURATION["${branch_name}"]}s)"
}

# ---------------------------------------------------------------------------
# Run all branches in priority order
# ---------------------------------------------------------------------------
run_batch() {
  log_step "Starting batch rollout: ${BATCH_ID}"

  local total_branches
  total_branches=$(jq '.branches | length' "${BATCH_CONFIG}")
  log_info "Total branches to process: ${total_branches}"

  # Sort branches by priority (ascending), process in order
  local idx=0
  while IFS= read -r branch_json; do
    idx=$((idx + 1))
    process_branch "${idx}" "${branch_json}"

    # Pause between branches (not after the last one)
    if [[ ${idx} -lt ${total_branches} ]]; then
      if [[ "${DRY_RUN}" == "false" ]]; then
        log_info "Pausing ${PAUSE_BETWEEN_BRANCHES}s before next branch …"
        sleep "${PAUSE_BETWEEN_BRANCHES}"
      else
        log_dry "Would pause ${PAUSE_BETWEEN_BRANCHES}s before next branch"
      fi
    fi
  done < <(jq -c '.branches | sort_by(.priority // 999) | .[]' "${BATCH_CONFIG}")
}

# ---------------------------------------------------------------------------
# Batch summary report
# ---------------------------------------------------------------------------
record_batch_summary() {
  log_step "Generating batch report"

  local report_file="${LOG_DIR}/batch-report-${BATCH_ID}-${SCRIPT_START_TIME}.txt"
  local batch_desc
  batch_desc=$(jq -r '.batch.description // "Batch Rollout"' "${BATCH_CONFIG}")
  local batch_contact
  batch_contact=$(jq -r '.batch.contact // "N/A"' "${BATCH_CONFIG}")

  local total_pass=0 total_fail=0 total_warn=0
  local total_duration=0

  {
    echo "================================================================================"
    echo " BloodBank — Batch Rollout Report"
    echo "================================================================================"
    echo " Batch ID   : ${BATCH_ID}"
    echo " Description: ${batch_desc}"
    echo " Contact    : ${batch_contact}"
    echo " Timestamp  : $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
    echo " Dry-run    : ${DRY_RUN}"
    echo "--------------------------------------------------------------------------------"
    printf " %-30s %-12s %-12s %-12s %-10s\n" "BRANCH" "ONBOARD" "VERIFY" "SCALING" "DURATION"
    echo "--------------------------------------------------------------------------------"

    for branch_name in "${BRANCH_NAMES[@]}"; do
      local onboard="${ONBOARD_STATUS["${branch_name}"]:-SKIP}"
      local verify="${VERIFY_STATUS["${branch_name}"]:-SKIP}"
      local scaling="${SCALING_STATUS["${branch_name}"]:-SKIP}"
      local duration="${BRANCH_DURATION["${branch_name}"]:-0}"

      printf " %-30s %-12s %-12s %-12s %-10s\n" \
        "${branch_name}" "${onboard}" "${verify}" "${scaling}" "${duration}s"

      [[ "${onboard}" == "FAIL" || "${verify}" == "FAIL" ]] && total_fail=$((total_fail + 1)) || true
      [[ "${onboard}" == "PASS" && "${verify}" == "PASS" ]] && total_pass=$((total_pass + 1)) || true
      [[ "${scaling}" == "WARN" ]] && total_warn=$((total_warn + 1)) || true
      total_duration=$((total_duration + duration))
    done

    echo "--------------------------------------------------------------------------------"
    echo " Totals:"
    echo "   Successful branches : ${total_pass}"
    echo "   Failed branches     : ${total_fail}"
    echo "   Scaling warnings    : ${total_warn}"
    echo "   Total duration      : ${total_duration}s"
    echo "--------------------------------------------------------------------------------"
    echo " Log directory: ${LOG_DIR}"
    echo " Batch log    : ${BATCH_LOG_FILE}"
    echo "================================================================================"

    if [[ ${total_fail} -gt 0 ]]; then
      echo ""
      echo " ⚠  ACTION REQUIRED: ${total_fail} branch(es) failed onboarding or verification."
      echo "    Review individual logs and re-run failed branches before proceeding."
    fi
  } | tee "${report_file}"

  log_success "Batch report saved: ${report_file}"

  # Exit non-zero if any failures
  if [[ ${total_fail} -gt 0 ]]; then
    exit 1
  fi
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
  echo -e "\n${BOLD}${GREEN}BloodBank — Batch Rollout Orchestrator (M11-002)${NC}"
  echo -e "${CYAN}Started: $(date -u '+%Y-%m-%d %H:%M:%S UTC')${NC}\n"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_warn "DRY-RUN MODE — no changes will be made"
  fi

  validate_inputs
  check_prerequisites
  run_batch
  record_batch_summary

  echo -e "\n${BOLD}${GREEN}✔ Batch rollout complete: ${BATCH_ID}${NC}\n"
}

main "$@"
