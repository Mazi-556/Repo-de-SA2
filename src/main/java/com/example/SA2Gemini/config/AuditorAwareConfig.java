package com.example.SA2Gemini.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareConfig implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("SYSTEM");
        }

        String username = authentication.getName();
        
        // Si es anonymous, retornar ANONYMOUS
        if ("anonymousUser".equals(username)) {
            return Optional.of("ANONYMOUS");
        }

        return Optional.of(username);
    }
}