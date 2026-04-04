package com.bloodbank.common.security;

import java.util.List;
import java.util.UUID;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

@Aspect
@Component
public class BranchDataFilterAspect {

    private static final Logger log = LoggerFactory.getLogger(BranchDataFilterAspect.class);

    private final EntityManager entityManager;

    public BranchDataFilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before("execution(* *..*..*Repository*.*(..))")
    public void enableBranchFilter() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        List<String> roles = JwtUtils.extractRoles(authentication);
        if (roles.contains(RoleConstants.SUPER_ADMIN) || roles.contains(RoleConstants.REGIONAL_ADMIN)) {
            log.debug("Skipping branch filter for user with role: {}", roles);
            return;
        }

        UUID branchId = JwtUtils.extractBranchId(authentication);
        if (branchId == null) {
            log.debug("No branch_id claim found in JWT, skipping branch filter");
            return;
        }

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("branchFilter").setParameter("branchId", branchId);
        log.debug("Branch filter enabled for branchId: {}", branchId);
    }
}
