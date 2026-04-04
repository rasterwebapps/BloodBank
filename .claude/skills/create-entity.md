# Skill: Create JPA Entity

Generate a JPA entity class following BloodBank project patterns.

## Rules

1. **NO LOMBOK** — Never use any Lombok annotation
2. Extend `BaseEntity` (global data) or `BranchScopedEntity` (branch-scoped data)
3. Use explicit getters and setters for ALL fields
4. Protected no-arg constructor for JPA: `protected ClassName() {}`
5. Public all-args constructor for application use
6. Use `@Enumerated(EnumType.STRING)` for all enums
7. Use `@Column(name = "snake_case", nullable = false/true)` on every field
8. Column names MUST match the Flyway migration table definition

## Branch-Scoped Entity Template

```java
package com.bloodbank.{servicename}.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

@Entity
@Table(name = "{table_name}")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class {EntityName} extends BranchScopedEntity {

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private {StatusEnum} status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_id", nullable = false)
    private {RelatedEntity} related;

    protected {EntityName}() {} // JPA required

    public {EntityName}(String fieldName, {StatusEnum} status) {
        this.fieldName = fieldName;
        this.status = status;
    }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public {StatusEnum} getStatus() { return status; }
    public void setStatus({StatusEnum} status) { this.status = status; }

    public {RelatedEntity} getRelated() { return related; }
    public void setRelated({RelatedEntity} related) { this.related = related; }
}
```

## Global Entity Template (No Branch Scoping)

```java
@Entity
@Table(name = "{table_name}")
public class {EntityName} extends BaseEntity {
    // Same pattern but extends BaseEntity, NO @FilterDef/@Filter
}
```

## Relationship Patterns

- `@ManyToOne(fetch = FetchType.LAZY)` — Always LAZY for to-one
- `@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)` — Cascade on parent side
- `@ManyToMany` — Use a join entity instead (explicit join table entity)
- Never use `FetchType.EAGER` on any relationship

## Validation

- [ ] No Lombok annotations
- [ ] Extends correct base class
- [ ] Branch-scoped entities have `@FilterDef` + `@Filter`
- [ ] Protected no-arg constructor exists
- [ ] All getters/setters are explicit
- [ ] Column names are snake_case matching Flyway migrations
- [ ] All enums use `@Enumerated(EnumType.STRING)`
- [ ] All to-one relationships use `FetchType.LAZY`
