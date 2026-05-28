package runners;

import io.cucumber.core.cli.Main;
import novac.utils.RunManager;
import novac.utils.WaitHandler;
import novac.reporting.managers.ReportManager;
import novac.utils.IterativeFeatureGenerator;

import org.apache.logging.log4j.core.config.Configurator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestRunner {
    
    public static void main(String[] args) {
        System.out.println("[START] Starting NAF_TestSuite_Template Execution...\n");
        
        // CRITICAL: Initialize TestRunReportManager FIRST to create Run folder
        System.out.println("[DEBUG] STEP 1: Initializing TestRunReportManager (creates Run folder)");
        novac.reporting.managers.TestRunReportManager reportManager = novac.reporting.managers.TestRunReportManager.getInstance();
        System.out.println("[DEBUG] STEP 2: TestRunReportManager initialized");
        System.out.println("   [DIR] Run ID: " + reportManager.getRunId());
        System.out.println("   [DIR] Run Directory: " + new java.io.File(reportManager.getRunDirectory()).getAbsolutePath());
        
        // Set Run directory for Log4j2 file appender and force reconfiguration
        System.setProperty("runDir", reportManager.getRunDirectory());
        Configurator.reconfigure();
        
        // Initialize ReportManager - it will use the existing Run directory
        System.out.println("\n[DEBUG] STEP 3: Initializing ReportManager (uses existing Run folder)");
        novac.reporting.managers.ReportManager.getInstance().initializeReports();
        System.out.println("[DEBUG] STEP 4: ReportManager initialized\n");
        
        // Initialize RunManager
        RunManager.initialize();
        
        // Build cucumber arguments - UPDATED glue paths
        List<String> cucumberArgsList = new ArrayList<>(Arrays.asList(
            "--glue", "novac.hooks",
            "--glue", "stepdefinitions"
        ));
        
        // Add plugin configurations
        List<String> plugins = ReportManager.getInstance().getCucumberPluginConfiguration();
        for (String plugin : plugins) {
            cucumberArgsList.add("--plugin");
            cucumberArgsList.add(plugin);
        }
        
        // Resolve suite-based tags (overrides includeTags if selectedSuite is set)
        String suiteTags = SuiteResolver.resolveSelectedSuiteTags();
        
        // Add tag expression from RunManager or Suite
        String tagExpression = (suiteTags != null) ? suiteTags : RunManager.getTagExpression();
        if (!tagExpression.isEmpty()) {
            cucumberArgsList.add("--tags");
            cucumberArgsList.add(tagExpression);
            System.out.println("[OK] Applied tag filter: " + tagExpression);
            System.out.println("[CHECK] Cucumber will filter scenarios with: " + tagExpression);
        } else {
            System.out.println("[WARN] No tag filter applied - all scenarios will run");
        }
        
        // Collect feature paths based on allowed modules (folder-based resolution)
        String includeTags = (suiteTags != null) ? suiteTags : RunManager.getIncludeTags();
        java.util.Set<String> allowedModules = ModuleResolver.resolveModules(includeTags);
        List<String> originalFeaturePaths = new ArrayList<>();
        
        if (allowedModules.isEmpty()) {
            originalFeaturePaths.add("src/test/resources/features");
            System.out.println("[CHECK] No tag filter - scanning all modules: features/");
        } else {
            System.out.println("[CHECK] Allowed modules: " + allowedModules);
            
            for (String module : allowedModules) {
                String modulePath = "src/test/resources/features/" + module;
                originalFeaturePaths.add(modulePath);
                System.out.println("[CHECK] Adding feature path: " + modulePath);
            }
        }
        
        // ITERATION SUPPORT: Generate iterated features
        System.out.println("\n[ITER] Generating iterated feature files...");
        List<String> featurePaths = IterativeFeatureGenerator.generateIterativeFeatures(originalFeaturePaths);
        System.out.println("[OK] Feature generation complete\n");

        
        // Add feature paths to cucumber arguments
        cucumberArgsList.addAll(featurePaths);
        
        String[] cucumberArgs = cucumberArgsList.toArray(new String[0]);
        
        try {
            Main.main(cucumberArgs);
        } catch (Exception e) {
            if (RunManager.isFailFastEnabled() && WaitHandler.isFatalExecutionStopped()) {
                System.err.println("[FATAL] Browser/session lost. Execution aborted.");
            } else {
                throw e;
            }
        }
        
        if (RunManager.isFailFastEnabled() && WaitHandler.isFatalExecutionStopped()) {
            System.err.println("[FATAL] Execution terminated due to unrecoverable session failure.");
        } else {
            System.out.println("[OK] Test execution completed");
        }
    }
}
