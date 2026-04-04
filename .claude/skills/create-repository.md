# Skill: Create JPA Repository

Generate a Spring Data JPA repository interface following BloodBank patterns.

## Rules

1. Extend `JpaRepository<Entity, UUID>` + `JpaSpecificationExecutor<Entity>`
2. Package: `com.bloodbank.{servicename}.repository`
3. Use Spring Data derived query methods where possible
4. Use `@Query` with JPQL for complex queries
5. Use `Optional<T>` for single-result finders
6. Use `boolean existsBy...()` for existence checks
7. All IDs are `UUID` type

## Template

```java
package com.bloodbank.{servicename}.repository;

import com.bloodbank.{servicename}.entity.{Entity};
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface {Entity}Repository extends JpaRepository<{Entity}, UUID>,
                                            JpaSpecificationExecutor<{Entity}> {

    Optional<{Entity}> findByEmail(String email);

    boolean existsByEmail(String email);

    List<{Entity}> findByBranchId(UUID branchId);

    List<{Entity}> findByStatusAndBranchId({StatusEnum} status, UUID branchId);

    Page<{Entity}> findByBranchId(UUID branchId, Pageable pageable);

    @Query("SELECT e FROM {Entity} e WHERE e.branchId = :branchId AND e.status = :status ORDER BY e.createdAt DESC")
    List<{Entity}> findActiveByBranch(@Param("branchId") UUID branchId, @Param("status") {StatusEnum} status);

    @Query("SELECT COUNT(e) FROM {Entity} e WHERE e.branchId = :branchId AND e.status = :status")
    long countByBranchAndStatus(@Param("branchId") UUID branchId, @Param("status") {StatusEnum} status);
}
```

## Common Query Patterns

- **By branch**: `findByBranchId(UUID branchId)` — all branch-scoped entities
- **By status**: `findByStatus({Enum} status)` — workflow states
- **By date range**: `findByCreatedAtBetween(LocalDateTime start, LocalDateTime end)`
- **Search**: `findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String, String)`
- **Exists check**: `existsByEmailAndBranchId(String email, UUID branchId)`

## Validation

- [ ] Extends both `JpaRepository` and `JpaSpecificationExecutor`
- [ ] Uses `UUID` as ID type
- [ ] `Optional` for single-result finders
- [ ] No Lombok annotations
- [ ] JPQL queries use named parameters with `@Param`
