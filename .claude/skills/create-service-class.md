# Skill: Create Service Class

Generate a Spring `@Service` class following BloodBank patterns.

## Rules

1. **NO LOMBOK** — No `@RequiredArgsConstructor`, no `@Slf4j`
2. Explicit `LoggerFactory.getLogger()` for logging
3. Constructor injection for ALL dependencies
4. `@Transactional(readOnly = true)` at class level
5. `@Transactional` on individual write methods
6. Use MapStruct mapper for entity ↔ DTO conversion
7. Publish domain events via `ApplicationEventPublisher` or `RabbitTemplate`

## Template

```java
package com.bloodbank.{servicename}.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.{servicename}.dto.*;
import com.bloodbank.{servicename}.entity.{Entity};
import com.bloodbank.{servicename}.mapper.{Entity}Mapper;
import com.bloodbank.{servicename}.repository.{Entity}Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class {Entity}Service {

    private static final Logger log = LoggerFactory.getLogger({Entity}Service.class);

    private final {Entity}Repository {entity}Repository;
    private final {Entity}Mapper {entity}Mapper;
    private final ApplicationEventPublisher eventPublisher;

    public {Entity}Service({Entity}Repository {entity}Repository,
                           {Entity}Mapper {entity}Mapper,
                           ApplicationEventPublisher eventPublisher) {
        this.{entity}Repository = {entity}Repository;
        this.{entity}Mapper = {entity}Mapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public {Entity}Response create({Entity}CreateRequest request) {
        log.info("Creating {entity}: {}", request);
        {Entity} entity = {entity}Mapper.toEntity(request);
        entity = {entity}Repository.save(entity);
        log.info("{Entity} created with ID: {}", entity.getId());
        return {entity}Mapper.toResponse(entity);
    }

    public {Entity}Response getById(UUID id) {
        log.debug("Fetching {entity} by ID: {}", id);
        {Entity} entity = {entity}Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("{Entity}", "id", id));
        return {entity}Mapper.toResponse(entity);
    }

    public PagedResponse<{Entity}Response> list(Pageable pageable) {
        log.debug("Listing {entities}, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<{Entity}> page = {entity}Repository.findAll(pageable);
        return PagedResponse.of(page.map({entity}Mapper::toResponse));
    }

    @Transactional
    public {Entity}Response update(UUID id, {Entity}UpdateRequest request) {
        log.info("Updating {entity} ID: {}", id);
        {Entity} entity = {entity}Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("{Entity}", "id", id));
        {entity}Mapper.updateEntity(request, entity);
        entity = {entity}Repository.save(entity);
        log.info("{Entity} updated: {}", id);
        return {entity}Mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting {entity} ID: {}", id);
        if (!{entity}Repository.existsById(id)) {
            throw new ResourceNotFoundException("{Entity}", "id", id);
        }
        {entity}Repository.deleteById(id);
        log.info("{Entity} deleted: {}", id);
    }
}
```

## Event Publishing Pattern

```java
// For RabbitMQ events (cross-service)
@Transactional
public {Entity}Response complete(UUID id) {
    // ... business logic ...
    eventPublisher.publishEvent(new {Domain}CompletedEvent(
        entity.getId(),
        entity.getRelatedId(),
        entity.getBranchId(),
        Instant.now()
    ));
    return {entity}Mapper.toResponse(entity);
}
```

## Validation

- [ ] No Lombok annotations
- [ ] `LoggerFactory.getLogger()` — not `@Slf4j`
- [ ] Constructor injection — not `@Autowired` or `@RequiredArgsConstructor`
- [ ] `@Transactional(readOnly = true)` at class level
- [ ] `@Transactional` on write methods
- [ ] Uses MapStruct mapper (not manual mapping)
- [ ] Throws `ResourceNotFoundException` for missing entities
- [ ] Logs at appropriate levels (info for writes, debug for reads)
