package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@Order(2)
@Tags(value = {
        @Tag("install")
})
public class AddonImageReferenceITCase {

    private static NamespacedOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)))
            .inNamespace("redhat-openshift-connectors");

    @DisplayName("image should have sha reference:")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getAllDeployments")
    public void imageShouldHaveShaReference(Deployment deployment) {
        deployment.getSpec().getTemplate().getSpec().getContainers().forEach(c -> {
            Assertions.assertTrue(c.getImage().contains("@sha256:"), () -> "no sha found in " + c.getImage());
        });

    }

    static Stream<Deployment> getAllDeployments() {
        return client.apps().deployments().list().getItems().stream();
    }

}
