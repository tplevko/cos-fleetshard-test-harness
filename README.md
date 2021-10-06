# Connectors Add-on Test Harness

 It does the following:

* Tests for the existence of the following CRDs:
  * managedconnectorclusters.cos.bf2.org
  * managedconnectoroperators.cos.bf2.org
  * managedconnectors.cos.bf2.org
* Writes out the files expected by the [osde2e](https://github.com/openshift/osde2e) test framework to the `/test-run-results` directory:
  * `junit-report.xml`
  * `addon-metadata.json`
