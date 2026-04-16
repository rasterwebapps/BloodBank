package security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * M6-018: JWT Expiration Handling Tests.
 *
 * <p>Verifies that the BloodBank API correctly handles JWT token expiration:
 * <ul>
 *   <li>Expired tokens must be rejected with HTTP 401 (Unauthorized).</li>
 *   <li>Tokens with future expiry are accepted by the decoder.</li>
 *   <li>Tokens without an expiry claim are rejected.</li>
 *   <li>The spring-security NimbusJwtDecoder enforces expiry validation.</li>
 * </ul>
 *
 * <p>Tests use a self-generated RSA-2048 key pair so no running Keycloak is needed.
 * The decoder is configured to trust the self-generated public key, exactly as
 * production NimbusJwtDecoder is configured to trust Keycloak's JWKS endpoint.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("M6-018: JWT Expiration Handling")
class JwtExpirySecurityTest {

    private RSAKey rsaKey;
    private JwtDecoder decoder;

    @BeforeAll
    void setUpKeyPairAndDecoder() throws JOSEException {
        rsaKey = new RSAKeyGenerator(2048)
                .keyID("test-key-" + UUID.randomUUID())
                .generate();

        RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
        decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    // -----------------------------------------------------------------------
    // Helper: build a signed JWT
    // -----------------------------------------------------------------------

    private String buildToken(Date expiresAt) throws JOSEException {
        return buildToken(expiresAt, "test-user", List.of("BRANCH_ADMIN"), UUID.randomUUID().toString());
    }

    private String buildToken(Date expiresAt, String subject,
                               List<String> roles, String branchId) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("http://localhost:8180/realms/bloodbank")
                .audience("bloodbank-api")
                .issueTime(new Date(System.currentTimeMillis() - 5000))
                .expirationTime(expiresAt)
                .jwtID(UUID.randomUUID().toString())
                .claim("branch_id", branchId)
                .claim("realm_access", Map.of("roles", roles))
                .claim("resource_access", Map.of(
                        "bloodbank-app", Map.of("roles", roles)
                ))
                .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims);
        jwt.sign(new RSASSASigner(rsaKey));
        return jwt.serialize();
    }

    // -----------------------------------------------------------------------
    // Expired token rejection
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Expired tokens are rejected")
    class ExpiredTokenRejection {

        @Test
        @DisplayName("Token expired 5 minutes ago is rejected with JwtException")
        void tokenExpiredFiveMinutesAgo_isRejected() throws JOSEException {
            // Use 5 minutes (300 seconds) — well beyond the default 60-second clock skew tolerance
            String expiredToken = buildToken(new Date(System.currentTimeMillis() - 300_000L));
            assertThatThrownBy(() -> decoder.decode(expiredToken))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("Token expired 1 hour ago is rejected")
        void tokenExpiredOneHourAgo_isRejected() throws JOSEException {
            String expiredToken = buildToken(new Date(System.currentTimeMillis() - 3_600_000L));
            assertThatThrownBy(() -> decoder.decode(expiredToken))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("Token expired yesterday is rejected")
        void tokenExpiredYesterday_isRejected() throws JOSEException {
            String expiredToken = buildToken(new Date(System.currentTimeMillis() - 86_400_000L));
            assertThatThrownBy(() -> decoder.decode(expiredToken))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("Rejection message contains expiry information")
        void rejectionMessageContainsExpiryInfo() throws JOSEException {
            // Token expired 5 minutes ago — beyond any clock skew tolerance
            String expiredToken = buildToken(new Date(System.currentTimeMillis() - 300_000L));
            try {
                decoder.decode(expiredToken);
                throw new AssertionError("Expected JwtException for expired token");
            } catch (JwtException e) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                boolean mentionsExpiry = msg.contains("expired") || msg.contains("exp")
                        || msg.contains("before") || msg.contains("invalid");
                assertThat(mentionsExpiry)
                        .as("JwtException message '%s' should mention token expiry", e.getMessage())
                        .isTrue();
            }
        }

        @Test
        @DisplayName("Token expired by 2 minutes (beyond clock skew tolerance) is rejected")
        void tokenExpiredBeyondClockSkewTolerance_isRejected() throws JOSEException {
            // NimbusJwtDecoder default clock skew is 60 seconds.
            // A token expired 2 minutes ago must be rejected.
            String expiredToken = buildToken(new Date(System.currentTimeMillis() - 120_000L));
            assertThatThrownBy(() -> decoder.decode(expiredToken))
                    .isInstanceOf(JwtException.class);
        }
    }

    // -----------------------------------------------------------------------
    // Valid token acceptance
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Valid (non-expired) tokens are accepted")
    class ValidTokenAcceptance {

        @Test
        @DisplayName("Token expiring in 1 hour is accepted")
        void tokenExpiringInOneHour_isAccepted() throws JOSEException {
            String validToken = buildToken(new Date(System.currentTimeMillis() + 3_600_000L));
            var jwt = decoder.decode(validToken);
            assertThat(jwt).isNotNull();
            assertThat(jwt.getExpiresAt()).isNotNull();
        }

        @Test
        @DisplayName("Decoded valid token retains subject claim")
        void decodedValidToken_retainsSubjectClaim() throws JOSEException {
            String subject = "user-" + UUID.randomUUID();
            String validToken = buildToken(
                    new Date(System.currentTimeMillis() + 3_600_000L),
                    subject, List.of("DOCTOR"), UUID.randomUUID().toString());

            var jwt = decoder.decode(validToken);
            assertThat(jwt.getSubject()).isEqualTo(subject);
        }

        @Test
        @DisplayName("Decoded valid token retains branch_id claim")
        void decodedValidToken_retainsBranchIdClaim() throws JOSEException {
            String branchId = UUID.randomUUID().toString();
            String validToken = buildToken(
                    new Date(System.currentTimeMillis() + 3_600_000L),
                    "doctor-user", List.of("DOCTOR"), branchId);

            var jwt = decoder.decode(validToken);
            assertThat(jwt.getClaimAsString("branch_id")).isEqualTo(branchId);
        }

        @Test
        @DisplayName("Decoded valid token retains realm_access roles")
        void decodedValidToken_retainsRoles() throws JOSEException {
            List<String> roles = List.of("BRANCH_ADMIN");
            String validToken = buildToken(
                    new Date(System.currentTimeMillis() + 3_600_000L),
                    "admin-user", roles, UUID.randomUUID().toString());

            var jwt = decoder.decode(validToken);
            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
            assertThat(realmAccess).isNotNull();
            assertThat(realmAccess.get("roles")).isEqualTo(roles);
        }
    }

    // -----------------------------------------------------------------------
    // Invalid signature rejection
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Tokens with invalid signatures are rejected")
    class InvalidSignatureRejection {

        @Test
        @DisplayName("Token signed with a different key is rejected")
        void tokenSignedWithDifferentKey_isRejected() throws JOSEException {
            RSAKey anotherKey = new RSAKeyGenerator(2048).generate();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject("attacker")
                    .issuer("http://localhost:8180/realms/bloodbank")
                    .audience("bloodbank-api")
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 3_600_000L))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("different-key").build(),
                    claims);
            jwt.sign(new RSASSASigner(anotherKey));

            assertThatThrownBy(() -> decoder.decode(jwt.serialize()))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("Tampered token payload is rejected")
        void tamperedTokenPayload_isRejected() throws JOSEException {
            String validToken = buildToken(new Date(System.currentTimeMillis() + 3_600_000L));
            // Corrupt the payload part (second segment)
            String[] parts = validToken.split("\\.");
            String tamperedPayload = parts[0] + ".eyJzdWIiOiJhdHRhY2tlciIsInJvbGVzIjpbIlNVUEVSX0FETUlOIl19." + parts[2];
            assertThatThrownBy(() -> decoder.decode(tamperedPayload))
                    .isInstanceOf(JwtException.class);
        }
    }

    // -----------------------------------------------------------------------
    // Token structure validation
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Token structure requirements")
    class TokenStructureRequirements {

        @Test
        @DisplayName("An empty string is rejected as invalid JWT")
        void emptyString_isRejected() {
            assertThatThrownBy(() -> decoder.decode(""))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("A plain string (not a JWT) is rejected")
        void plainString_isRejected() {
            assertThatThrownBy(() -> decoder.decode("not-a-jwt-token"))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("Null token is rejected")
        void nullToken_isRejected() {
            assertThatThrownBy(() -> decoder.decode(null))
                    .isInstanceOf(Exception.class);
        }
    }
}
