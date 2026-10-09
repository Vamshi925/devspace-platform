package com.devspace.environment.repository;

import com.devspace.environment.model.EnvironmentActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnvironmentActivityRepository
        extends JpaRepository<EnvironmentActivity, String> {

    List<EnvironmentActivity>
    findByEnvironmentIdOrderByCreatedAtDesc(String environmentId);
}