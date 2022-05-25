package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

@Order(0)
public class AddonCrdITCase {
    private static DefaultOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)));

    @DisplayName("crd should have been created:")
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "managedconnectorclusters.cos.bf2.org",
            "managedconnectoroperators.cos.bf2.org",
            "managedconnectors.cos.bf2.org",
            "builds.camel.apache.org",
            "camelcatalogs.camel.apache.org",
            "integrationkits.camel.apache.org",
            "integrationplatforms.camel.apache.org",
            "integrations.camel.apache.org",
            "kameletbindings.camel.apache.org",
            "kamelets.camel.apache.org",
            "kafkabridges.kafka.strimzi.io",
            "kafkaconnectors.kafka.strimzi.io",
            "kafkaconnects.kafka.strimzi.io",
            "kafkamirrormaker2s.kafka.strimzi.io",
            "kafkamirrormakers.kafka.strimzi.io",
            "kafkarebalances.kafka.strimzi.io",
            "kafkas.kafka.strimzi.io",
            "kafkatopics.kafka.strimzi.io",
            "kafkausers.kafka.strimzi.io",
            "strimzipodsets.core.strimzi.io",
    })
    public void crdShouldHaveBeenCreated(String crd) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Assertions.assertNotNull(
                            client.apiextensions().v1().customResourceDefinitions().withName(crd).get(),
                            () -> crd + " CRD not created"
                    );
                }
        );
    }

}
