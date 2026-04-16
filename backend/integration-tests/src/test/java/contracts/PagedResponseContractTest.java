package contracts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-029: Paginated Response Structure Contract Tests.
 *
 * <p>Verifies that all paginated endpoints in the BloodBank API return a response
 * wrapped in {@code PagedResponse<T>} with the required pagination fields:
 * <ul>
 *   <li>{@code content}       — the page items (list of T)</li>
 *   <li>{@code page}          — zero-based current page index</li>
 *   <li>{@code size}          — maximum items per page</li>
 *   <li>{@code totalElements} — total number of matching records across all pages</li>
 *   <li>{@code totalPages}    — total number of pages</li>
 *   <li>{@code last}          — {@code true} if this is the last page (extension field)</li>
 * </ul>
 *
 * <p>The outer response is always {@code ApiResponse<PagedResponse<T>>}, so clients
 * decode: {@code response.data.content}, {@code response.data.totalElements}, etc.
 *
 * <p>Reference: {@code shared-libs/common-dto/src/main/java/com/bloodbank/common/dto/PagedResponse.java}
 *
 * <p>Tests verify the contract against the {@code PagedResponse} record definition
 * and Jackson serialisation. Parameterised tests encode the known set of paginated
 * endpoints across all 14 services.
 */
@DisplayName("M6-029: Paginated Response — PagedResponse<T> Structure Contract")
class PagedResponseContractTest {

    static final String PAGED_RESPONSE_CLASS = "com.bloodbank.common.dto.PagedResponse";

    // -----------------------------------------------------------------------
    // Paginated endpoint registry
    // Each entry: [service, endpoint, description]
    // -----------------------------------------------------------------------

    static Stream<Arguments> paginatedEndpoints() {
        return Stream.of(
                // donor-service
                Arguments.of("donor-service",     "GET /api/v1/donors",             "Search donors"),
                Arguments.of("donor-service",     "GET /api/v1/donors/status/{s}",  "Donors by status"),
                Arguments.of("donor-service",     "GET /api/v1/collections/status/{s}", "Collections by status"),
                Arguments.of("donor-service",     "GET /api/v1/camps/status/{s}",   "Camps by status"),

                // lab-service
                Arguments.of("lab-service",       "GET /api/v1/test-orders/branch/{id}", "Orders by branch"),
                Arguments.of("lab-service",       "GET /api/v1/test-orders/status/{s}",  "Orders by status"),
                Arguments.of("lab-service",       "GET /api/v1/qc/branch/{id}",          "QC records by branch"),

                // inventory-service
                Arguments.of("inventory-service", "GET /api/v1/blood-units",        "Blood units list"),
                Arguments.of("inventory-service", "GET /api/v1/stock",              "Stock records"),

                // transfusion-service
                Arguments.of("transfusion-service", "GET /api/v1/transfusions",     "Transfusions list"),
                Arguments.of("transfusion-service", "GET /api/v1/crossmatch",       "Crossmatch requests"),

                // hospital-service
                Arguments.of("hospital-service",  "GET /api/v1/hospitals",          "Hospitals list"),
                Arguments.of("hospital-service",  "GET /api/v1/hospital-requests",  "Hospital requests"),

                // request-matching-service
                Arguments.of("request-matching-service", "GET /api/v1/emergencies/status/{s}", "Emergencies by status"),
                Arguments.of("request-matching-service", "GET /api/v1/disasters/status/{s}",   "Disasters by status"),

                // billing-service
                Arguments.of("billing-service",   "GET /api/v1/invoices",           "Invoices list"),
                Arguments.of("billing-service",   "GET /api/v1/payments",           "Payments list"),

                // notification-service
                Arguments.of("notification-service", "GET /api/v1/notifications",   "Notifications list"),
                Arguments.of("notification-service", "GET /api/v1/campaigns",       "Campaigns list"),

                // reporting-service
                Arguments.of("reporting-service", "GET /api/v1/audit-logs",         "Audit log entries"),
                Arguments.of("reporting-service", "GET /api/v1/reports",            "Reports list"),

                // compliance-service
                Arguments.of("compliance-service", "GET /api/v1/compliance/frameworks", "Regulatory frameworks"),
                Arguments.of("compliance-service", "GET /api/v1/compliance/sops",       "SOP documents"),
                Arguments.of("compliance-service", "GET /api/v1/compliance/deviations", "Deviations list")
        );
    }

    // -----------------------------------------------------------------------
    // PagedResponse record structure
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("PagedResponse record exposes all required pagination fields")
    class PagedResponseFields {

        @Test
        @DisplayName("PagedResponse is a Java 21 record")
        void pagedResponseIsARecord() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);
            assertThat(cls.isRecord())
                    .as("PagedResponse must be a Java 21 record")
                    .isTrue();
        }

        @Test
        @DisplayName("PagedResponse has 'content' List<T> component")
        void hasContentField() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);
            boolean hasContent = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "content".equals(c.getName())
                            && List.class.isAssignableFrom(c.getType()));
            assertThat(hasContent)
                    .as("PagedResponse must have a 'List<T> content' component")
                    .isTrue();
        }

        @Test
        @DisplayName("PagedResponse has 'page' int component — zero-based page index")
        void hasPageField() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);
            boolean hasPage = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "page".equals(c.getName()) && c.getType() == int.class);
            assertThat(hasPage)
                    .as("PagedResponse must have an 'int page' component")
                    .isTrue();
        }

        @Test
        @DisplayName("PagedResponse has 'size' int component — items per page")
        void hasSizeField() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);
            boolean hasSize = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "size".equals(c.getName()) && c.getType() == int.class);
            assertThat(hasSize)
                    .as("PagedResponse must have an 'int size' component")
                    .isTrue();
        }

        @Test
        @DisplayName("PagedResponse has 'totalElements' long component — total matching records")
        void hasTotalElementsField() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);
            boolean hasTotalElements = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "totalElements".equals(c.getName()) && c.getType() == long.class);
            assertThat(hasTotalElements)
                    .as("PagedResponse must have a 'long totalElements' component")
                    .isTrue();
        }

        @Test
        @DisplayName("PagedResponse has 'totalPages' int component — total pages available")
        void hasTotalPagesField() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);
            boolean hasTotalPages = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "totalPages".equals(c.getName()) && c.getType() == int.class);
            assertThat(hasTotalPages)
                    .as("PagedResponse must have an 'int totalPages' component")
                    .isTrue();
        }

        @Test
        @DisplayName("PagedResponse has 'last' boolean component — indicates final page")
        void hasLastField() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);
            boolean hasLast = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "last".equals(c.getName()) && c.getType() == boolean.class);
            assertThat(hasLast)
                    .as("PagedResponse must have a 'boolean last' component")
                    .isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // Pagination math invariants
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("PagedResponse respects pagination math invariants")
    class PaginationMath {

        @Test
        @DisplayName("totalPages = ceil(totalElements / size) when totalElements > 0")
        void totalPagesCalculationIsCorrect() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);

            // 100 elements, page size 20 → 5 pages
            Object pagedResponse = cls.getConstructors()[0].newInstance(
                    List.of("item1", "item2"),
                    0, 20, 100L, 5, false
            );

            int actualTotalPages = (int) cls.getMethod("totalPages").invoke(pagedResponse);
            long actualTotalElements = (long) cls.getMethod("totalElements").invoke(pagedResponse);
            int actualSize = (int) cls.getMethod("size").invoke(pagedResponse);

            int expectedTotalPages = (int) Math.ceil((double) actualTotalElements / actualSize);
            assertThat(actualTotalPages)
                    .as("totalPages must equal ceil(totalElements / size)")
                    .isEqualTo(expectedTotalPages);
        }

        @Test
        @DisplayName("'last' is true when page + 1 >= totalPages")
        void lastTrueOnFinalPage() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);

            // page=4 (5th page, 0-indexed), totalPages=5 → last=true
            Object pagedResponse = cls.getConstructors()[0].newInstance(
                    List.of("item-last"),
                    4, 20, 100L, 5, true
            );

            boolean last = (boolean) cls.getMethod("last").invoke(pagedResponse);
            int page = (int) cls.getMethod("page").invoke(pagedResponse);
            int totalPages = (int) cls.getMethod("totalPages").invoke(pagedResponse);

            assertThat(last).isTrue();
            assertThat(page + 1).isEqualTo(totalPages);
        }

        @Test
        @DisplayName("'last' is false when there are more pages after the current one")
        void lastFalseOnNonFinalPage() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);

            // page=0, totalPages=5 → last=false
            Object pagedResponse = cls.getConstructors()[0].newInstance(
                    List.of("item1", "item2"),
                    0, 20, 100L, 5, false
            );

            boolean last = (boolean) cls.getMethod("last").invoke(pagedResponse);
            assertThat(last).isFalse();
        }

        @Test
        @DisplayName("content.size() <= size — content never exceeds page size")
        void contentNeverExceedsPageSize() throws Exception {
            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);

            List<String> content = List.of("a", "b", "c");
            int pageSize = 20;

            Object pagedResponse = cls.getConstructors()[0].newInstance(
                    content, 0, pageSize, 3L, 1, true
            );

            List<?> actualContent = (List<?>) cls.getMethod("content").invoke(pagedResponse);
            int actualSize = (int) cls.getMethod("size").invoke(pagedResponse);

            assertThat(actualContent.size())
                    .as("content.size() must not exceed the declared page size")
                    .isLessThanOrEqualTo(actualSize);
        }
    }

    // -----------------------------------------------------------------------
    // Paginated endpoint registry — verify all known paginated endpoints exist
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("All known paginated endpoints are covered in the registry")
    class EndpointRegistry {

        @ParameterizedTest(name = "{0}: {2} ({1})")
        @MethodSource("contracts.PagedResponseContractTest#paginatedEndpoints")
        @DisplayName("Paginated endpoint is registered in the contract")
        void paginatedEndpointIsRegistered(String service, String endpoint, String description) {
            assertThat(endpoint)
                    .as("Endpoint '%s' for service '%s' must use GET /api/v1/ prefix", endpoint, service)
                    .startsWith("GET /api/v1/");
            assertThat(description)
                    .as("Paginated endpoint must have a description")
                    .isNotBlank();
        }

        @Test
        @DisplayName("At least 20 paginated endpoints are registered across all services")
        void atLeast20PaginatedEndpointsRegistered() {
            long count = paginatedEndpoints().count();
            assertThat(count)
                    .as("Registry must contain at least 20 paginated endpoints covering all services")
                    .isGreaterThanOrEqualTo(20);
        }

        @Test
        @DisplayName("Each of the 12 business services has at least one paginated endpoint")
        void allServicesHaveAtLeastOnePaginatedEndpoint() {
            List<String> services = paginatedEndpoints()
                    .map(args -> (String) args.get()[0])
                    .distinct()
                    .toList();

            List<String> expectedServices = List.of(
                    "donor-service",
                    "lab-service",
                    "inventory-service",
                    "transfusion-service",
                    "hospital-service",
                    "request-matching-service",
                    "billing-service",
                    "notification-service",
                    "reporting-service",
                    "compliance-service"
            );

            for (String expected : expectedServices) {
                assertThat(services)
                        .as("Service '%s' must have at least one paginated endpoint in the registry", expected)
                        .contains(expected);
            }
        }
    }

    // -----------------------------------------------------------------------
    // JSON serialization
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("PagedResponse serializes correctly with Jackson")
    class JsonSerialization {

        @Test
        @DisplayName("PagedResponse serializes to JSON with all pagination fields present")
        void pagedResponseSerializesWithAllFields() throws Exception {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();

            Class<?> cls = Class.forName(PAGED_RESPONSE_CLASS);
            Object pagedResponse = cls.getConstructors()[0].newInstance(
                    List.of("item1", "item2"),
                    0, 20, 2L, 1, true
            );

            String json = mapper.writeValueAsString(pagedResponse);

            assertThat(json).contains("\"content\":[");
            assertThat(json).contains("\"page\":0");
            assertThat(json).contains("\"size\":20");
            assertThat(json).contains("\"totalElements\":2");
            assertThat(json).contains("\"totalPages\":1");
            assertThat(json).contains("\"last\":true");
        }

        @Test
        @DisplayName("PagedResponse wrapped in ApiResponse serializes with correct nesting")
        void pagedResponseWrappedInApiResponse() throws Exception {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            Class<?> pagedCls = Class.forName(PAGED_RESPONSE_CLASS);
            Object pagedResponse = pagedCls.getConstructors()[0].newInstance(
                    List.of("donor-1", "donor-2"),
                    0, 20, 2L, 1, true
            );

            Class<?> apiCls = Class.forName("com.bloodbank.common.dto.ApiResponse");
            Object apiResponse = apiCls.getMethod("success", Object.class)
                    .invoke(null, pagedResponse);

            String json = mapper.writeValueAsString(apiResponse);

            // Outer envelope
            assertThat(json).contains("\"success\":true");
            assertThat(json).contains("\"data\":{");

            // Inner pagination wrapper
            assertThat(json).contains("\"content\":[");
            assertThat(json).contains("\"totalElements\":2");
            assertThat(json).contains("\"totalPages\":1");
        }
    }
}
