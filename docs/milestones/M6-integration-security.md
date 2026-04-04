# M6: Integration + Security Testing

**Duration:** 2 weeks
**Dependencies:** M5 (Gateway + Frontend)
**Exit Gate:** Full lifecycle tested end-to-end, security sign-off

---

## Objective

End-to-end integration testing of the complete blood lifecycle and comprehensive security testing.

## Issues

### Full Lifecycle Integration Tests
- [ ] **M6-001**: E2E Test: Donor Registration → Eligibility → Collection → Testing → Component Processing → Inventory
- [ ] **M6-002**: E2E Test: Hospital Request → Match → Cross-Match → Issue → Transfusion → Outcome
- [ ] **M6-003**: E2E Test: Camp Planning → Registration → Collection → Post-Camp Follow-Up
- [ ] **M6-004**: E2E Test: Test Result → Quarantine → Release → Stock Update
- [ ] **M6-005**: E2E Test: Emergency Request → O-Neg Issue → Transfusion → Reaction → Hemovigilance
- [ ] **M6-006**: E2E Test: Recall Initiated → Notification → Investigation → Resolution
- [ ] **M6-007**: E2E Test: Invoice Generation → Payment → Credit Note
- [ ] **M6-008**: E2E Test: Inter-Branch Transfer → Cold Chain Log → Delivery Confirmation
- [ ] **M6-009**: E2E Test: Donor Portal self-registration → Appointment → Digital Card

### Cross-Service Event Flow Tests
- [ ] **M6-010**: Verify all 15 RabbitMQ events flow correctly between publishers and consumers
- [ ] **M6-011**: Test dead letter queue handling (3 retries → DLQ)
- [ ] **M6-012**: Test event idempotency (duplicate event handling)

### Security Testing
- [ ] **M6-013**: Role-based access testing — all 16 roles × all endpoints
- [ ] **M6-014**: Test 4-layer branch data isolation (user cannot see other branch data)
- [ ] **M6-015**: Test break-glass access for DOCTOR emergency override
- [ ] **M6-016**: Test dual authorization workflows (test result release, blood issuing)
- [ ] **M6-017**: OWASP ZAP automated vulnerability scan
- [ ] **M6-018**: Test JWT token validation and expiration
- [ ] **M6-019**: Test CSRF protection
- [ ] **M6-020**: Test SQL injection protection
- [ ] **M6-021**: Test XSS protection
- [ ] **M6-022**: Test role escalation prevention
- [ ] **M6-023**: Test data masking (PII hidden for unauthorized roles)
- [ ] **M6-024**: Test GDPR erasure workflow (anonymization)
- [ ] **M6-025**: Test audit log immutability (cannot UPDATE/DELETE audit_logs)

### API Contract Validation
- [ ] **M6-026**: Validate all endpoints match OpenAPI specs
- [ ] **M6-027**: Test API versioning (/api/v1/)
- [ ] **M6-028**: Test error response format (RFC 7807 Problem Details)
- [ ] **M6-029**: Test pagination responses (PagedResponse)
- [ ] **M6-030**: Test rate limiting behavior

## Deliverables

1. Full vein-to-vein lifecycle integration test suite
2. Security test report (OWASP ZAP)
3. Role access matrix validation report
4. Branch isolation verification report
5. GDPR compliance test report
