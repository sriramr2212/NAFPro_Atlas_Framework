package stepdefinitions;

import io.cucumber.java.en.*;
import novac.ai.defect.*;
import novac.ai.rca.*;
import novac.reporting.StepDescription;
import novac.reporting.StepReportingWrapper;
import novac.reporting.StepHookManager;
import novac.utils.RunManager;
import novac.utils.TestContext;
import novac.wrapper.GenericWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DemoDefectWriterSteps {

    private static final Logger logger = LogManager.getLogger(DemoDefectWriterSteps.class);

    @When("I trigger a locator failure on element {string}")
    @StepDescription("Intentionally trigger NoSuchElementException for defect writer validation")
    public void triggerLocatorFailure(String elementId) {
        simulateFailureAndGenerateDefect(() -> {
            WebDriver driver = GenericWrapper.getDriver();
            // Use a brittle positional XPath to ensure LOCATOR_ISSUE classification
            driver.findElement(By.xpath(
                    "//div[3]/div[2]/section/div[4]/div/div/span[contains(@class,'ant-btn')][2]/button"));
        });
    }

    @When("I trigger an assertion failure expecting {string} but finding {string}")
    @StepDescription("Intentionally trigger AssertionError for defect writer validation")
    public void triggerAssertionFailure(String expected, String actual) {
        simulateFailureAndGenerateDefect(() -> {
            throw new AssertionError(String.format(
                    "expected [%s] but found [%s]", expected, actual));
        });
    }

    @When("I trigger a stale element failure on the table")
    @StepDescription("Intentionally trigger StaleElementReferenceException for defect writer validation")
    public void triggerStaleElementFailure() {
        simulateFailureAndGenerateDefect(() -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement row = driver.findElement(By.cssSelector("table tbody tr"));
            // Remove the element from DOM via JS to make it stale
            ((JavascriptExecutor) driver).executeScript("arguments[0].remove();", row);
            // Now access the stale element
            row.getText();
        });
    }

    @When("I trigger an environment failure by navigating to {string}")
    @StepDescription("Intentionally trigger environment/network failure for defect writer validation")
    public void triggerEnvironmentFailure(String url) {
        simulateFailureAndGenerateDefect(() -> {
            // Simulate a connection refused error (real Selenium pattern)
            throw new org.openqa.selenium.WebDriverException(
                    "unknown error: net::ERR_CONNECTION_REFUSED\n" +
                    "  (Session info: chrome=120.0)\n" +
                    "  (Driver info: chromedriver=120.0)");
        });
    }

    @Then("the defect draft should have classification {string}")
    @StepDescription("Verify defect classification")
    public void verifyClassification(String expected) {
        StepReportingWrapper.executeStep("Verify classification: " + expected, () -> {
            DefectDraft draft = getDefectDraft();
            DefectClassification expectedClassification = DefectClassification.valueOf(expected);
            assertEquals(expectedClassification, draft.getDefectClassification(),
                    "Classification mismatch");
            logger.info("Classification verified: {} ✓", expected);
        });
    }

    @Then("the defect draft should have classification {string} or {string}")
    @StepDescription("Verify defect classification (either/or)")
    public void verifyClassificationEither(String expected1, String expected2) {
        StepReportingWrapper.executeStep("Verify classification: " + expected1 + " or " + expected2, () -> {
            DefectDraft draft = getDefectDraft();
            DefectClassification c1 = DefectClassification.valueOf(expected1);
            DefectClassification c2 = DefectClassification.valueOf(expected2);
            assertTrue(draft.getDefectClassification() == c1 || draft.getDefectClassification() == c2,
                    "Classification should be " + expected1 + " or " + expected2 +
                            " but was " + draft.getDefectClassification());
            logger.info("Classification verified: {} ✓", draft.getDefectClassification());
        });
    }

    @Then("the defect draft should have owner {string}")
    @StepDescription("Verify defect owner")
    public void verifyOwner(String expected) {
        StepReportingWrapper.executeStep("Verify owner: " + expected, () -> {
            DefectDraft draft = getDefectDraft();
            assertEquals(expected, draft.getLikelyOwner(), "Owner mismatch");
            logger.info("Owner verified: {} ✓", expected);
        });
    }

    @Then("the defect draft should have owner {string} or {string}")
    @StepDescription("Verify defect owner (either/or)")
    public void verifyOwnerEither(String expected1, String expected2) {
        StepReportingWrapper.executeStep("Verify owner: " + expected1 + " or " + expected2, () -> {
            DefectDraft draft = getDefectDraft();
            assertTrue(expected1.equals(draft.getLikelyOwner()) || expected2.equals(draft.getLikelyOwner()),
                    "Owner should be " + expected1 + " or " + expected2 +
                            " but was " + draft.getLikelyOwner());
            logger.info("Owner verified: {} ✓", draft.getLikelyOwner());
        });
    }

    @Then("the defect draft should have severity {string} or {string}")
    @StepDescription("Verify defect severity (either/or)")
    public void verifySeverityEither(String expected1, String expected2) {
        StepReportingWrapper.executeStep("Verify severity: " + expected1 + " or " + expected2, () -> {
            DefectDraft draft = getDefectDraft();
            assertTrue(expected1.equals(draft.getSeverity()) || expected2.equals(draft.getSeverity()),
                    "Severity should be " + expected1 + " or " + expected2 +
                            " but was " + draft.getSeverity());
            logger.info("Severity verified: {} ✓", draft.getSeverity());
        });
    }

    @Then("the defect draft title should contain {string}")
    @StepDescription("Verify defect title contains expected text")
    public void verifyTitleContains(String expected) {
        StepReportingWrapper.executeStep("Verify title contains: " + expected, () -> {
            DefectDraft draft = getDefectDraft();
            assertTrue(draft.getDefectTitle().toLowerCase().contains(expected.toLowerCase()),
                    "Title should contain '" + expected + "' but was: " + draft.getDefectTitle());
            logger.info("Title verified: '{}' contains '{}' ✓", draft.getDefectTitle(), expected);
        });
    }

    @Then("the defect draft title should contain {string} or {string}")
    @StepDescription("Verify defect title contains expected text (either/or)")
    public void verifyTitleContainsEither(String expected1, String expected2) {
        StepReportingWrapper.executeStep("Verify title contains: " + expected1 + " or " + expected2, () -> {
            DefectDraft draft = getDefectDraft();
            String titleLower = draft.getDefectTitle().toLowerCase();
            assertTrue(titleLower.contains(expected1.toLowerCase()) || titleLower.contains(expected2.toLowerCase()),
                    "Title should contain '" + expected1 + "' or '" + expected2 +
                            "' but was: " + draft.getDefectTitle());
            logger.info("Title verified: '{}' ✓", draft.getDefectTitle());
        });
    }

    @Then("the defect draft should be renderable in all formats")
    @StepDescription("Verify HTML, Markdown, and Plain Text rendering")
    public void verifyAllFormats() {
        StepReportingWrapper.executeStep("Verify all output formats render correctly", () -> {
            DefectDraft draft = getDefectDraft();

            // HTML
            String html = DefectTemplateRenderer.renderHtmlCard(draft);
            assertNotNull(html);
            assertTrue(html.contains("defect-card"), "HTML should contain defect-card class");
            assertTrue(html.contains("AI Defect Draft"), "HTML should contain title");
            assertTrue(html.contains(draft.getDefectClassification().getDisplayName()),
                    "HTML should contain classification");

            // Markdown
            String md = DefectTemplateRenderer.renderMarkdown(draft);
            assertNotNull(md);
            assertTrue(md.contains("## "), "Markdown should have title header");
            assertTrue(md.contains("**Classification:**"), "Markdown should have classification");
            assertTrue(md.contains("### Reproduction Steps"), "Markdown should have repro steps");

            // Plain Text
            String txt = DefectTemplateRenderer.renderPlainText(draft);
            assertNotNull(txt);
            assertTrue(txt.contains("DEFECT DRAFT"), "Plain text should have header");
            assertTrue(txt.contains("Title:"), "Plain text should have title");

            logger.info("All 3 output formats verified ✓ (HTML: {}chars, MD: {}chars, TXT: {}chars)",
                    html.length(), md.length(), txt.length());
        });
    }

    // --- Internal helpers ---

    private void simulateFailureAndGenerateDefect(Runnable failureAction) {
        // Capture the failure, run RCA + Defect Writer manually (since we catch the exception)
        Throwable captured = null;
        String stepName = "Simulated failure step";
        try {
            failureAction.run();
        } catch (Throwable t) {
            captured = t;
        }

        if (captured == null) {
            throw new RuntimeException("Expected failure did not occur");
        }

        // Run RCA
        RCAContext context = RCAContext.builder(stepName, captured)
                .moduleName(safeGetModule())
                .testCaseId(safeGetTestCaseId())
                .build();

        RCAAssistant assistant = new RCAAssistant();
        RCAResult rcaResult = assistant.analyze(context);
        TestContext.setLastRCAResult(rcaResult);

        // Store RCA result
        String tcId = safeGetTestCaseId();
        if (tcId != null) {
            novac.reporting.managers.TestRunReportManager.getInstance().storeRCAResult(tcId, rcaResult);
        }

        // Run Defect Writer
        DefectContext defectContext = DefectContext.builder(rcaResult)
                .failedStep(stepName)
                .testCaseId(tcId != null ? tcId : "")
                .moduleName(safeGetModule())
                .exceptionMessage(captured.getMessage())
                .build();

        DefectWriterAssistant writer = new DefectWriterAssistant();
        DefectDraft draft = writer.generate(defectContext);
        TestContext.setLastDefectDraft(draft);

        // Store for report rendering
        if (tcId != null) {
            novac.reporting.managers.TestRunReportManager.getInstance().storeDefectDraft(tcId, draft);
        }

        logger.info("Failure simulated → RCA: {} ({}%) → Defect: {} [{}]",
                rcaResult.getCategory().getDisplayName(),
                rcaResult.getConfidenceScore(),
                draft.getDefectClassification().getDisplayName(),
                draft.getSeverity());

        // Log to report
        StepReportingWrapper.recordManualStep(
                String.format("RCA: %s (%d%%) | Defect: %s [%s] | Owner: %s",
                        rcaResult.getCategory().getDisplayName(),
                        rcaResult.getConfidenceScore(),
                        draft.getDefectClassification().getDisplayName(),
                        draft.getSeverity(),
                        draft.getLikelyOwner()),
                "INFO");
    }

    private DefectDraft getDefectDraft() {
        DefectDraft draft = TestContext.getLastDefectDraft();
        if (draft == null) {
            throw new RuntimeException("No defect draft available. Failure simulation may not have run.");
        }
        return draft;
    }

    private String safeGetModule() {
        try { return TestContext.getCurrentModule(); }
        catch (Exception e) { return "DefectWriter"; }
    }

    private String safeGetTestCaseId() {
        try { return TestContext.getCurrentTestCaseId(); }
        catch (Exception e) { return null; }
    }
}
