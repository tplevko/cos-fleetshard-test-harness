package org.bf2.cos.e2e.tests.listener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class MetadataTestExecutionListener implements TestExecutionListener {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataTestExecutionListener.class);

    public static final String REPORT_METADATA_CATEGORY_KEY = "REPORT_METADATA_CATEGORY_KEY";
    public static final String REPORT_METADATA_ENTRY_KEY = "REPORT_METADATA_ENTRY_KEY";

    private JSONObject metadata = new JSONObject();

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        TestExecutionListener.super.executionStarted(testIdentifier);
        if(testIdentifier.isTest()) {
            LOG.info("Running {} {}", testIdentifier.getLegacyReportingName(), testIdentifier.getDisplayName());
        }
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
        Map<String, String> map = entry.getKeyValuePairs();
        String key = map.get(REPORT_METADATA_CATEGORY_KEY);
        JSONObject category = metadata.optJSONObject(key);
        if (category == null) {
            category = new JSONObject();
            metadata.put(key, category);
        }
        JSONObject jsonEntry = new JSONObject(map);
        category.put(map.get(REPORT_METADATA_ENTRY_KEY), jsonEntry);
        jsonEntry.remove(REPORT_METADATA_ENTRY_KEY);
        jsonEntry.remove(REPORT_METADATA_CATEGORY_KEY);
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        try {
            Files.writeString(Path.of("target/addon-metadata.json"), metadata.toString());
        } catch (IOException e) {
            System.err.println("could not write metadata");
            e.printStackTrace();
        }
}
}
