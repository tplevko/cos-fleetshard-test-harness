package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.ClusterServiceVersion;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@Tags(value = {
        @Tag("upgrade-phase-2"),
})
@Order(0)
public class UpgradeITCase {

    private static NamespacedOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)))
            .inNamespace("redhat-openshift-connectors");

    @DisplayName("csv should have been upgraded:")
    @ParameterizedTest(name = "{0}")
    @Order(0)
    @MethodSource("csvReplacementProvider")
    public void csvShouldHaveBeenUpgraded(String csvName, String previousCsv) {
        Awaitility.await()
                .atMost(Duration.ofMinutes(5))
                .pollInterval(Duration.ofSeconds(10))
                .pollDelay(Duration.ofSeconds(0))
                .untilAsserted(() -> {
                            List<ClusterServiceVersion> csvs = client.operatorHub().clusterServiceVersions().list().getItems()
                                    .stream()
                                    .filter(c -> c.getMetadata().getName().startsWith(csvName))
                                    .collect(Collectors.toList());
                            Assertions.assertEquals(1, csvs.size(), "old csv still present");
                            Assertions.assertEquals(previousCsv, csvs.get(0).getSpec().getReplaces(), "new csv not created");
                        }
                );
    }

    @DisplayName("upgraded pods should be running")
    @ParameterizedTest(name = "{0}")
    @Order(1)
    @MethodSource("csvReplacementProvider")
    public void podsShouldHaveBeenUpgraded(String csvName, String previousVersion) {
        ClusterServiceVersion csv = client.operatorHub().clusterServiceVersions()
                .list().getItems()
                .stream()
                .filter(c -> c.getMetadata().getName().startsWith(csvName))
                .findFirst()
                .get(); // will be present from previous test

        Deployment deployment = client.apps().deployments().withLabel("olm.owner", csv.getMetadata().getName()).list().getItems().get(0);
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
                            List<Pod> podList = client.pods().withLabel("app.kubernetes.io/name", csvName).list().getItems();
                            Assertions.assertEquals(podList.size(), 1, () -> csvName + " pod not present");
                            Assertions.assertEquals(
                                    newImage,
                                    podList.get(0).getSpec().getContainers().get(0).getImage(),
                                    () -> csvName + " pod uses old image"
                            );
                            PodResource<Pod> pod = client.pods().withName(podList.get(0).getMetadata().getName());
                            Assertions.assertTrue(pod.isReady(), () -> csvName + " pod not ready");
                        }
                );
    }

    static Stream<Arguments> csvReplacementProvider() throws IOException {
        String metaS = Files.readString(Path.of(System.getProperty("test.upgrade.metadata")));
        JSONObject metadata = new JSONObject(metaS);
        return Stream.of(
                "cos-fleetshard-operator-camel",
                "cos-fleetshard-operator-debezium",
                "cos-fleetshard-sync"
        ).map(c -> arguments(c, metadata.getJSONObject("csv").getJSONObject(c).getString("name")));
    }

}
