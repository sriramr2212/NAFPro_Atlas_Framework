package stepdefinitions;

import io.cucumber.java.en.Then;
import novac.model.FlexiFieldType;
import novac.reporting.StepReportingWrapper;
import novac.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ValueCaptureStepDef {

    private static final Logger logger = LogManager.getLogger(ValueCaptureStepDef.class);

    @Then("I remember value of {string} on {string} page as {string}")
    public void i_remember_value(String elementName, String page, String key) {
        String targetPage = resolvePageName(page);
        ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(targetPage, elementName);
        ActionType actionType = info.getActionType();

        StepReportingWrapper.executeStepWithContext(
                "Remember value of '" + elementName + "' on '" + targetPage + "' as '" + key + "'",
                elementName, targetPage, null,
                () -> {
                    WebElement element = WaitHandler.waitForVisibilityWithHealing(
                            By.xpath(info.getXpath()), actionType, elementName, targetPage);

                    String value = FlexiFieldValidator.extractValue(element, mapActionTypeToFieldType(actionType));

                    if (value == null || value.trim().isEmpty()) {
                        throw new RuntimeException("Value captured from '" + elementName
                                + "' on '" + targetPage + "' is null or empty");
                    }

                    TestDataManager.get().setRuntimeValue(key, value);
                    logger.info("Remembered '{}' = '{}' from '{}' on '{}'", key, value, elementName, targetPage);
                });
    }

    @Then("I verify {string} on {string} page equals remembered value {string}")
    public void i_verify_equals_remembered(String elementName, String page, String key) {
        String targetPage = resolvePageName(page);
        ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(targetPage, elementName);
        ActionType actionType = info.getActionType();

        StepReportingWrapper.executeStepWithContext(
                "Verify '" + elementName + "' on '" + targetPage + "' equals remembered '" + key + "'",
                elementName, targetPage, null,
                () -> {
                    String expected = TestDataManager.get().getRuntimeValue(key);
                    if (expected == null) {
                        throw new RuntimeException("No remembered value found for key '" + key
                                + "'. Available keys: " + getAvailableKeys());
                    }

                    WebElement element = WaitHandler.waitForVisibilityWithHealing(
                            By.xpath(info.getXpath()), actionType, elementName, targetPage);

                    String actual = FlexiFieldValidator.extractValue(element, mapActionTypeToFieldType(actionType));

                    if (!expected.equals(actual)) {
                        throw new AssertionError("Value mismatch for '" + elementName
                                + "' on '" + targetPage + "': expected (remembered) = '"
                                + expected + "', actual = '" + actual + "'");
                    }

                    logger.info("Verified '{}' on '{}' matches remembered '{}' = '{}'",
                            elementName, targetPage, key, expected);
                });
    }

    @Then("I use remembered value {string} in {string} on {string} page")
    public void i_use_remembered_value(String key, String elementName, String page) {
        String targetPage = resolvePageName(page);
        ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(targetPage, elementName);
        ActionType actionType = info.getActionType();

        StepReportingWrapper.executeStepWithContext(
                "Use remembered '" + key + "' in '" + elementName + "' on '" + targetPage + "'",
                elementName, targetPage, null,
                () -> {
                    String value = TestDataManager.get().getRuntimeValue(key);
                    if (value == null) {
                        throw new RuntimeException("No remembered value found for key '" + key
                                + "'. Available keys: " + getAvailableKeys());
                    }

                    switch (actionType) {
                        case INPUT:
                            GenericActionHandler.handleElementInput(targetPage, elementName, value);
                            break;
                        case DROPDOWN:
                            GenericActionHandler.handleElementDropdown(targetPage, elementName, value);
                            break;
                        case TOGGLE:
                            boolean targetState = "true".equalsIgnoreCase(value)
                                    || "yes".equalsIgnoreCase(value)
                                    || "on".equalsIgnoreCase(value);
                            GenericActionHandler.handleElementToggle(targetPage, elementName, targetState);
                            break;
                        case DATE_PICKER:
                            GenericActionHandler.handleElementInput(targetPage, elementName, value);
                            break;
                        default:
                            throw new RuntimeException("Cannot use remembered value for ActionType '"
                                    + actionType + "' on element '" + elementName
                                    + "'. Supported: INPUT, DROPDOWN, TOGGLE, DATE_PICKER");
                    }

                    logger.info("Used remembered '{}' = '{}' in '{}' on '{}'",
                            key, value, elementName, targetPage);
                });
    }

    // -- Helpers --

    private static FlexiFieldType mapActionTypeToFieldType(ActionType actionType) {
        switch (actionType) {
            case DROPDOWN:    return FlexiFieldType.DROPDOWN;
            case INPUT:       return FlexiFieldType.TEXTBOX;
            case TOGGLE:      return FlexiFieldType.TOGGLE;
            case DATE_PICKER: return FlexiFieldType.DATE_PICKER;
            default:          return null;
        }
    }

    private static String resolvePageName(String page) {
        return novac.utils.PageResolver.resolve(page);
    }

    private static String getAvailableKeys() {
        // Reflection-free: just indicate the key was not set
        return "(check prior 'I remember' steps executed in this scenario)";
    }
}
