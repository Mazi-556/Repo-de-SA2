package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.AuditLog;
import com.example.SA2Gemini.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void logOperation(String entityName, String entityId, AuditLog.OperationType operationType, String description) {
        logOperation(entityName, entityId, operationType, description, null, null);
    }

    public void logOperation(String entityName, String entityId, AuditLog.OperationType operationType, 
                           String description, Object oldEntity, Object newEntity) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEntityName(entityName);
            auditLog.setEntityId(entityId);
            auditLog.setOperationType(operationType);
            auditLog.setUserName(getCurrentUser());
            auditLog.setUserIp(getCurrentUserIp());
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setDescription(description);

            // Si tenemos entidades para comparar, generar detalles de cambios
            if (oldEntity != null && newEntity != null) {
                Map<String, Object> changes = compareEntities(oldEntity, newEntity);
                auditLog.setOldValues(objectMapper.writeValueAsString(extractValues(oldEntity, changes.keySet())));
                auditLog.setNewValues(objectMapper.writeValueAsString(extractValues(newEntity, changes.keySet())));
                auditLog.setChangedFields(String.join(", ", changes.keySet()));
            } else if (newEntity != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(extractAllValues(newEntity)));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log el error pero no fallar la operación principal
            System.err.println("Error al guardar log de auditoría: " + e.getMessage());
        }
    }

    public void logEntityCreate(Object entity) {
        String entityName = entity.getClass().getSimpleName();
        String entityId = getEntityId(entity);
        logOperation(entityName, entityId, AuditLog.OperationType.CREATE, 
                    "Entidad " + entityName + " creada", null, entity);
    }

    public void logEntityUpdate(Object oldEntity, Object newEntity) {
        String entityName = newEntity.getClass().getSimpleName();
        String entityId = getEntityId(newEntity);
        logOperation(entityName, entityId, AuditLog.OperationType.UPDATE, 
                    "Entidad " + entityName + " actualizada", oldEntity, newEntity);
    }

    public void logEntityDelete(Object entity) {
        String entityName = entity.getClass().getSimpleName();
        String entityId = getEntityId(entity);
        logOperation(entityName, entityId, AuditLog.OperationType.DELETE, 
                    "Entidad " + entityName + " eliminada", entity, null);
    }

    public void logLogin(String username, boolean successful) {
        String description = successful ? "Inicio de sesión exitoso" : "Intento de inicio de sesión fallido";
        logOperation("Usuario", username, AuditLog.OperationType.LOGIN, description);
    }

    public void logLogout(String username) {
        logOperation("Usuario", username, AuditLog.OperationType.LOGOUT, "Cierre de sesión");
    }

    public List<AuditLog> getAuditLogsForEntity(String entityName, String entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId);
    }

    public List<AuditLog> getAuditLogsByUser(String username) {
        return auditLogRepository.findByUserNameOrderByTimestampDesc(username);
    }

    public List<AuditLog> getRecentAuditLogs(int limit) {
        return auditLogRepository.findTopByOrderByTimestampDesc(limit);
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM";
        }
        String username = authentication.getName();
        return "anonymousUser".equals(username) ? "ANONYMOUS" : username;
    }

    private String getCurrentUserIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Ignorar errores de contexto
        }
        return "localhost";
    }

    private String getEntityId(Object entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(entity);
            return id != null ? id.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private Map<String, Object> compareEntities(Object oldEntity, Object newEntity) {
        Map<String, Object> changes = new HashMap<>();
        
        try {
            Field[] fields = oldEntity.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object oldValue = field.get(oldEntity);
                Object newValue = field.get(newEntity);
                
                // Comparar valores
                if (oldValue == null && newValue != null) {
                    changes.put(field.getName(), newValue);
                } else if (oldValue != null && !oldValue.equals(newValue)) {
                    changes.put(field.getName(), newValue);
                }
            }
        } catch (Exception e) {
            // En caso de error, registrar que hubo cambios sin detalles
            changes.put("error", "No se pudieron comparar los cambios");
        }
        
        return changes;
    }

    private Map<String, Object> extractValues(Object entity, java.util.Set<String> fieldNames) {
        Map<String, Object> values = new HashMap<>();
        
        try {
            for (String fieldName : fieldNames) {
                Field field = entity.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                values.put(fieldName, field.get(entity));
            }
        } catch (Exception e) {
            values.put("error", "Error extrayendo valores");
        }
        
        return values;
    }

    private Map<String, Object> extractAllValues(Object entity) {
        Map<String, Object> values = new HashMap<>();
        
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value != null) {
                    values.put(field.getName(), value);
                }
            }
        } catch (Exception e) {
            values.put("error", "Error extrayendo valores");
        }
        
        return values;
    }
}