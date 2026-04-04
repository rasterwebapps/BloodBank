#!/usr/bin/env bash
# Hook: Pre-Push Checks
# Comprehensive validation before pushing code
# Usage: bash .claude/hooks/pre-push-checks.sh

set -euo pipefail

echo "🚀 Running pre-push validation checks..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

PASS=0
FAIL=0

# --- Step 1: No Lombok Check ---
echo ""
echo "Step 1/5: Checking for Lombok violations..."
if bash "$SCRIPT_DIR/validate-no-lombok.sh" "$PROJECT_ROOT" 2>/dev/null; then
    PASS=$((PASS + 1))
else
    FAIL=$((FAIL + 1))
fi

# --- Step 2: Code Pattern Validation ---
echo ""
echo "Step 2/5: Validating code patterns..."
if bash "$SCRIPT_DIR/validate-code-patterns.sh" "$PROJECT_ROOT" 2>/dev/null; then
    PASS=$((PASS + 1))
else
    FAIL=$((FAIL + 1))
fi

# --- Step 3: Check for secrets/credentials ---
echo ""
echo "Step 3/5: Scanning for hardcoded secrets..."
SECRETS_FOUND=0
PATTERNS=(
    "password\s*=\s*\"[^\"]\+\""
    "secret\s*=\s*\"[^\"]\+\""
    "api[_-]key\s*=\s*\"[^\"]\+\""
    "-----BEGIN.*PRIVATE KEY-----"
    "jdbc:postgresql://.*:.*@"
)
for pattern in "${PATTERNS[@]}"; do
    if grep -rnI "$pattern" "$PROJECT_ROOT" \
        --include="*.java" --include="*.yml" --include="*.yaml" --include="*.properties" \
        --exclude-dir=build --exclude-dir=.gradle --exclude-dir=node_modules \
        --exclude-dir=.git --exclude="*.sh" --exclude="*.md" 2>/dev/null | \
        grep -v "test" | grep -v "example" | grep -v "template" | head -5; then
        SECRETS_FOUND=$((SECRETS_FOUND + 1))
    fi
done
if [ "$SECRETS_FOUND" -gt 0 ]; then
    echo "⚠️  WARNING: Potential hardcoded secrets found — review above matches"
    FAIL=$((FAIL + 1))
else
    echo "✅ No hardcoded secrets found"
    PASS=$((PASS + 1))
fi

# --- Step 4: Check Gradle build compiles ---
echo ""
echo "Step 4/5: Checking Gradle build..."
if [ -f "$PROJECT_ROOT/gradlew" ]; then
    if "$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" classes --no-daemon -q 2>/dev/null; then
        echo "✅ Gradle compilation successful"
        PASS=$((PASS + 1))
    else
        echo "❌ Gradle compilation failed"
        FAIL=$((FAIL + 1))
    fi
else
    echo "⏭️  Skipped: gradlew not found"
    PASS=$((PASS + 1))
fi

# --- Step 5: Check for TODO/FIXME in staged files ---
echo ""
echo "Step 5/5: Checking for unresolved TODOs..."
TODO_COUNT=$(git diff --cached --name-only 2>/dev/null | \
    xargs grep -c "TODO\|FIXME\|HACK\|XXX" 2>/dev/null | \
    awk -F: '{sum += $2} END {print sum+0}')
if [ "$TODO_COUNT" -gt 0 ]; then
    echo "⚠️  WARNING: $TODO_COUNT TODO/FIXME markers in staged files"
else
    echo "✅ No unresolved TODOs in staged files"
fi
PASS=$((PASS + 1))

# --- Summary ---
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Results: $PASS passed, $FAIL failed"
if [ "$FAIL" -gt 0 ]; then
    echo "⛔ Pre-push checks FAILED — fix issues before pushing"
    exit 1
else
    echo "✅ All pre-push checks PASSED"
    exit 0
fi
