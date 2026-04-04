# M8: Performance Testing

**Duration:** 2 weeks
**Dependencies:** M6 (Integration), M7 (Infrastructure)
**Exit Gate:** Performance targets met under load

---

## Objective

Load test the system at 1000 concurrent users and optimize to meet performance targets.

## Issues

### Performance Test Setup
- [ ] **M8-001**: Set up Gatling/k6 test framework
- [ ] **M8-002**: Create test data generators (donors, blood units, hospitals, requests)
- [ ] **M8-003**: Seed test database with realistic volume (100K donors, 500K blood units)
- [ ] **M8-004**: Configure staging environment for performance tests

### Load Tests
- [ ] **M8-005**: Load test: Donor registration — 100 concurrent registrations/sec
- [ ] **M8-006**: Load test: Blood request submission — 50 concurrent requests/sec
- [ ] **M8-007**: Load test: Inventory search — 200 concurrent queries/sec
- [ ] **M8-008**: Load test: Dashboard data loading — 500 concurrent users
- [ ] **M8-009**: Load test: Report generation — 20 concurrent large reports
- [ ] **M8-010**: Load test: Mixed workload — 1000 concurrent users across all services

### Stress Tests
- [ ] **M8-011**: Stress test: Gradual ramp to 2000 concurrent users
- [ ] **M8-012**: Stress test: Spike to 5000 users for 60 seconds
- [ ] **M8-013**: Stress test: Service failure and recovery (kill one service, observe degradation)
- [ ] **M8-014**: Stress test: Database connection pool exhaustion

### Endurance Tests
- [ ] **M8-015**: Endurance test: 500 concurrent users sustained for 4 hours
- [ ] **M8-016**: Monitor memory leaks, connection pool drift, thread count growth

### Optimization
- [ ] **M8-017**: Optimize slow database queries (add indexes, rewrite JPQL)
- [ ] **M8-018**: Optimize Redis caching — hit rates, eviction policies
- [ ] **M8-019**: Tune JVM settings (ZGC, heap size, thread pools)
- [ ] **M8-020**: Tune Hikari connection pool (min/max, timeout, leak detection)
- [ ] **M8-021**: Tune Kubernetes resource limits based on profiling
- [ ] **M8-022**: Implement database query pagination for large result sets
- [ ] **M8-023**: Add missing database indexes for slow queries

### Performance Target Validation
- [ ] **M8-024**: Verify P95 API response time < 200ms
- [ ] **M8-025**: Verify P99 API response time < 500ms
- [ ] **M8-026**: Verify sustained throughput > 500 req/sec
- [ ] **M8-027**: Verify P95 database query time < 100ms
- [ ] **M8-028**: Verify zero-downtime rolling deployment

## Deliverables

1. Performance test suite (Gatling/k6)
2. Load test report with P50/P95/P99 latencies
3. Optimization report documenting all tuning changes
4. Updated K8s resource limits based on profiling
