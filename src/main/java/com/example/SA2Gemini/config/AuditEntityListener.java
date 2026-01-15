package com.example.SA2Gemini.config;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Listener de auditoría.
 *
 * Implementación mínima para evitar fallos de compilación.
 * Si en el futuro se quiere completar auditoría custom, se puede implementar acá.
 */
public class AuditEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        // no-op
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        // no-op
    }
}
