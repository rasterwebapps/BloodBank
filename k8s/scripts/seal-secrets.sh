#!/usr/bin/env bash
# =============================================================================
# seal-secrets.sh
# BloodBank — Generate Sealed Secrets for a target namespace
#
# Usage:
#   bash k8s/scripts/seal-secrets.sh <namespace>
#
# Examples:
#   bash k8s/scripts/seal-secrets.sh bloodbank-prod
#   bash k8s/scripts/seal-secrets.sh bloodbank-uat
#   bash k8s/scripts/seal-secrets.sh bloodbank-staging
#   bash k8s/scripts/seal-secrets.sh bloodbank-dev
#
# Prerequisites:
#   - kubeseal CLI installed (https://github.com/bitnami-labs/sealed-secrets/releases)
#   - kubectl configured and pointing at the target cluster
#   - Sealed Secrets controller running in kube-system
#     (see k8s/secrets/sealed-secrets-controller.yml)
#
# Output:
#   k8s/secrets/bloodbank-sealed-secret-<env>.yml
#
# Apply the output:
#   kubectl apply -f k8s/secrets/bloodbank-sealed-secret-<env>.yml
# =============================================================================

set -euo pipefail

CONTROLLER_NAME="sealed-secrets-controller"
CONTROLLER_NAMESPACE="kube-system"
SECRET_NAME="bloodbank-secrets"

# ---------------------------------------------------------------------------
# Argument validation
# ---------------------------------------------------------------------------
NAMESPACE="${1:-}"
if [[ -z "$NAMESPACE" ]]; then
  echo "ERROR: Namespace argument required."
  echo "Usage: $0 <namespace>"
  echo "       $0 bloodbank-prod"
  exit 1
fi

VALID_NAMESPACES=("bloodbank-prod" "bloodbank-uat" "bloodbank-staging" "bloodbank-dev")
VALID=false
for ns in "${VALID_NAMESPACES[@]}"; do
  [[ "$ns" == "$NAMESPACE" ]] && VALID=true && break
done
if [[ "$VALID" == false ]]; then
  echo "ERROR: Unknown namespace '$NAMESPACE'."
  echo "Valid namespaces: ${VALID_NAMESPACES[*]}"
  exit 1
fi

# Derive a short environment label (prod, uat, staging, dev)
ENV="${NAMESPACE#bloodbank-}"
OUTPUT_FILE="k8s/secrets/bloodbank-sealed-secret-${ENV}.yml"

# ---------------------------------------------------------------------------
# Dependency checks
# ---------------------------------------------------------------------------
for cmd in kubeseal kubectl; do
  if ! command -v "$cmd" &>/dev/null; then
    echo "ERROR: '$cmd' not found. Please install it and try again."
    exit 1
  fi
done

# Verify controller is reachable
if ! kubeseal --fetch-cert \
    --controller-name="$CONTROLLER_NAME" \
    --controller-namespace="$CONTROLLER_NAMESPACE" \
    > /dev/null 2>&1; then
  echo "ERROR: Cannot reach Sealed Secrets controller '$CONTROLLER_NAME' in namespace '$CONTROLLER_NAMESPACE'."
  echo "Verify the controller is running: kubectl get pods -n kube-system -l app.kubernetes.io/name=sealed-secrets"
  exit 1
fi

# ---------------------------------------------------------------------------
# Helper: read a secret value with optional default
# ---------------------------------------------------------------------------
prompt_secret() {
  local key="$1"
  local prompt="$2"
  local default_val="${3:-}"

  local value=""
  if [[ -n "$default_val" ]]; then
    read -r -s -p "$prompt [press Enter for auto-generated]: " value
    echo
    if [[ -z "$value" ]]; then
      value="$default_val"
      echo "  → Using auto-generated value"
    fi
  else
    while [[ -z "$value" ]]; do
      read -r -s -p "$prompt: " value
      echo
      if [[ -z "$value" ]]; then
        echo "  ERROR: Value cannot be empty."
      fi
    done
  fi

  echo "$value"
}

# ---------------------------------------------------------------------------
# Helper: seal a single key=value pair
# ---------------------------------------------------------------------------
seal_value() {
  local key="$1"
  local value="$2"

  printf '%s' "$value" | kubeseal --raw \
    --namespace "$NAMESPACE" \
    --name "$SECRET_NAME" \
    --controller-name "$CONTROLLER_NAME" \
    --controller-namespace "$CONTROLLER_NAMESPACE" \
    --from-file /dev/stdin
}

# ---------------------------------------------------------------------------
# Collect credentials interactively
# ---------------------------------------------------------------------------
echo ""
echo "═══════════════════════════════════════════════════════════"
echo " BloodBank — Seal Secrets for namespace: $NAMESPACE"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo "Enter credentials for each key.  Values are never written to disk"
echo "in plaintext — only the kubeseal-encrypted ciphertext is stored."
echo ""

# Database
echo "── PostgreSQL ──────────────────────────────────────────────"
DB_USERNAME=$(prompt_secret "DB_USERNAME" "DB_USERNAME (PostgreSQL username)")
DB_PASSWORD=$(prompt_secret "DB_PASSWORD" "DB_PASSWORD (PostgreSQL password)" "$(openssl rand -base64 32)")

# RabbitMQ
echo ""
echo "── RabbitMQ ────────────────────────────────────────────────"
RABBITMQ_USERNAME=$(prompt_secret "RABBITMQ_USERNAME" "RABBITMQ_USERNAME")
RABBITMQ_PASSWORD=$(prompt_secret "RABBITMQ_PASSWORD" "RABBITMQ_PASSWORD" "$(openssl rand -base64 32)")

# MinIO
echo ""
echo "── MinIO (document-service) ────────────────────────────────"
MINIO_ACCESS_KEY=$(prompt_secret "MINIO_ACCESS_KEY" "MINIO_ACCESS_KEY" "bloodbank-$(openssl rand -hex 8)")
MINIO_SECRET_KEY=$(prompt_secret "MINIO_SECRET_KEY" "MINIO_SECRET_KEY" "$(openssl rand -base64 40)")

# Spring Cloud Config encryption key
echo ""
echo "── Spring Cloud Config ─────────────────────────────────────"
ENCRYPT_KEY=$(prompt_secret "ENCRYPT_KEY" "ENCRYPT_KEY (256-bit hex — press Enter to auto-generate)" "$(openssl rand -hex 32)")

# ---------------------------------------------------------------------------
# Seal each value
# ---------------------------------------------------------------------------
echo ""
echo "Sealing secrets with cluster public key..."

SEALED_DB_USERNAME=$(seal_value "DB_USERNAME" "$DB_USERNAME")
echo "  ✓ DB_USERNAME"

SEALED_DB_PASSWORD=$(seal_value "DB_PASSWORD" "$DB_PASSWORD")
echo "  ✓ DB_PASSWORD"

SEALED_RABBITMQ_USERNAME=$(seal_value "RABBITMQ_USERNAME" "$RABBITMQ_USERNAME")
echo "  ✓ RABBITMQ_USERNAME"

SEALED_RABBITMQ_PASSWORD=$(seal_value "RABBITMQ_PASSWORD" "$RABBITMQ_PASSWORD")
echo "  ✓ RABBITMQ_PASSWORD"

SEALED_MINIO_ACCESS_KEY=$(seal_value "MINIO_ACCESS_KEY" "$MINIO_ACCESS_KEY")
echo "  ✓ MINIO_ACCESS_KEY"

SEALED_MINIO_SECRET_KEY=$(seal_value "MINIO_SECRET_KEY" "$MINIO_SECRET_KEY")
echo "  ✓ MINIO_SECRET_KEY"

SEALED_ENCRYPT_KEY=$(seal_value "ENCRYPT_KEY" "$ENCRYPT_KEY")
echo "  ✓ ENCRYPT_KEY"

# ---------------------------------------------------------------------------
# Write the SealedSecret manifest
# ---------------------------------------------------------------------------
cat > "$OUTPUT_FILE" <<EOF
# =============================================================================
# bloodbank-sealed-secret-${ENV}.yml
# BloodBank — SealedSecret for namespace: ${NAMESPACE}
#
# Generated: $(date -u +"%Y-%m-%dT%H:%M:%SZ")
# Controller: ${CONTROLLER_NAME} in ${CONTROLLER_NAMESPACE}
# Scope: strict (namespace-scoped — cannot be moved to another namespace)
#
# ✅ Safe to commit to Git — values are encrypted with the cluster public key.
#
# Apply:
#   kubectl apply -f ${OUTPUT_FILE}
#
# Verify:
#   kubectl get secret ${SECRET_NAME} -n ${NAMESPACE}
# =============================================================================
apiVersion: bitnami.com/v1alpha1
kind: SealedSecret
metadata:
  name: ${SECRET_NAME}
  namespace: ${NAMESPACE}
  annotations:
    sealedsecrets.bitnami.com/managed: "true"
  labels:
    app.kubernetes.io/name: ${SECRET_NAME}
    app.kubernetes.io/component: credentials
    app.kubernetes.io/part-of: bloodbank
    environment: ${ENV}
spec:
  encryptedData:
    DB_USERNAME: ${SEALED_DB_USERNAME}
    DB_PASSWORD: ${SEALED_DB_PASSWORD}
    RABBITMQ_USERNAME: ${SEALED_RABBITMQ_USERNAME}
    RABBITMQ_PASSWORD: ${SEALED_RABBITMQ_PASSWORD}
    MINIO_ACCESS_KEY: ${SEALED_MINIO_ACCESS_KEY}
    MINIO_SECRET_KEY: ${SEALED_MINIO_SECRET_KEY}
    ENCRYPT_KEY: ${SEALED_ENCRYPT_KEY}
  template:
    metadata:
      name: ${SECRET_NAME}
      namespace: ${NAMESPACE}
      labels:
        app.kubernetes.io/name: ${SECRET_NAME}
        app.kubernetes.io/component: credentials
        app.kubernetes.io/part-of: bloodbank
        environment: ${ENV}
    type: Opaque
EOF

echo ""
echo "═══════════════════════════════════════════════════════════"
echo " ✅ Done!  Output: ${OUTPUT_FILE}"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo "Next steps:"
echo "  1. Review the generated file:  cat ${OUTPUT_FILE}"
echo "  2. Commit to Git:              git add ${OUTPUT_FILE} && git commit -m 'chore: add sealed secrets for ${ENV}'"
echo "  3. Apply to cluster:           kubectl apply -f ${OUTPUT_FILE}"
echo "  4. Verify the Secret exists:   kubectl get secret ${SECRET_NAME} -n ${NAMESPACE}"
echo ""
echo "⚠️  IMPORTANT: Back up the master sealing key immediately after first install:"
echo "  kubectl get secret -n kube-system -l sealedsecrets.bitnami.com/sealed-secrets-key=active -o yaml \\"
echo "    > /secure-offline-backup/sealed-secrets-master-key.yaml"
echo ""
