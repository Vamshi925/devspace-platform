package com.devspace.template.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devspace.template.model.EnvironmentTemplate;

@Repository
public interface EnvironmentTemplateRepository
        extends JpaRepository<EnvironmentTemplate, String> {

    Optional<EnvironmentTemplate> findByName(String name);

    List<EnvironmentTemplate> findByActiveTrue();
}