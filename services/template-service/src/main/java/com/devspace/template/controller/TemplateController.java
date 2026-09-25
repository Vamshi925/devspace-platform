package com.devspace.template.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.devspace.template.dto.request.CreateTemplateRequest;
import com.devspace.template.dto.response.TemplateResponse;
import com.devspace.template.service.TemplateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    // Create Template
    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request) {

        TemplateResponse template =
                templateService.createTemplate(request);

        return new ResponseEntity<>(template, HttpStatus.CREATED);
    }

    // Get All Templates
    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {

        List<TemplateResponse> templates =
                templateService.getAllTemplates();

        return ResponseEntity.ok(templates);
    }

    // Get Active Templates
    @GetMapping("/active")
    public ResponseEntity<List<TemplateResponse>> getActiveTemplates() {

        List<TemplateResponse> templates =
                templateService.getActiveTemplates();

        return ResponseEntity.ok(templates);
    }

    // Get Template By ID
    @GetMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> getTemplateById(
            @PathVariable String templateId) {

        TemplateResponse template =
                templateService.getTemplateById(templateId);

        return ResponseEntity.ok(template);
    }
}