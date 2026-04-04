# ADR-005: URI-Based API Versioning (/api/v1/)

**Status:** Accepted
**Date:** 2026-04-04
**Decision Makers:** Architecture Team

## Context

BloodBank exposes REST APIs consumed by:

- The Angular 21 frontend (Staff Portal, Hospital Portal, Donor Portal).
- External hospital information systems (HIS) via HL7 FHIR R4.
- Future mobile applications.
- Third-party integrations (payment gateways, SMS providers).

API versioning is required to:

- Allow breaking changes without disrupting existing clients.
- Support multiple API versions during migration periods.
- Provide clear, discoverable API contracts.

## Decision

All REST APIs use **URI-based versioning** with the prefix `/api/v1/`.

### URL Pattern

```
https://{gateway-host}/api/v1/{resource}
```

### Examples

| Service | Endpoint |
|---|---|
| donor-service | `GET /api/v1/donors` |
| donor-service | `POST /api/v1/donors` |
| donor-service | `GET /api/v1/donors/{id}` |
| inventory-service | `GET /api/v1/blood-units` |
| inventory-service | `GET /api/v1/storage-locations` |
| lab-service | `POST /api/v1/test-orders` |
| branch-service | `GET /api/v1/branches` |
| billing-service | `GET /api/v1/invoices` |
| hospital-service | `POST /api/v1/hospital-requests` |
| transfusion-service | `POST /api/v1/crossmatch-requests` |

### Response Envelope

All endpoints return responses wrapped in `ApiResponse<T>`:

```json
{
  "success": true,
  "data": { ... },
  "message": "Donor registered successfully",
  "timestamp": "2026-04-04T14:00:00Z"
}
```

Paginated endpoints use `PagedResponse<T>`:

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "timestamp": "2026-04-04T14:00:00Z"
}
```

### Error Responses

Errors follow RFC 7807 Problem Details:

```json
{
  "type": "https://bloodbank.example.com/errors/validation",
  "title": "Validation Error",
  "status": 400,
  "detail": "Email format is invalid",
  "instance": "/api/v1/donors",
  "timestamp": "2026-04-04T14:00:00Z"
}
```

### Version Migration Strategy

When `v2` is needed in the future:

1. New endpoints are added under `/api/v2/`.
2. Both `/api/v1/` and `/api/v2/` run simultaneously.
3. Clients are given a deprecation notice and migration window.
4. After the migration window, `/api/v1/` is removed.

## Consequences

### Positive

- **Simple and explicit** — The version is visible in every URL. No hidden headers or content negotiation.
- **Cacheable** — URI-based versions work with HTTP caches and CDNs without configuration.
- **Discoverable** — API documentation (OpenAPI/Swagger) clearly shows the version in paths.
- **Gateway routing** — Spring Cloud Gateway can route by URI prefix (`/api/v1/**` → service v1, `/api/v2/**` → service v2).
- **Testing** — Version-specific integration tests target explicit URLs.

### Negative

- **URL changes on version bump** — Clients must update URLs when migrating to v2. Mitigated by: long deprecation windows, client SDKs, and the API gateway handling routing.
- **Duplicate controllers** — If both v1 and v2 coexist, two controller classes are needed. Mitigated by: service layer reuse; only the controller and DTO layer changes between versions.

### Alternatives Considered

| Alternative | Reason Rejected |
|---|---|
| Header-based versioning (`Accept: application/vnd.bloodbank.v1+json`) | Not visible in URLs; harder to test with curl/browser; requires custom content negotiation |
| Query parameter versioning (`?version=1`) | Non-standard; not cacheable by default; version is optional and easy to forget |
| No versioning | Unacceptable for a system with external integrations (hospitals, payment gateways) |
| Media type versioning | Over-complicated for REST APIs; limited tooling support |

## References

- CLAUDE.md — API Conventions → Prefix: `/api/v1/`
- RFC 7807 — Problem Details for HTTP APIs
- OpenAPI 3.0 Specification
