package com.devspace.environment.dto.response;

import com.devspace.environment.model.EnvironmentActivityType;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentActivityResponse {
    private String activityId;
    private String environmentId;
    private EnvironmentActivityType type;
    private String message;
    private Instant createdAt;
}