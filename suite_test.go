package main

import (
	"testing"

	. "github.com/onsi/ginkgo"
	"github.com/onsi/ginkgo/reporters"
	. "github.com/onsi/gomega"
	"org.bf2/cos-fleetshard-test-harness/pkg/metadata"
	_ "org.bf2/cos-fleetshard-test-harness/pkg/tests"
)

const (
	testResultsDirectory = "/test-run-results/"
	jUnitOutputFilename  = testResultsDirectory + "junit-report.xml"
	addonMetadataName    = testResultsDirectory + "addon-metadata.json"
)

func TestHarness(t *testing.T) {
	RegisterFailHandler(Fail)
	jUnitReporter := reporters.NewJUnitReporter(jUnitOutputFilename)

	RunSpecsWithDefaultAndCustomReporters(t, "Test Harness", []Reporter{jUnitReporter})

	err := metadata.Instance.WriteToJSON(addonMetadataName)
	if err != nil {
		t.Errorf("error while writing metadata: %v", err)
	}
}
