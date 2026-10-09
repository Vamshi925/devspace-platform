package com.devspace.environment.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "environment_activity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentActivity {

    @Id
    private String activityId;

    @Column(nullable = false)
    private String environmentId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnvironmentActivityType type;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (activityId == null) activityId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }
}