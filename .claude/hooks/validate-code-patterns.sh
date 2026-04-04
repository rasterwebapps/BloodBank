#!/usr/bin/env bash
# Hook: Validate Code Patterns
# Runs after file creation/modification to check BloodBank code conventions
# Usage: bash .claude/hooks/validate-code-patterns.sh [file_or_directory]

set -euo pipefail

TARGET="${1:-.}"
WARNINGS=0
ERRORS=0

echo "🔍 Validating BloodBank code patterns in: $TARGET"

# --- Check 1: Controllers must have @PreAuthorize ---
echo ""
echo "📋 Checking controllers for @PreAuthorize..."
while IFS= read -r -d '' file; do
    if grep -q "@RestController" "$file" 2>/dev/null; then
        # Count public methods (excluding constructor)
        PUBLIC_METHODS=$(grep -c "public ResponseEntity\|public void\|public .*Response" "$file" 2>/dev/null || true)
        PREAUTH_COUNT=$(grep -c "@PreAuthorize" "$file" 2>/dev/null || true)
        
        if [ "$PUBLIC_METHODS" -gt 0 ] && [ "$PREAUTH_COUNT" -lt "$PUBLIC_METHODS" ]; then
            echo "⚠️  MISSING @PreAuthorize: $file ($PREAUTH_COUNT/@PreAuthorize for $PUBLIC_METHODS endpoint methods)"
            WARNINGS=$((WARNINGS + 1))
        fi
    fi
done < <(find "$TARGET" -name "*Controller.java" -not -path "*/build/*" -print0)

# --- Check 2: Services must use constructor injection ---
echo ""
echo "📋 Checking services for @Autowired field injection..."
while IFS= read -r -d '' file; do
    if grep -q "@Service" "$file" 2>/dev/null; then
        if grep -q "@Autowired" "$file" 2>/dev/null; then
            echo "❌ FIELD INJECTION: $file — Use constructor injection instead of @Autowired"
            ERRORS=$((ERRORS + 1))
        fi
    fi
done < <(find "$TARGET" -name "*Service.java" -not -path "*/build/*" -print0)

# --- Check 3: DTOs should be records ---
echo ""
echo "📋 Checking DTOs are Java records..."
while IFS= read -r -d '' file; do
    BASENAME=$(basename "$file")
    if [[ "$BASENAME" == *"Request.java" || "$BASENAME" == *"Response.java" || "$BASENAME" == *"Event.java" ]]; then
        if ! grep -q "public record" "$file" 2>/dev/null; then
            if grep -q "public class" "$file" 2>/dev/null; then
                echo "⚠️  NOT A RECORD: $file — DTOs and Events should be Java 21 records"
                WARNINGS=$((WARNINGS + 1))
            fi
        fi
    fi
done < <(find "$TARGET" -path "*/dto/*" -name "*.java" -not -path "*/build/*" -print0)

# --- Check 4: Entities must extend BaseEntity or BranchScopedEntity ---
echo ""
echo "📋 Checking entities extend base classes..."
while IFS= read -r -d '' file; do
    if grep -q "@Entity" "$file" 2>/dev/null; then
        if ! grep -q "extends BaseEntity\|extends BranchScopedEntity" "$file" 2>/dev/null; then
            echo "⚠️  NO BASE CLASS: $file — Entities must extend BaseEntity or BranchScopedEntity"
            WARNINGS=$((WARNINGS + 1))
        fi
    fi
done < <(find "$TARGET" -path "*/entity/*" -name "*.java" -not -path "*/build/*" -print0)

# --- Check 5: Flyway must be disabled in service application.yml ---
echo ""
echo "📋 Checking Flyway is disabled in services..."
while IFS= read -r -d '' file; do
    # Skip db-migration module and test files
    if [[ "$file" != *"db-migration"* && "$file" != *"test"* ]]; then
        if grep -q "flyway" "$file" 2>/dev/null; then
            if grep -q "enabled: true" "$file" 2>/dev/null; then
                echo "❌ FLYWAY ENABLED: $file — Services must set spring.flyway.enabled=false"
                ERRORS=$((ERRORS + 1))
            fi
        fi
    fi
done < <(find "$TARGET" -name "application*.yml" -not -path "*/build/*" -print0)

# --- Check 6: API prefix must be /api/v1/ ---
echo ""
echo "📋 Checking API prefix convention..."
while IFS= read -r -d '' file; do
    if grep -q "@RequestMapping" "$file" 2>/dev/null; then
        if grep -q '@RequestMapping("/' "$file" 2>/dev/null; then
            if ! grep -q '@RequestMapping("/api/v1/' "$file" 2>/dev/null; then
                echo "⚠️  WRONG API PREFIX: $file — Must use /api/v1/ prefix"
                WARNINGS=$((WARNINGS + 1))
            fi
        fi
    fi
done < <(find "$TARGET" -name "*Controller.java" -not -path "*/build/*" -print0)

# --- Summary ---
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ "$ERRORS" -gt 0 ]; then
    echo "⛔ FAILED: $ERRORS error(s), $WARNINGS warning(s)"
    exit 1
elif [ "$WARNINGS" -gt 0 ]; then
    echo "⚠️  PASSED WITH WARNINGS: $WARNINGS warning(s)"
    exit 0
else
    echo "✅ PASSED: All code patterns valid"
    exit 0
fi
