package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@Order(3)
@Tags(value = {
        @Tag("install"),
        @Tag("upgrade-phase-1"),
        @Tag("upgrade-phase-2"),
})
public class FleetshardCommunicationTestITCase {

    private static final Logger LOG = LoggerFactory.getLogger(FleetshardCommunicationTestITCase.class);

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
                .pollDelay(Duration.ofSeconds(0))
                .untilAsserted(() -> {
                            String log = client.pods().withName(pod.getMetadata().getName()).getLog();
                            Assertions.assertTrue(log.contains("No connectors for cluster"),
                                    () -> {
                                        LOG.error("pod log:\n {}", log);
                                        return "sync not communicating with manager:\n" + log;
                                    });
                        }
                );


    }
}
