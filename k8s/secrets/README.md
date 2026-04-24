# BloodBank — Secrets Management

## Decision: Bitnami Sealed Secrets

**Chosen approach**: [Bitnami Sealed Secrets](https://github.com/bitnami-labs/sealed-secrets)
**Alternatives evaluated**: HashiCorp Vault

### Why Sealed Secrets

| Criterion | Sealed Secrets | Vault |
|---|---|---|
| Time to deploy | Minutes (single controller) | Hours (HA cluster + policies) |
| GitOps compatible | ✅ Encrypted blobs safe in Git | ⚠️ Requires separate Vault server |
| K8s-native | ✅ Creates real K8s Secrets | ⚠️ Requires sidecar / CSI driver |
| No existing manifests changes | ✅ `secretKeyRef: name: bloodbank-secrets` unchanged | ✅ Same if using External Secrets Operator |
| Operational complexity | Low | High |
| Dynamic secrets / rotation | ❌ Manual re-seal | ✅ Built-in |
| Full audit log | K8s audit + cluster logs | ✅ Vault audit backend |

**Conclusion**: Sealed Secrets delivers the required security posture (encrypted at rest in Git, decrypted only inside the cluster) with minimal infrastructure. Vault should be re-evaluated when the team has dedicated platform engineering capacity and requires dynamic database credentials or automated rotation.

---

## How Sealed Secrets Works

```
Developer                    Git Repo            K8s Cluster
─────────────────────────────────────────────────────────────
kubeseal encrypts  ──────►  SealedSecret  ──────►  Controller decrypts
using cluster public key    (committed)            into native K8s Secret
                                                   named "bloodbank-secrets"
```

All existing `secretKeyRef: name: bloodbank-secrets` entries in every
deployment/statefulset manifest continue to work without any change.

---

## Prerequisites

- `kubectl` configured with cluster admin access
- `kubeseal` CLI installed: `brew install kubeseal` or see [releases](https://github.com/bitnami-labs/sealed-secrets/releases)
- `helm` >= 3.x

---

## Step 1 — Install the Sealed Secrets Controller

```bash
# Add the Sealed Secrets Helm repo
helm repo add sealed-secrets https://bitnami-labs.github.io/sealed-secrets
helm repo update

# Install the controller in kube-system
helm install sealed-secrets sealed-secrets/sealed-secrets \
  --namespace kube-system \
  --version 2.16.1 \
  --set fullnameOverride=sealed-secrets-controller

# Verify the controller is running
kubectl get pods -n kube-system -l app.kubernetes.io/name=sealed-secrets
```

---

## Step 2 — Fetch the Cluster Public Key

```bash
# Save the public key for offline sealing (safe to commit)
kubeseal --fetch-cert \
  --controller-name=sealed-secrets-controller \
  --controller-namespace=kube-system \
  > k8s/secrets/sealed-secrets-pub.pem
```

---

## Step 3 — Generate and Apply Sealed Secrets

Run the provided bootstrap script for each target namespace:

```bash
# Interactive — prompts for each credential
bash k8s/scripts/seal-secrets.sh bloodbank-prod

# Apply the generated SealedSecret
kubectl apply -f k8s/secrets/bloodbank-sealed-secret-prod.yml
```

The script outputs a `bloodbank-sealed-secret-{namespace}.yml` file.
Commit this file to Git — the encrypted values are safe to store.

---

## Sealed Secret Keys Managed

The single `bloodbank-secrets` Secret contains:

| Key | Used By | Description |
|---|---|---|
| `DB_USERNAME` | All services, Flyway Job | PostgreSQL username |
| `DB_PASSWORD` | All services, Flyway Job | PostgreSQL password |
| `RABBITMQ_USERNAME` | All services | RabbitMQ username |
| `RABBITMQ_PASSWORD` | All services | RabbitMQ password |
| `MINIO_ACCESS_KEY` | document-service | MinIO / S3 access key |
| `MINIO_SECRET_KEY` | document-service | MinIO / S3 secret key |
| `ENCRYPT_KEY` | config-server | Spring Cloud Config encryption key |

---

## Secret Rotation

1. Update the plaintext value
2. Re-run `seal-secrets.sh` for the affected namespace
3. Commit the new `bloodbank-sealed-secret-{namespace}.yml`
4. Apply: `kubectl apply -f k8s/secrets/bloodbank-sealed-secret-{namespace}.yml`
5. Restart affected pods: `kubectl rollout restart deployment/<name> -n <namespace>`

---

## Key Backup and Disaster Recovery

The controller's master key is stored in a K8s Secret named `sealed-secrets-key`
in the `kube-system` namespace. **Back this up immediately after install:**

```bash
kubectl get secret sealed-secrets-key -n kube-system -o yaml \
  > /secure-offline-backup/sealed-secrets-master-key.yaml
```

Store this backup in an offline, encrypted location (e.g., an HSM or password manager vault).
Without this key, sealed secrets cannot be decrypted if the cluster is rebuilt.

---

## Namespace-Scoped Encryption

Each SealedSecret is tied to a specific namespace (scope: `strict`).
A SealedSecret for `bloodbank-prod` **cannot** be decrypted in `bloodbank-uat`.
This ensures environment isolation.

---

## Future Upgrade Path to Vault

If dynamic credentials or automated rotation become a requirement, migrate using the
[External Secrets Operator](https://external-secrets.io/):

1. Deploy Vault + External Secrets Operator
2. Migrate secrets to Vault KV v2
3. Create `ExternalSecret` resources pointing to Vault paths
4. Remove `SealedSecret` resources

The `secretKeyRef: name: bloodbank-secrets` references in all deployments remain unchanged.
