package com.example.SA2Gemini.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e) {
        logger.warn("Access denied: " + e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e) {
        logger.error("Unhandled exception occurred", e);
        return "error"; // or a more specific error page
    }
}