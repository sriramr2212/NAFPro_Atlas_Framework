package stepdefinitions;


import io.cucumber.java.en.*;

import novac.wrapper.GenericWrapper;
import novac.wrapper.AntDTableHandler;
import novac.reporting.StepDescription;
import novac.reporting.StepReportingWrapper;
import novac.utils.ActionType;
import novac.utils.ConstantsResolver;
import novac.utils.ElementScrollHelper;
import novac.utils.GenericActionHandler;
import novac.utils.MissingDataTracker;
import novac.utils.PageFactory;
import novac.utils.PageResolver;
import novac.utils.TestDataManager;
import novac.utils.TestContext;
import novac.utils.WaitHandler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import io.cucumber.java.Scenario;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CommonSteps {
    
    private static final Logger logger = LogManager.getLogger(CommonSteps.class);

    
    // ========== NAVIGATION STEPS ==========
    @Given("the application is available")
    @StepDescription("Verify application availability")
    public void the_application_is_available() {
        StepReportingWrapper.recordStepInfo("Verify application is available");
    }
    

    // ========== UNIVERSAL CLICK ACTION ==========
    @When("^I click on \"([^\"]+)\"$")
    @StepDescription("Click on element")
    public void i_click_on_element_simple(String element) {
        handleElementClick(element);
    }
    
    @When("^I click on \"([^\"]+)\" on \"([^\"]+)\" page$")
    @StepDescription("Click on element on page")
    public void i_click_on_element_on_page(String element, String page) {
        handleElementClick(element, page);
    }
    
    @When("^I click on \"([^\"]+)\" in \"([^\"]+)\" on \"([^\"]+)\" page$")
    @StepDescription("Click on element in section on page")
    public void i_click_on_element_in_section_on_page(String element, String section, String page) {
        handleElementClick(element, section, page);
    }
    

    
    private void handleElementClick(String element) {
        handleElementClick(element, (String) null, (String) null);
    }
    
    private void handleElementClick(String element, String page) {
        handleElementClick(element, null, page);
    }
    
    private void handleElementClick(String element, String section, String page) {
        // Resolve page with fallback
        String resolvedPage = resolvePage(page);
        String resolvedSection = resolveSection(section, resolvedPage);
        
        // Validate context before proceeding
        validateClickContext(element, resolvedPage, resolvedSection);
        
        // Build step description
        String stepDescription = buildClickDescription(element, resolvedSection, resolvedPage);
        
        // Log resolution before execution
        logger.info("Click resolution: element='{}', section='{}', page='{}'", 
            element, resolvedSection != null ? resolvedSection : "none", resolvedPage);
        
        StepReportingWrapper.executeStep(stepDescription, () -> {
            try {
                // Scroll element into view before click to avoid viewport issues
                ElementScrollHelper.scrollIntoViewIfNeeded(resolvedPage, element);

                // Enhanced click with retry mechanism
                int maxRetries = 3;
                Exception lastException = null;
                
                for (int attempt = 1; attempt <= maxRetries; attempt++) {
                    try {
                        GenericActionHandler.handleElementClick(resolvedPage, element, resolvedSection);
                        logger.info("Click successful on attempt {} for element '{}'", attempt, element);
                        return; // Success, exit retry loop
                    } catch (org.openqa.selenium.StaleElementReferenceException | 
                             org.openqa.selenium.ElementNotInteractableException e) {
                        lastException = e;
                        if (attempt < maxRetries) {
                            logger.warn("Click attempt {} failed for '{}', retrying: {}", attempt, element, e.getMessage());
                        try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        }
                    }
                }
                
                // All retries failed
                String errorMsg = String.format("Failed to click element '%s' on page '%s'%s after %d attempts: %s", 
                    element, resolvedPage, 
                    resolvedSection != null ? " in section '" + resolvedSection + "'" : "", 
                    maxRetries, lastException.getMessage());
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg, lastException);
                
            } catch (Exception e) {
                if (!(e instanceof RuntimeException)) {
                    String errorMsg = String.format("Unexpected error clicking element '%s' on page '%s'%s: %s", 
                        element, resolvedPage, 
                        resolvedSection != null ? " in section '" + resolvedSection + "'" : "", 
                        e.getMessage());
                    logger.error(errorMsg);
                    throw new RuntimeException(errorMsg, e);
                }
                throw e;
            }
        });
    }
    
    // ========== VALIDATION METHODS ==========
    private void validateClickContext(String element, String page, String section) {
        if (element == null || element.trim().isEmpty()) {
            throw new IllegalArgumentException("Element name cannot be null or empty");
        }
        logger.debug("Click context validated: element='{}', page='{}', section='{}'", 
            element, page, section);
    }
    
    // ========== UNIVERSAL DROPDOWN SELECTION ==========
    @When("I select {string} from {string} on {string} page")
    @StepDescription("Select option from dropdown on page")
    public void i_select_from_dropdown_on_page(String option, String dropdown, String page) {
        String targetPage = resolvePage(page);
        String resolvedOption = resolveTestData(option, targetPage);
        String stepDescription = String.format("Select '%s' from '%s' on '%s' page", 
            option.startsWith("TD_") ? "test data" : option, dropdown, targetPage);
        
        StepReportingWrapper.executeStepWithContext(
            stepDescription, dropdown, targetPage, resolvedOption,
            () -> GenericActionHandler.handleElementDropdown(targetPage, dropdown, resolvedOption)
        );
    }
    
    @When("I select {string} from {string} in modal")
    @StepDescription("Select option from dropdown in modal")
    public void i_select_from_dropdown_in_modal(String option, String dropdown) {
        String targetPage = getCurrentModule() != null ? getCurrentModule() + "Page" : "LoginPage";
        String resolvedOption = resolveTestData(option, targetPage);
        String stepDescription = String.format("Select '%s' from '%s' in modal", 
            option.startsWith("TD_") ? "test data" : option, dropdown);
        
        StepReportingWrapper.executeStepWithContext(
            stepDescription, dropdown, targetPage, resolvedOption,
            () -> GenericActionHandler.handleElementDropdownInModal(targetPage, dropdown, resolvedOption)
        );
    }
    
    // ========== UNIVERSAL DATE PICKER ==========
    @When("I set {string} date to {string} on {string} page")
    @StepDescription("Set date field on page")
    public void i_set_date_on_page(String field, String dateValue, String page) {
        String targetPage = resolvePage(page);
        String resolvedDate = resolveTestData(dateValue, targetPage);
        String stepDescription = String.format("Set '%s' date to '%s' on '%s' page", 
            field, dateValue.startsWith("TD_") ? "test data" : dateValue, targetPage);
        
        if (resolvedDate == null || resolvedDate.trim().isEmpty()) {
            logger.info("Skipping date '{}' on '{}' - empty test data", field, targetPage);
            return;
        }
        StepReportingWrapper.executeStepWithContext(
            stepDescription, field, targetPage, resolvedDate,
            () -> {
                try {
                    ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(targetPage, field);
                    LocalDate date = novac.utils.DatePickerUtils.parseDate(resolvedDate);
                    
                    novac.wrapper.AntDDatePicker picker = new novac.wrapper.AntDDatePicker(GenericWrapper.getDriver(), 20);
                    picker.selectDate(By.xpath(info.getXpath()), date);
                    logger.info("Successfully set '{}' date to '{}'", field, resolvedDate);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to set date for field: " + field, e);
                }
            }
        );
    }
    
    @When("I set {string} date to {string} in modal")
    @StepDescription("Set date field in modal")
    public void i_set_date_in_modal(String field, String dateValue) {
        String targetPage = getCurrentModule() != null ? getCurrentModule() + "Page" : "LoginPage";
        String resolvedDate = resolveTestData(dateValue, targetPage);
        String stepDescription = String.format("Set '%s' date to '%s' in modal", 
            field, dateValue.startsWith("TD_") ? "test data" : dateValue);
        
        if (resolvedDate == null || resolvedDate.trim().isEmpty()) {
            logger.info("Skipping date '{}' in modal - empty test data", field);
            return;
        }
        StepReportingWrapper.executeStepWithContext(
            stepDescription, field, targetPage, resolvedDate,
            () -> {
                try {
                    ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(targetPage, field);
                    LocalDate date = novac.utils.DatePickerUtils.parseDate(resolvedDate);
                    
                    novac.wrapper.AntDDatePicker picker = new novac.wrapper.AntDDatePicker(GenericWrapper.getDriver(), 20);
                    picker.selectDate(By.xpath(info.getXpath()), date);
                    logger.info("Successfully set '{}' date to '{}' in modal", field, resolvedDate);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to set date in modal for field: " + field, e);
                }
            }
        );
    }
    
    @When("I set {string} datetime to {string} on {string} page")
    @StepDescription("Set datetime field on page")
    public void i_set_datetime_on_page(String field, String dateTimeValue, String page) {
        String targetPage = resolvePage(page);
        String resolvedDateTime = resolveTestData(dateTimeValue, targetPage);
        String stepDescription = String.format("Set '%s' datetime to '%s' on '%s' page", 
            field, dateTimeValue.startsWith("TD_") ? "test data" : dateTimeValue, targetPage);
        
        StepReportingWrapper.executeStepWithContext(
            stepDescription, field, targetPage, resolvedDateTime,
            () -> {
                try {
                    ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(targetPage, field);
                    WebElement element = WaitHandler.waitForVisibilityWithHealing(info.getXpath().startsWith("//") ? By.xpath(info.getXpath()) : By.xpath(info.getXpath()), ActionType.INPUT, field, targetPage);
                    novac.utils.DatePickerUtils.setDatePickerValue(element, resolvedDateTime);
                    logger.info("Successfully set '{}' datetime to '{}'", field, resolvedDateTime);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to set datetime for field: " + field, e);
                }
            }
        );
    }
    
    // ========== UNIVERSAL TOGGLE SWITCH ==========
    @When("I set {string} to {string} on {string} page")
    @StepDescription("Set toggle switch on page")
    public void i_set_toggle_on_page(String toggle, String state, String page) {
        String targetPage = resolvePage(page);
        String resolvedState = resolveTestData(state, targetPage);
        if (resolvedState == null || resolvedState.trim().isEmpty()) {
            logger.info("Skipping toggle '{}' on '{}' - empty test data", toggle, targetPage);
            return;
        }
        boolean targetState = "true".equalsIgnoreCase(resolvedState) || "yes".equalsIgnoreCase(resolvedState) || "on".equalsIgnoreCase(resolvedState);
        
        String stepDescription = String.format("Set '%s' to '%s' on '%s' page", toggle, resolvedState, targetPage);
        
        StepReportingWrapper.executeStepWithContext(
            stepDescription, toggle, targetPage, resolvedState,
            () -> GenericActionHandler.handleElementToggle(targetPage, toggle, targetState)
        );
    }
    
    // ========== UNIVERSAL RADIO BUTTON SELECTION ==========
    @When("I select radio option {string} from {string} on {string} page")
    @StepDescription("Select radio option on page")
    public void i_select_radio_option_on_page(String option, String group, String page) {
        String targetPage = resolvePage(page);
        String resolvedOption = resolveTestData(option, targetPage);
        String stepDescription = String.format("Select radio option '%s' from '%s' on '%s' page",
            option.startsWith("TD_") ? "test data" : option, group, targetPage);

        StepReportingWrapper.executeStepWithContext(
            stepDescription, group, targetPage, resolvedOption,
            () -> GenericActionHandler.handleElementClick(targetPage, group, resolvedOption)
        );
    }

    // ========== UNIVERSAL CHECKBOX ACTION ==========
    @When("I set {string} checkbox to {string} on {string} page")
    @StepDescription("Set checkbox on page")
    public void i_set_checkbox_on_page(String checkbox, String action, String page) {
        String targetPage = resolvePage(page);
        String resolvedAction = resolveTestData(action, targetPage);
        String stepDescription = String.format("Set '%s' checkbox to '%s' on '%s' page", checkbox, resolvedAction, targetPage);
        
        StepReportingWrapper.executeStep(stepDescription, () -> {
            GenericActionHandler.handleElementCheckbox(targetPage, checkbox, resolvedAction);
        });
    }
    
    // ========== MODAL OPERATIONS ==========
    @When("I click on {string} tab in modal")
    @StepDescription("Click tab in modal")
    public void i_click_tab_in_modal(String tab) {
        String stepDescription = String.format("Click on '%s' tab in modal", tab);
        
        StepReportingWrapper.executeStep(stepDescription, () -> {
            try {
                String modalPage = getCurrentModule() != null ? getCurrentModule() + "Page" : "LoginPage";
                ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(modalPage, tab);
                WebElement element = WaitHandler.waitForClickable(By.xpath(info.getXpath()), ActionType.CLICK);
                element.click();
                
                // Wait for tab content to load
                WaitHandler.waitForCustomCondition(driver -> {
                    try {
                        return driver.findElement(By.xpath("//div[contains(@class,'ant-tabs-tabpane-active')]"));
                    } catch (Exception e) {
                        return null;
                    }
                }, WaitHandler.WaitTier.SHORT);
                logger.info("Successfully clicked '{}' tab in modal", tab);
            } catch (Exception e) {
                throw new RuntimeException("Failed to click tab in modal: " + tab, e);
            }
        });
    }
    
    @When("I click on {string} in modal")
    @StepDescription("Click element in modal")
    public void i_click_in_modal(String element) {
        String stepDescription = String.format("Click on '%s' in modal", element);
        
        StepReportingWrapper.executeStep(stepDescription, () -> {
            try {
                String modalPage = getCurrentModule() != null ? getCurrentModule() + "Page" : "LoginPage";
                ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(modalPage, element);
                WebElement el = WaitHandler.waitForClickable(By.xpath(info.getXpath()), ActionType.CLICK);
                el.click();
                logger.info("Successfully clicked '{}' in modal", element);
            } catch (Exception e) {
                throw new RuntimeException("Failed to click element in modal: " + element, e);
            }
        });
    }
    
    @When("I close modal")
    @StepDescription("Close modal")
    public void i_close_modal() {
        StepReportingWrapper.executeStep("Close modal", () -> {
            try {
                GenericWrapper.getDriver().findElement(By.xpath("//div[contains(@class,'ant-modal-wrap')]")).sendKeys(Keys.ESCAPE);
                try { Thread.sleep(300); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                logger.info("Successfully closed modal");
            } catch (Exception e) {
                logger.warn("Modal close attempted but no modal found or already closed");
            }
        });
    }
    
    // ========== UNIVERSAL TEXT INPUT ACTION ==========
    @When("I enter {string} in {string} field on {string} page")
    @StepDescription("Enter text in field on page")
    public void i_enter_in_field_on_page(String value, String field, String page) {
        String targetPage = resolvePage(page);
        String actualValue = resolveTestData(value, targetPage);
        String stepDescription = String.format("Enter '%s' in '%s' field on '%s' page", 
            value.startsWith("TD_") ? "test data" : value, field, targetPage);
        
        StepReportingWrapper.executeStepWithContext(
            stepDescription, field, targetPage, actualValue,
            () -> GenericActionHandler.handleElementInput(targetPage, field, actualValue)
        );
    }
    
    @When("I enter {string} in {string} on {string} page")
    @StepDescription("Enter text in field")
    public void i_enter_in_field_on_page_alt(String value, String field, String page) {
        i_enter_in_field_on_page(value, field, page);
    }
    
    @When("I enter {string} in {string}")
    @StepDescription("Enter text in field")
    public void i_enter_in_field_simple(String value, String field) {
        String targetPage = getCurrentModule() != null ? getCurrentModule() + "Page" : "LoginPage";
        String actualValue = resolveTestData(value, targetPage);
        String stepDescription = String.format("Enter '%s' in '%s'", 
            value.startsWith("TD_") ? "test data" : value, field);
        
        StepReportingWrapper.executeStepWithContext(
            stepDescription, field, targetPage, actualValue,
            () -> GenericActionHandler.handleElementInput(targetPage, field, actualValue)
        );
    }
    

    
    // ========== TOAST VERIFICATION ==========
    @Then("I verify toast message {string}")
    @StepDescription("Verify toast message")
    public void i_verify_toast_message(String expected) {
        String resolvedExpected = resolveTestData(expected, getCurrentModule() + "Page");
        StepReportingWrapper.executeStep("Verify toast message", () -> {
            String actual = new novac.wrapper.ToastWaiter(novac.wrapper.GenericWrapper.getDriver())
                    .waitForToastText(WaitHandler.WaitTier.MEDIUM.getTimeout());
            StepReportingWrapper.recordManualStep("Toast message: \"" + actual + "\"", "INFO");
            if (!resolvedExpected.equals(actual)) {
                throw new AssertionError(String.format("Toast mismatch: expected '%s', actual '%s'", resolvedExpected, actual));
            }
        });
    }

    // ========== GENERIC MESSAGE VALIDATION ==========
    @Then("I validate message {string} contains {string} on {string} page")
    @StepDescription("Validate actual message from element contains expected text")
    public void i_validate_message_contains(String element, String expectedMessage, String page) {
        String targetPage = resolvePage(page);
        String resolvedExpected = resolveTestData(expectedMessage, targetPage);
        validateMessageFromElement(element, resolvedExpected, expectedMessage, targetPage, null);
    }

    @Then("I validate message {string} contains {string} from {string} module on {string} page")
    @Then("I validate message {string} contains {string} from {string} sheet on {string} page")  // legacy — kept for backward compat
    @StepDescription("Validate actual message from element contains expected text resolved from specific module")
    public void i_validate_message_contains_from_module(String element, String expectedMessage, String moduleName, String page) {
        String targetPage = resolvePage(page);
        String resolvedExpected = resolveTestDataFromSheet(expectedMessage, moduleName);
        validateMessageFromElement(element, resolvedExpected, expectedMessage, targetPage, moduleName);
    }

    private void validateMessageFromElement(String element, String resolvedExpected, String rawExpected, String targetPage, String moduleName) {
        String stepDescription = String.format("Validate message in '%s' contains '%s'%s on '%s' page",
            element, rawExpected.startsWith("TD_") ? "test data" : rawExpected,
            moduleName != null ? " from module '" + moduleName + "'" : "", targetPage);

        StepReportingWrapper.executeStep(stepDescription, () -> {
            try {
                ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(targetPage, element);
                By locator = By.xpath(info.getXpath());

                WebElement el = null;
                try {
                    el = WaitHandler.waitForVisibility(locator, WaitHandler.WaitTier.MEDIUM);
                } catch (RuntimeException e) {
                    if (resolvedExpected != null && !resolvedExpected.isEmpty()) {
                        By toastFallback = By.xpath(
                            "//*[contains(@class,'Toastify') or contains(@class,'ant-message')]//*[contains(text(),'" 
                            + resolvedExpected.replace("'", "\\'") + "')]");
                        try {
                            el = WaitHandler.waitForVisibility(toastFallback, WaitHandler.WaitTier.SHORT);
                        } catch (RuntimeException e2) {
                            throw new RuntimeException(
                                "Toast/message element not found for '" + element 
                                + "'. The toast may have already disappeared.", e);
                        }
                    } else {
                        throw e;
                    }
                }

                String actualMessage = el.getText().trim();
                if (actualMessage.isEmpty()) {
                    String title = el.getAttribute("title");
                    if (title != null && !title.isEmpty()) actualMessage = title.trim();
                }
                if (actualMessage.isEmpty()) {
                    String val = el.getAttribute("value");
                    if (val != null && !val.isEmpty()) actualMessage = val.trim();
                }
                logger.info("Actual: '{}' | Expected to contain: '{}'", actualMessage, resolvedExpected);
                StepReportingWrapper.recordManualStep(
                    String.format("Actual: '%s' | Expected: '%s'", actualMessage, resolvedExpected), "INFO");

                if (!actualMessage.contains(resolvedExpected)) {
                    throw new AssertionError(String.format(
                        "Message validation failed: Actual '%s' does not contain Expected '%s'",
                        actualMessage, resolvedExpected));
                }
                logger.info("Message validated successfully");
            } catch (AssertionError e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Failed to validate message: " + e.getMessage(), e);
            }
        });
    }

    private String resolveTestDataFromSheet(String value, String sheetName) {
        if (!value.startsWith("TD_")) return value;
        try {
            String testCaseId = extractTestCaseId();
            var data = TestDataManager.get().getDataFromModule(sheetName, testCaseId);
            String resolved = data.get(value.substring(3));
            if (resolved != null && !resolved.isEmpty()) {
                logger.info("Resolved TD_{} from {}.{} = {}", value.substring(3), sheetName, testCaseId, resolved);
                return resolved;
            }
            MissingDataTracker.record(sheetName, testCaseId, value.substring(3));
            logger.warn("No test data found for TD_{} in {}.{}", value.substring(3), sheetName, testCaseId);
            return "";
        } catch (Exception e) {
            logger.error("Error resolving test data for {} from module {}: {}", value, sheetName, e.getMessage());
            return "";
        }
    }

    // ========== DEPENDENT DROPDOWN STEP ==========
    @When("I select {string} from {string} and verify {string} is populated with {string} and is {string} on {string} page")
    @StepDescription("Select dropdown and verify dependent field")
    public void i_select_dropdown_and_verify_dependent_field(String dropdownValue, String dropdownName, 
            String dependentField, String expectedValue, String fieldState, String page) {
        String targetPage = resolvePage(page);
        String resolvedDropdownValue = resolveTestData(dropdownValue, targetPage);
        String resolvedExpectedValue = resolveTestData(expectedValue, targetPage);
        
        String stepDescription = String.format("Select '%s' from '%s', verify '%s' = '%s' and is %s", 
            dropdownValue.startsWith("TD_") ? "test data" : dropdownValue, dropdownName, 
            dependentField, expectedValue.startsWith("TD_") ? "test data" : expectedValue, fieldState);
        
        StepReportingWrapper.executeStep(stepDescription, () -> {
            try {
                // Step 1: Select dropdown value
                ConstantsResolver.ElementInfo dropdownInfo = ConstantsResolver.resolve(targetPage, dropdownName);
                WebElement dropdown = WaitHandler.waitForClickable(By.xpath(dropdownInfo.getXpath()), ActionType.DROPDOWN);
                dropdown.click();
                
                novac.wrapper.AntDUtils antDUtils = new novac.wrapper.AntDUtils(GenericWrapper.getDriver(), 15);
                antDUtils.selectAntDOptionByText(resolvedDropdownValue);
                logger.info("Selected '{}' from '{}'", dropdownValue, dropdownName);
                
                // Step 2: Wait for dependent field to populate
                try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                
                // Step 3: Verify populated value
                ConstantsResolver.ElementInfo fieldInfo = ConstantsResolver.resolve(targetPage, dependentField);
                WebElement field = WaitHandler.waitForVisibility(By.xpath(fieldInfo.getXpath()), ActionType.INPUT);
                
                String actualValue = field.getAttribute("value");
                if (actualValue == null || actualValue.isEmpty()) {
                    actualValue = field.getText();
                }
                
                if (!resolvedExpectedValue.equals(actualValue)) {
                    throw new RuntimeException(String.format("Value mismatch in '%s': expected '%s', found '%s'", 
                        dependentField, resolvedExpectedValue, actualValue));
                }
                logger.info("Verified '{}' value = '{}'", dependentField, resolvedExpectedValue);
                
                // Step 4: Verify field state (enabled/disabled)
                boolean isEnabled = field.isEnabled();
                boolean expectedEnabled = "enabled".equalsIgnoreCase(fieldState);
                
                if (isEnabled != expectedEnabled) {
                    throw new RuntimeException(String.format("State mismatch in '%s': expected %s, found %s", 
                        dependentField, fieldState, isEnabled ? "enabled" : "disabled"));
                }
                logger.info("Verified '{}' is {}", dependentField, fieldState);
                
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Failed to handle dependent dropdown: " + e.getMessage(), e);
            }
        });
    }
    
    // ========== UNIVERSAL VALIDATION STEPS ==========
    @Then("I {word} element {string} is {word} on {string} page")
    @StepDescription("Validate element state")
    public void i_validate_element_state(String action, String elementName, String state, String pageName) {
        StepReportingWrapper.executeStep(String.format("%s element '%s' is %s on %s page", action, elementName, state, pageName), () -> {
            GenericActionHandler.handleElementVerification(pageName, elementName, state);
        });
    }
    
    @Then("I verify {string} has value {string} and is {string} on {string} page")
    @StepDescription("Verify input field value and state")
    public void i_verify_input_field_value_and_state(String field, String expectedValue, String expectedState, String page) {
        String targetPage = resolvePage(page);
        String resolvedValue = resolveTestData(expectedValue, targetPage);
        String stepDescription = String.format("Verify '%s' = '%s' and is %s", 
            field, expectedValue.startsWith("TD_") ? "test data" : expectedValue, expectedState);
        
        StepReportingWrapper.executeStep(stepDescription, () -> {
            try {
                ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(targetPage, field);
                WebElement element = WaitHandler.waitForVisibility(By.xpath(info.getXpath()), ActionType.INPUT);
                
                String actualValue = element.getAttribute("value");
                if (actualValue == null || actualValue.isEmpty()) {
                    actualValue = element.getText();
                }
                
                if (!resolvedValue.equals(actualValue)) {
                    throw new RuntimeException(String.format("Value mismatch in '%s': expected '%s', found '%s'", 
                        field, resolvedValue, actualValue));
                }
                logger.info("Verified '{}' value = '{}'", field, resolvedValue);
                
                boolean isEnabled = element.isEnabled();
                boolean expectedEnabled = "enabled".equalsIgnoreCase(expectedState);
                
                if (isEnabled != expectedEnabled) {
                    throw new RuntimeException(String.format("State mismatch in '%s': expected %s, found %s", 
                        field, expectedState, isEnabled ? "enabled" : "disabled"));
                }
                logger.info("Verified '{}' is {}", field, expectedState);
                
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Failed to verify field '" + field + "': " + e.getMessage(), e);
            }
        });
    }
    
    // ========== SIDEBAR MENU NAVIGATION ==========
    @When("I open {string} from main menu")
    @StepDescription("Open module from sidebar main menu")
    public void i_open_module_from_main_menu(String moduleName) {
        StepReportingWrapper.executeStep(String.format("Open '%s' from main menu", moduleName), () -> {
            try {
                String menuItemXPath = "//li[contains(@class,'ant-menu-item')][@title='" + moduleName + "']";
                
                WebElement menuItem = new WebDriverWait(GenericWrapper.getDriver(), Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath(menuItemXPath)));
                
                ((JavascriptExecutor) GenericWrapper.getDriver()).executeScript("arguments[0].scrollIntoView(true);", menuItem);
                
                String hrefPath = null;
                WebElement clickTarget = menuItem;
                
                try {
                    WebElement anchor = menuItem.findElement(By.xpath(".//a"));
                    String href = anchor.getAttribute("href");
                    if (href != null && !href.isEmpty()) {
                        int idx = href.indexOf("/CoreApp");
                        if (idx >= 0) {
                            hrefPath = href.substring(idx);
                        }
                        clickTarget = anchor;
                    }
                } catch (org.openqa.selenium.NoSuchElementException ignored) {}
                
                clickTarget.click();
                
                final String expectedPath = hrefPath;
                boolean navigationSucceeded = new WebDriverWait(GenericWrapper.getDriver(), Duration.ofSeconds(15))
                    .until(driver -> {
                        if (expectedPath != null) {
                            return driver.getCurrentUrl().contains(expectedPath);
                        }
                        
                        try {
                            driver.findElement(By.xpath("//li[contains(@class,'ant-menu-item-selected')][@title='" + moduleName + "']"));
                            return true;
                        } catch (Exception ignored) {}
                        
                        try {
                            String headerText = driver.findElement(By.xpath("//h1 | //h2")).getText();
                            if (headerText.toLowerCase().contains(moduleName.toLowerCase())) {
                                return true;
                            }
                        } catch (Exception ignored) {}
                        
                        return false;
                    });
                
                if (!navigationSucceeded) {
                    throw new RuntimeException("Failed to open module '" + moduleName + "' from main menu");
                }
                
                StepReportingWrapper.recordManualStep("Opened module from main menu: " + moduleName, "PASS");
            } catch (Exception e) {
                if (e instanceof RuntimeException && e.getMessage().contains("Failed to open module")) {
                    throw e;
                }
                throw new RuntimeException("Failed to open module '" + moduleName + "' from main menu: " + e.getMessage(), e);
            }
        });
    }
    // ========== MANUAL REPORTING STEP ==========
    @When("I log {string} with status {string}")
    @StepDescription("Log custom message to report")
    public void logToReport(String message, String status) {
        StepReportingWrapper.recordManualStep(message, status);
    }
    
    @Then("I report step {string} with status {string}")
    @StepDescription("Report manual step")
    public void i_report_step_with_status(String message, String status) {
        StepReportingWrapper.recordManualStep(message, status);
    }
    
    // ========== CONDITIONAL CLICK (Yes/No from test data) ==========
    @When("I click on {string} on {string} page if {string} is Yes")
    @StepDescription("Conditionally click on element based on Yes/No test data")
    public void i_click_on_element_if_yes(String element, String page, String condition) {
        String targetPage = resolvePage(page);
        String resolvedCondition = resolveTestData(condition, targetPage);
        String stepDescription = String.format("Conditionally click '%s' on '%s' page (condition=%s)", element, targetPage, resolvedCondition);

        if ("Yes".equalsIgnoreCase(resolvedCondition != null ? resolvedCondition.trim() : "")) {
            logger.info("Condition '{}' is Yes -- clicking '{}'", condition, element);
            StepReportingWrapper.executeStep(stepDescription, () -> {
                GenericActionHandler.handleElementClick(targetPage, element, null);
            });
        } else {
            logger.info("Condition '{}' is not Yes (value='{}') -- skipping click on '{}'", condition, resolvedCondition, element);
            StepReportingWrapper.recordManualStep(stepDescription + " -- SKIPPED (value='" + resolvedCondition + "')", "INFO");
        }
    }

    // ========== GENERIC WAIT STEP ==========
    @When("I wait for {string} seconds")
    @StepDescription("Wait for specified seconds")
    public void i_wait_for_seconds(String seconds) {
        StepReportingWrapper.executeStep(String.format("Wait for %s seconds", seconds), () -> {
            try {
                int waitTime = Integer.parseInt(seconds);
                Thread.sleep(waitTime * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Wait interrupted", e);
            } catch (Exception e) {
                throw new RuntimeException("Wait failed", e);
            }
        });
    }
    
    // ========== GENERIC LISTING TABLE OPERATIONS ==========
    
    @Then("I verify record with {string} = {string} exists in {string} table")
    @StepDescription("Verify record exists in table by column value")
    public void i_verify_record_exists_in_table(String columnName, String value, String tableId) {
        String resolvedValue = resolveTestData(value, getCurrentModule() + "Page");
        String stepDesc = String.format("Verify record with %s='%s' exists in %s table", 
            columnName, value.startsWith("TD_") ? "test data" : value, tableId);
        
        StepReportingWrapper.executeStep(stepDesc, () -> {
            try {
                AntDTableHandler handler = new AntDTableHandler(GenericWrapper.getDriver(), 10);
                WebElement table = handler.findTable(tableId);
                Map<String, Integer> columnMap = handler.buildColumnMap(table);
                handler.findRowByColumnValue(table, columnName, resolvedValue, columnMap);
                logger.info("Record found: {}='{}'", columnName, resolvedValue);
            } catch (Exception e) {
                throw new RuntimeException("Record verification failed: " + e.getMessage(), e);
            }
        });
    }
    
    @Then("I verify record with {string} = {string} in {string} table has:")
    @StepDescription("Verify full row data in table")
    public void i_verify_record_data_in_table(String locatorColumn, String locatorValue, String tableId, io.cucumber.datatable.DataTable dataTable) {
        String resolvedLocator = resolveTestData(locatorValue, getCurrentModule() + "Page");
        String stepDesc = String.format("Verify record with %s='%s' data in %s table", 
            locatorColumn, locatorValue.startsWith("TD_") ? "test data" : locatorValue, tableId);
        
        StepReportingWrapper.executeStep(stepDesc, () -> {
            try {
                AntDTableHandler handler = new AntDTableHandler(GenericWrapper.getDriver(), 10);
                WebElement table = handler.findTable(tableId);
                Map<String, Integer> columnMap = handler.buildColumnMap(table);
                WebElement row = handler.findRowByColumnValue(table, locatorColumn, resolvedLocator, columnMap);
                
                Map<String, String> expectedData = new HashMap<>();
                for (Map<String, String> entry : dataTable.asMaps()) {
                    String colName = entry.keySet().iterator().next();
                    String rawValue = entry.get(colName);
                    String resolvedValue = resolveTestData(rawValue, getCurrentModule() + "Page");
                    expectedData.put(colName, resolvedValue);
                }
                
                handler.validateRowData(row, columnMap, expectedData);
                logger.info("Row data validated successfully");
            } catch (Exception e) {
                throw new RuntimeException("Row validation failed: " + e.getMessage(), e);
            }
        });
    }
    
    @Then("I verify first row in {string} table has {string} = {string}")
    @StepDescription("Verify first row column value")
    public void i_verify_first_row_value(String tableId, String columnName, String expectedValue) {
        String resolvedValue = resolveTestData(expectedValue, getCurrentModule() + "Page");
        String stepDesc = String.format("Verify first row in %s table has %s='%s'", 
            tableId, columnName, expectedValue.startsWith("TD_") ? "test data" : expectedValue);
        
        StepReportingWrapper.executeStep(stepDesc, () -> {
            try {
                AntDTableHandler handler = new AntDTableHandler(GenericWrapper.getDriver(), 10);
                WebElement table = handler.findTable(tableId);
                Map<String, Integer> columnMap = handler.buildColumnMap(table);
                WebElement firstRow = handler.getFirstRow(table);
                String actualValue = handler.getCellValue(firstRow, columnName, columnMap);
                
                if (!actualValue.equals(resolvedValue)) {
                    throw new AssertionError(String.format("First row %s mismatch: expected '%s', found '%s'", 
                        columnName, resolvedValue, actualValue));
                }
                logger.info("First row validated: {}='{}'", columnName, actualValue);
            } catch (AssertionError e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("First row validation failed: " + e.getMessage(), e);
            }
        });
    }
    
    @Then("I verify record with {string} = {string} has status {string} in {string} table")
    @StepDescription("Verify record status in table")
    public void i_verify_record_status_in_table(String columnName, String value, String expectedStatus, String tableId) {
        String resolvedValue = resolveTestData(value, getCurrentModule() + "Page");
        String stepDesc = String.format("Verify record with %s='%s' has status '%s' in %s table", 
            columnName, value.startsWith("TD_") ? "test data" : value, expectedStatus, tableId);
        
        StepReportingWrapper.executeStep(stepDesc, () -> {
            try {
                AntDTableHandler handler = new AntDTableHandler(GenericWrapper.getDriver(), 10);
                WebElement table = handler.findTable(tableId);
                Map<String, Integer> columnMap = handler.buildColumnMap(table);
                WebElement row = handler.findRowByColumnValue(table, columnName, resolvedValue, columnMap);
                String actualStatus = handler.getRowStatus(row);
                
                if (!actualStatus.equalsIgnoreCase(expectedStatus)) {
                    throw new AssertionError(String.format("Status mismatch: expected '%s', found '%s'", expectedStatus, actualStatus));
                }
                logger.info("Status validated: {}", actualStatus);
            } catch (AssertionError e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Status validation failed: " + e.getMessage(), e);
            }
        });
    }
    
    @When("I perform {string} action on record with {string} = {string} in {string} table")
    @StepDescription("Perform action on table row")
    public void i_perform_action_on_record(String action, String columnName, String value, String tableId) {
        String resolvedValue = resolveTestData(value, getCurrentModule() + "Page");
        String stepDesc = String.format("Perform '%s' action on record with %s='%s' in %s table", 
            action, columnName, value.startsWith("TD_") ? "test data" : value, tableId);
        
        StepReportingWrapper.executeStep(stepDesc, () -> {
            try {
                AntDTableHandler handler = new AntDTableHandler(GenericWrapper.getDriver(), 10);
                WebElement table = handler.findTable(tableId);
                Map<String, Integer> columnMap = handler.buildColumnMap(table);
                WebElement row = handler.findRowByColumnValue(table, columnName, resolvedValue, columnMap);
                handler.performRowAction(row, action);
                logger.info("Action '{}' performed successfully", action);
            } catch (Exception e) {
                throw new RuntimeException("Action failed: " + e.getMessage(), e);
            }
        });
    }
    
    @When("I perform {string} action on record with {string} = {string} having status {string} in {string} table")
    @StepDescription("Perform action on table row with status check")
    public void i_perform_action_on_record_with_status(String action, String columnName, String value, String expectedStatus, String tableId) {
        String resolvedValue = resolveTestData(value, getCurrentModule() + "Page");
        String stepDesc = String.format("Perform '%s' action on record with %s='%s' (status=%s) in %s table", 
            action, columnName, value.startsWith("TD_") ? "test data" : value, expectedStatus, tableId);
        
        StepReportingWrapper.executeStep(stepDesc, () -> {
            try {
                AntDTableHandler handler = new AntDTableHandler(GenericWrapper.getDriver(), 10);
                WebElement table = handler.findTable(tableId);
                Map<String, Integer> columnMap = handler.buildColumnMap(table);
                WebElement row = handler.findRowByColumnValue(table, columnName, resolvedValue, columnMap);
                
                String actualStatus = handler.getRowStatus(row);
                if (!actualStatus.equalsIgnoreCase(expectedStatus)) {
                    throw new AssertionError(String.format("Status mismatch: expected '%s', found '%s'. Action not performed.", 
                        expectedStatus, actualStatus));
                }
                
                handler.performRowAction(row, action);
                logger.info("Action '{}' performed on row with status '{}'", action, actualStatus);
            } catch (AssertionError e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Action with status check failed: " + e.getMessage(), e);
            }
        });
    }
    
    // ========== TABLE ROW ACTION STEPS (cross-module data resolution) ==========

    @When("I perform {string} action on product with {string} = {string} using {string} from {string} module on {string} page")
    @When("I perform {string} action on product with {string} = {string} using {string} from {string} sheet on {string} page")  // legacy — kept for backward compat
    @StepDescription("Perform action on product row using more button, with data from specific module")
    public void i_perform_action_on_product_with_using_from_module_on_page(String action, String tableConstant, String value, String moreBtnConstant, String moduleName, String page) {
        String targetPage = resolvePage(page);
        String resolvedValue = resolveTestDataFromSheet(value, moduleName);
        String stepDesc = String.format("Perform '%s' on product with %s='%s' using '%s' from '%s' module on %s",
            action, tableConstant, value.startsWith("TD_") ? "test data" : value, moreBtnConstant, moduleName, targetPage);

        StepReportingWrapper.executeStep(stepDesc, () -> {
            try {
                WebDriver driver = GenericWrapper.getDriver();

                String rowXpath = "//tbody[contains(@class,'ant-table-tbody')]//tr[.//td[normalize-space()='" + resolvedValue + "']]";
                WebElement row = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath(rowXpath)));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", row);
                Thread.sleep(300);

                WebElement moreBtn;
                try {
                    moreBtn = row.findElement(By.xpath(".//button[contains(@id,'MeatBallMenu')]"));
                } catch (Exception btnEx) {
                    moreBtn = row.findElement(By.xpath(".//td[last()]//button | .//td[last()]//span[contains(@class,'anticon')]"));
                }
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", moreBtn);
                new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(moreBtn)).click();
                logger.info("Clicked more button for product '{}'", resolvedValue);

                Thread.sleep(500);
                String actionXpath = "//div[contains(@class,'ant-dropdown') and not(contains(@class,'ant-dropdown-hidden'))]//span[normalize-space()='" + action + "'] | " +
                                    "//ul[contains(@class,'ant-dropdown-menu')]//span[normalize-space()='" + action + "'] | " +
                                    "//div[contains(@class,'ant-dropdown') and not(contains(@class,'ant-dropdown-hidden'))]//li[normalize-space()='" + action + "']";
                WebElement actionItem = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath(actionXpath)));
                actionItem.click();
                logger.info("Performed '{}' action on product '{}'", action, resolvedValue);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Failed to perform '%s' on product '%s': %s",
                    action, resolvedValue, e.getMessage()), e);
            }
        });
    }

    @When("I perform {string} action on row with {string} = {string} from {string} module on {string} page")
    @When("I perform {string} action on row with {string} = {string} from {string} sheet on {string} page")  // legacy — kept for backward compat
    @StepDescription("Perform action on table row by matching cell value, with data from specific module")
    public void i_perform_action_on_row_from_module(String action, String columnConstant, String value, String moduleName, String page) {
        String targetPage = resolvePage(page);
        String resolvedValue = resolveTestDataFromSheet(value, moduleName);
        String stepDesc = String.format("Perform '%s' on row with %s='%s' from '%s' module on %s",
            action, columnConstant, value.startsWith("TD_") ? "test data" : value, moduleName, targetPage);

        StepReportingWrapper.executeStep(stepDesc, () -> {
            try {
                WebDriver driver = GenericWrapper.getDriver();

                String rowXpath = "//tbody[contains(@class,'ant-table-tbody')]//tr[.//td[normalize-space()='" + resolvedValue + "']]";
                WebElement row = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath(rowXpath)));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", row);
                Thread.sleep(300);

                WebElement actionBtn;
                try {
                    actionBtn = row.findElement(By.xpath(
                        ".//img[@alt='EditIcon'] | .//*[contains(@id,'EditIcon')] | .//img[@alt='edit'] | .//img[contains(@alt,'Edit') and not(contains(@alt,'View'))]"));
                } catch (Exception editEx) {
                    List<WebElement> icons = row.findElements(By.xpath(".//td[last()]//img | .//td[last()]//button"));
                    if (icons.isEmpty()) {
                        throw new RuntimeException("No action icons found in row for value '" + resolvedValue + "'");
                    }
                    actionBtn = icons.get(0);
                }
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", actionBtn);
                new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(actionBtn)).click();
                logger.info("Performed '{}' on row with value '{}'", action, resolvedValue);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Failed to perform '%s' on row '%s': %s",
                    action, resolvedValue, e.getMessage()), e);
            }
        });
    }

    // ========== HELPER METHODS ==========
    private String resolveTestData(String value, String pageName) {
        // Handle cross-testcase references (testcaseID.FieldName)
        if (value.contains(".") && !value.startsWith("TD_")) {
            try {
                String resolved = TestDataManager.get().resolveReference(value);
                if (!resolved.equals(value)) {
                    logger.info("Resolved cross-reference {} = {}", value, resolved);
                    return resolved;
                }
            } catch (Exception e) {
                logger.error("Error resolving cross-reference {}: {}", value, e.getMessage());
            }
        }
        
        // Handle TD_ prefix (existing functionality)
        if (!value.startsWith("TD_")) return value;
        
        try {
            // Map page names to module names
            String moduleName = getModuleNameForPage(pageName);
            String testCaseId = extractTestCaseId();
            
            var data = TestDataManager.get().getData(moduleName, testCaseId);
            String resolvedValue = data.get(value.substring(3));
            
            if (resolvedValue != null && !resolvedValue.isEmpty()) {
                logger.info("Resolved TD_{} from {}.{} = {}", value.substring(3), moduleName, testCaseId, resolvedValue);
                return resolvedValue;
            }
            
            // Record missing data
            MissingDataTracker.record(moduleName, testCaseId, value.substring(3));
            logger.warn("No test data found for TD_{} in {}.{}, returning empty", value.substring(3), moduleName, testCaseId);
            return "";
        } catch (Exception e) {
            // Record missing data on exception
            try {
                String fieldName = value.startsWith("TD_") ? value.substring(3) : value;
                String moduleName = getModuleNameForPage(pageName);
                String testCaseId = extractTestCaseId();
                MissingDataTracker.record(moduleName, testCaseId, fieldName);
            } catch (Exception ignored) { /* Ignore tracker errors */ }
            
            logger.error("Error resolving test data for {}: {}", value, e.getMessage());
            return "";
        }
    }
    
    private String resolvePage(String page) {
        return PageResolver.resolve(page);
    }
    
    private String resolveSection(String section, String page) {
        if (section == null) return null;
        return section;
    }
    
    private String buildClickDescription(String element, String section, String page) {
        StringBuilder desc = new StringBuilder("Click on '").append(element).append("'");
        if (section != null) {
            desc.append(" in '").append(section).append("'");
        }
        desc.append(" on '").append(page).append("' page");
        return desc.toString();
    }
    
    private String getModuleNameForPage(String pageName) {
        return PageResolver.getModuleName(pageName);
    }
    

    public static void setCurrentTestCaseId(String testCaseId) {
        TestContext.setCurrentTestCaseId(testCaseId);
    }
    
    public static void clearCurrentTestCaseId() {
        TestContext.clearCurrentTestCaseId();
    }
    
    public static void setCurrentScenario(io.cucumber.java.Scenario scenario) {
        TestContext.setCurrentScenario(scenario);
    }
    
    public static void clearCurrentScenario() {
        TestContext.clearCurrentScenario();
    }
    
    public static void setCurrentModule(String module) {
        TestContext.setCurrentModule(module);
    }
    
    public static String getCurrentModule() {
        return TestContext.getCurrentModule();
    }
    
    public static void clearCurrentModule() {
        TestContext.clearCurrentModule();
    }
    
    public static String getCurrentTestCaseId() {
        CommonSteps instance = new CommonSteps();
        return instance.extractTestCaseId();
    }
    
    private String extractTestCaseId() {
        // First try to get from scenario tags
        Scenario scenario = TestContext.getCurrentScenario();
        if (scenario != null) {
            for (String tag : scenario.getSourceTagNames()) {
                if (tag.startsWith("@TC_")) {
                    String testCaseId = tag.substring(1); // Remove @ prefix
                    logger.debug("Extracted test case ID from scenario tag: {}", testCaseId);
                    return testCaseId;
                }
            }
        }
        
        // Fallback to manually set test case ID
        String testCaseId = TestContext.getCurrentTestCaseId();
        if (testCaseId != null) {
            return testCaseId;
        }
        

        logger.warn("No test case ID found, using default TC_LOGIN_001");
        return "TC_LOGIN_001";
    }
}