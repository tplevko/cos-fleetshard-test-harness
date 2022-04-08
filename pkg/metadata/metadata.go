package metadata

import (
	"encoding/json"
	"io/ioutil"
	"os"
)

// metadata houses metadata to be written out to the additional-metadata.json
type metadata struct {
	// Whether the CRD was found. Typically Spyglass seems to have issues displaying non-strings, so
	// this will be written out as a string despite the native JSON boolean type.

	// Managed Connector CRDs
	ManagedConnecorsCRD ManagedConnectors

	// Camel-k CRDs
	CamelKCRD CamelK

	// Strimzi CRDs
	StimziCRD Strimzi

	// Operators
	Pods Pods

    // Check communication with manager
    Communication Communication
}
type ManagedConnectors struct {
	FoundManagedConnectorClusterCRD  bool `json:"found-managedconnectorcluster-crd,string"`
	FoundManagedConnectorOperatorCRD bool `json:"found-managedconnectoroperator-crd,string"`
	FoundManagedConnectorCRD         bool `json:"found-managedconnector-crd,string"`
}

type CamelK struct {
	FoundBuildCRD               bool `json:"found-build-crd,string"`
	FoundCamelCatalogCRD        bool `json:"found-camelcatalog-crd,string"`
	FoundConsoleYAMLSampleCRD   bool `json:"found-consoleyamlsample-crd,string"`
	FoundIntegrationKitCRD      bool `json:"found-integrationkit-crd,string"`
	FoundIntegrationPlatformCRD bool `json:"found-integrationplatform-crd,string"`
	FoundIntegrationCRD         bool `json:"found-integration-crd,string"`
	FoundKameletBindingCRD      bool `json:"found-kameletbinding-crd,string"`
	FoundKameletCRD             bool `json:"found-kamelet-crd,string"`
}

type Strimzi struct {
	FoundKafkaBridgeCRD       bool `json:"found-kafkabridge-crd,string"`
	FoundKafkaConnectorCRD    bool `json:"found-kafkaconnector-crd,string"`
	FoundKafkaConnectCRD      bool `json:"found-kafkaconnect-crd,string"`
	FoundKafkaMirrorMaker2CRD bool `json:"found-kafkamirrormaker2-crd,string"`
	FoundKafkaMirrorMakerCRD  bool `json:"found-kafkamirrormaker-crd,string"`
	FoundKafkaRebalanceCRD    bool `json:"found-kafkarebalance-crd,string"`
	FoundKafkaCRD             bool `json:"found-kafka-crd,string"`
	FoundKafkaTopicCRD        bool `json:"found-kafkatopic-crd,string"`
	FoundKafkaUserCRD         bool `json:"found-kafkauser-crd,string"`
	FoundStrimziPodSetCRD     bool `json:"found-strimzipodset-crd,string"`
}

type Pods struct {
	CamelKOperator                bool `json:"camel-k-operator-running,string"`
	CosFleetshardOperatorCamel    bool `json:"cos-fleetshard-operator-camel-pod-running,string"`
	CosFleetshardOperatorDebezium bool `json:"cos-fleetshard-operator-debezium-pod-running,string"`
	CosFleetshardSyncOperator     bool `json:"cos-fleetshard-sync-pod-running,string"`
	StrimziClusterOperator        bool `json:"strimzi-cluster-operator-pod-running,string"`
}

type Communication struct {
    CosFleetshardSyncCommunication bool  `json:"cos-fleetshard-sync-communicating,string"`
}

// Instance is the singleton instance of metadata.
var Instance = metadata{}

// WriteToJSON will marshall the metadata struct and write it into the given file.
func (m *metadata) WriteToJSON(outputFilename string) (err error) {
	var data []byte
	if data, err = json.Marshal(m); err != nil {
		return err
	}

	if err = ioutil.WriteFile(outputFilename, data, os.FileMode(0644)); err != nil {
		return err
	}

	return nil
}
