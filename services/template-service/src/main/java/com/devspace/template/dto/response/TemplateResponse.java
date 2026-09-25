package com.devspace.template.dto.response;

import java.time.Instant;

import com.devspace.template.model.DatabaseType;
import com.devspace.template.model.RuntimeLanguage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateResponse {

    private String templateId;
    private String name;
    private String description;

    private RuntimeLanguage runtimeLanguage;
    private String runtimeVersion;

    private Integer applicationPort;

    private DatabaseType databaseType;

    private Boolean redisEnabled;
    private Boolean kafkaEnabled;

    private String cpuRequest;
    private String cpuLimit;

    private String memoryRequest;
    private String memoryLimit;

    private Boolean active;

    private Instant createdAt;
    private Instant updatedAt;
}