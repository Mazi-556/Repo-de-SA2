package com.example.SA2Gemini.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "operation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_ip")
    private String userIp;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "changed_fields", columnDefinition = "TEXT")
    private String changedFields;

    @Column(name = "description")
    private String description;

    public enum OperationType {
        CREATE("Creación"),
        UPDATE("Actualización"),
        DELETE("Eliminación"),
        VIEW("Consulta"),
        LOGIN("Inicio de Sesión"),
        LOGOUT("Cierre de Sesión");

        private final String displayName;

        OperationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructores
    public AuditLog() {}

    public AuditLog(String entityName, String entityId, OperationType operationType, 
                   String userName, String userIp, String description) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.operationType = operationType;
        this.userName = userName;
        this.userIp = userIp;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}