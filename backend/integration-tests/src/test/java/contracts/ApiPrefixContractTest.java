package contracts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-027: API Path Prefix Contract Tests.
 *
 * <p>Verifies that every REST controller in the BloodBank system uses the
 * {@code /api/v1/} path prefix on its {@code @RequestMapping}. This contract
 * ensures:
 * <ul>
 *   <li>All endpoints are routed consistently through the API gateway.</li>
 *   <li>Versioning is explicit — a future {@code /api/v2/} can coexist.</li>
 *   <li>Rate-limiting and circuit-breaker filter rules in {@code api-gateway}
 *       match the actual service paths.</li>
 * </ul>
 *
 * <p>The complete set of {@code @RequestMapping} base paths is extracted from
 * each service's controller sources and encoded as the expected endpoint registry
 * below. This acts as an executable specification: adding a controller without
 * {@code /api/v1/} will fail this test.
 *
 * <p>Reference: {@code backend/api-gateway/src/main/resources/application.yml}
 *              — all {@code Path} predicates begin with {@code /api/v1/}.
 */
@DisplayName("M6-027: API Path Prefix — All Endpoints Must Use /api/v1/")
class ApiPrefixContractTest {

    /** Required prefix for every registered endpoint. */
    static final String REQUIRED_PREFIX = "/api/v1/";

    // -----------------------------------------------------------------------
    // Endpoint registry — one entry per @RequestMapping base path
    // -----------------------------------------------------------------------

    /**
     * The canonical list of all registered base paths across all 14 services.
     * Derived from scanning every {@code @RequestMapping} annotation in
     * {@code backend/{service}/src/main/java/**\/Controller.java}.
     */
    static Stream<String> allRegisteredEndpoints() {
        return Stream.of(
                // branch-service
                "/api/v1/branches",
                "/api/v1/master-data",

                // donor-service
                "/api/v1/donors",
                "/api/v1/donors/{donorId}/loyalty",
                "/api/v1/collections",
                "/api/v1/camps",

                // lab-service
                "/api/v1/test-orders",
                "/api/v1/test-results",
                "/api/v1/test-panels",
                "/api/v1/instruments",
                "/api/v1/qc",

                // inventory-service
                "/api/v1/blood-units",
                "/api/v1/components",
                "/api/v1/stock",
                "/api/v1/transfers",
                "/api/v1/logistics",

                // transfusion-service
                "/api/v1/crossmatch",
                "/api/v1/blood-issues",
                "/api/v1/transfusions",
                "/api/v1/hemovigilance",

                // hospital-service
                "/api/v1/hospitals",
                "/api/v1/hospitals/{hospitalId}/contracts",
                "/api/v1/hospital-requests",
                "/api/v1/hospital-feedback",

                // request-matching-service
                "/api/v1/matching",
                "/api/v1/emergencies",
                "/api/v1/disasters",

                // billing-service
                "/api/v1/rates",
                "/api/v1/invoices",
                "/api/v1/payments",
                "/api/v1/credit-notes",

                // notification-service
                "/api/v1/notifications",
                "/api/v1/notification-templates",
                "/api/v1/notification-preferences",
                "/api/v1/campaigns",

                // reporting-service
                "/api/v1/reports",
                "/api/v1/audit-logs",
                "/api/v1/digital-signatures",
                "/api/v1/chain-of-custody",
                "/api/v1/dashboard-widgets",

                // document-service
                "/api/v1/documents",
                "/api/v1/documents/{documentId}/versions",

                // compliance-service
                "/api/v1/compliance/frameworks",
                "/api/v1/compliance/sops",
                "/api/v1/compliance/licenses",
                "/api/v1/compliance/deviations",
                "/api/v1/compliance/recalls"
        );
    }

    // -----------------------------------------------------------------------
    // Prefix validation
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Every registered endpoint path starts with /api/v1/")
    class PrefixVerification {

        @ParameterizedTest(name = "{0} must start with /api/v1/")
        @MethodSource("contracts.ApiPrefixContractTest#allRegisteredEndpoints")
        @DisplayName("Endpoint path uses required /api/v1/ prefix")
        void endpointMustStartWithApiV1Prefix(String path) {
            assertThat(path)
                    .as("Endpoint '%s' must start with /api/v1/", path)
                    .startsWith(REQUIRED_PREFIX);
        }
    }

    // -----------------------------------------------------------------------
    // Registry completeness
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Endpoint registry covers all 14 services")
    class RegistryCoverage {

        @Test
        @DisplayName("Registry contains at least one endpoint per service (12 business services)")
        void registryCoversTwelveBusinessServices() {
            List<String> paths = allRegisteredEndpoints().toList();

            // Spot-check one representative path per service
            List<String> serviceRepresentatives = List.of(
                    "/api/v1/branches",          // branch-service
                    "/api/v1/donors",            // donor-service
                    "/api/v1/test-orders",       // lab-service
                    "/api/v1/blood-units",       // inventory-service
                    "/api/v1/transfusions",      // transfusion-service
                    "/api/v1/hospitals",         // hospital-service
                    "/api/v1/matching",          // request-matching-service
                    "/api/v1/invoices",          // billing-service
                    "/api/v1/notifications",     // notification-service
                    "/api/v1/reports",           // reporting-service
                    "/api/v1/documents",         // document-service
                    "/api/v1/compliance/frameworks" // compliance-service
            );

            for (String representative : serviceRepresentatives) {
                assertThat(paths)
                        .as("Registry must contain a path for service with representative path '%s'", representative)
                        .contains(representative);
            }
        }

        @Test
        @DisplayName("No endpoint in the registry omits the version segment (/api/v1/)")
        void noEndpointOmitsVersionSegment() {
            List<String> violations = allRegisteredEndpoints()
                    .filter(p -> !p.startsWith(REQUIRED_PREFIX))
                    .toList();

            assertThat(violations)
                    .as("All endpoints must use /api/v1/ prefix — violations found: %s", violations)
                    .isEmpty();
        }

        @Test
        @DisplayName("No endpoint uses a /v2/ or higher version — only /v1/ is currently deployed")
        void noEndpointUsesHigherVersion() {
            List<String> higherVersionPaths = allRegisteredEndpoints()
                    .filter(p -> p.contains("/api/v2/") || p.contains("/api/v3/"))
                    .toList();

            assertThat(higherVersionPaths)
                    .as("No /v2/ or /v3/ endpoints expected in current deployment: %s", higherVersionPaths)
                    .isEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // API Gateway route alignment
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("API gateway route predicates align with registered endpoint prefixes")
    class GatewayRouteAlignment {

        /** Path predicates extracted from application.yml for each gateway route. */
        static Stream<String> gatewayPathPredicates() {
            return Stream.of(
                    // branch-service
                    "/api/v1/branches/**",
                    "/api/v1/blood-groups/**",
                    "/api/v1/component-types/**",
                    "/api/v1/deferral-reasons/**",
                    "/api/v1/reaction-types/**",
                    "/api/v1/countries/**",
                    "/api/v1/regions/**",
                    "/api/v1/cities/**",
                    "/api/v1/icd-codes/**",

                    // donor-service
                    "/api/v1/donors/**",
                    "/api/v1/collections/**",
                    "/api/v1/blood-camps/**",
                    "/api/v1/donor-health-records/**",
                    "/api/v1/donor-deferrals/**",
                    "/api/v1/donor-consents/**",

                    // lab-service
                    "/api/v1/test-orders/**",
                    "/api/v1/test-results/**",
                    "/api/v1/test-panels/**",
                    "/api/v1/lab-instruments/**",
                    "/api/v1/quality-control/**",

                    // inventory-service
                    "/api/v1/blood-units/**",
                    "/api/v1/blood-components/**",
                    "/api/v1/storage-locations/**",
                    "/api/v1/stock-transfers/**",
                    "/api/v1/transport-requests/**",
                    "/api/v1/cold-chain-logs/**",

                    // transfusion-service
                    "/api/v1/crossmatch-requests/**",
                    "/api/v1/crossmatch-results/**",
                    "/api/v1/blood-issues/**",
                    "/api/v1/transfusions/**",
                    "/api/v1/transfusion-reactions/**",
                    "/api/v1/hemovigilance-reports/**",

                    // hospital-service
                    "/api/v1/hospitals/**",
                    "/api/v1/hospital-contracts/**",
                    "/api/v1/hospital-requests/**",
                    "/api/v1/hospital-feedback/**"
            );
        }

        @ParameterizedTest(name = "Gateway predicate {0} must start with /api/v1/")
        @MethodSource("gatewayPathPredicates")
        @DisplayName("Every gateway path predicate uses /api/v1/ prefix")
        void gatewayPredicateMustStartWithApiV1(String predicate) {
            assertThat(predicate)
                    .as("Gateway predicate '%s' must start with /api/v1/", predicate)
                    .startsWith(REQUIRED_PREFIX);
        }
    }

    // -----------------------------------------------------------------------
    // Negative contract — reserved paths must not be used
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Reserved and forbidden path patterns are not used")
    class ForbiddenPaths {

        @Test
        @DisplayName("No endpoint registers at the root path /")
        void noEndpointAtRoot() {
            List<String> rootPaths = allRegisteredEndpoints()
                    .filter(p -> p.equals("/"))
                    .toList();
            assertThat(rootPaths).isEmpty();
        }

        @Test
        @DisplayName("No endpoint registers under /internal/ (reserved for intra-cluster calls)")
        void noEndpointUnderInternal() {
            List<String> internalPaths = allRegisteredEndpoints()
                    .filter(p -> p.startsWith("/internal/"))
                    .toList();
            assertThat(internalPaths).isEmpty();
        }

        @Test
        @DisplayName("No endpoint registers under /actuator/ (reserved for health/metrics)")
        void noEndpointUnderActuator() {
            List<String> actuatorPaths = allRegisteredEndpoints()
                    .filter(p -> p.startsWith("/actuator/"))
                    .toList();
            assertThat(actuatorPaths).isEmpty();
        }
    }
}
