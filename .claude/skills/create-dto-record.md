# Skill: Create DTO as Java 21 Record

Generate request/response DTOs as Java 21 records with Jakarta Validation.

## Rules

1. **ALL DTOs MUST be Java 21 records** — never use classes with Lombok
2. Request records: include Jakarta Validation annotations
3. Response records: no validation annotations (read-only)
4. Package: `com.bloodbank.{servicename}.dto`
5. Each entity typically has: `{Entity}CreateRequest`, `{Entity}UpdateRequest`, `{Entity}Response`

## Request Record Template

```java
package com.bloodbank.{servicename}.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record {Entity}CreateRequest(
    @NotBlank(message = "{entity.firstName.required}")
    String firstName,

    @NotBlank(message = "{entity.lastName.required}")
    String lastName,

    @NotNull(message = "{entity.type.required}")
    {TypeEnum} type,

    @Email(message = "{entity.email.invalid}")
    String email,

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "{entity.phone.invalid}")
    String phone,

    @NotNull(message = "{entity.branchId.required}")
    UUID branchId
) {}
```

## Update Request Template

```java
public record {Entity}UpdateRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email String email,
    String phone
) {}
```

## Response Record Template

```java
package com.bloodbank.{servicename}.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record {Entity}Response(
    UUID id,
    String firstName,
    String lastName,
    {TypeEnum} type,
    String email,
    String phone,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

## Search/Filter Request Template

```java
public record {Entity}SearchRequest(
    String keyword,
    {StatusEnum} status,
    UUID branchId,
    @Min(0) int page,
    @Min(1) @Max(100) int size,
    String sortBy,
    String sortDirection
) {}
```

## Conventions

- Validation messages reference `messages.properties` keys: `{entity.field.constraint}`
- Use `@Size(min, max)` for string length constraints
- Use `@Past` / `@PastOrPresent` for date-of-birth fields
- Use `@Positive` for numeric IDs and amounts
- Nested validation: `@Valid` on nested records
- Lists: `@NotEmpty List<@Valid {NestedRecord}> items`

## Validation

- [ ] All DTOs are Java 21 records (not classes)
- [ ] No Lombok annotations
- [ ] Request records have Jakarta Validation annotations
- [ ] Validation messages reference properties file keys
- [ ] Response records have no validation annotations
- [ ] All UUID fields typed as `java.util.UUID`
- [ ] All date/time fields typed as `java.time.*`
