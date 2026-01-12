package com.example.SA2Gemini.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "created_from_ip")
    private String createdFromIp;

    @Column(name = "last_modified_from_ip")
    private String lastModifiedFromIp;

    // Getters y Setters
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedFromIp() {
        return createdFromIp;
    }

    public void setCreatedFromIp(String createdFromIp) {
        this.createdFromIp = createdFromIp;
    }

    public String getLastModifiedFromIp() {
        return lastModifiedFromIp;
    }

    public void setLastModifiedFromIp(String lastModifiedFromIp) {
        this.lastModifiedFromIp = lastModifiedFromIp;
    }

    @PrePersist
    protected void prePersist() {
        if (this.createdFromIp == null) {
            this.createdFromIp = getCurrentUserIp();
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.lastModifiedFromIp = getCurrentUserIp();
    }

    private String getCurrentUserIp() {
        // Obtener IP del request actual (se implementará en el contexto web)
        return "localhost"; // Placeholder - se mejorará con el contexto web
    }
}