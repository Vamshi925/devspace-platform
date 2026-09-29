package com.devspace.environment.model;

import java.time.Instant;
import java.util.UUID;

import com.devspace.environment.model.EnvironmentStatus;
import com.devspace.environment.model.EnvironmentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "environment")
public class Environment {

    // Primary Key
    @Id
    @Column(name = "environmentId", nullable = false, updatable = false)
    private String environmentId;

    // Local Attributes
    @Column(name = "environmentCode", nullable = false, unique = true)
    private String environmentCode;

    @Column(name = "applicationName", nullable = false)
    private String applicationName;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Column(name = "templateId", nullable = false)
    private String templateId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "environmentType", nullable = false)
    private EnvironmentType environmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnvironmentStatus status;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "updatedAt", nullable = false)
    private Instant updatedAt;

    @Column(name = "expiresAt", nullable = false)
    private Instant expiresAt;

    @Column(name = "namespace")
    private String namespace;

    @Column(name = "applicationUrl")
    private String applicationUrl;

    @Column(name = "repositoryUrl")
    private String repositoryUrl;

    @Column(name = "branchName")
    private String branchName;

    @Column(name = "failureReason", length = 2000)
    private String failureReason;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    public void insert() {

        if (this.environmentId == null) {
            this.environmentId = UUID.randomUUID().toString();
        }

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        if (this.status == null) {
            this.status = EnvironmentStatus.REQUESTED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
