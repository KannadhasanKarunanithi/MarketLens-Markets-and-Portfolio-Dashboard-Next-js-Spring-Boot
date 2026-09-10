package com.marketlens.audit;

import java.io.IOException;
import java.util.Set;

import com.marketlens.common.security.CurrentUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Records one audit row for every state changing API request. Runs inside the
 * security chain so the authenticated user is known.
 */
@Component
@Order(60)
public class AuditFilter extends OncePerRequestFilter {

    private static final Set<String> RECORDED = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditService auditService;

    public AuditFilter(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        chain.doFilter(request, response);

        String path = request.getRequestURI();
        if (!RECORDED.contains(request.getMethod()) || path == null || !path.startsWith("/api/")) {
            return;
        }
        try {
            auditService.record(currentActor(), request.getMethod(), path, response.getStatus());
        } catch (RuntimeException ex) {
            logger.warn("Could not write audit row for " + path);
        }
    }

    private String currentActor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser user) {
            return user.getUsername();
        }
        return "anonymous";
    }
}
