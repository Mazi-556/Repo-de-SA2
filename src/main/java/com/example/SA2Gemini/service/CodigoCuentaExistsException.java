package com.example.SA2Gemini.service;

public class CodigoCuentaExistsException extends RuntimeException {
    public CodigoCuentaExistsException(String message) {
        super(message);
    }
}