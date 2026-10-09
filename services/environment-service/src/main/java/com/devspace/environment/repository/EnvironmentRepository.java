package com.devspace.environment.repository;

import com.devspace.environment.model.Environment;
import com.devspace.environment.model.EnvironmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, String> {

    // Fetch environments by user ID
    @Query("SELECT e FROM Environment e WHERE e.userId = :userId")
    List<Environment> findEnvironmentsByUserId(@Param("userId") String userId);

    // Fetch environments by status
    @Query("SELECT e FROM Environment e WHERE e.status = :status")
    List<Environment> findEnvironmentsByStatus(@Param("status") EnvironmentStatus status);

    // Fetch environment by environment code
    @Query("SELECT e FROM Environment e WHERE e.environmentCode = :environmentCode")
    Optional<Environment> findByEnvironmentCode(@Param("environmentCode") String environmentCode);

    List<Environment> findByStatus(EnvironmentStatus status);
    
    // Fetch expired environments based on status and expiry time
    @Query("""
           SELECT e
           FROM Environment e
           WHERE e.status = :status
           AND e.expiresAt <= :currentTime
           """)
    List<Environment> findExpiredEnvironments(
            @Param("status") EnvironmentStatus status,
            @Param("currentTime") Instant currentTime
    );

long countByUserId(String userId);

long countByUserIdAndStatus(
        String userId,
        EnvironmentStatus status
);

long countByStatus(EnvironmentStatus status);

}
