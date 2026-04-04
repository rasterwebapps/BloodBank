package com.bloodbank.common.security;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class JwtUtils {

    private JwtUtils() {
        // Prevent instantiation
    }

    public static UUID extractBranchId(Authentication authentication) {
        JwtAuthenticationToken jwtToken = getJwt(authentication);
        if (jwtToken == null) {
            return null;
        }
        Object branchId = jwtToken.getToken().getClaim("branch_id");
        if (branchId instanceof String branchIdStr) {
            try {
                return UUID.fromString(branchIdStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public static UUID extractUserId(Authentication authentication) {
        JwtAuthenticationToken jwtToken = getJwt(authentication);
        if (jwtToken == null) {
            return null;
        }
        Object sub = jwtToken.getToken().getClaim("sub");
        if (sub instanceof String subStr) {
            try {
                return UUID.fromString(subStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public static List<String> extractRoles(Authentication authentication) {
        if (authentication == null) {
            return Collections.emptyList();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .toList();
    }

    private static JwtAuthenticationToken getJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken;
        }
        return null;
    }
}
