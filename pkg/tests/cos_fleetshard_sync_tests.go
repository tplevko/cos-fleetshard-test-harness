package test

import (
	"context"
	"flag"
	"path/filepath"

	"github.com/onsi/ginkgo"
	. "github.com/onsi/gomega"
	"k8s.io/apiextensions-apiserver/pkg/client/clientset/clientset"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
	"org.bf2/cos-fleetshard-test-harness/pkg/metadata"
)

var _ = ginkgo.Describe("cos-fleetshard-sync", func() {
	defer ginkgo.GinkgoRecover()
	config, err := rest.InClusterConfig()

	if err != nil {
		var kubeconfig *string
		if home := homedir.HomeDir(); home != "" {
			kubeconfig = flag.String("kubeconfig", filepath.Join(home, ".kube", "config"), "(optional) absolute path to the kubeconfig file")
		}
		// use the current context in kubeconfig
		config, err = clientcmd.BuildConfigFromFlags("", *kubeconfig)
	}
	if err != nil {
		panic(err.Error())
	}

	ginkgo.It("ManagedConnectorCluster CRD exists", func() {
		apiextensions, err := clientset.NewForConfig(config)
		Expect(err).NotTo(HaveOccurred())

		// Make sure the CRD exists
		ctx := context.TODO()
		_, err = apiextensions.ApiextensionsV1().CustomResourceDefinitions().Get(ctx, "managedconnectorclusters.cos.bf2.org", v1.GetOptions{})

		if err != nil {
			metadata.Instance.FoundManagedConnectorClusterCRD = false
		} else {
			metadata.Instance.FoundManagedConnectorClusterCRD = true
		}

		Expect(err).NotTo(HaveOccurred())
	})

	ginkgo.It("ManagedConnectorOperator CRD exists", func() {
		apiextensions, err := clientset.NewForConfig(config)
		Expect(err).NotTo(HaveOccurred())

		// Make sure the CRD exists
		ctx := context.TODO()
		_, err = apiextensions.ApiextensionsV1().CustomResourceDefinitions().Get(ctx, "managedconnectoroperators.cos.bf2.org", v1.GetOptions{})

		if err != nil {
			metadata.Instance.FoundManagedConnectorOperatorCRD = false
		} else {
			metadata.Instance.FoundManagedConnectorOperatorCRD = true
		}

		Expect(err).NotTo(HaveOccurred())
	})

	ginkgo.It("ManagedConnector CRD exists", func() {
		apiextensions, err := clientset.NewForConfig(config)
		Expect(err).NotTo(HaveOccurred())

		// Make sure the CRD exists
		ctx := context.TODO()
		_, err = apiextensions.ApiextensionsV1().CustomResourceDefinitions().Get(ctx, "managedconnectors.cos.bf2.org", v1.GetOptions{})

		if err != nil {
			metadata.Instance.FoundManagedConnectorCRD = false
		} else {
			metadata.Instance.FoundManagedConnectorCRD = true
		}

		Expect(err).NotTo(HaveOccurred())
	})

})
