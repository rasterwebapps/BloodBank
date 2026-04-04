# Skill: Create MapStruct Mapper

Generate a MapStruct mapper interface following BloodBank patterns.

## Rules

1. `@Mapper(componentModel = "spring")` — Spring-managed bean
2. Define: `toEntity()`, `toResponse()`, `toResponseList()`, `updateEntity()`
3. Use `@Mapping` for field name mismatches
4. Use `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` on update methods
5. Package: `com.bloodbank.{servicename}.mapper`

## Template

```java
package com.bloodbank.{servicename}.mapper;

import com.bloodbank.{servicename}.dto.*;
import com.bloodbank.{servicename}.entity.{Entity};
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface {Entity}Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    {Entity} toEntity({Entity}CreateRequest request);

    {Entity}Response toResponse({Entity} entity);

    List<{Entity}Response> toResponseList(List<{Entity}> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity({Entity}UpdateRequest request, @MappingTarget {Entity} entity);
}
```

## Common Mapping Patterns

### Enum Mapping
```java
@Mapping(target = "status", expression = "java({StatusEnum}.ACTIVE)")
{Entity} toEntity({Entity}CreateRequest request);
```

### Nested Object Mapping
```java
@Mapping(target = "branchName", source = "branch.name")
{Entity}Response toResponse({Entity} entity);
```

### Custom Method
```java
default {Entity}SummaryResponse toSummary({Entity} entity) {
    return new {Entity}SummaryResponse(entity.getId(), entity.getName(), entity.getStatus());
}
```

## Validation

- [ ] `@Mapper(componentModel = "spring")`
- [ ] Base entity fields (id, timestamps, version) ignored on create/update
- [ ] `@MappingTarget` used on update methods
- [ ] `NullValuePropertyMappingStrategy.IGNORE` on update
- [ ] No Lombok annotations
