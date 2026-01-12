package com.example.SA2Gemini.config;

import com.example.SA2Gemini.service.AuditService;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditEntityListener {

    private static AuditService auditService;

    @Autowired
    public void setAuditService(AuditService auditService) {
        AuditEntityListener.auditService = auditService;
    }

    @PostPersist
    public void afterCreate(Object entity) {
        if (auditService != null) {
            auditService.logEntityCreate(entity);
        }
    }

    @PostUpdate
    public void afterUpdate(Object entity) {
        if (auditService != null) {
            // Para las actualizaciones, necesitaríamos el estado anterior
            // Por ahora registramos que hubo una actualización
            String entityName = entity.getClass().getSimpleName();
            String entityId = getEntityId(entity);
            auditService.logOperation(entityName, entityId, 
                com.example.SA2Gemini.entity.AuditLog.OperationType.UPDATE, 
                "Entidad " + entityName + " actualizada");
        }
    }

    @PreRemove
    public void beforeDelete(Object entity) {
        if (auditService != null) {
            auditService.logEntityDelete(entity);
        }
    }

    private String getEntityId(Object entity) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(entity);
            return id != null ? id.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}