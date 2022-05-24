package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Order(2)
public class FleetshardCommunicationTestITCase {
    private static NamespacedOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)))
            .inNamespace("redhat-openshift-connectors");

    @Test
    @DisplayName("sync should communicate with manager")
    public void syncShouldCommunicateWithManager() {
        Pod pod = client.pods().withLabelSelector("app.kubernetes.io/name=cos-fleetshard-sync")
                .list().getItems().get(0);
        Awaitility.await()
                .atMost(Duration.ofMinutes(2))
                .pollInterval(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        Assertions.assertTrue(
                                client.pods().withName(pod.getMetadata().getName())
                                        .getLog()
                                        .contains("No connectors for cluster")
                        )
                );


    }
}
