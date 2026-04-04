# Skill: Create REST Controller

Generate a Spring REST controller following BloodBank patterns.

## Rules

1. **NO LOMBOK** — No `@RequiredArgsConstructor`, no `@Slf4j`
2. Constructor injection for all dependencies
3. `@PreAuthorize` on EVERY public endpoint method
4. OpenAPI `@Tag`, `@Operation`, `@ApiResponse` annotations
5. Return `ResponseEntity<ApiResponse<T>>` or `ResponseEntity<PagedResponse<T>>`
6. API prefix: `/api/v1/{resource-plural}`
7. Use `@Valid` on all `@RequestBody` parameters

## Template

```java
package com.bloodbank.{servicename}.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.{servicename}.dto.*;
import com.bloodbank.{servicename}.service.{Entity}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/{resources}")
@Tag(name = "{Entity} Management", description = "{Entity} lifecycle operations")
public class {Entity}Controller {

    private final {Entity}Service {entity}Service;

    public {Entity}Controller({Entity}Service {entity}Service) {
        this.{entity}Service = {entity}Service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','{ALLOWED_ROLES}')")
    @Operation(summary = "Create a new {entity}")
    public ResponseEntity<ApiResponse<{Entity}Response>> create(
            @Valid @RequestBody {Entity}CreateRequest request) {
        {Entity}Response response = {entity}Service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "{Entity} created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','{ALLOWED_ROLES}','AUDITOR')")
    @Operation(summary = "Get {entity} by ID")
    public ResponseEntity<ApiResponse<{Entity}Response>> getById(@PathVariable UUID id) {
        {Entity}Response response = {entity}Service.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','{ALLOWED_ROLES}','AUDITOR')")
    @Operation(summary = "List {entities} with pagination")
    public ResponseEntity<PagedResponse<{Entity}Response>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponse<{Entity}Response> response = {entity}Service.list(
                PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy)));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','{ALLOWED_ROLES}')")
    @Operation(summary = "Update {entity}")
    public ResponseEntity<ApiResponse<{Entity}Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody {Entity}UpdateRequest request) {
        {Entity}Response response = {entity}Service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "{Entity} updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Delete {entity}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        {entity}Service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "{Entity} deleted successfully"));
    }
}
```

## Role Matrix Reference

| Service Domain | Allowed Roles |
|---|---|
| Donor | BRANCH_ADMIN, BRANCH_MANAGER, RECEPTIONIST, PHLEBOTOMIST |
| Collection | BRANCH_ADMIN, BRANCH_MANAGER, PHLEBOTOMIST |
| Lab/Testing | BRANCH_ADMIN, LAB_TECHNICIAN |
| Inventory | BRANCH_ADMIN, BRANCH_MANAGER, INVENTORY_MANAGER |
| Transfusion | BRANCH_ADMIN, DOCTOR, NURSE |
| Hospital | BRANCH_ADMIN, BRANCH_MANAGER, HOSPITAL_USER |
| Billing | BRANCH_ADMIN, BRANCH_MANAGER, BILLING_CLERK |
| Compliance | BRANCH_ADMIN, BRANCH_MANAGER, AUDITOR |
| Notification | BRANCH_ADMIN, SYSTEM_ADMIN |
| Reporting | BRANCH_ADMIN, BRANCH_MANAGER, AUDITOR, REGIONAL_ADMIN |
| Branch/Master | SUPER_ADMIN, REGIONAL_ADMIN, BRANCH_ADMIN |
| Camps | BRANCH_ADMIN, BRANCH_MANAGER, CAMP_COORDINATOR |

## Validation

- [ ] `@PreAuthorize` on every public method
- [ ] Constructor injection (no `@Autowired`, no `@RequiredArgsConstructor`)
- [ ] `@Valid` on all `@RequestBody` params
- [ ] Returns `ApiResponse<T>` wrapper
- [ ] API path starts with `/api/v1/`
- [ ] OpenAPI annotations present (`@Tag`, `@Operation`)
- [ ] No Lombok annotations
