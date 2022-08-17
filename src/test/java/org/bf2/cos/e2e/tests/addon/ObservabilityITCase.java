package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;

import java.time.Duration;

@Order(10)
@Tags(value = {
        @Tag("install")
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ObservabilityITCase {

    private static NamespacedOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)))
            .inNamespace("redhat-openshift-connectors");

    private static final CustomResourceDefinitionContext OBSERVABILITY_CONTEXT = new CustomResourceDefinitionContext.Builder()
            .withName("Observability")
            .withGroup("observability.redhat.com")
            .withVersion("v1")
            .withPlural("observabilities")
            .withScope("Namespaced")
            .build();

    @Test
    @Order(1)
    @DisplayName("observability custom resource should be created")
    public void observabilityCustomResourceShouldBeCreated() {
        Awaitility.await()
                .atMost(Duration.ofMinutes(3))
                .untilAsserted(() -> Assertions.assertNotNull(client.genericKubernetesResources(OBSERVABILITY_CONTEXT)
                        .withName("rhoc-observability-stack").get())
                );
        // TODO: check the custom resource values as well
    }

    @Test
    @Order(2)
    @DisplayName("observability namespace should be created")
    public void observabilityNamespaceShouldBeCreated() {
        Awaitility.await()
                .atMost(Duration.ofMinutes(3))
                .untilAsserted(() -> Assertions.assertNotNull(client.namespaces().withName("redhat-openshift-connectors-observability").get()));
        // TODO: check the pods in the namespace are running
    }

}
