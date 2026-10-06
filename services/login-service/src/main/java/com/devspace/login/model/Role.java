package com.devspace.login.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @Column(name = "role_id")
    private String roleId;

    @Column(
            name = "role_name",
            nullable = false,
            unique = true
    )
    private String roleName;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {

        if (roleId == null) {
            roleId =
                    UUID.randomUUID()
                            .toString();
        }

        createdAt =
                Instant.now();

        updatedAt =
                Instant.now();
    }

    @PreUpdate
    public void onUpdate() {

        updatedAt =
                Instant.now();
    }
}