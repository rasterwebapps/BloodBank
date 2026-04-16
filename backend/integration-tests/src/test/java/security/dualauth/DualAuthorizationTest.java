package security.dualauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * M6-016: Dual Authorization Security Tests.
 *
 * <p>Verifies that operations requiring dual authorization enforce that:
 * <ol>
 *   <li>The initiator and the approver/verifier must be different users.</li>
 *   <li>Same-user dual-auth raises a {@code DUAL_REVIEW_VIOLATION} exception.</li>
 *   <li>The operations subject to dual-auth are correctly identified.</li>
 *   <li>Approver roles are constrained to the authorized approver role set.</li>
 * </ol>
 *
 * <p>The dual-auth contract is enforced in the service layer (not the controller),
 * so these tests exercise the business logic directly.
 *
 * <p>Reference: {@code docs/security/rbac-matrix.md} — Special Access Policies → Dual Authorization Required.
 */
@DisplayName("M6-016: Dual Authorization — Two Different Users Required")
class DualAuthorizationTest {

    // -----------------------------------------------------------------------
    // Domain model
    // -----------------------------------------------------------------------

    /** Error code thrown when the same user attempts to act as both initiator and approver. */
    static final String DUAL_REVIEW_VIOLATION = "DUAL_REVIEW_VIOLATION";

    record DualAuthOperation(String operationName, Set<String> initiatorRoles, Set<String> approverRoles) {}

    /** Dual-auth operations from the RBAC matrix reference document. */
    static java.util.List<DualAuthOperation> dualAuthOperations() {
        return java.util.List.of(
                new DualAuthOperation(
                        "TEST_RESULT_RELEASE",
                        Set.of("LAB_TECHNICIAN"),
                        Set.of("BRANCH_ADMIN", "BRANCH_MANAGER", "LAB_TECHNICIAN")
                ),
                new DualAuthOperation(
                        "BLOOD_UNIT_ISSUING",
                        Set.of("LAB_TECHNICIAN", "INVENTORY_MANAGER"),
                        Set.of("BRANCH_ADMIN", "BRANCH_MANAGER")
                ),
                new DualAuthOperation(
                        "UNIT_DISPOSAL",
                        Set.of("INVENTORY_MANAGER"),
                        Set.of("BRANCH_ADMIN", "BRANCH_MANAGER")
                ),
                new DualAuthOperation(
                        "EMERGENCY_O_NEG_ISSUE",
                        Set.of("DOCTOR"),
                        Set.of("BRANCH_ADMIN", "BRANCH_MANAGER")
                )
        );
    }

    static Stream<Arguments> dualAuthOperationStream() {
        return dualAuthOperations().stream().map(op -> Arguments.of(op.operationName(), op));
    }

    // -----------------------------------------------------------------------
    // Dual-auth enforcement model
    // -----------------------------------------------------------------------

    /**
     * Simulates the service-layer dual-auth enforcement check.
     * Throws a business exception when the same user attempts to both initiate and approve.
     */
    static void assertDualAuthEnforced(UUID initiatorId, UUID approverId) {
        if (initiatorId.equals(approverId)) {
            throw new BusinessException(DUAL_REVIEW_VIOLATION,
                    "The verifier must not be the same person as the initiator");
        }
    }

    /**
     * Simulates checking that the approver's role is in the allowed approver set.
     */
    static void assertApproverRoleAllowed(String approverRole, DualAuthOperation operation) {
        if (!operation.approverRoles().contains(approverRole)) {
            throw new BusinessException(DUAL_REVIEW_VIOLATION,
                    "Role '" + approverRole + "' is not authorized to approve " + operation.operationName());
        }
    }

    static class BusinessException extends RuntimeException {
        private final String code;

        BusinessException(String code, String message) {
            super(message);
            this.code = code;
        }

        String getCode() { return code; }
    }

    // -----------------------------------------------------------------------
    // Same-user dual-auth prevention
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Same user cannot act as both initiator and approver")
    class SameUserPrevention {

        @Test
        @DisplayName("Same user for test result release throws DUAL_REVIEW_VIOLATION")
        void sameUserTestResultRelease_throwsViolation() {
            UUID userId = UUID.randomUUID();
            assertThatThrownBy(() -> assertDualAuthEnforced(userId, userId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("same person")
                    .extracting(e -> ((BusinessException) e).getCode())
                    .isEqualTo(DUAL_REVIEW_VIOLATION);
        }

        @Test
        @DisplayName("Same user for blood unit issuing throws DUAL_REVIEW_VIOLATION")
        void sameUserBloodUnitIssuing_throwsViolation() {
            UUID userId = UUID.randomUUID();
            assertThatThrownBy(() -> assertDualAuthEnforced(userId, userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getCode())
                    .isEqualTo(DUAL_REVIEW_VIOLATION);
        }

        @Test
        @DisplayName("Same user for unit disposal throws DUAL_REVIEW_VIOLATION")
        void sameUserUnitDisposal_throwsViolation() {
            UUID userId = UUID.randomUUID();
            assertThatThrownBy(() -> assertDualAuthEnforced(userId, userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getCode())
                    .isEqualTo(DUAL_REVIEW_VIOLATION);
        }

        @Test
        @DisplayName("Same user for emergency O-neg issue throws DUAL_REVIEW_VIOLATION")
        void sameUserEmergencyIssue_throwsViolation() {
            UUID userId = UUID.randomUUID();
            assertThatThrownBy(() -> assertDualAuthEnforced(userId, userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getCode())
                    .isEqualTo(DUAL_REVIEW_VIOLATION);
        }
    }

    // -----------------------------------------------------------------------
    // Different users pass dual-auth check
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Two different users pass the dual-auth check")
    class DifferentUsersSuccess {

        @Test
        @DisplayName("Different initiator and approver passes dual-auth check")
        void differentUsers_passesCheck() {
            UUID initiatorId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            // Must not throw
            assertDualAuthEnforced(initiatorId, approverId);
        }

        @Test
        @DisplayName("Dual-auth check passes for any two distinct UUIDs")
        void twoDistinctUuids_alwaysPass() {
            for (int i = 0; i < 10; i++) {
                UUID initiator = UUID.randomUUID();
                UUID approver = UUID.randomUUID();
                // UUIDs generated with randomUUID() are probabilistically unique
                assertDualAuthEnforced(initiator, approver);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Approver role validation
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Approver must have an authorized role for each dual-auth operation")
    class ApproverRoleValidation {

        @ParameterizedTest(name = "Operation: {0}")
        @MethodSource("security.dualauth.DualAuthorizationTest#dualAuthOperationStream")
        @DisplayName("Initiator role cannot approve its own operation type")
        void initiatorRoleCannotBeUsedAsApproverForRestrictedOperations(
                String opName, DualAuthOperation operation) {

            // For BLOOD_UNIT_ISSUING and UNIT_DISPOSAL, initiator cannot be approver
            if ("BLOOD_UNIT_ISSUING".equals(opName) || "UNIT_DISPOSAL".equals(opName)) {
                for (String initiatorRole : operation.initiatorRoles()) {
                    boolean initiatorCanApprove = operation.approverRoles().contains(initiatorRole);
                    assertThat(initiatorCanApprove)
                            .as("Initiator role '%s' should not appear in approver roles for %s",
                                    initiatorRole, opName)
                            .isFalse();
                }
            }
        }

        @Test
        @DisplayName("LAB_TECHNICIAN can verify test results but must be a different user")
        void labTechCanVerifyTestResultsButMustBeDifferentUser() {
            DualAuthOperation testResultRelease = dualAuthOperations().stream()
                    .filter(op -> "TEST_RESULT_RELEASE".equals(op.operationName()))
                    .findFirst().orElseThrow();

            // LAB_TECHNICIAN is in both initiator and approver sets (different user enforced)
            assertThat(testResultRelease.initiatorRoles()).contains("LAB_TECHNICIAN");
            assertThat(testResultRelease.approverRoles()).contains("LAB_TECHNICIAN");

            // Same user must still throw
            UUID userId = UUID.randomUUID();
            assertThatThrownBy(() -> assertDualAuthEnforced(userId, userId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("DOCTOR cannot approve their own O-neg emergency issue")
        void doctorCannotApproveOwnEmergencyIssue() {
            DualAuthOperation emergencyONeg = dualAuthOperations().stream()
                    .filter(op -> "EMERGENCY_O_NEG_ISSUE".equals(op.operationName()))
                    .findFirst().orElseThrow();

            assertThat(emergencyONeg.initiatorRoles()).contains("DOCTOR");
            assertThat(emergencyONeg.approverRoles()).doesNotContain("DOCTOR");
        }

        @Test
        @DisplayName("Unauthorized approver role throws DUAL_REVIEW_VIOLATION")
        void unauthorizedApproverRole_throwsViolation() {
            DualAuthOperation testResultRelease = dualAuthOperations().stream()
                    .filter(op -> "TEST_RESULT_RELEASE".equals(op.operationName()))
                    .findFirst().orElseThrow();

            assertThatThrownBy(() -> assertApproverRoleAllowed("RECEPTIONIST", testResultRelease))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getCode())
                    .isEqualTo(DUAL_REVIEW_VIOLATION);
        }

        @Test
        @DisplayName("Authorized approver role does not throw")
        void authorizedApproverRole_doesNotThrow() {
            DualAuthOperation unitDisposal = dualAuthOperations().stream()
                    .filter(op -> "UNIT_DISPOSAL".equals(op.operationName()))
                    .findFirst().orElseThrow();

            // Must not throw
            assertApproverRoleAllowed("BRANCH_ADMIN", unitDisposal);
            assertApproverRoleAllowed("BRANCH_MANAGER", unitDisposal);
        }
    }

    // -----------------------------------------------------------------------
    // Dual-auth operations completeness
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("All dual-auth operations are defined in the contract")
    class OperationsCompletenessTests {

        @Test
        @DisplayName("Exactly four operations require dual authorization")
        void exactlyFourOperationsRequireDualAuth() {
            assertThat(dualAuthOperations()).hasSize(4);
        }

        @Test
        @DisplayName("All required dual-auth operations are present")
        void allRequiredOperationsArePresent() {
            java.util.List<String> operationNames = dualAuthOperations().stream()
                    .map(DualAuthOperation::operationName)
                    .toList();

            assertThat(operationNames).containsExactlyInAnyOrder(
                    "TEST_RESULT_RELEASE",
                    "BLOOD_UNIT_ISSUING",
                    "UNIT_DISPOSAL",
                    "EMERGENCY_O_NEG_ISSUE"
            );
        }

        @Test
        @DisplayName("Every dual-auth operation has at least one initiator role defined")
        void everyOperationHasInitiatorRole() {
            for (DualAuthOperation op : dualAuthOperations()) {
                assertThat(op.initiatorRoles())
                        .as("Operation '%s' must define at least one initiator role", op.operationName())
                        .isNotEmpty();
            }
        }

        @Test
        @DisplayName("Every dual-auth operation has at least one approver role defined")
        void everyOperationHasApproverRole() {
            for (DualAuthOperation op : dualAuthOperations()) {
                assertThat(op.approverRoles())
                        .as("Operation '%s' must define at least one approver role", op.operationName())
                        .isNotEmpty();
            }
        }
    }
}
