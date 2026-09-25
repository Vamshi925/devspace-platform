package com.devspace.environment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtendEnvironmentRequest {

    @NotNull(message = "Additional hours is required")
    @Positive(message = "Additional hours must be greater than 0")
    private Integer additionalHours;
}