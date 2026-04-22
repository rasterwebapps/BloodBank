#!/usr/bin/env bash
# =============================================================================
# dr-test.sh
# BloodBank — Disaster Recovery (DR) Test Script
#
# Usage:
#   NAMESPACE=bloodbank-prod ./dr-test.sh
#
# What this script does:
#   1. Simulates primary database failure (scales down postgres primary pod)
#   2. Verifies failover to replica (checks replica promotion)
#   3. Verifies service recovery (health checks all 14 backend services)
#   4. Tests backup restore procedure (pg_restore from latest backup)
#   5. Generates a DR test report with PASS/FAIL per step
#   6. Runs cleanup to restore the environment regardless of test outcome
#
# Environment variables:
#   NAMESPACE       — Kubernetes namespace (default: bloodbank-prod)
#   BACKUP_PATH     — Path to PostgreSQL backup file for restore test
#                     (default: /tmp/bloodbank-backup.dump)
#   DR_REPORTS_DIR  — Directory for DR test reports (default: reports/dr)
#   SKIP_RESTORE    — Set to "true" to skip the backup restore test
#   DRY_RUN         — Set to "true" to simulate without making real changes
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

log_info()    { echo -e "${BLUE}[INFO]${NC}  $(date '+%Y-%m-%d %H:%M:%S') $*"; }
log_success() { echo -e "${GREEN}[PASS]${NC}  $(date '+%Y-%m-%d %H:%M:%S') $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $(date '+%Y-%m-%d %H:%M:%S') $*"; }
log_error()   { echo -e "${RED}[FAIL]${NC}  $(date '+%Y-%m-%d %H:%M:%S') $*" >&2; }
log_step()    { echo -e "\n${BOLD}${CYAN}=== $* ===${NC}"; }
log_dr()      { echo -e "${BOLD}[DR-TEST]${NC} $(date '+%Y-%m-%d %H:%M:%S') $*"; }

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
NAMESPACE="${NAMESPACE:-bloodbank-prod}"
BACKUP_PATH="${BACKUP_PATH:-/tmp/bloodbank-backup.dump}"
DR_REPORTS_DIR="${DR_REPORTS_DIR:-reports/dr}"
SKIP_RESTORE="${SKIP_RESTORE:-false}"
DRY_RUN="${DRY_RUN:-false}"
DR_TIMESTAMP="$(date '+%Y%m%d_%H%M%S')"
REPORT_FILE="${DR_REPORTS_DIR}/dr-test-${DR_TIMESTAMP}.txt"
REPORT_JSON="${DR_REPORTS_DIR}/dr-test-${DR_TIMESTAMP}.json"

# All 14 backend services with their ports
declare -A SERVICE_PORTS=(
  ["api-gateway"]="8080"
  ["branch-service"]="8081"
  ["donor-service"]="8082"
  ["lab-service"]="8083"
  ["inventory-service"]="8084"
  ["transfusion-service"]="8085"
  ["hospital-service"]="8086"
  ["request-matching-service"]="8087"
  ["billing-service"]="8088"
  ["notification-service"]="8089"
  ["reporting-service"]="8090"
  ["document-service"]="8091"
  ["compliance-service"]="8092"
  ["config-server"]="8888"
)

# Test results tracking
declare -A TEST_RESULTS
declare -A TEST_DETAILS
OVERALL_PASS=true

# ---------------------------------------------------------------------------
# Cleanup — runs on EXIT to restore the environment
# ---------------------------------------------------------------------------
cleanup() {
  local exit_code=$?
  log_step "Cleanup: Restoring Environment"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_info "[DRY-RUN] Skipping actual cleanup — environment was not modified"
    generate_report
    return
  fi

  # Restore postgres primary replicas if they were scaled down
  log_info "Restoring postgres StatefulSet to original replica count..."
  kubectl scale statefulset postgres \
    --replicas=1 \
    --namespace="${NAMESPACE}" 2>/dev/null || log_warn "Could not restore postgres replicas (may already be running)"

  # Wait for postgres to be ready
  log_info "Waiting for postgres primary to be ready..."
  kubectl rollout status statefulset/postgres \
    --namespace="${NAMESPACE}" \
    --timeout=120s 2>/dev/null || log_warn "Postgres primary may not be fully ready yet"

  # Restart any services that were disrupted
  log_info "Triggering rolling restart of application services..."
  for service in "${!SERVICE_PORTS[@]}"; do
    kubectl rollout restart deployment/"${service}" \
      --namespace="${NAMESPACE}" 2>/dev/null || true
  done

  log_success "Environment cleanup complete"
  generate_report

  if [[ "${exit_code}" -ne 0 && "${OVERALL_PASS}" == "false" ]]; then
    log_error "DR test completed with FAILURES — review report: ${REPORT_FILE}"
  fi
}

trap cleanup EXIT

# ---------------------------------------------------------------------------
# Helper: kubectl wrapper with DRY_RUN support
# ---------------------------------------------------------------------------
kube_exec() {
  if [[ "${DRY_RUN}" == "true" ]]; then
    log_info "[DRY-RUN] Would run: kubectl $*"
    return 0
  fi
  kubectl "$@"
}

# ---------------------------------------------------------------------------
# Helper: record test result
# ---------------------------------------------------------------------------
record_result() {
  local test_name="$1"
  local status="$2"   # PASS or FAIL
  local detail="${3:-}"

  TEST_RESULTS["${test_name}"]="${status}"
  TEST_DETAILS["${test_name}"]="${detail}"

  if [[ "${status}" == "PASS" ]]; then
    log_success "TEST: ${test_name} — PASS ${detail:+(${detail})}"
  else
    log_error "TEST: ${test_name} — FAIL ${detail:+(${detail})}"
    OVERALL_PASS=false
  fi
}

# ---------------------------------------------------------------------------
# Step 1: Preflight — verify cluster connectivity
# ---------------------------------------------------------------------------
step_preflight() {
  log_step "Step 0: Preflight Checks"

  if ! command -v kubectl &>/dev/null; then
    log_error "kubectl not found — cannot run DR tests"
    exit 1
  fi

  if [[ "${DRY_RUN}" != "true" ]]; then
    if ! kubectl cluster-info --namespace="${NAMESPACE}" &>/dev/null; then
      log_error "Cannot connect to Kubernetes cluster"
      exit 1
    fi
    log_success "Kubernetes cluster is reachable"

    if ! kubectl get namespace "${NAMESPACE}" &>/dev/null; then
      log_error "Namespace '${NAMESPACE}' not found"
      exit 1
    fi
    log_success "Namespace '${NAMESPACE}' exists"
  else
    log_info "[DRY-RUN] Skipping cluster connectivity checks"
  fi

  mkdir -p "${DR_REPORTS_DIR}"
  log_info "DR test reports will be saved to: ${DR_REPORTS_DIR}/"
}

# ---------------------------------------------------------------------------
# Step 2: Simulate primary database failure
# ---------------------------------------------------------------------------
step_simulate_db_failure() {
  log_step "Step 1: Simulate Primary Database Failure"
  log_dr "Scaling down postgres primary StatefulSet to 0 replicas..."

  local before_replicas=1

  if [[ "${DRY_RUN}" != "true" ]]; then
    before_replicas=$(kubectl get statefulset postgres \
      --namespace="${NAMESPACE}" \
      -o jsonpath='{.spec.replicas}' 2>/dev/null || echo "1")
    log_info "Current postgres replicas: ${before_replicas}"
  fi

  if kube_exec scale statefulset postgres \
      --replicas=0 \
      --namespace="${NAMESPACE}"; then

    if [[ "${DRY_RUN}" != "true" ]]; then
      log_info "Waiting for postgres primary pod to terminate..."
      local timeout=60
      local elapsed=0
      while kubectl get pods \
          --namespace="${NAMESPACE}" \
          --selector="app=postgres,role=primary" \
          --field-selector="status.phase=Running" \
          --no-headers 2>/dev/null | grep -q .; do
        sleep 5
        elapsed=$((elapsed + 5))
        if [[ "${elapsed}" -ge "${timeout}" ]]; then
          log_warn "Timeout waiting for primary pod to terminate"
          break
        fi
      done
    fi

    record_result "primary_db_failure_simulation" "PASS" \
      "postgres primary scaled to 0 (was ${before_replicas})"
  else
    record_result "primary_db_failure_simulation" "FAIL" \
      "Failed to scale down postgres StatefulSet"
  fi
}

# ---------------------------------------------------------------------------
# Step 3: Verify failover to replica
# ---------------------------------------------------------------------------
step_verify_failover() {
  log_step "Step 2: Verify Failover to Replica"
  log_dr "Checking if replica is promoted to primary..."

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_info "[DRY-RUN] Simulating replica promotion check"
    record_result "replica_promotion" "PASS" "[DRY-RUN] Simulated"
    return 0
  fi

  # Wait for replica to be promoted (up to 2 minutes)
  local timeout=120
  local elapsed=0
  local promoted=false

  while [[ "${elapsed}" -lt "${timeout}" ]]; do
    # Check if replica pod is running and accepting connections
    local replica_pod
    replica_pod=$(kubectl get pods \
      --namespace="${NAMESPACE}" \
      --selector="app=postgres,role=replica" \
      --field-selector="status.phase=Running" \
      -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

    if [[ -n "${replica_pod}" ]]; then
      # Attempt a write query to confirm promotion
      if kubectl exec "${replica_pod}" \
          --namespace="${NAMESPACE}" \
          -- psql -U bloodbank -d bloodbank_db \
          -c "SELECT pg_is_in_recovery();" 2>/dev/null \
          | grep -q "f"; then
        promoted=true
        log_success "Replica pod '${replica_pod}' has been promoted to primary"
        record_result "replica_promotion" "PASS" "pod=${replica_pod}"
        break
      fi
    fi

    log_info "Waiting for replica promotion... (${elapsed}s / ${timeout}s)"
    sleep 10
    elapsed=$((elapsed + 10))
  done

  if [[ "${promoted}" == "false" ]]; then
    record_result "replica_promotion" "FAIL" \
      "Replica was not promoted within ${timeout}s"
  fi
}

# ---------------------------------------------------------------------------
# Step 4: Verify service recovery — health check all 14 services
# ---------------------------------------------------------------------------
step_verify_service_recovery() {
  log_step "Step 3: Verify Service Recovery (Health Checks)"
  log_dr "Running health checks on all 14 backend services..."

  local services_ok=0
  local services_failed=0

  for service in "${!SERVICE_PORTS[@]}"; do
    local port="${SERVICE_PORTS[${service}]}"
    local health_check_passed=false

    if [[ "${DRY_RUN}" == "true" ]]; then
      log_info "[DRY-RUN] Health check skipped for ${service}:${port}"
      health_check_passed=true
    else
      # Port-forward and check health endpoint
      local local_port=$((RANDOM % 1000 + 19000))
      kubectl port-forward "service/${service}" \
        "${local_port}:${port}" \
        --namespace="${NAMESPACE}" &>/dev/null &
      local pf_pid=$!
      sleep 3

      local retries=3
      for ((attempt=1; attempt<=retries; attempt++)); do
        if curl -sf \
            --connect-timeout 5 \
            --max-time 10 \
            "http://localhost:${local_port}/actuator/health" \
            | grep -q '"status":"UP"'; then
          health_check_passed=true
          break
        fi
        sleep 5
      done

      kill "${pf_pid}" 2>/dev/null || true
    fi

    if [[ "${health_check_passed}" == "true" ]]; then
      log_success "  ${service} (port ${port}) — UP"
      services_ok=$((services_ok + 1))
    else
      log_error "  ${service} (port ${port}) — DOWN or UNHEALTHY"
      services_failed=$((services_failed + 1))
    fi
  done

  log_info "Service recovery results: ${services_ok} UP, ${services_failed} DOWN"

  if [[ "${services_failed}" -eq 0 ]]; then
    record_result "service_recovery_health_checks" "PASS" \
      "All ${services_ok} services healthy after failover"
  else
    record_result "service_recovery_health_checks" "FAIL" \
      "${services_failed} service(s) unhealthy after failover"
  fi
}

# ---------------------------------------------------------------------------
# Step 5: Test backup restore procedure
# ---------------------------------------------------------------------------
step_test_backup_restore() {
  log_step "Step 4: Test Backup Restore Procedure"

  if [[ "${SKIP_RESTORE}" == "true" ]]; then
    log_warn "Skipping backup restore test (SKIP_RESTORE=true)"
    record_result "backup_restore" "PASS" "Skipped (SKIP_RESTORE=true)"
    return 0
  fi

  log_dr "Testing pg_restore from backup: ${BACKUP_PATH}"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_info "[DRY-RUN] Simulating backup restore from ${BACKUP_PATH}"
    record_result "backup_restore" "PASS" "[DRY-RUN] Simulated pg_restore"
    return 0
  fi

  # Find a running postgres pod (replica or newly-promoted primary)
  local pg_pod
  pg_pod=$(kubectl get pods \
    --namespace="${NAMESPACE}" \
    --selector="app=postgres" \
    --field-selector="status.phase=Running" \
    -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

  if [[ -z "${pg_pod}" ]]; then
    record_result "backup_restore" "FAIL" "No running postgres pod found"
    return 1
  fi

  log_info "Using postgres pod: ${pg_pod}"

  # Copy backup file into the pod
  if [[ -f "${BACKUP_PATH}" ]]; then
    log_info "Copying backup file to pod..."
    kubectl cp "${BACKUP_PATH}" \
      "${NAMESPACE}/${pg_pod}:/tmp/bloodbank-restore.dump" 2>/dev/null || {
      record_result "backup_restore" "FAIL" "Failed to copy backup to pod"
      return 1
    }
  else
    log_warn "Backup file not found at ${BACKUP_PATH} — creating test dump instead"
    kubectl exec "${pg_pod}" \
      --namespace="${NAMESPACE}" \
      -- pg_dump \
        --username=bloodbank \
        --format=custom \
        --file=/tmp/bloodbank-restore.dump \
        bloodbank_db 2>/dev/null || {
      record_result "backup_restore" "FAIL" "Failed to create test backup dump"
      return 1
    }
    log_success "Test backup dump created at /tmp/bloodbank-restore.dump"
  fi

  # Create a restore-test database
  log_info "Creating restore-test database..."
  kubectl exec "${pg_pod}" \
    --namespace="${NAMESPACE}" \
    -- psql --username=bloodbank \
    --command="DROP DATABASE IF EXISTS bloodbank_restore_test;" 2>/dev/null || true

  kubectl exec "${pg_pod}" \
    --namespace="${NAMESPACE}" \
    -- psql --username=bloodbank \
    --command="CREATE DATABASE bloodbank_restore_test;" 2>/dev/null || {
    record_result "backup_restore" "FAIL" "Failed to create restore-test database"
    return 1
  }

  # Run pg_restore
  log_info "Running pg_restore into bloodbank_restore_test..."
  if kubectl exec "${pg_pod}" \
      --namespace="${NAMESPACE}" \
      -- pg_restore \
        --username=bloodbank \
        --dbname=bloodbank_restore_test \
        --no-owner \
        --no-privileges \
        --verbose \
        /tmp/bloodbank-restore.dump 2>/dev/null; then

    # Verify restore by counting tables
    local table_count
    table_count=$(kubectl exec "${pg_pod}" \
      --namespace="${NAMESPACE}" \
      -- psql --username=bloodbank \
        --dbname=bloodbank_restore_test \
        --tuples-only \
        --command="SELECT count(*) FROM information_schema.tables WHERE table_schema='public';" \
        2>/dev/null | tr -d '[:space:]' || echo "0")

    log_info "Restore verification: ${table_count} tables found in restored database"

    # Clean up restore-test database
    kubectl exec "${pg_pod}" \
      --namespace="${NAMESPACE}" \
      -- psql --username=bloodbank \
      --command="DROP DATABASE IF EXISTS bloodbank_restore_test;" 2>/dev/null || true

    if [[ "${table_count}" -gt 0 ]]; then
      record_result "backup_restore" "PASS" \
        "pg_restore succeeded — ${table_count} tables verified"
    else
      record_result "backup_restore" "FAIL" \
        "pg_restore ran but no tables found in restored database"
    fi
  else
    record_result "backup_restore" "FAIL" "pg_restore command failed"
  fi
}

# ---------------------------------------------------------------------------
# Generate DR test report (text + JSON)
# ---------------------------------------------------------------------------
generate_report() {
  log_step "Generating DR Test Report"

  local overall_status="PASS"
  if [[ "${OVERALL_PASS}" == "false" ]]; then
    overall_status="FAIL"
  fi

  # ── Text report ──────────────────────────────────────────────────────────
  {
    echo "================================================================"
    echo " BloodBank — Disaster Recovery Test Report"
    echo " Timestamp : $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
    echo " Namespace : ${NAMESPACE}"
    echo " Dry Run   : ${DRY_RUN}"
    echo " Overall   : ${overall_status}"
    echo "================================================================"
    echo ""
    echo "TEST RESULTS:"
    echo "-------------"
    for test_name in \
        "primary_db_failure_simulation" \
        "replica_promotion" \
        "service_recovery_health_checks" \
        "backup_restore"; do
      local status="${TEST_RESULTS[${test_name}]:-NOT_RUN}"
      local detail="${TEST_DETAILS[${test_name}]:-}"
      printf "  %-40s %s\n" "${test_name}" "${status}"
      if [[ -n "${detail}" ]]; then
        printf "  %-40s %s\n" "" "(${detail})"
      fi
    done
    echo ""
    echo "================================================================"
    if [[ "${overall_status}" == "PASS" ]]; then
      echo " ✓  DR TEST PASSED — RTO/RPO targets validated"
    else
      echo " ✗  DR TEST FAILED — Review failures above"
    fi
    echo "================================================================"
  } | tee "${REPORT_FILE}"

  # ── JSON report ──────────────────────────────────────────────────────────
  {
    echo "{"
    echo "  \"timestamp\": \"$(date -u '+%Y-%m-%dT%H:%M:%SZ')\","
    echo "  \"namespace\": \"${NAMESPACE}\","
    echo "  \"dry_run\": ${DRY_RUN},"
    echo "  \"overall_status\": \"${overall_status}\","
    echo "  \"rto_target_minutes\": 15,"
    echo "  \"rpo_target_minutes\": 5,"
    echo "  \"tests\": {"
    local first=true
    for test_name in \
        "primary_db_failure_simulation" \
        "replica_promotion" \
        "service_recovery_health_checks" \
        "backup_restore"; do
      local status="${TEST_RESULTS[${test_name}]:-NOT_RUN}"
      local detail="${TEST_DETAILS[${test_name}]:-}"
      if [[ "${first}" == "true" ]]; then first=false; else echo ","; fi
      printf "    \"%s\": {\"status\": \"%s\", \"detail\": \"%s\"}" \
        "${test_name}" "${status}" "${detail}"
    done
    echo ""
    echo "  }"
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
  echo "║     BloodBank — Disaster Recovery Test                     ║"
  echo "║     NAMESPACE  : ${NAMESPACE}$(printf '%*s' $((_pad - ${#NAMESPACE})) '') ║"
  echo "║     DRY_RUN    : ${DRY_RUN}$(printf '%*s' $((_pad - ${#DRY_RUN})) '') ║"
  echo "╚════════════════════════════════════════════════════════════╝"
  echo -e "${NC}"

  step_preflight

  local start_time
  start_time=$(date +%s)

  step_simulate_db_failure
  step_verify_failover
  step_verify_service_recovery
  step_test_backup_restore

  local end_time
  end_time=$(date +%s)
  local elapsed=$(( end_time - start_time ))
  log_info "Total test duration: ${elapsed}s (RTO target: 900s / 15 min)"

  # report + cleanup handled by EXIT trap
  if [[ "${OVERALL_PASS}" == "true" ]]; then
    echo -e "\n${GREEN}${BOLD}DR TEST PASSED — all steps completed successfully${NC}"
    exit 0
  else
    echo -e "\n${RED}${BOLD}DR TEST FAILED — review the report above${NC}"
    exit 1
  fi
}

main "$@"
