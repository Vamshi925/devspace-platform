package com.devspace.provisioning.kubernetes;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class NamespaceDeletionChecker {

    private static final Logger logger =
            LoggerFactory.getLogger(NamespaceDeletionChecker.class);

    private final KubernetesClient kubernetesClient;

    @Value("${devspace.kubernetes.mock:false}")
    private boolean mockKubernetes;

    public NamespaceDeletionChecker(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient =
                kubernetesClient;
    }

    public void waitUntilDeleted(
            String namespace) {

        if (mockKubernetes) {

            logger.info(
                    "Mock Kubernetes mode - namespace deletion confirmed: {}",
                    namespace
            );

            return;
        }

        logger.info(
                "Waiting for namespace deletion - Namespace: {}",
                namespace
        );

        long timeoutSeconds = 60;
        long intervalSeconds = 2;
        long elapsedSeconds = 0;

        while (elapsedSeconds < timeoutSeconds) {

            boolean exists =
                    kubernetesClient.namespaces()
                            .withName(namespace)
                            .get() != null;

            if (!exists) {

                logger.info(
                        "Namespace deletion confirmed - Namespace: {}",
                        namespace
                );

                return;
            }

            try {

                TimeUnit.SECONDS.sleep(
                        intervalSeconds
                );

            } catch (InterruptedException ex) {

                Thread.currentThread()
                        .interrupt();

                throw new IllegalStateException(
                        "Interrupted while waiting for namespace deletion",
                        ex
                );
            }

            elapsedSeconds += intervalSeconds;
        }

        throw new IllegalStateException(
                "Namespace deletion timed out: "
                        + namespace
        );
    }
}