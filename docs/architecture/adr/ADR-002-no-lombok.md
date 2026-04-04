# ADR-002: No Lombok — Java 21 Records Instead

**Status:** Accepted
**Date:** 2026-04-04
**Decision Makers:** Architecture Team

## Context

Lombok is a popular Java library that generates boilerplate code (getters, setters, constructors, builders, loggers) via annotation processing. While it reduces verbosity, it introduces implicit code generation that can conflict with modern Java features and complicate debugging, code navigation, and security auditing.

Java 21 (LTS) provides native language features that cover most Lombok use cases:

- **Records** — immutable data carriers with auto-generated `equals()`, `hashCode()`, `toString()`, and accessors.
- **Sealed classes** — restricted type hierarchies.
- **Pattern matching** — `switch` expressions and `instanceof` patterns.

## Decision

**Lombok is banned from the entire project.** No Lombok annotations, no Lombok dependencies, no Lombok imports.

### Banned Annotations

`@Data`, `@Getter`, `@Setter`, `@Builder`, `@Value`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`, `@Slf4j`, `@Log4j2`, `@Log`, `@CommonsLog`, `@ToString`, `@EqualsAndHashCode`, `@With`, `@Wither`, `@Accessors`, `@Delegate`

### Required Alternatives

| Use Case | Pattern |
|---|---|
| DTOs (request/response) | Java 21 `record` |
| Event payloads | Java 21 `record` |
| JPA entities | Explicit `getXxx()` / `setXxx()` methods |
| No-arg constructor (JPA) | `protected ClassName() {}` |
| All-args constructor | Explicit constructor |
| Logger | `private static final Logger log = LoggerFactory.getLogger(ClassName.class);` |
| Builder | Static factory method or constructor |
| toString / equals / hashCode | Explicit methods (or use `record`) |

### Enforcement

- **Pre-commit hook** — `validate-no-lombok.sh` scans all `.java` files for banned annotations.
- **CI pipeline** — Validation runs in Jenkins stage 2 (build).
- **Code review** — PR template includes a "No Lombok" checklist item.

## Consequences

### Positive

- **Explicit code** — Every getter, setter, and constructor is visible in source. No hidden code generation.
- **IDE-friendly** — Full code navigation, refactoring, and debugging without Lombok plugin requirements.
- **Security auditing** — All code paths are visible for HIPAA/FDA compliance audits.
- **Java 21 alignment** — Records are the language-native solution for immutable data. The codebase uses modern Java idioms.
- **Compilation simplicity** — No annotation processor dependency chain. MapStruct is the only annotation processor needed.
- **Immutable DTOs** — Records enforce immutability by default, which is correct for request/response objects and events.

### Negative

- **More lines of code** — JPA entities require explicit getters/setters. Mitigated by IDE generation and MapStruct for mapping.
- **Developer habit** — Developers accustomed to Lombok need to adjust. Mitigated by clear patterns in CLAUDE.md and automated validation hooks.

### Alternatives Considered

| Alternative | Reason Rejected |
|---|---|
| Lombok with selective use | Partial adoption leads to inconsistency; annotation processor conflicts with MapStruct; hidden code complicates auditing |
| Kotlin data classes | Team expertise is Java; Kotlin interop adds build complexity; Java 21 records cover the same use case |
| AutoValue (Google) | More verbose than records; records are the standard Java solution since Java 16 |

## References

- CLAUDE.md — ⛔ ABSOLUTE RULES → NO LOMBOK — ANYWHERE
- JEP 395 — Records (Java 16, production-ready)
- `.claude/hooks/validate-no-lombok.sh` — Automated enforcement
