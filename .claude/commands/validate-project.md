# /project:validate-project

Run all validation checks on the BloodBank project.

## Steps

1. Run `.claude/hooks/validate-no-lombok.sh` to check for Lombok violations
2. Run `.claude/hooks/validate-code-patterns.sh` to check code conventions
3. Check that all DTOs in `*/dto/` directories are Java 21 records
4. Check that all entities extend `BaseEntity` or `BranchScopedEntity`
5. Check that all controllers have `@PreAuthorize` on every public method
6. Check that all services use constructor injection (no `@Autowired`)
7. Check that `spring.flyway.enabled=false` in all service application.yml files
8. Check that no `lombok` dependency exists in any `build.gradle.kts`
9. Report a summary of all findings
