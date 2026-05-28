package stepdefinitions;

import io.cucumber.java.en.*;
import novac.reporting.StepDescription;
import novac.reporting.StepHookManager;
import novac.reporting.StepReportingWrapper;
import novac.wrapper.GenericWrapper;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DemoRCASteps {

    @When("I attempt to click element with invalid locator {string}")
    @StepDescription("Attempt click on non-existent element to trigger NoSuchElementException")
    public void i_attempt_click_invalid_locator(String locator) {
        StepReportingWrapper.executeStep("Click element: " + locator, () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement element = driver.findElement(By.xpath(locator));
            element.click();
        });
    }

    @Then("the step should fail with LOCATOR_ISSUE classification")
    @StepDescription("This step should not execute — previous step should have failed")
    public void step_should_fail_locator() {
        // This step should never execute — the previous step triggers the failure
        StepReportingWrapper.executeStep("Verify LOCATOR_ISSUE classification", () -> {
            // No-op — RCA is validated in the report
        });
    }

    @When("I trigger a stale element reference by navigating away mid-interaction")
    @StepDescription("Trigger StaleElementReferenceException deterministically")
    public void i_trigger_stale_element() {
        StepReportingWrapper.executeStep("Trigger stale element reference", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            // Find an element on the current page
            WebElement element = driver.findElement(By.cssSelector("table tbody tr:first-child td"));
            // Navigate away — this invalidates all element references
            driver.get("about:blank");
            // Now interact with the stale reference
            StepHookManager.setLastFailureException(
                    new StaleElementReferenceException("stale element reference: element is not attached to the page document"));
            element.getText();
        });
    }

    @Then("the step should fail with TIMING_SYNC_ISSUE classification")
    @StepDescription("This step should not execute — previous step should have failed")
    public void step_should_fail_timing() {
        StepReportingWrapper.executeStep("Verify TIMING_SYNC_ISSUE classification", () -> {
            // No-op
        });
    }

    @When("I navigate to unreachable URL {string}")
    @StepDescription("Navigate to unreachable URL to trigger timeout/connection error")
    public void i_navigate_to_unreachable_url(String url) {
        StepReportingWrapper.executeStep("Navigate to: " + url, () -> {
            WebDriver driver = GenericWrapper.getDriver();
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(5));
            try {
                driver.get(url);
            } finally {
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(300));
            }
        });
    }

    @Then("the page should load successfully")
    @StepDescription("This step should not execute — previous step should have failed")
    public void page_should_load() {
        StepReportingWrapper.executeStep("Verify page loaded", () -> {
            // No-op
        });
    }
}
