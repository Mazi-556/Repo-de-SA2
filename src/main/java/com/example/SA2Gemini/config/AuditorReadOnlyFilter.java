package com.example.SA2Gemini.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Fuerza que el rol AUDITOR sea estrictamente de solo lectura.
 * Bloquea cualquier método HTTP que no sea GET/HEAD/OPTIONS.
 */
@Component
public class AuditorReadOnlyFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            boolean isAuditor = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_AUDITOR"));

            if (isAuditor) {
                String uri = request.getRequestURI();
                boolean isWriteLikeGet = uri.contains("/eliminar") || uri.contains("/borrar") || uri.contains("/delete");

                if (!ALLOWED_METHODS.contains(request.getMethod()) || isWriteLikeGet) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "AUDITOR es solo lectura");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
