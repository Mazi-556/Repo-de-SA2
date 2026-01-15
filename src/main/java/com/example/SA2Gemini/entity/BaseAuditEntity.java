package com.example.SA2Gemini.entity;

import jakarta.persistence.MappedSuperclass;

/**
 * Clase base para entidades que requieren auditoría.
 *
 * Nota: actualmente se usa principalmente para compatibilidad con clases que la extienden.
 * La auditoría real se maneja con @CreatedBy/@CreatedDate en cada entidad.
 */
@MappedSuperclass
public abstract class BaseAuditEntity {
    // Placeholder para compatibilidad. Se puede extender en el futuro.
}
