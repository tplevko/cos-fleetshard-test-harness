package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.ClusterServiceVersion;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.bf2.cos.e2e.tests.listener.MetadataTestExecutionListener;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Order(1)
@Tags(value = {
        @Tag("install"),
        @Tag("upgrade-phase-1"),
})
public class AddonCsvITCase {

    private static NamespacedOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)))
            .inNamespace("redhat-openshift-connectors");

    private final String newCsvVersion = System.getProperty("test.csv.version", "");

    private final TestReporter reporter;

    public AddonCsvITCase(TestReporter reporter) {
        this.reporter = reporter;
    }

    @DisplayName("csv should have been created:")
    @ParameterizedTest(name = "{0}")
    @MethodSource("csvProvider")
    public void csvShouldHaveBeenCreated(String csvName) {
        Awaitility.await()
                .atMost(Duration.ofMinutes(3))
                .pollInterval(Duration.ofSeconds(10))
                .pollDelay(Duration.ofSeconds(0))
                .untilAsserted(() -> {
                            List<ClusterServiceVersion> csvs = client.operatorHub().clusterServiceVersions().list().getItems();
                            Optional<ClusterServiceVersion> csv = csvs.stream().filter(c -> c.getMetadata().getName().startsWith(csvName)).findFirst();
                            Assertions.assertTrue(csv.isPresent(), () -> csvName + " csv not created");
                            if (!newCsvVersion.isEmpty()) {
                                // newCsvVersion will not be supplied by osde2e run, ignore
                                Assertions.assertEquals(newCsvVersion, csv.get().getSpec().getVersion(), "wrong csv version");
                            }
                            reporter.publishEntry(Map.of(
                                    MetadataTestExecutionListener.REPORT_METADATA_CATEGORY_KEY, "csv",
                                    MetadataTestExecutionListener.REPORT_METADATA_ENTRY_KEY, csvName,
                                    "version", csv.get().getSpec().getVersion(),
                                    "name", csv.get().getMetadata().getName(),
                                    "replaces", Optional.ofNullable(csv.get().getSpec().getReplaces()).orElse("none")
                            ));
                        }
                );
    }

    static Stream<String> csvProvider() {
        return Stream.of(
                "camel-k-operator",
                "strimzi-kafka-operator",
                "cos-fleetshard-operator-camel",
                "cos-fleetshard-operator-debezium",
                "cos-fleetshard-sync"
        );
    }
}
