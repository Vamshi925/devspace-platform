package com.devspace.login.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devspace.login.model.User;

@Repository
public interface UserRepository
        extends JpaRepository<User, String> {

    Optional<User> findByEmail(
            String email
    );

    boolean existsByEmail(
            String email
    );
}