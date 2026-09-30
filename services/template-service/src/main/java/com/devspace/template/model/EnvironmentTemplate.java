package com.devspace.template.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "environment_template")
public class EnvironmentTemplate {

    // Primary Key
    @Id
    @Column(name = "templateId", nullable = false, updatable = false)
    private String templateId;

    // Local Attributes
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "runtimeLanguage", nullable = false)
    private RuntimeLanguage runtimeLanguage;

    @Column(name = "runtimeVersion", nullable = false)
    private String runtimeVersion;

    @Column(name = "applicationPort", nullable = false)
    private Integer applicationPort;

    @Enumerated(EnumType.STRING)
    @Column(name = "databaseType", nullable = false)
    private DatabaseType databaseType;

    @Column(name = "redisEnabled", nullable = false)
    private Boolean redisEnabled;

    @Column(name = "kafkaEnabled", nullable = false)
    private Boolean kafkaEnabled;

    @Column(name = "cpuRequest", nullable = false)
    private String cpuRequest;

    @Column(name = "cpuLimit", nullable = false)
    private String cpuLimit;

    @Column(name = "memoryRequest", nullable = false)
    private String memoryRequest;

    @Column(name = "memoryLimit", nullable = false)
    private String memoryLimit;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "updatedAt", nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private String containerImage;
    
    @PrePersist
    public void insert() {

        if (this.templateId == null) {
            this.templateId = UUID.randomUUID().toString();
        }

        if (this.redisEnabled == null) {
            this.redisEnabled = false;
        }

        if (this.kafkaEnabled == null) {
            this.kafkaEnabled = false;
        }

        if (this.active == null) {
            this.active = true;
        }

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}