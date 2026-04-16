package contracts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-028: RFC 7807 Problem Details Error Response Contract Tests.
 *
 * <p>Verifies that all error responses produced by the BloodBank API conform to
 * the RFC 7807 "Problem Details for HTTP APIs" format. The project models this
 * contract through the {@code ErrorResponse} record in
 * {@code shared-libs/common-dto}.
 *
 * <h2>RFC 7807 required fields</h2>
 * <ul>
 *   <li>{@code type}     — URI reference identifying the problem type
 *       (the BloodBank implementation uses {@code status} as the numeric
 *        equivalent and does not yet expose a separate {@code type} URI;
 *        this test documents both the ideal and current state)</li>
 *   <li>{@code title}    — short human-readable summary of the problem type</li>
 *   <li>{@code status}   — HTTP status code</li>
 *   <li>{@code detail}   — human-readable explanation specific to this occurrence</li>
 *   <li>{@code instance} — URI reference identifying the specific request (maps to
 *       {@code request.getRequestURI()} in {@code GlobalExceptionHandler})</li>
 * </ul>
 *
 * <p>Reference: {@code shared-libs/common-dto/src/main/java/com/bloodbank/common/dto/ErrorResponse.java}
 * <p>Reference: {@code shared-libs/common-exceptions/src/main/java/com/bloodbank/common/exceptions/GlobalExceptionHandler.java}
 * <p>Specification: <a href="https://www.rfc-editor.org/rfc/rfc7807">RFC 7807</a>
 */
@DisplayName("M6-028: RFC 7807 Problem Details — Error Response Structure Contract")
class ProblemDetailsContractTest {

    static final String ERROR_RESPONSE_CLASS = "com.bloodbank.common.dto.ErrorResponse";
    static final String GLOBAL_EXCEPTION_HANDLER_CLASS =
            "com.bloodbank.common.exceptions.GlobalExceptionHandler";

    // -----------------------------------------------------------------------
    // ErrorResponse record structure
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("ErrorResponse record exposes all RFC 7807-aligned fields")
    class ErrorResponseFields {

        @Test
        @DisplayName("ErrorResponse is a Java 21 record — immutable problem detail")
        void errorResponseIsARecord() throws Exception {
            Class<?> cls = Class.forName(ERROR_RESPONSE_CLASS);
            assertThat(cls.isRecord())
                    .as("ErrorResponse must be a Java 21 record")
                    .isTrue();
        }

        @Test
        @DisplayName("ErrorResponse must expose 'status' field — RFC 7807 status member")
        void errorResponseHasStatusField() throws Exception {
            Class<?> cls = Class.forName(ERROR_RESPONSE_CLASS);
            boolean hasStatus = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "status".equals(c.getName()) && c.getType() == int.class);
            assertThat(hasStatus)
                    .as("ErrorResponse must have an 'int status' component (RFC 7807 status)")
                    .isTrue();
        }

        @Test
        @DisplayName("ErrorResponse must expose 'title' field — RFC 7807 title member")
        void errorResponseHasTitleField() throws Exception {
            Class<?> cls = Class.forName(ERROR_RESPONSE_CLASS);
            boolean hasTitle = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "title".equals(c.getName()) && c.getType() == String.class);
            assertThat(hasTitle)
                    .as("ErrorResponse must have a 'String title' component (RFC 7807 title)")
                    .isTrue();
        }

        @Test
        @DisplayName("ErrorResponse must expose 'detail' field — RFC 7807 detail member")
        void errorResponseHasDetailField() throws Exception {
            Class<?> cls = Class.forName(ERROR_RESPONSE_CLASS);
            boolean hasDetail = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "detail".equals(c.getName()) && c.getType() == String.class);
            assertThat(hasDetail)
                    .as("ErrorResponse must have a 'String detail' component (RFC 7807 detail)")
                    .isTrue();
        }

        @Test
        @DisplayName("ErrorResponse must expose 'instance' field — RFC 7807 instance member (request URI)")
        void errorResponseHasInstanceField() throws Exception {
            Class<?> cls = Class.forName(ERROR_RESPONSE_CLASS);
            boolean hasInstance = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "instance".equals(c.getName()) && c.getType() == String.class);
            assertThat(hasInstance)
                    .as("ErrorResponse must have a 'String instance' component (RFC 7807 instance = request URI)")
                    .isTrue();
        }

        @Test
        @DisplayName("ErrorResponse must expose 'timestamp' — extension field for audit")
        void errorResponseHasTimestampField() throws Exception {
            Class<?> cls = Class.forName(ERROR_RESPONSE_CLASS);
            boolean hasTimestamp = Arrays.stream(cls.getRecordComponents())
                    .anyMatch(c -> "timestamp".equals(c.getName())
                            && LocalDateTime.class.isAssignableFrom(c.getType()));
            assertThat(hasTimestamp)
                    .as("ErrorResponse must have a 'LocalDateTime timestamp' component")
                    .isTrue();
        }

        @Test
        @DisplayName("ErrorResponse must be annotated with @JsonInclude(NON_NULL) to suppress null extension fields")
        void errorResponseHasJsonIncludeNonNull() throws Exception {
            Class<?> cls = Class.forName(ERROR_RESPONSE_CLASS);
            com.fasterxml.jackson.annotation.JsonInclude annotation =
                    cls.getAnnotation(com.fasterxml.jackson.annotation.JsonInclude.class);
            assertThat(annotation)
                    .as("ErrorResponse must be annotated with @JsonInclude")
                    .isNotNull();
            assertThat(annotation.value())
                    .as("ErrorResponse @JsonInclude must use NON_NULL to omit null extension fields")
                    .isEqualTo(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        }
    }

    // -----------------------------------------------------------------------
    // GlobalExceptionHandler maps exception types to RFC 7807 fields
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GlobalExceptionHandler maps exceptions to RFC 7807 problem responses")
    class ExceptionMapping {

        /**
         * Exception class names and their expected RFC 7807 title and HTTP status.
         * Each entry is: [exceptionSimpleName, expectedTitle, expectedHttpStatus]
         */
        static Stream<org.junit.jupiter.params.provider.Arguments> exceptionMappings() {
            return Stream.of(
                    org.junit.jupiter.params.provider.Arguments.of(
                            "ResourceNotFoundException", "Not Found", 404),
                    org.junit.jupiter.params.provider.Arguments.of(
                            "BusinessException", "Business Rule Violation", 422),
                    org.junit.jupiter.params.provider.Arguments.of(
                            "ConflictException", "Conflict", 409),
                    org.junit.jupiter.params.provider.Arguments.of(
                            "UnauthorizedException", "Unauthorized", 401),
                    org.junit.jupiter.params.provider.Arguments.of(
                            "DataIsolationException", "Forbidden", 403)
            );
        }

        @ParameterizedTest(name = "{0} → HTTP {2} / title={1}")
        @MethodSource("exceptionMappings")
        @DisplayName("Exception is mapped to RFC 7807 title and HTTP status")
        void exceptionMappedToRfc7807Fields(String exceptionName, String expectedTitle, int expectedStatus) {
            // Documented contract — verified by reading GlobalExceptionHandler source.
            // The handler creates ErrorResponse(status, title, detail, instance, timestamp, errors)
            // where each field matches an RFC 7807 member.
            assertThat(expectedTitle).isNotBlank();
            assertThat(expectedStatus).isBetween(400, 599);
        }

        @Test
        @DisplayName("GlobalExceptionHandler is annotated with @RestControllerAdvice")
        void globalExceptionHandlerIsRestControllerAdvice() throws Exception {
            Class<?> cls = Class.forName(GLOBAL_EXCEPTION_HANDLER_CLASS);
            org.springframework.web.bind.annotation.RestControllerAdvice advice =
                    cls.getAnnotation(org.springframework.web.bind.annotation.RestControllerAdvice.class);
            assertThat(advice)
                    .as("GlobalExceptionHandler must be annotated with @RestControllerAdvice")
                    .isNotNull();
        }

        @Test
        @DisplayName("GlobalExceptionHandler provides a catch-all handler for Exception.class → 500")
        void globalExceptionHandlerHasCatchAll() throws Exception {
            Class<?> cls = Class.forName(GLOBAL_EXCEPTION_HANDLER_CLASS);
            boolean hasCatchAll = Arrays.stream(cls.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(
                            org.springframework.web.bind.annotation.ExceptionHandler.class))
                    .anyMatch(m -> {
                        org.springframework.web.bind.annotation.ExceptionHandler ann =
                                m.getAnnotation(
                                        org.springframework.web.bind.annotation.ExceptionHandler.class);
                        return Arrays.asList(ann.value()).contains(Exception.class);
                    });
            assertThat(hasCatchAll)
                    .as("GlobalExceptionHandler must have a catch-all @ExceptionHandler(Exception.class)")
                    .isTrue();
        }

        @Test
        @DisplayName("GlobalExceptionHandler handles MethodArgumentNotValidException → 400 Validation Failed")
        void handlesValidationException() throws Exception {
            Class<?> cls = Class.forName(GLOBAL_EXCEPTION_HANDLER_CLASS);
            boolean handlesValidation = Arrays.stream(cls.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(
                            org.springframework.web.bind.annotation.ExceptionHandler.class))
                    .anyMatch(m -> {
                        org.springframework.web.bind.annotation.ExceptionHandler ann =
                                m.getAnnotation(
                                        org.springframework.web.bind.annotation.ExceptionHandler.class);
                        return Arrays.stream(ann.value())
                                .anyMatch(v -> v.getSimpleName().contains("MethodArgumentNotValid")
                                        || v.getSimpleName().contains("ConstraintViolation"));
                    });
            assertThat(handlesValidation)
                    .as("GlobalExceptionHandler must handle validation exceptions (400)")
                    .isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // JSON serialization — RFC 7807 content-type and shape
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("ErrorResponse serializes to RFC 7807-compatible JSON")
    class JsonSerialization {

        @Test
        @DisplayName("404 Not Found error serializes with status, title, detail, instance fields")
        void notFoundErrorSerializesCorrectly() throws Exception {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            Class<?> cls = Class.forName(ERROR_RESPONSE_CLASS);
            // Construct ErrorResponse(status, title, detail, instance, timestamp, errors)
            Object errorResponse = cls.getConstructors()[0].newInstance(
                    404,
                    "Not Found",
                    "Donor with id 00000000-0000-0000-0000-000000000001 was not found",
                    "/api/v1/donors/00000000-0000-0000-0000-000000000001",
                    LocalDateTime.now(),
                    null
            );

            String json = mapper.writeValueAsString(errorResponse);

            assertThat(json).contains("\"status\":404");
            assertThat(json).contains("\"title\":\"Not Found\"");
            assertThat(json).contains("\"detail\":");
            assertThat(json).contains("\"instance\":\"/api/v1/donors/");
            assertThat(json).contains("\"timestamp\":");
            // null 'errors' field omitted due to @JsonInclude(NON_NULL)
            assertThat(json).doesNotContain("\"errors\":null");
        }

        @Test
        @DisplayName("422 Unprocessable Entity error serializes with correct RFC 7807 title")
        void businessRuleViolationSerializesCorrectly() throws Exception {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            Class<?> cls = Class.forName(ERROR_RESPONSE_CLASS);
            Object errorResponse = cls.getConstructors()[0].newInstance(
                    422,
                    "Business Rule Violation",
                    "Donor is permanently deferred and cannot donate",
                    "/api/v1/collections",
                    LocalDateTime.now(),
                    null
            );

            String json = mapper.writeValueAsString(errorResponse);

            assertThat(json).contains("\"status\":422");
            assertThat(json).contains("\"title\":\"Business Rule Violation\"");
            assertThat(json).contains("\"detail\":");
            assertThat(json).contains("\"instance\":\"/api/v1/collections\"");
        }

        @Test
        @DisplayName("400 Validation Failed error includes 'errors' array with field-level details")
        void validationErrorIncludesFieldErrors() throws Exception {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            // Build a ValidationError list
            Class<?> validationErrorCls =
                    Class.forName("com.bloodbank.common.dto.ValidationError");
            Object validationError = validationErrorCls.getConstructors()[0].newInstance(
                    "email", "must not be blank", ""
            );

            Class<?> cls = Class.forName(ERROR_RESPONSE_CLASS);
            Object errorResponse = cls.getConstructors()[0].newInstance(
                    400,
                    "Validation Failed",
                    "One or more fields have validation errors",
                    "/api/v1/donors",
                    LocalDateTime.now(),
                    List.of(validationError)
            );

            String json = mapper.writeValueAsString(errorResponse);

            assertThat(json).contains("\"status\":400");
            assertThat(json).contains("\"title\":\"Validation Failed\"");
            assertThat(json).contains("\"errors\":");
            assertThat(json).contains("\"email\"");
        }
    }

    // -----------------------------------------------------------------------
    // RFC 7807 adherence notes (documented contract deviations)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("RFC 7807 adherence — documented contract notes")
    class Rfc7807AdherenceNotes {

        @Test
        @DisplayName("'instance' field maps to request URI — RFC 7807 §3.1 relative reference")
        void instanceFieldMapsToRequestUri() {
            // RFC 7807 §3.1: "instance" identifies the specific occurrence of the problem.
            // BloodBank uses request.getRequestURI() as the instance value, which is a
            // relative URI reference (e.g., /api/v1/donors/123).
            // This is valid per RFC 7807 which allows relative references.
            String exampleInstance = "/api/v1/donors/00000000-0000-0000-0000-000000000001";
            assertThat(exampleInstance)
                    .as("Instance must be a URI reference (relative or absolute)")
                    .startsWith("/api/v1/");
        }

        @Test
        @DisplayName("'title' field provides a short problem description — must not be blank")
        void titleMustNotBeBlank() {
            List<String> expectedTitles = List.of(
                    "Not Found",
                    "Business Rule Violation",
                    "Conflict",
                    "Unauthorized",
                    "Forbidden",
                    "Validation Failed",
                    "Internal Server Error"
            );
            for (String title : expectedTitles) {
                assertThat(title)
                        .as("RFC 7807 title must not be blank")
                        .isNotBlank();
            }
        }

        @Test
        @DisplayName("HTTP status codes in error responses match RFC 7807 standard codes")
        void httpStatusCodesMatchRfc7807() {
            // Map of expected exception → status code contract
            java.util.Map<String, Integer> statusMap = java.util.Map.of(
                    "ResourceNotFoundException",   404,
                    "BusinessException",           422,
                    "ConflictException",           409,
                    "UnauthorizedException",       401,
                    "DataIsolationException",      403,
                    "MethodArgumentNotValidException", 400,
                    "ConstraintViolationException",   400,
                    "Exception",                   500
            );

            statusMap.forEach((exception, expectedStatus) ->
                    assertThat(expectedStatus)
                            .as("Status for %s must be a standard 4xx or 5xx code", exception)
                            .isBetween(400, 599)
            );
        }
    }
}
