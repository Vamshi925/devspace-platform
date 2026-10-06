package com.devspace.environment.scheduler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

        Instant now =
                Instant.now();

        List<Environment> expiredEnvironments =
                new ArrayList<>();

        expiredEnvironments.addAll(
                environmentRepository.findExpiredEnvironments(
                        EnvironmentStatus.READY,
                        now
                )
        );

        expiredEnvironments.addAll(
                environmentRepository.findExpiredEnvironments(
                        EnvironmentStatus.EXPIRED,
                        now
                )
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

            } catch (Exception ex) {

                System.out.println(
                        "Failed to process expired environment: "
                                + environment.getEnvironmentId()
                                + " - "
                                + ex.getMessage()
                );
            }
        }
    }
}