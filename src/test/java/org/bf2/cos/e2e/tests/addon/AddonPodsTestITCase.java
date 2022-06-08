package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.bf2.cos.e2e.tests.listener.MetadataTestExecutionListener;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Order(2)
@Tags(value = {
        @Tag("install"),
        @Tag("upgrade-phase-1"),
})
public class AddonPodsTestITCase {
    private static final Logger LOG = LoggerFactory.getLogger(AddonPodsTestITCase.class);

    private static NamespacedOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)))
            .inNamespace("redhat-openshift-connectors");

    private final TestReporter reporter;

    public AddonPodsTestITCase(TestReporter reporter) {
        this.reporter = reporter;
    }

    @DisplayName("pod should be running:")
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "name=camel-k-operator",
            "name=strimzi-cluster-operator",
            "app.kubernetes.io/name=cos-fleetshard-sync",
            "app.kubernetes.io/name=cos-fleetshard-operator-camel",
            "app.kubernetes.io/name=cos-fleetshard-operator-debezium",
    })

    public void podShouldHaveStarted(String podSelector) {
        Awaitility.await()
                .atMost(Duration.ofMinutes(4))
                .pollInterval(Duration.ofSeconds(10))
                .pollDelay(Duration.ofSeconds(0))
                .untilAsserted(() -> {
                            List<Pod> podList = client.pods().withLabelSelector(podSelector).list().getItems();
                            Assertions.assertEquals(podList.size(), 1, () -> podSelector + " pod not present");
                            PodResource<Pod> pod = client.pods().withName(podList.get(0).getMetadata().getName());
                            Assertions.assertTrue(pod.isReady(), () ->{
                                LOG.error("pod status: {}",  podList.get(0).getStatus());
                                return podSelector + " pod not ready";
                            });
                            reporter.publishEntry(Map.of(
                                    MetadataTestExecutionListener.REPORT_METADATA_CATEGORY_KEY, "pod",
                                    MetadataTestExecutionListener.REPORT_METADATA_ENTRY_KEY, podSelector,
                                    "image", podList.get(0).getSpec().getContainers().get(0).getImage()
                            ));
                        }
                );
    }
}
