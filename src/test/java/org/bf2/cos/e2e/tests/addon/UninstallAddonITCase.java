package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Optional;

@Tags(value = {
        @Tag("uninstall")
})
public class UninstallAddonITCase {

    private static DefaultOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)));

    @DisplayName("Namespace should be deleted:")
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "redhat-openshift-connectors",
            "redhat-openshift-connectors-observability",
    })
    public void namespaceShouldHaveBeenDeleted(String namespaceName) {
        Awaitility.await()
                .atMost(Duration.ofMinutes(15))
                .pollInterval(Duration.ofSeconds(15))
                .pollDelay(Duration.ofSeconds(0))
                .untilAsserted(() -> {
                            Optional<Namespace> namespace = client.namespaces().list().getItems().stream().filter(
                                    n -> n.getMetadata().getName().equals(namespaceName)).findFirst();
                            Assertions.assertTrue(!namespace.isPresent(), () -> namespace + " namespace still present");
                        }
                );
    }
}
