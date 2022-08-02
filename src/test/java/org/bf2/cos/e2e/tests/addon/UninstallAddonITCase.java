package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

@Tags(value = {
        @Tag("uninstall")
})
public class UninstallAddonITCase {

    private static DefaultOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)));

    @Test
    public void namespaceShouldHaveBeenDeleted() {
        Awaitility.await()
                .atMost(Duration.ofMinutes(15))
                .pollInterval(Duration.ofSeconds(15))
                .pollDelay(Duration.ofSeconds(0))
                .untilAsserted(() -> {
                            Optional<Namespace> namespace = client.namespaces().list().getItems().stream().filter(
                                    n -> n.getMetadata().getName().equals("redhat-openshift-connectors")).findFirst();
                            Assertions.assertTrue(!namespace.isPresent(), () -> namespace + " namespace still present");
                        }
                );
    }
}
