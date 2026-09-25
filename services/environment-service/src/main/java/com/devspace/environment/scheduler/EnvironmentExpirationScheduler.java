package com.devspace.environment.scheduler;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.devspace.environment.model.Environment;
import com.devspace.environment.model.EnvironmentStatus;
import com.devspace.environment.repository.EnvironmentRepository;
import com.devspace.environment.service.EnvironmentService;

@Component
public class EnvironmentExpirationScheduler {

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private EnvironmentService environmentService;

    @Scheduled(fixedRate = 60000)
    public void expireEnvironments() {

        List<Environment> expiredEnvironments =
                environmentRepository.findExpiredEnvironments(
                        EnvironmentStatus.READY,
                        Instant.now()
                );

        for (Environment environment : expiredEnvironments) {

    try {

        environmentService.expireEnvironment(
                environment.getEnvironmentId()
        );

    } catch (ObjectOptimisticLockingFailureException ex) {

        System.out.println(
                "Environment already processed by another instance: "
                        + environment.getEnvironmentId()
        );
    }
}
    }
}