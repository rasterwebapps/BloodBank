#!/usr/bin/env bash
# Hook: Validate No Lombok
# Runs after file creation/modification to ensure no Lombok annotations are present
# Usage: bash .claude/hooks/validate-no-lombok.sh [file_or_directory]

set -euo pipefail

TARGET="${1:-.}"
VIOLATIONS=0

echo "🔍 Scanning for Lombok violations in: $TARGET"

# List of banned Lombok annotations
LOMBOK_PATTERNS=(
    "@Data"
    "@Getter"
    "@Setter"
    "@Builder"
    "@Value"
    "@NoArgsConstructor"
    "@AllArgsConstructor"
    "@RequiredArgsConstructor"
    "@Slf4j"
    "@Log4j2"
    "@Log"
    "@CommonsLog"
    "@ToString"
    "@EqualsAndHashCode"
    "@With"
    "@Wither"
    "@Accessors"
    "@Delegate"
    "import lombok"
    "lombok"
)

# Scan Java files
while IFS= read -r -d '' file; do
    for pattern in "${LOMBOK_PATTERNS[@]}"; do
        if grep -qn "$pattern" "$file" 2>/dev/null; then
            echo "❌ LOMBOK VIOLATION: $file"
            grep -n "$pattern" "$file" | while read -r line; do
                echo "   Line: $line"
            done
            VIOLATIONS=$((VIOLATIONS + 1))
        fi
    done
done < <(find "$TARGET" -name "*.java" -not -path "*/build/*" -not -path "*/.gradle/*" -print0)

# Scan Gradle build files for lombok dependencies
while IFS= read -r -d '' file; do
    if grep -qni "lombok" "$file" 2>/dev/null; then
        echo "❌ LOMBOK IN BUILD: $file"
        grep -ni "lombok" "$file" | while read -r line; do
            echo "   Line: $line"
        done
        VIOLATIONS=$((VIOLATIONS + 1))
    fi
done < <(find "$TARGET" -name "*.gradle.kts" -o -name "*.gradle" -print0)

if [ "$VIOLATIONS" -gt 0 ]; then
    echo ""
    echo "⛔ FAILED: Found $VIOLATIONS Lombok violation(s)"
    echo "   This project does NOT use Lombok. Use Java 21 records for DTOs,"
    echo "   explicit getters/setters for entities, and LoggerFactory for logging."
    exit 1
else
    echo "✅ PASSED: No Lombok violations found"
    exit 0
fi
