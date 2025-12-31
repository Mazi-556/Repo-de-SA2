package com.example.SA2Gemini.entity;

public enum EstadoOrdenCompra {
    EMITIDA,
    EN_PROCESO,
    RECIBIDA_PARCIAL("RECIBIDA PARCIAL"),
    RECIBIDA_COMPLETA("RECIBIDA COMPLETA"),
    FACTURADA,
    CANCELADA;
    
    private final String displayName;
    
    EstadoOrdenCompra() {
        this.displayName = null;
    }
    
    EstadoOrdenCompra(String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public String toString() {
        return displayName != null ? displayName : name();
    }
}