# /project:create-entity

Generate a JPA entity following BloodBank patterns.

## Arguments

- `$ARGUMENTS` should be: `{service-name} {EntityName} {table_name} [branch-scoped|global]`

## Steps

1. Read CLAUDE.md for entity patterns (NO LOMBOK, explicit getters/setters)
2. Read README.md for the entity's table definition and relationships
3. Determine if entity is branch-scoped or global from the arguments
4. Generate entity class in `backend/{service-name}/src/main/java/com/bloodbank/{servicename}/entity/`
5. If branch-scoped: extend `BranchScopedEntity`, add `@FilterDef` + `@Filter`
6. If global: extend `BaseEntity`
7. Add all columns matching the Flyway migration table definition
8. Use `@Enumerated(EnumType.STRING)` for enums
9. Use `FetchType.LAZY` for all `@ManyToOne` relationships
10. Add protected no-arg constructor
11. Add explicit getters and setters for ALL fields
12. Generate corresponding repository interface
13. Generate corresponding DTO records (CreateRequest, UpdateRequest, Response)
14. Generate corresponding MapStruct mapper
