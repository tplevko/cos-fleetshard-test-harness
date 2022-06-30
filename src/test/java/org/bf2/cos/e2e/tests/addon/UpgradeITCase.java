package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.ClusterServiceVersion;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Tags(value = {
        @Tag("upgrade-phase-2"),
})
@Order(0)
public class UpgradeITCase {

    private static NamespacedOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)))
            .inNamespace("redhat-openshift-connectors");

    private final String newCsvVersion = System.getProperty("test.csv.version");

    @DisplayName("csv should have been upgraded:")
    @ParameterizedTest(name = "{0}")
    @Order(0)
    @MethodSource("csvReplacementProvider")
    public void csvShouldHaveBeenUpgraded(String csvName) {
        Awaitility.await()
                .atMost(Duration.ofMinutes(5))
                .pollInterval(Duration.ofSeconds(10))
                .pollDelay(Duration.ofSeconds(0))
                .untilAsserted(() -> {
                            var csv = client.operatorHub().clusterServiceVersions().withName(csvName + ".v" + newCsvVersion).get();
                            Assertions.assertNotNull(csv, "new csv not created");
                            Assertions.assertEquals(newCsvVersion, csv.getSpec().getVersion(), "new csv has wrong version");
                            Assertions.assertNotNull(csv.getStatus(), "csv status is null"); // not sure how it could be, but got a NPE on the next line once
                            Assertions.assertEquals("Succeeded", csv.getStatus().getPhase(), "csv upgrade not succeeded");
                        }
                );
    }

    @DisplayName("upgraded pods should be running")
    @ParameterizedTest(name = "{0}")
    @Order(1)
    @MethodSource("csvReplacementProvider")
    public void podsShouldHaveBeenUpgraded(String csvName) {

        Deployment deployment = Awaitility.await()
                .atMost(Duration.ofMinutes(5))
                .pollInterval(Duration.ofSeconds(20))
                .pollDelay(Duration.ofSeconds(0))
                .until(
                        () -> client.apps().deployments().withLabel("olm.owner", csvName + ".v" + newCsvVersion).list().getItems(),
                        d -> !d.isEmpty()
                ).get(0);

        String newImage = deployment
                .getSpec()
                .getTemplate()
                .getSpec()
                .getContainers()
                .get(0)
                .getImage();

        Awaitility.await()
                .atMost(Duration.ofMinutes(5))
                .pollInterval(Duration.ofSeconds(10))
                .pollDelay(Duration.ofSeconds(0))
                .untilAsserted(() -> {
                            List<Pod> podList = client.pods().withLabelSelector(deployment.getSpec().getSelector()).list().getItems();
                            Assertions.assertEquals(1, podList.size(), csvName + " pod not present");
                            Assertions.assertEquals(
                                    newImage,
                                    podList.get(0).getSpec().getContainers().get(0).getImage(),
                                    csvName + " pod uses old image"
                            );
                            PodResource<Pod> pod = client.pods().withName(podList.get(0).getMetadata().getName());
                            Assertions.assertTrue(pod.isReady(), csvName + " pod not ready");
                        }
                );
    }

    static Stream<String> csvReplacementProvider() {
        return Stream.of(
                "camel-k-operator",
                "strimzi-kafka-operator",
                "cos-fleetshard-operator-camel",
                "cos-fleetshard-operator-debezium",
                "cos-fleetshard-sync"
        );
    }

}
