package security.owasp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-017: OWASP ZAP Scan Configuration Tests.
 *
 * <p>Verifies that the OWASP ZAP automation framework scan configuration
 * ({@code security/owasp-zap-scan.yaml}) exists, is valid, and covers all
 * required BloodBank API endpoint groups.
 *
 * <p>The YAML config file is located at:
 * {@code backend/integration-tests/src/test/resources/security/owasp-zap-scan.yaml}
 *
 * <p>To execute the scan against a running environment:
 * <pre>
 *   docker run --network host \
 *     -v $(pwd)/backend/integration-tests/src/test/resources/security:/zap/wrk/:rw \
 *     ghcr.io/zaproxy/zaproxy:stable \
 *     zap.sh -cmd -autorun /zap/wrk/owasp-zap-scan.yaml
 * </pre>
 */
@DisplayName("M6-017: OWASP ZAP Scan Configuration")
class OwaspZapScanConfigTest {

    private static final String CONFIG_RESOURCE = "/security/owasp-zap-scan.yaml";

    private String loadConfig() {
        try (InputStream is = getClass().getResourceAsStream(CONFIG_RESOURCE)) {
            assertThat(is)
                    .as("OWASP ZAP config not found at classpath:%s", CONFIG_RESOURCE)
                    .isNotNull();
            try (Scanner scanner = new Scanner(is)) {
                scanner.useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load ZAP config", e);
        }
    }

    // -----------------------------------------------------------------------
    // Config file existence and structure
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Config file existence and structure")
    class ConfigFileStructure {

        @Test
        @DisplayName("OWASP ZAP config file exists on the test classpath")
        void configFileExists() {
            InputStream is = getClass().getResourceAsStream(CONFIG_RESOURCE);
            assertThat(is).as("OWASP ZAP scan config must exist at %s", CONFIG_RESOURCE).isNotNull();
        }

        @Test
        @DisplayName("Config file is not empty")
        void configFileIsNotEmpty() {
            String config = loadConfig();
            assertThat(config).isNotBlank();
        }

        @Test
        @DisplayName("Config file declares at least one context")
        void configFileDeclaresContext() {
            String config = loadConfig();
            assertThat(config).contains("contexts:");
            assertThat(config).contains("BloodBank API");
        }

        @Test
        @DisplayName("Config file specifies the API base URL")
        void configFileSpecifiesApiUrl() {
            String config = loadConfig();
            assertThat(config).contains("http://localhost:8080");
            assertThat(config).contains("/api/v1/");
        }

        @Test
        @DisplayName("Config file includes active scan job for injection/XSS checks")
        void configIncludesActiveScan() {
            String config = loadConfig();
            assertThat(config).contains("type: activeScan");
        }

        @Test
        @DisplayName("Config file includes report generation job")
        void configIncludesReportGeneration() {
            String config = loadConfig();
            assertThat(config).contains("type: report");
        }

        @Test
        @DisplayName("Config file includes passive scan configuration")
        void configIncludesPassiveScan() {
            String config = loadConfig();
            assertThat(config).contains("passiveScan");
        }

        @Test
        @DisplayName("Config file includes authentication configuration")
        void configIncludesAuthentication() {
            String config = loadConfig();
            assertThat(config).contains("authentication:");
            assertThat(config).contains("bearer");
        }
    }

    // -----------------------------------------------------------------------
    // API endpoint coverage
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("All critical API endpoint groups are included in scan scope")
    class EndpointCoverageTests {

        private final String config = loadConfig();

        @Test
        @DisplayName("Donor management endpoints are in scan scope")
        void donorEndpointsInScope() {
            assertThat(config).contains("/api/v1/donors");
        }

        @Test
        @DisplayName("Blood collection endpoints are in scan scope")
        void collectionEndpointsInScope() {
            assertThat(config).contains("/api/v1/collections");
        }

        @Test
        @DisplayName("Lab testing endpoints are in scan scope")
        void labEndpointsInScope() {
            assertThat(config).contains("/api/v1/test-orders");
            assertThat(config).contains("/api/v1/test-results");
        }

        @Test
        @DisplayName("Inventory management endpoints are in scan scope")
        void inventoryEndpointsInScope() {
            assertThat(config).contains("/api/v1/blood-units");
            assertThat(config).contains("/api/v1/blood-components");
        }

        @Test
        @DisplayName("Cross-match and issuing endpoints are in scan scope")
        void crossMatchEndpointsInScope() {
            assertThat(config).contains("/api/v1/crossmatch-requests");
            assertThat(config).contains("/api/v1/blood-issues");
        }

        @Test
        @DisplayName("Transfusion endpoints are in scan scope")
        void transfusionEndpointsInScope() {
            assertThat(config).contains("/api/v1/transfusions");
            assertThat(config).contains("/api/v1/hemovigilance-reports");
        }

        @Test
        @DisplayName("Branch management endpoints are in scan scope")
        void branchEndpointsInScope() {
            assertThat(config).contains("/api/v1/branches");
        }

        @Test
        @DisplayName("Billing endpoints are in scan scope")
        void billingEndpointsInScope() {
            assertThat(config).contains("/api/v1/invoices");
            assertThat(config).contains("/api/v1/payments");
        }

        @Test
        @DisplayName("Hospital management endpoints are in scan scope")
        void hospitalEndpointsInScope() {
            assertThat(config).contains("/api/v1/hospitals");
            assertThat(config).contains("/api/v1/hospital-requests");
        }

        @Test
        @DisplayName("Compliance endpoints are in scan scope")
        void complianceEndpointsInScope() {
            assertThat(config).contains("/api/v1/regulatory-frameworks");
            assertThat(config).contains("/api/v1/recall-records");
        }

        @Test
        @DisplayName("Audit and reporting endpoints are in scan scope")
        void auditEndpointsInScope() {
            assertThat(config).contains("/api/v1/audit-logs");
            assertThat(config).contains("/api/v1/reports");
        }

        @Test
        @DisplayName("Emergency request endpoints are in scan scope")
        void emergencyEndpointsInScope() {
            assertThat(config).contains("/api/v1/emergency-requests");
            assertThat(config).contains("/api/v1/disaster-events");
        }

        @Test
        @DisplayName("Document management endpoints are in scan scope")
        void documentEndpointsInScope() {
            assertThat(config).contains("/api/v1/documents");
        }
    }

    // -----------------------------------------------------------------------
    // Security rule coverage
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Critical security rules are enabled in the scan policy")
    class SecurityRuleCoverage {

        private final String config = loadConfig();

        @Test
        @DisplayName("SQL injection rules are enabled in active scan policy")
        void sqlInjectionRulesEnabled() {
            assertThat(config).contains("SQL Injection");
        }

        @Test
        @DisplayName("XSS rules are enabled in active scan policy")
        void xssRulesEnabled() {
            assertThat(config).contains("Cross Site Scripting");
        }

        @Test
        @DisplayName("Path traversal rules are enabled")
        void pathTraversalRulesEnabled() {
            assertThat(config).contains("Path Traversal");
        }

        @Test
        @DisplayName("Command injection rules are enabled")
        void commandInjectionRulesEnabled() {
            assertThat(config).contains("Remote OS Command Injection");
        }

        @Test
        @DisplayName("Authentication bypass checks are included")
        void authBypassChecksIncluded() {
            assertThat(config).contains("auth-bypass-checks");
            assertThat(config).contains("httpStatusCode: 401");
        }
    }

    // -----------------------------------------------------------------------
    // Multiple test users defined for RBAC coverage
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Multiple test users are defined for RBAC coverage")
    class TestUserCoverage {

        private final String config = loadConfig();

        @Test
        @DisplayName("Admin-level user is defined for full-access scans")
        void adminUserIsDefined() {
            assertThat(config).contains("super_admin");
        }

        @Test
        @DisplayName("Branch admin user is defined for branch-scoped scans")
        void branchAdminUserIsDefined() {
            assertThat(config).contains("branch_admin");
        }

        @Test
        @DisplayName("Clinical user (doctor) is defined for clinical endpoint scans")
        void doctorUserIsDefined() {
            assertThat(config).contains("doctor");
        }

        @Test
        @DisplayName("Auditor user is defined for read-only access verification")
        void auditorUserIsDefined() {
            assertThat(config).contains("auditor");
        }

        @Test
        @DisplayName("At least 5 distinct test users are defined")
        void atLeastFiveTestUsersDefined() {
            List<String> userNames = List.of(
                    "super_admin", "branch_admin", "doctor", "lab_technician", "auditor", "donor");
            long presentCount = userNames.stream().filter(config::contains).count();
            assertThat(presentCount)
                    .as("Expected at least 5 test users in ZAP config, found %d", presentCount)
                    .isGreaterThanOrEqualTo(5);
        }
    }
}
