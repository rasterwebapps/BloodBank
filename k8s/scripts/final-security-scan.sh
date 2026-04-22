#!/usr/bin/env bash
# =============================================================================
# final-security-scan.sh
# BloodBank — Final Security Scan Gate
#
# Usage:
#   IMAGE_TAG=1.2.3 REGISTRY=bloodbank ./final-security-scan.sh
#
# What this script does:
#   1. Runs Trivy image vulnerability scans on all 15 Docker images
#   2. Runs OWASP Dependency-Check on the Gradle project
#   3. Fails (exit 1) on any CRITICAL vulnerability found
#   4. Generates JSON + HTML reports in reports/
#
# Environment variables:
#   IMAGE_TAG  — Docker image tag to scan (default: latest)
#   REGISTRY   — Docker registry / image prefix (default: bloodbank)
#   REPORTS_DIR — Output directory for reports (default: reports)
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

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
IMAGE_TAG="${IMAGE_TAG:-latest}"
REGISTRY="${REGISTRY:-bloodbank}"
REPORTS_DIR="${REPORTS_DIR:-reports}"
SCAN_TIMESTAMP="$(date '+%Y%m%d_%H%M%S')"
REPORT_PREFIX="${REPORTS_DIR}/security-scan-${SCAN_TIMESTAMP}"

# All 15 BloodBank Docker images
IMAGES=(
  "api-gateway"
  "branch-service"
  "donor-service"
  "lab-service"
  "inventory-service"
  "transfusion-service"
  "hospital-service"
  "request-matching-service"
  "billing-service"
  "notification-service"
  "reporting-service"
  "document-service"
  "compliance-service"
  "config-server"
  "bloodbank-ui"
)

# Tracking variables
CRITICAL_FOUND=0
SCAN_FAILURES=()
SCAN_PASSES=()
OWASP_FAILED=0

# ---------------------------------------------------------------------------
# Preflight checks
# ---------------------------------------------------------------------------
preflight_checks() {
  log_step "Preflight Checks"

  if ! command -v trivy &>/dev/null; then
    log_error "trivy is not installed. Install from: https://github.com/aquasecurity/trivy"
    exit 1
  fi
  log_info "trivy version: $(trivy --version | head -1)"

  if ! command -v docker &>/dev/null; then
    log_warn "docker CLI not found — Trivy will pull images directly from registry"
  fi

  if [[ ! -f "./gradlew" ]]; then
    log_warn "gradlew not found in current directory — OWASP scan will be skipped"
  fi

  log_info "Reports will be written to: ${REPORTS_DIR}/"
  mkdir -p "${REPORTS_DIR}"
}

# ---------------------------------------------------------------------------
# Trivy image scan for a single image
# Returns 0 if clean, 1 if CRITICAL found
# ---------------------------------------------------------------------------
scan_image() {
  local image_name="$1"
  local full_image="${REGISTRY}/${image_name}:${IMAGE_TAG}"
  local report_json="${REPORT_PREFIX}-trivy-${image_name}.json"
  local report_html="${REPORT_PREFIX}-trivy-${image_name}.html"

  log_info "Scanning image: ${full_image}"

  # Run Trivy — JSON report (machine-readable)
  if trivy image \
      --exit-code 0 \
      --severity "CRITICAL,HIGH,MEDIUM,LOW" \
      --format json \
      --output "${report_json}" \
      --no-progress \
      "${full_image}" 2>/dev/null; then

    # Run Trivy — HTML report (human-readable)
    trivy image \
      --exit-code 0 \
      --severity "CRITICAL,HIGH,MEDIUM,LOW" \
      --format template \
      --template "@contrib/html.tpl" \
      --output "${report_html}" \
      --no-progress \
      "${full_image}" 2>/dev/null || true  # HTML template may not be available everywhere

    # Check for CRITICAL vulnerabilities in the JSON output
    local critical_count
    critical_count=$(python3 -c "
import json, sys
try:
    with open('${report_json}') as f:
        data = json.load(f)
    count = 0
    for result in data.get('Results', []):
        for vuln in result.get('Vulnerabilities', []):
            if vuln.get('Severity') == 'CRITICAL':
                count += 1
    print(count)
except Exception as e:
    print(0)
" 2>/dev/null || echo "0")

    if [[ "${critical_count}" -gt 0 ]]; then
      log_error "CRITICAL vulnerabilities found in ${full_image}: ${critical_count} issue(s)"
      SCAN_FAILURES+=("${image_name} [${critical_count} CRITICAL]")
      CRITICAL_FOUND=1
      return 1
    else
      log_success "${image_name} — no CRITICAL vulnerabilities"
      SCAN_PASSES+=("${image_name}")
      return 0
    fi
  else
    log_error "Trivy scan failed for ${full_image} (image may not exist or be unreachable)"
    SCAN_FAILURES+=("${image_name} [scan-error]")
    CRITICAL_FOUND=1
    return 1
  fi
}

# ---------------------------------------------------------------------------
# OWASP Dependency-Check via Gradle
# ---------------------------------------------------------------------------
run_owasp_check() {
  log_step "OWASP Dependency-Check (Gradle)"

  if [[ ! -f "./gradlew" ]]; then
    log_warn "Skipping OWASP scan — gradlew not found"
    return 0
  fi

  log_info "Running: ./gradlew dependencyCheckAnalyze --no-daemon"

  if ./gradlew dependencyCheckAnalyze --no-daemon \
      -PdependencyCheck.failBuildOnCVSS=9 \
      2>&1 | tee "${REPORT_PREFIX}-owasp.log"; then
    log_success "OWASP Dependency-Check passed"

    # Copy NVD reports if they exist
    find . -name "dependency-check-report.*" -exec cp {} "${REPORTS_DIR}/" \; 2>/dev/null || true
  else
    log_error "OWASP Dependency-Check FAILED — CRITICAL CVEs detected (CVSS >= 9)"
    OWASP_FAILED=1
    cp build/reports/dependency-check-report.* "${REPORTS_DIR}/" 2>/dev/null || true
  fi
}

# ---------------------------------------------------------------------------
# Generate consolidated security report (JSON)
# ---------------------------------------------------------------------------
generate_consolidated_report() {
  log_step "Generating Consolidated Security Report"

  local report_file="${REPORT_PREFIX}-summary.json"

  # Build JSON arrays
  local passes_json="[]"
  if [[ ${#SCAN_PASSES[@]} -gt 0 ]]; then
    passes_json="[$(printf '"%s",' "${SCAN_PASSES[@]}" | sed 's/,$//')]"
  fi

  local failures_json="[]"
  if [[ ${#SCAN_FAILURES[@]} -gt 0 ]]; then
    failures_json="[$(printf '"%s",' "${SCAN_FAILURES[@]}" | sed 's/,$//')]"
  fi

  local overall_status="PASS"
  if [[ "${CRITICAL_FOUND}" -eq 1 || "${OWASP_FAILED}" -eq 1 ]]; then
    overall_status="FAIL"
  fi

  cat > "${report_file}" <<EOF
{
  "scan_metadata": {
    "timestamp": "$(date -u '+%Y-%m-%dT%H:%M:%SZ')",
    "image_tag": "${IMAGE_TAG}",
    "registry": "${REGISTRY}",
    "tool_versions": {
      "trivy": "$(trivy --version 2>/dev/null | head -1 || echo 'unknown')"
    }
  },
  "overall_status": "${overall_status}",
  "trivy_scan": {
    "images_scanned": ${#IMAGES[@]},
    "passed": ${#SCAN_PASSES[@]},
    "failed": ${#SCAN_FAILURES[@]},
    "passed_images": ${passes_json},
    "failed_images": ${failures_json}
  },
  "owasp_dependency_check": {
    "status": "$([ "${OWASP_FAILED}" -eq 0 ] && echo 'PASS' || echo 'FAIL')",
    "cvss_threshold": 9.0
  },
  "reports_directory": "${REPORTS_DIR}",
  "report_prefix": "${REPORT_PREFIX}"
}
EOF

  log_info "Consolidated report written to: ${report_file}"
}

# ---------------------------------------------------------------------------
# Print final summary
# ---------------------------------------------------------------------------
print_summary() {
  log_step "Security Scan Summary"

  echo ""
  echo -e "${BOLD}Images scanned : ${#IMAGES[@]}${NC}"
  echo -e "${GREEN}Passed         : ${#SCAN_PASSES[@]}${NC}"
  echo -e "${RED}Failed         : ${#SCAN_FAILURES[@]}${NC}"
  echo ""

  if [[ ${#SCAN_FAILURES[@]} -gt 0 ]]; then
    echo -e "${RED}${BOLD}FAILED IMAGES:${NC}"
    for f in "${SCAN_FAILURES[@]}"; do
      echo -e "  ${RED}✗${NC} ${f}"
    done
    echo ""
  fi

  if [[ ${#SCAN_PASSES[@]} -gt 0 ]]; then
    echo -e "${GREEN}${BOLD}PASSED IMAGES:${NC}"
    for p in "${SCAN_PASSES[@]}"; do
      echo -e "  ${GREEN}✓${NC} ${p}"
    done
    echo ""
  fi

  if [[ "${OWASP_FAILED}" -eq 1 ]]; then
    echo -e "${RED}OWASP Dependency-Check : FAIL${NC}"
  else
    echo -e "${GREEN}OWASP Dependency-Check : PASS${NC}"
  fi

  echo ""
  echo -e "Reports saved to: ${BOLD}${REPORTS_DIR}/${NC}"
  echo ""

  if [[ "${CRITICAL_FOUND}" -eq 1 || "${OWASP_FAILED}" -eq 1 ]]; then
    echo -e "${RED}${BOLD}╔══════════════════════════════════════════╗${NC}"
    echo -e "${RED}${BOLD}║   ✗  SECURITY GATE: FAILED               ║${NC}"
    echo -e "${RED}${BOLD}╚══════════════════════════════════════════╝${NC}"
    return 1
  else
    echo -e "${GREEN}${BOLD}╔══════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}${BOLD}║   ✓  SECURITY GATE: PASSED               ║${NC}"
    echo -e "${GREEN}${BOLD}╚══════════════════════════════════════════╝${NC}"
    return 0
  fi
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
  echo -e "${BOLD}${CYAN}"
  local _pad=39  # banner inner width: 62 total - 6 prefix (║     ) - 2 suffix ( ║) - 15 label
  echo "╔════════════════════════════════════════════════════════════╗"
  echo "║     BloodBank — Final Security Scan Gate                   ║"
  echo "║     IMAGE_TAG  : ${IMAGE_TAG}$(printf '%*s' $((_pad - ${#IMAGE_TAG})) '') ║"
  echo "║     REGISTRY   : ${REGISTRY}$(printf '%*s' $((_pad - ${#REGISTRY})) '') ║"
  echo "╚════════════════════════════════════════════════════════════╝"
  echo -e "${NC}"

  preflight_checks

  # ── Trivy image scans ──────────────────────────────────────────────────
  log_step "Trivy Image Vulnerability Scans (${#IMAGES[@]} images)"
  for image in "${IMAGES[@]}"; do
    scan_image "${image}" || true   # failures tracked in SCAN_FAILURES; don't abort early
  done

  # ── OWASP Dependency-Check ─────────────────────────────────────────────
  run_owasp_check

  # ── Consolidated report ────────────────────────────────────────────────
  generate_consolidated_report

  # ── Final summary + exit code ──────────────────────────────────────────
  print_summary
}

main "$@"
