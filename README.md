# Connectors Add-on Test Harness

 It does the following:

* Tests for the existence of the fleetshard CRDs:
  * managedconnectorclusters.cos.bf2.org
  * managedconnectoroperators.cos.bf2.org
  * managedconnectors.cos.bf2.org
* Test for the existence of Camel-k CRDs:
  * builds.camel.apache.org
  * camelcatalogs.camel.apache.org
  * integrationkits.camel.apache.org
  * integrationplatforms.camel.apache.org
  * integrations.camel.apache.org
  * kameletbindings.camel.apache.org
  * kamelets.camel.apache.org
* Test for the existence of Strimzi CRDs:
  * kafkabridges.kafka.strimzi.io
  * kafkaconnectors.kafka.strimzi.io
  * kafkaconnects.kafka.strimzi.io
  * kafkamirrormaker2s.kafka.strimzi.io
  * kafkamirrormakers.kafka.strimzi.io
  * kafkarebalances.kafka.strimzi.io
  * kafkas.kafka.strimzi.io
  * kafkatopics.kafka.strimzi.io
  * kafkausers.kafka.strimzi.io
  * strimzipodsets.core.strimzi.io
* Test whether all the required pods were started:
  * camel-k-operator
  * cos-fleetshard-sync
  * cos-fleetshard-operator-camel
  * cos-fleetshard-operator-debezium
  * strimzi-cluster-operator
* Writes out the files expected by the [osde2e](https://github.com/openshift/osde2e) test framework to the `/test-run-results` directory:
  * `junit-report.xml`
  * `addon-metadata.json`

## Running locally

To run the tests locally, build the test image and mount valid kubeconfig file

```
$ docker build -f Dockerfile -t cos-fleetshard-test-harness:latest .
$ docker run --rm -v ~/.kube:/.kube:z -it cos-fleetshard-test-harness:latest
```