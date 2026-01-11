package com.example.SA2Gemini.config;

import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       org.springframework.security.access.AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        
        // Redirigir a la misma página con parámetro de error
        String referer = request.getHeader("Referer");
        String redirectUrl = referer != null ? referer : "/";
        
        // Si la URL ya tiene parámetros, usar &, sino ?
        String separator = redirectUrl.contains("?") ? "&" : "?";
        redirectUrl += separator + "accessDenied=true";
        
        response.sendRedirect(redirectUrl);
    }
}
