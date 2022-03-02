package test

import (
	"context"
	"flag"
	"fmt"
	"path/filepath"
	"strings"
	"time"

	"github.com/onsi/ginkgo"
	"github.com/onsi/ginkgo/extensions/table"
	. "github.com/onsi/gomega"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apiextensions-apiserver/pkg/client/clientset/clientset"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
	"org.bf2/cos-fleetshard-test-harness/pkg/metadata"
)

var config *rest.Config

const cosNamespace string = "redhat-openshift-connectors"

func init() {
	// Try inClusterConfig, fallback to using ~/.kube/config
	runtimeConfig, err := rest.InClusterConfig()
	if err != nil {
		var kubeconfig *string

		if home := homedir.HomeDir(); home != "" {
			kubeconfig = flag.String("kubeconfig", filepath.Join(home, ".kube", "config"), "(optional) absolute path to the kubeconfig file")
		} else {
			kubeconfig = flag.String("kubeconfig", "", "absolute path to the kubeconfig file")
		}
		// use the current context in kubeconfig
		config, err = clientcmd.BuildConfigFromFlags("", *kubeconfig)
		if err != nil {
			panic(err.Error())
		}
	} else {
		config = runtimeConfig
	}
}

var _ = ginkgo.Describe("cos-fleetshard-sync test harness", func() {

	table.DescribeTable("CRD's verification",
		func(crdName string, found *bool) {
			apiextensions, err := clientset.NewForConfig(config)
			Expect(err).NotTo(HaveOccurred())
			ctx := context.TODO()
			_, err = apiextensions.ApiextensionsV1().CustomResourceDefinitions().Get(ctx, crdName, v1.GetOptions{})

			if err != nil {
				*found = false
			} else {
				*found = true
			}
			Expect(err).NotTo(HaveOccurred())
		},
		// ManagedConnector CRD's
		table.Entry("ManagedConnectorCluster CRD exists", "managedconnectorclusters.cos.bf2.org", &metadata.Instance.ManagedConnecorsCRD.FoundManagedConnectorClusterCRD),
		table.Entry("ManagedConnectorOperator CRD exists", "managedconnectoroperators.cos.bf2.org", &metadata.Instance.ManagedConnecorsCRD.FoundManagedConnectorOperatorCRD),
		table.Entry("ManagedConnector CRD exists", "managedconnectors.cos.bf2.org", &metadata.Instance.ManagedConnecorsCRD.FoundManagedConnectorCRD),
		// Camel-k CRD's
		table.Entry("Build CRD exists", "builds.camel.apache.org", &metadata.Instance.CamelKCRD.FoundBuildCRD),
		table.Entry("CamelCatalog CRD exists", "camelcatalogs.camel.apache.org", &metadata.Instance.CamelKCRD.FoundCamelCatalogCRD),
		table.Entry("IntegrationKit CRD exists", "integrationkits.camel.apache.org", &metadata.Instance.CamelKCRD.FoundIntegrationCRD),
		table.Entry("IntegrationPlatform CRD exists", "integrationplatforms.camel.apache.org", &metadata.Instance.CamelKCRD.FoundIntegrationPlatformCRD),
		table.Entry("Integrations CRD exists", "integrations.camel.apache.org", &metadata.Instance.CamelKCRD.FoundIntegrationCRD),
		table.Entry("KameletBinding CRD exists", "kameletbindings.camel.apache.org", &metadata.Instance.CamelKCRD.FoundKameletBindingCRD),
		table.Entry("Kamelet CRD exists", "kamelets.camel.apache.org", &metadata.Instance.CamelKCRD.FoundKameletCRD),
		// Strimzi CRD's
		table.Entry("KafkaBridge CRD exists", "kafkabridges.kafka.strimzi.io", &metadata.Instance.StimziCRD.FoundKafkaBridgeCRD),
		table.Entry("KafkaConnector CRD exists", "kafkaconnectors.kafka.strimzi.io", &metadata.Instance.StimziCRD.FoundKafkaConnectorCRD),
		table.Entry("KafkaConnect CRD exists", "kafkaconnects.kafka.strimzi.io", &metadata.Instance.StimziCRD.FoundKafkaConnectCRD),
		table.Entry("KafkaMirrorMakers2 CRD exists", "kafkamirrormaker2s.kafka.strimzi.io", &metadata.Instance.StimziCRD.FoundKafkaMirrorMaker2CRD),
		table.Entry("KafkaMirrorMaker CRD exists", "kafkamirrormakers.kafka.strimzi.io", &metadata.Instance.StimziCRD.FoundKafkaMirrorMakerCRD),
		table.Entry("KafkaBalance CRD exists", "kafkarebalances.kafka.strimzi.io", &metadata.Instance.StimziCRD.FoundKafkaRebalanceCRD),
		table.Entry("Kafka CRD exists", "kafkas.kafka.strimzi.io", &metadata.Instance.StimziCRD.FoundKafkaCRD),
		table.Entry("KafkaTopic CRD exists", "kafkatopics.kafka.strimzi.io", &metadata.Instance.StimziCRD.FoundKafkaTopicCRD),
		table.Entry("KafkaUsers CRD exists", "kafkausers.kafka.strimzi.io", &metadata.Instance.StimziCRD.FoundKafkaUserCRD),
		table.Entry("StrimziPodSet CRD exists", "strimzipodsets.core.strimzi.io", &metadata.Instance.StimziCRD.FoundStrimziPodSetCRD),
	)

	table.DescribeTable("pods running verification",
		func(podName string, found *bool) {
			kubernetes, err := kubernetes.NewForConfig(config)
			Expect(err).NotTo(HaveOccurred())
			var checkErr error = nil
			retry := 0

			// Wait 3 minutes for every pod to start
			for retry <= 18 {
				result, pod := CheckPodStatus(kubernetes, podName)
				if result {
					break
				} else if retry == 18 {
					checkErr = fmt.Errorf("Pod is not running after waiting 3 minutes : %v", pod)
				} else {
					time.Sleep(10 * time.Second)
				}
				retry = retry + 1
				fmt.Printf("%d Retry to check pods status(every 10s)\n", retry)
			}

			if err != nil {
				*found = false
			} else {
				*found = true
			}
			Expect(checkErr).NotTo(HaveOccurred())
		},
		table.Entry("Camel-k operator exists", "camel-k-operator", &metadata.Instance.Pods.CamelKOperator),
		table.Entry("COS fleetshard sync operator exists", "cos-fleetshard-sync", &metadata.Instance.Pods.CosFleetshardSyncOperator),
		table.Entry("COS fleetshard camel operator exists", "cos-fleetshard-operator-camel", &metadata.Instance.Pods.CosFleetshardOperatorCamel),
		table.Entry("COS fleetshard debezium operator exists", "cos-fleetshard-operator-debezium", &metadata.Instance.Pods.CosFleetshardOperatorDebezium),
		table.Entry("Strimzi cluster operator exists", "strimzi-cluster-operator", &metadata.Instance.Pods.StrimziClusterOperator),
	)
})

func CheckPodStatus(clientset *kubernetes.Clientset, podName string) (bool, corev1.Pod) {
	pods, err := clientset.CoreV1().Pods(cosNamespace).List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		panic(err.Error())
	}
	Expect(pods.Items).NotTo(BeEmpty())

	for _, pod := range pods.Items {
		if strings.Contains(pod.GetName(), podName) {
			containers := pod.Status.ContainerStatuses
			for i := range containers {
				if !containers[i].Ready {
					return false, pod
				}
			}
		}
	}
	return true, corev1.Pod{}
}
