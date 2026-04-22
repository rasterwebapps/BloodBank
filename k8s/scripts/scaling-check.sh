#!/usr/bin/env bash
# =============================================================================
# M11-005: scaling-check.sh
# BloodBank — Scaling and Infrastructure Health Check
#
# Usage:
#   ./scaling-check.sh [--namespace <ns>] [--dry-run] [--output <format>]
#
# What this script checks:
#   1. HPA (HorizontalPodAutoscaler) status for all 14 services
#      — Current vs desired replicas, CPU/memory utilisation
#   2. Database (PostgreSQL) connection pool health
#      — Active/idle/waiting connections per pool
#   3. Redis cache hit rates
#      — Global hit rate, per-key-space stats, eviction count
#   4. RabbitMQ queue depths
#      — Messages ready/unacked, consumer counts, dead-letter queues
#   5. Generates a consolidated scaling health report
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
DRY_RUN=false
LOG_DIR="${LOG_DIR:-/tmp/bloodbank-logs}"
SCRIPT_START_TIME=$(date '+%Y%m%d-%H%M%S')
OUTPUT_FORMAT="${OUTPUT_FORMAT:-text}"  # text | json

# Thresholds
HPA_CPU_WARN_PCT=80          # Warn if HPA avg CPU utilisation > 80%
HPA_MEM_WARN_PCT=85          # Warn if HPA avg memory utilisation > 85%
HPA_AT_MAX_REPLICAS_WARN=1   # Warn if any HPA is at max replicas

DB_MAX_CONNECTIONS_WARN=150  # Warn if active DB connections > 150
DB_WAITING_CONNS_FAIL=5      # Fail if waiting connections > 5

REDIS_HIT_RATE_WARN_PCT=70   # Warn if Redis hit rate < 70%
REDIS_EVICTION_WARN=1000     # Warn if evicted keys > 1000

RABBITMQ_QUEUE_DEPTH_WARN=500    # Warn if any queue has >500 ready messages
RABBITMQ_DLQ_MESSAGES_WARN=10   # Warn if any DLQ has messages

# Result tracking
declare -A CHECK_RESULTS=()
TOTAL_PASS=0
TOTAL_FAIL=0
TOTAL_WARN=0

LOG_FILE="${LOG_DIR}/scaling-${SCRIPT_START_TIME}.log"
REPORT_FILE="${LOG_DIR}/scaling-report-${SCRIPT_START_TIME}.txt"

usage() {
  cat <<EOF
Usage: $0 [OPTIONS]

Options:
  --namespace   Kubernetes namespace (default: bloodbank-prod)
  --dry-run     Print actions without executing them
  --output      Output format: text|json (default: text)
  --help        Show this message

Thresholds (set via env vars):
  HPA_CPU_WARN_PCT=${HPA_CPU_WARN_PCT}      Warn if HPA CPU utilisation exceeds this
  HPA_MEM_WARN_PCT=${HPA_MEM_WARN_PCT}      Warn if HPA memory utilisation exceeds this
  DB_MAX_CONNECTIONS_WARN=${DB_MAX_CONNECTIONS_WARN}  Warn if active DB connections exceed this
  REDIS_HIT_RATE_WARN_PCT=${REDIS_HIT_RATE_WARN_PCT}  Warn if Redis hit rate below this
  RABBITMQ_QUEUE_DEPTH_WARN=${RABBITMQ_QUEUE_DEPTH_WARN}  Warn if queue depth exceeds this
EOF
}

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --namespace)  NAMESPACE="$2";      shift 2 ;;
    --dry-run)    DRY_RUN=true;        shift   ;;
    --output)     OUTPUT_FORMAT="$2";  shift 2 ;;
    --help|-h)    usage; exit 0        ;;
    *)            log_error "Unknown argument: $1"; usage; exit 1 ;;
  esac
done

mkdir -p "${LOG_DIR}"
exec > >(tee -a "${LOG_FILE}") 2>&1

# ---------------------------------------------------------------------------
# Check result helpers
# ---------------------------------------------------------------------------
record_pass() {
  local check="$1"; local detail="${2:-}"
  CHECK_RESULTS["${check}"]="PASS"
  TOTAL_PASS=$((TOTAL_PASS + 1))
  log_success "  PASS: ${check}${detail:+ — ${detail}}"
}

record_fail() {
  local check="$1"; local detail="${2:-}"
  CHECK_RESULTS["${check}"]="FAIL"
  TOTAL_FAIL=$((TOTAL_FAIL + 1))
  log_error "  FAIL: ${check}${detail:+ — ${detail}}"
}

record_warn() {
  local check="$1"; local detail="${2:-}"
  CHECK_RESULTS["${check}"]="WARN"
  TOTAL_WARN=$((TOTAL_WARN + 1))
  log_warn "  WARN: ${check}${detail:+ — ${detail}}"
}

# ---------------------------------------------------------------------------
# Section 1 — HPA Status
# ---------------------------------------------------------------------------
check_hpa() {
  log_step "Section 1 — HPA (HorizontalPodAutoscaler) Status"

  # All known HPA names follow the pattern "<service>-hpa"
  declare -a HPA_SERVICES=(
    api-gateway branch-service donor-service lab-service
    inventory-service transfusion-service hospital-service
    request-matching-service billing-service notification-service
    reporting-service document-service compliance-service config-server
  )

  if [[ "${DRY_RUN}" == "true" ]]; then
    for svc in "${HPA_SERVICES[@]}"; do
      log_dry "Would check HPA: ${svc}-hpa in ${NAMESPACE}"
      record_pass "hpa.${svc}" "[dry-run]"
    done
    return
  fi

  # Dump all HPAs once for efficiency
  local hpa_json
  hpa_json=$(kubectl get hpa -n "${NAMESPACE}" -o json 2>/dev/null || echo '{"items":[]}')

  for svc in "${HPA_SERVICES[@]}"; do
    local hpa_name="${svc}-hpa"
    local hpa_data
    hpa_data=$(echo "${hpa_json}" | \
      jq -r --arg name "${hpa_name}" '.items[] | select(.metadata.name==$name)' 2>/dev/null || echo "")

    if [[ -z "${hpa_data}" ]]; then
      record_warn "hpa.${svc}" "HPA '${hpa_name}' not found in namespace '${NAMESPACE}'"
      continue
    fi

    local min_replicas desired_replicas current_replicas max_replicas
    min_replicas=$(echo "${hpa_data}"     | jq -r '.spec.minReplicas // 1')
    max_replicas=$(echo "${hpa_data}"     | jq -r '.spec.maxReplicas // 10')
    desired_replicas=$(echo "${hpa_data}" | jq -r '.status.desiredReplicas // 0')
    current_replicas=$(echo "${hpa_data}" | jq -r '.status.currentReplicas // 0')

    local detail="replicas=${current_replicas}/${desired_replicas} (min=${min_replicas} max=${max_replicas})"

    # Check if at max replicas (scaling pressure)
    if [[ "${current_replicas}" -ge "${max_replicas}" ]]; then
      record_warn "hpa.${svc}.replicas" "${detail} — AT MAX REPLICAS (potential bottleneck)"
    elif [[ "${current_replicas}" -lt "${desired_replicas}" ]]; then
      record_warn "hpa.${svc}.replicas" "${detail} — scaling in progress"
    else
      record_pass "hpa.${svc}.replicas" "${detail}"
    fi

    # Check CPU utilisation from HPA metrics
    local cpu_current
    cpu_current=$(echo "${hpa_data}" | jq -r '
      .status.currentMetrics // [] |
      .[] | select(.type=="Resource" and .resource.name=="cpu") |
      .resource.current.averageUtilization // 0' 2>/dev/null | head -1)
    cpu_current="${cpu_current:-0}"

    local cpu_target
    cpu_target=$(echo "${hpa_data}" | jq -r '
      .spec.metrics // [] |
      .[] | select(.type=="Resource" and .resource.name=="cpu") |
      .resource.target.averageUtilization // 70' 2>/dev/null | head -1)
    cpu_target="${cpu_target:-70}"

    if [[ "${cpu_current}" -gt "${HPA_CPU_WARN_PCT}" ]]; then
      record_warn "hpa.${svc}.cpu" "util=${cpu_current}% (target=${cpu_target}%) — high CPU pressure"
    else
      record_pass "hpa.${svc}.cpu" "util=${cpu_current}% (target=${cpu_target}%)"
    fi

    # Check memory utilisation
    local mem_current
    mem_current=$(echo "${hpa_data}" | jq -r '
      .status.currentMetrics // [] |
      .[] | select(.type=="Resource" and .resource.name=="memory") |
      .resource.current.averageUtilization // 0' 2>/dev/null | head -1)
    mem_current="${mem_current:-0}"

    if [[ "${mem_current}" -gt "${HPA_MEM_WARN_PCT}" ]]; then
      record_warn "hpa.${svc}.memory" "util=${mem_current}% — high memory pressure"
    else
      record_pass "hpa.${svc}.memory" "util=${mem_current}%"
    fi
  done
}

# ---------------------------------------------------------------------------
# Section 2 — Database Connection Pool
# ---------------------------------------------------------------------------
check_database() {
  log_step "Section 2 — Database Connection Pool"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would check PostgreSQL connection pool via pg_stat_activity"
    record_pass "db.connection_pool" "[dry-run]"
    return
  fi

  # Find the postgres pod
  local pg_pod
  pg_pod=$(kubectl get pods -n "${NAMESPACE}" \
    -l "app=postgres" \
    -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

  if [[ -z "${pg_pod}" ]]; then
    # Try StatefulSet naming
    pg_pod=$(kubectl get pods -n "${NAMESPACE}" \
      -l "app.kubernetes.io/name=postgresql" \
      -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
  fi

  if [[ -z "${pg_pod}" ]]; then
    record_warn "db.pod" "PostgreSQL pod not found in namespace '${NAMESPACE}'"
    return
  fi
  log_info "PostgreSQL pod: ${pg_pod}"

  # Query connection stats
  local conn_stats
  conn_stats=$(kubectl exec "${pg_pod}" -n "${NAMESPACE}" \
    -- psql -U bloodbank -d bloodbank_db -t -A -c "
      SELECT
        state,
        COUNT(*) AS count
      FROM pg_stat_activity
      WHERE datname = 'bloodbank_db'
      GROUP BY state
      ORDER BY state;" 2>/dev/null || echo "")

  if [[ -z "${conn_stats}" ]]; then
    record_warn "db.connection_stats" "Could not query pg_stat_activity"
    return
  fi

  log_info "Connection state breakdown:"
  echo "${conn_stats}" | while IFS='|' read -r state count; do
    log_info "  state=${state:-NULL}  count=${count}"
  done

  local active_count idle_count waiting_count total_count
  active_count=$(echo "${conn_stats}"  | grep "^active|"   | cut -d'|' -f2 | tr -d ' ' || echo "0")
  idle_count=$(echo "${conn_stats}"    | grep "^idle|"     | cut -d'|' -f2 | tr -d ' ' || echo "0")
  waiting_count=$(echo "${conn_stats}" | grep -i "waiting" | cut -d'|' -f2 | tr -d ' ' || echo "0")
  active_count="${active_count:-0}"
  idle_count="${idle_count:-0}"
  waiting_count="${waiting_count:-0}"
  total_count=$((active_count + idle_count))

  # Check waiting connections (indicate pool exhaustion)
  if [[ "${waiting_count}" -gt "${DB_WAITING_CONNS_FAIL}" ]]; then
    record_fail "db.waiting_connections" \
      "waiting=${waiting_count} (threshold=${DB_WAITING_CONNS_FAIL}) — pool exhaustion risk"
  elif [[ "${waiting_count}" -gt 0 ]]; then
    record_warn "db.waiting_connections" "waiting=${waiting_count} — monitor closely"
  else
    record_pass "db.waiting_connections" "waiting=0"
  fi

  # Check total active connections
  if [[ "${active_count}" -gt "${DB_MAX_CONNECTIONS_WARN}" ]]; then
    record_warn "db.active_connections" \
      "active=${active_count} (threshold=${DB_MAX_CONNECTIONS_WARN})"
  else
    record_pass "db.active_connections" \
      "active=${active_count}  idle=${idle_count}  total=${total_count}"
  fi

  # Check PostgreSQL replication lag (if replica exists)
  local repl_lag
  repl_lag=$(kubectl exec "${pg_pod}" -n "${NAMESPACE}" \
    -- psql -U bloodbank -d bloodbank_db -t -A -c "
      SELECT EXTRACT(EPOCH FROM (NOW() - pg_last_xact_replay_timestamp()))::INT AS lag_seconds;" \
    2>/dev/null | tr -d ' ' || echo "")

  if [[ -n "${repl_lag}" && "${repl_lag}" != "" && "${repl_lag}" -gt 30 ]]; then
    record_warn "db.replication_lag" "lag=${repl_lag}s (>30s — replica may be behind)"
  elif [[ -n "${repl_lag}" ]]; then
    record_pass "db.replication_lag" "lag=${repl_lag}s"
  else
    log_info "  Replication lag not applicable (primary-only or no replica found)"
  fi

  # Check database size growth
  local db_size
  db_size=$(kubectl exec "${pg_pod}" -n "${NAMESPACE}" \
    -- psql -U bloodbank -d bloodbank_db -t -A -c "
      SELECT pg_size_pretty(pg_database_size('bloodbank_db'));" \
    2>/dev/null | tr -d ' ' || echo "unknown")
  log_info "  bloodbank_db size: ${db_size}"
  record_pass "db.size" "${db_size}"
}

# ---------------------------------------------------------------------------
# Section 3 — Redis Cache Hit Rates
# ---------------------------------------------------------------------------
check_redis() {
  log_step "Section 3 — Redis Cache Hit Rates"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would check Redis keyspace stats and hit rate"
    record_pass "redis.hit_rate" "[dry-run]"
    return
  fi

  # Find the Redis pod
  local redis_pod
  redis_pod=$(kubectl get pods -n "${NAMESPACE}" \
    -l "app=redis" \
    -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

  if [[ -z "${redis_pod}" ]]; then
    redis_pod=$(kubectl get pods -n "${NAMESPACE}" \
      -l "app.kubernetes.io/name=redis" \
      -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
  fi

  if [[ -z "${redis_pod}" ]]; then
    record_warn "redis.pod" "Redis pod not found in namespace '${NAMESPACE}'"
    return
  fi
  log_info "Redis pod: ${redis_pod}"

  # Get Redis INFO stats
  local redis_info
  redis_info=$(kubectl exec "${redis_pod}" -n "${NAMESPACE}" \
    -- redis-cli INFO all 2>/dev/null || echo "")

  if [[ -z "${redis_info}" ]]; then
    record_warn "redis.info" "Could not execute redis-cli INFO"
    return
  fi

  # Parse keyspace hits and misses
  local hits misses
  hits=$(echo "${redis_info}"   | grep "^keyspace_hits:"   | cut -d: -f2 | tr -d ' \r' || echo "0")
  misses=$(echo "${redis_info}" | grep "^keyspace_misses:" | cut -d: -f2 | tr -d ' \r' || echo "0")
  hits="${hits:-0}"
  misses="${misses:-0}"

  local total_ops=$((hits + misses))
  local hit_rate=0
  local hit_rate_display="0"
  if [[ ${total_ops} -gt 0 ]]; then
    # Use bc for decimal precision if available, otherwise fall back to integer
    # arithmetic. Note: integer division truncates (e.g. 75/100 → "75" not "75.0")
    # — acceptable for threshold comparison; the unit is always whole-percent.
    if command -v bc &>/dev/null; then
      hit_rate_display=$(echo "scale=1; ${hits} * 100 / ${total_ops}" | bc 2>/dev/null || echo "0")
    else
      hit_rate_display=$(( hits * 100 / total_ops ))
    fi
    hit_rate="${hit_rate_display}"
  fi

  log_info "  Keyspace hits:   ${hits}"
  log_info "  Keyspace misses: ${misses}"
  log_info "  Hit rate:        ${hit_rate}%"

  local hit_rate_int
  hit_rate_int=$(echo "${hit_rate}" | cut -d. -f1)
  if [[ ${total_ops} -eq 0 ]]; then
    record_pass "redis.hit_rate" "No operations yet (new deployment)"
  elif [[ "${hit_rate_int}" -lt "${REDIS_HIT_RATE_WARN_PCT}" ]]; then
    record_warn "redis.hit_rate" \
      "hit_rate=${hit_rate}% (threshold=${REDIS_HIT_RATE_WARN_PCT}%) — cache warming needed?"
  else
    record_pass "redis.hit_rate" "hit_rate=${hit_rate}%"
  fi

  # Parse evicted keys
  local evicted
  evicted=$(echo "${redis_info}" | grep "^evicted_keys:" | cut -d: -f2 | tr -d ' \r' || echo "0")
  evicted="${evicted:-0}"
  if [[ "${evicted}" -gt "${REDIS_EVICTION_WARN}" ]]; then
    record_warn "redis.evictions" "evicted_keys=${evicted} — maxmemory policy evicting data"
  else
    record_pass "redis.evictions" "evicted_keys=${evicted}"
  fi

  # Parse connected clients
  local clients
  clients=$(echo "${redis_info}" | grep "^connected_clients:" | cut -d: -f2 | tr -d ' \r' || echo "0")
  log_info "  Connected clients: ${clients}"
  record_pass "redis.connected_clients" "${clients}"

  # Parse used memory
  local used_memory_human
  used_memory_human=$(echo "${redis_info}" | grep "^used_memory_human:" | cut -d: -f2 | tr -d ' \r' || echo "unknown")
  local maxmemory_human
  maxmemory_human=$(echo "${redis_info}" | grep "^maxmemory_human:"  | cut -d: -f2 | tr -d ' \r' || echo "unknown")
  log_info "  Memory used: ${used_memory_human} / ${maxmemory_human}"
  record_pass "redis.memory" "used=${used_memory_human} max=${maxmemory_human}"

  # Keyspace info (db0 stats)
  local keyspace_info
  keyspace_info=$(echo "${redis_info}" | grep "^db0:" | tr -d ' \r' || echo "")
  if [[ -n "${keyspace_info}" ]]; then
    log_info "  ${keyspace_info}"
    local key_count
    key_count=$(echo "${keyspace_info}" | grep -oP 'keys=\K[0-9]+' || echo "0")
    local expires_count
    expires_count=$(echo "${keyspace_info}" | grep -oP 'expires=\K[0-9]+' || echo "0")
    record_pass "redis.keyspace" "keys=${key_count} expires=${expires_count}"
  fi
}

# ---------------------------------------------------------------------------
# Section 4 — RabbitMQ Queue Depths
# ---------------------------------------------------------------------------
check_rabbitmq() {
  log_step "Section 4 — RabbitMQ Queue Depths"

  local rmq_url="${RABBITMQ_MGMT_URL:-http://rabbitmq:15672}"
  local rmq_user="${RABBITMQ_USER:-bloodbank}"
  local rmq_pass="${RABBITMQ_PASS:-bloodbank_secret}"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_dry "Would check RabbitMQ at ${rmq_url}/api/queues"
    record_pass "rabbitmq.queues" "[dry-run]"
    return
  fi

  # Check RabbitMQ management API reachability
  local rmq_health_code
  rmq_health_code=$(curl -sS -o /dev/null -w "%{http_code}" --max-time 10 \
    -u "${rmq_user}:${rmq_pass}" \
    "${rmq_url}/api/healthchecks/node" 2>/dev/null || echo "000")

  if [[ "${rmq_health_code}" != "200" ]]; then
    record_warn "rabbitmq.management_api" "HTTP ${rmq_health_code} — management plugin may be unavailable"
    return
  fi
  record_pass "rabbitmq.management_api" "HTTP ${rmq_health_code}"

  # Fetch node overview
  local overview
  overview=$(curl -sS --max-time 10 \
    -u "${rmq_user}:${rmq_pass}" \
    "${rmq_url}/api/overview" 2>/dev/null || echo "{}")

  local msg_ready msg_unacked total_msg
  msg_ready=$(echo "${overview}"   | jq -r '.queue_totals.messages_ready // 0')
  msg_unacked=$(echo "${overview}" | jq -r '.queue_totals.messages_unacknowledged // 0')
  total_msg=$(echo "${overview}"   | jq -r '.queue_totals.messages // 0')

  log_info "  Cluster totals — ready=${msg_ready}  unacked=${msg_unacked}  total=${total_msg}"

  local node_name
  node_name=$(echo "${overview}" | jq -r '.node // "unknown"')
  record_pass "rabbitmq.node" "${node_name}"

  # Fetch all queues
  local queues_json
  queues_json=$(curl -sS --max-time 15 \
    -u "${rmq_user}:${rmq_pass}" \
    "${rmq_url}/api/queues" 2>/dev/null || echo "[]")

  local queue_count
  queue_count=$(echo "${queues_json}" | jq length 2>/dev/null || echo "0")
  log_info "  Total queues: ${queue_count}"

  if [[ "${queue_count}" -eq 0 ]]; then
    record_warn "rabbitmq.queues" "No queues found — may be created on first event"
    return
  fi

  # Per-queue analysis
  local high_depth_count=0
  local dlq_issue_count=0

  while IFS= read -r queue_item; do
    local q_name q_ready q_unacked q_consumers q_state
    q_name=$(echo "${queue_item}"      | jq -r '.name')
    q_ready=$(echo "${queue_item}"     | jq -r '.messages_ready // 0')
    q_unacked=$(echo "${queue_item}"   | jq -r '.messages_unacknowledged // 0')
    q_consumers=$(echo "${queue_item}" | jq -r '.consumers // 0')
    q_state=$(echo "${queue_item}"     | jq -r '.state // "unknown"')

    local q_check="rabbitmq.queue.${q_name//\./_}"

    # Check if queue is a DLQ
    local is_dlq=false
    echo "${q_name}" | grep -qi "\(dlq\|dead.letter\|dead_letter\)" && is_dlq=true

    if [[ "${is_dlq}" == "true" ]]; then
      if [[ "${q_ready}" -gt "${RABBITMQ_DLQ_MESSAGES_WARN}" ]]; then
        record_fail "${q_check}" \
          "DLQ has ${q_ready} messages — potential processing failures"
        dlq_issue_count=$((dlq_issue_count + 1))
      else
        record_pass "${q_check}" "DLQ depth=${q_ready}"
      fi
      continue
    fi

    # Regular queue checks
    if [[ "${q_state}" != "running" ]]; then
      record_warn "${q_check}" "state=${q_state} (not running)"
      continue
    fi

    if [[ "${q_consumers}" -eq 0 ]]; then
      record_warn "${q_check}" \
        "depth=${q_ready} unacked=${q_unacked} — NO CONSUMERS (messages accumulating)"
      high_depth_count=$((high_depth_count + 1))
    elif [[ "${q_ready}" -gt "${RABBITMQ_QUEUE_DEPTH_WARN}" ]]; then
      record_warn "${q_check}" \
        "depth=${q_ready} (threshold=${RABBITMQ_QUEUE_DEPTH_WARN}) consumers=${q_consumers}"
      high_depth_count=$((high_depth_count + 1))
    else
      record_pass "${q_check}" \
        "depth=${q_ready} unacked=${q_unacked} consumers=${q_consumers}"
    fi
  done < <(echo "${queues_json}" | jq -c '.[]')

  if [[ ${high_depth_count} -gt 0 || ${dlq_issue_count} -gt 0 ]]; then
    record_warn "rabbitmq.summary" \
      "${high_depth_count} high-depth queue(s), ${dlq_issue_count} DLQ issue(s)"
  else
    record_pass "rabbitmq.summary" "All queues healthy"
  fi
}

# ---------------------------------------------------------------------------
# Generate scaling report
# ---------------------------------------------------------------------------
generate_report() {
  log_step "Generating scaling health report"

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
    echo " BloodBank — Scaling Health Report (M11-005)"
    echo "================================================================================"
    echo " Namespace   : ${NAMESPACE}"
    echo " Timestamp   : $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
    echo " Dry-run     : ${DRY_RUN}"
    echo " Overall     : ${overall_status}"
    echo "--------------------------------------------------------------------------------"
    echo ""
    echo " Thresholds Used:"
    echo "   HPA CPU warn        : ${HPA_CPU_WARN_PCT}%"
    echo "   HPA Memory warn     : ${HPA_MEM_WARN_PCT}%"
    echo "   DB max connections  : ${DB_MAX_CONNECTIONS_WARN}"
    echo "   DB waiting fail     : ${DB_WAITING_CONNS_FAIL}"
    echo "   Redis hit rate warn : ${REDIS_HIT_RATE_WARN_PCT}%"
    echo "   Redis eviction warn : ${REDIS_EVICTION_WARN}"
    echo "   Queue depth warn    : ${RABBITMQ_QUEUE_DEPTH_WARN}"
    echo "   DLQ messages warn   : ${RABBITMQ_DLQ_MESSAGES_WARN}"
    echo ""
    echo "--------------------------------------------------------------------------------"
    printf " %-55s %s\n" "CHECK" "RESULT"
    echo "--------------------------------------------------------------------------------"

    for check in $(echo "${!CHECK_RESULTS[@]}" | tr ' ' '\n' | sort); do
      local status="${CHECK_RESULTS[$check]}"
      printf " %-55s %s\n" "${check}" "${status}"
    done

    echo "--------------------------------------------------------------------------------"
    echo " Summary: PASS=${TOTAL_PASS}  FAIL=${TOTAL_FAIL}  WARN=${TOTAL_WARN}"
    echo "--------------------------------------------------------------------------------"
    echo " Log    : ${LOG_FILE}"
    echo " Report : ${REPORT_FILE}"
    echo "================================================================================"

    if [[ ${TOTAL_FAIL} -gt 0 ]]; then
      echo ""
      echo " ⚠  ACTION REQUIRED:"
      for check in $(echo "${!CHECK_RESULTS[@]}" | tr ' ' '\n' | sort); do
        [[ "${CHECK_RESULTS[$check]}" == "FAIL" ]] && echo "    FAIL: ${check}"
      done
    fi
  } | tee "${REPORT_FILE}"

  log_success "Report saved: ${REPORT_FILE}"

  # If there are FAIL results, exit non-zero to signal problems to callers
  if [[ ${TOTAL_FAIL} -gt 0 ]]; then
    exit 1
  fi
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
  echo -e "\n${BOLD}${GREEN}BloodBank — Scaling & Infrastructure Health Check (M11-005)${NC}"
  echo -e "${CYAN}Started: $(date -u '+%Y-%m-%d %H:%M:%S UTC')${NC}\n"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_warn "DRY-RUN MODE — no changes will be made"
  fi

  check_hpa
  check_database
  check_redis
  check_rabbitmq
  generate_report

  echo -e "\n${BOLD}${GREEN}✔ Scaling health check complete${NC}\n"
}

main "$@"
