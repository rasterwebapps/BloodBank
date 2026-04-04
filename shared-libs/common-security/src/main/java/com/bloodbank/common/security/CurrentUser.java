package com.bloodbank.common.security;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public UUID getUserId() {
        return JwtUtils.extractUserId(getAuthentication());
    }

    public UUID getBranchId() {
        return JwtUtils.extractBranchId(getAuthentication());
    }

    public List<String> getRoles() {
        return JwtUtils.extractRoles(getAuthentication());
    }

    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    public boolean isSuperAdmin() {
        return hasRole(RoleConstants.SUPER_ADMIN);
    }

    public boolean isRegionalAdmin() {
        return hasRole(RoleConstants.REGIONAL_ADMIN);
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
