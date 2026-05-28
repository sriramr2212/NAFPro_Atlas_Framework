package stepdefinitions;

import io.cucumber.java.en.*;
import novac.reporting.StepDescription;
import novac.reporting.StepReportingWrapper;
import novac.utils.ConstantsResolver;
import novac.utils.MissingDataTracker;
import novac.utils.RunManager;
import novac.utils.TestContext;
import novac.utils.TestDataManager;
import novac.wrapper.desktop.core.DesktopEngine;
import novac.wrapper.desktop.core.DesktopLocator;
import novac.wrapper.desktop.factory.DesktopComponentFactory;
import novac.wrapper.desktop.session.DesktopSessionConfig;
import novac.wrapper.desktop.session.DesktopSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public class DesktopCalculatorSteps {

    private static final Logger logger = LogManager.getLogger(DesktopCalculatorSteps.class);
    private static final String MODULE_NAME = "DemoCalculator";
    private static final String CONSTANTS_CLASS = "novac.constants.DemoCalculatorConstants";

    @Given("I launch the desktop application {string}")
    @StepDescription("Launch desktop application via WinAppDriver")
    public void i_launch_desktop_application(String appName) {
        StepReportingWrapper.executeStep("Launch desktop application: " + appName, () -> {
            DesktopEngine engine = TestContext.getDesktopEngine();
            if (engine != null && DesktopSessionManager.isSessionActive()) {
                logger.info("Reusing existing desktop session for: {}", appName);
                return;
            }
            engine = DesktopComponentFactory.createEngine();
            DesktopSessionConfig config = new DesktopSessionConfig();
            config.setApplicationPath(resolveApplicationPath(appName));
            config.setLaunchTimeoutSeconds(RunManager.getDesktopLaunchTimeout());
            config.setReuseSession(RunManager.isDesktopReuseSession());
            DesktopSessionManager.startSession(engine, config);
            TestContext.setDesktopEngine(engine);
            TestContext.setDesktopConfig(config);
            logger.info("Desktop application launched: {}", appName);
        });
    }

    @When("I click desktop element {string}")
    @StepDescription("Click a desktop element by constants name")
    public void i_click_desktop_element(String elementName) {
        StepReportingWrapper.executeStep("Click desktop element: " + elementName, () -> {
            DesktopEngine engine = getEngine();
            DesktopLocator locator = resolveLocator(elementName);
            engine.click(locator);
            logger.info("Clicked desktop element: {}", elementName);
        });
    }

    @When("I double click desktop element {string}")
    @StepDescription("Double-click a desktop element")
    public void i_double_click_desktop_element(String elementName) {
        StepReportingWrapper.executeStep("Double-click desktop element: " + elementName, () -> {
            DesktopEngine engine = getEngine();
            DesktopLocator locator = resolveLocator(elementName);
            engine.doubleClick(locator);
            logger.info("Double-clicked desktop element: {}", elementName);
        });
    }

    @When("I type {string} into desktop element {string}")
    @StepDescription("Type text into a desktop element")
    public void i_type_into_desktop_element(String text, String elementName) {
        String resolvedText = resolveTestData(text);
        StepReportingWrapper.executeStep("Type '" + resolvedText + "' into: " + elementName, () -> {
            DesktopEngine engine = getEngine();
            DesktopLocator locator = resolveLocator(elementName);
            engine.type(locator, resolvedText);
            logger.info("Typed '{}' into: {}", resolvedText, elementName);
        });
    }

    @When("I type expression {string} into calculator")
    @StepDescription("Type a calculation expression using keyboard input")
    public void i_type_expression_into_calculator(String expression) {
        String resolvedExpression = resolveTestData(expression);
        StepReportingWrapper.executeStep("Type expression: " + resolvedExpression, () -> {
            DesktopEngine engine = getEngine();
            for (char c : resolvedExpression.toCharArray()) {
                String buttonName = mapCharToButton(c);
                if (buttonName != null) {
                    DesktopLocator locator = resolveLocator(buttonName);
                    engine.click(locator);
                }
            }
            logger.info("Typed expression: {}", resolvedExpression);
        });
    }

    @When("I open calculator navigation")
    @StepDescription("Open the calculator navigation pane")
    public void i_open_calculator_navigation() {
        StepReportingWrapper.executeStep("Open calculator navigation", () -> {
            DesktopEngine engine = getEngine();
            DesktopLocator locator = resolveLocator("BTN_NAV_TOGGLE");
            engine.click(locator);
            novac.wrapper.desktop.wait.DesktopWaitHandler.waitForIdle(500);
            logger.info("Calculator navigation opened");
        });
    }

    @When("I select calculator mode {string}")
    @StepDescription("Select a calculator mode from navigation")
    public void i_select_calculator_mode(String mode) {
        String resolvedMode = resolveTestData(mode);
        StepReportingWrapper.executeStep("Select calculator mode: " + resolvedMode, () -> {
            DesktopEngine engine = getEngine();
            DesktopLocator locator = DesktopLocator.byName(resolvedMode + " Calculator");
            engine.click(locator);
            novac.wrapper.desktop.wait.DesktopWaitHandler.waitForIdle(1000);
            logger.info("Selected calculator mode: {}", resolvedMode);
        });
    }

    @Then("the desktop element {string} should contain {string}")
    @StepDescription("Verify desktop element text contains expected value")
    public void desktop_element_should_contain(String elementName, String expectedValue) {
        String resolvedExpected = resolveTestData(expectedValue);
        StepReportingWrapper.executeStep(
                String.format("Verify '%s' contains '%s'", elementName, resolvedExpected), () -> {
            DesktopEngine engine = getEngine();
            DesktopLocator locator = resolveLocator(elementName);
            String actual = engine.getText(locator);
            if (!actual.contains(resolvedExpected)) {
                throw new AssertionError(String.format(
                        "Desktop element '%s' text mismatch: expected to contain '%s', actual '%s'",
                        elementName, resolvedExpected, actual));
            }
            logger.info("Verified: {} contains '{}' ✓", elementName, resolvedExpected);
        });
    }

    @Then("the desktop element {string} should be visible")
    @StepDescription("Verify desktop element is visible")
    public void desktop_element_should_be_visible(String elementName) {
        StepReportingWrapper.executeStep("Verify visible: " + elementName, () -> {
            DesktopEngine engine = getEngine();
            DesktopLocator locator = resolveLocator(elementName);
            if (!engine.isVisible(locator)) {
                throw new AssertionError("Desktop element '" + elementName + "' is not visible");
            }
            logger.info("Verified: {} is visible ✓", elementName);
        });
    }

    @Then("the window title should contain {string}")
    @StepDescription("Verify current window title contains text")
    public void window_title_should_contain(String expectedText) {
        String resolvedText = resolveTestData(expectedText);
        StepReportingWrapper.executeStep("Verify window title contains: " + resolvedText, () -> {
            DesktopEngine engine = getEngine();
            String title = engine.getCurrentWindowTitle();
            if (title == null || !title.contains(resolvedText)) {
                throw new AssertionError(String.format(
                        "Window title mismatch: expected to contain '%s', actual '%s'",
                        resolvedText, title));
            }
            logger.info("Verified: window title contains '{}' ✓", resolvedText);
        });
    }

    // --- Helpers ---

    private DesktopEngine getEngine() {
        DesktopEngine engine = TestContext.getDesktopEngine();
        if (engine == null) {
            throw new RuntimeException("Desktop engine not initialized. Use 'I launch the desktop application' step first.");
        }
        return engine;
    }

    private DesktopLocator resolveLocator(String elementName) {
        // First: try ConstantsResolver framework path (module-aware)
        try {
            ConstantsResolver.DesktopElementInfo info = ConstantsResolver.resolveDesktop(MODULE_NAME, elementName);
            return info.getLocator();
        } catch (Exception e) {
            logger.debug("ConstantsResolver could not resolve '{}': {}", elementName, e.getMessage());
        }

        // Second: direct class lookup (for demo before ModuleRegistry is wired)
        try {
            Class<?> clazz = Class.forName(CONSTANTS_CLASS);
            Field field = clazz.getDeclaredField(elementName);
            field.setAccessible(true);
            String locatorString = (String) field.get(null);
            return DesktopLocator.parse(locatorString);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            // Third: if elementName itself is a desktop locator string
            if (DesktopLocator.isDesktopLocator(elementName)) {
                return DesktopLocator.parse(elementName);
            }
            throw new RuntimeException("Desktop element '" + elementName + "' not found in " + CONSTANTS_CLASS, e);
        }
    }

    private String resolveApplicationPath(String appName) {
        if ("Calculator".equalsIgnoreCase(appName)) {
            return "Microsoft.WindowsCalculator_8wekyb3d8bbwe!App";
        }
        String configPath = RunManager.getDesktopApplicationPath();
        if (!configPath.isEmpty()) {
            return configPath;
        }
        return appName;
    }

    private String mapCharToButton(char c) {
        return switch (c) {
            case '0' -> "BTN_ZERO";
            case '1' -> "BTN_ONE";
            case '2' -> "BTN_TWO";
            case '3' -> "BTN_THREE";
            case '4' -> "BTN_FOUR";
            case '5' -> "BTN_FIVE";
            case '6' -> "BTN_SIX";
            case '7' -> "BTN_SEVEN";
            case '8' -> "BTN_EIGHT";
            case '9' -> "BTN_NINE";
            case '+' -> "BTN_PLUS";
            case '-' -> "BTN_MINUS";
            case '*' -> "BTN_MULTIPLY";
            case '/' -> "BTN_DIVIDE";
            case '=' -> "BTN_EQUALS";
            default -> null;
        };
    }

    private String resolveTestData(String value) {
        if (value == null || !value.startsWith("TD_")) return value;
        try {
            String module = TestContext.getCurrentModule();
            String tcId = extractTestCaseId();
            var data = TestDataManager.get().getData(module, tcId);
            String resolved = data.get(value.substring(3));
            if (resolved != null && !resolved.isEmpty()) {
                logger.debug("Resolved {} = {}", value, resolved);
                return resolved;
            }
            MissingDataTracker.record(module, tcId, value.substring(3));
            return "";
        } catch (Exception e) {
            logger.warn("Could not resolve test data for {}: {}", value, e.getMessage());
            return value;
        }
    }

    private String extractTestCaseId() {
        var scenario = TestContext.getCurrentScenario();
        if (scenario != null) {
            for (String tag : scenario.getSourceTagNames()) {
                if (tag.startsWith("@TC_")) return tag.substring(1);
            }
        }
        String tcId = TestContext.getCurrentTestCaseId();
        return tcId != null ? tcId : "TC_CALC_001";
    }
}
