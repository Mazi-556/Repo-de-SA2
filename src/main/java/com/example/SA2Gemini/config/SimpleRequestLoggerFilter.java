package com.example.SA2Gemini.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public class SimpleRequestLoggerFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("--- SimpleRequestLoggerFilter inicializado ---");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        System.out.println("--- FILTER LOG: Request URL: " + req.getRequestURL() + ", Method: " + req.getMethod() + " ---");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("--- SimpleRequestLoggerFilter destruido ---");
    }
}
