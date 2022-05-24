package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

@Order(1)
public class AddonPodsTestITCase {
    private static NamespacedOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)))
            .inNamespace("redhat-openshift-connectors");

    @DisplayName("pod should be running:")
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "name=camel-k-operator",
            "app.kubernetes.io/name=cos-fleetshard-sync",
            "app.kubernetes.io/name=cos-fleetshard-operator-camel",
            "app.kubernetes.io/name=cos-fleetshard-operator-debezium",
            "name=strimzi-cluster-operator",
    })
    public void podShouldHaveStarted(String podSelector) {
        Awaitility.await()
                .atMost(Duration.ofMinutes(3))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        Assertions.assertEquals(
                                client.pods().withLabelSelector(podSelector).list().getItems().size(),
                                1,
                                () -> podSelector + " pod not running"
                        )
                );
    }
}
