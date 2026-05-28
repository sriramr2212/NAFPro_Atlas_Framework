package stepdefinitions;

import io.cucumber.java.en.*;
import novac.helpers.FlexiFieldCaptureHelper;
import novac.model.FlexiField;
import novac.utils.FlexiFieldInteractor;
import novac.utils.FlexiFieldValidator;
import novac.utils.TestContext;
import novac.utils.TestDataManager;

import java.util.List;
import java.util.Map;

public class FlexiFieldStepDef {

    // -- Capture --

    @When("I capture flexifield definitions for {string} block on {string} page")
    public void i_capture_flexifield_definitions(String blockName, String pageName) {
        FlexiFieldCaptureHelper.captureAndStore(blockName, pageName, null);
    }

    @When("I capture flexifield definitions for {string} block on {string} page with locator {string}")
    public void i_capture_flexifield_definitions_with_locator(String blockName, String pageName, String blockLocator) {
        FlexiFieldCaptureHelper.captureAndStore(blockName, pageName, blockLocator);
    }

    @When("I recapture flexifield definitions for {string} block on {string} page")
    public void i_recapture_flexifield_definitions(String blockName, String pageName) {
        FlexiFieldCaptureHelper.captureAndStore(blockName, pageName, null);
    }

    @When("I recapture flexifield definitions for {string} block on {string} page with locator {string}")
    public void i_recapture_flexifield_definitions_with_locator(String blockName, String pageName, String blockLocator) {
        FlexiFieldCaptureHelper.captureAndStore(blockName, pageName, blockLocator);
    }

    // -- Interaction --

    @When("I set flexifield {string} to {string} in {string}")
    public void i_set_flexifield(String identifier, String value, String blockName) {
        FlexiField field = resolveField(blockName, identifier);
        FlexiFieldInteractor.interact(field, resolveValue(value));
    }

    @When("I set flexifield {string} to {string} using {string} action in {string}")
    public void i_set_flexifield_with_action(String identifier, String value, String action, String blockName) {
        FlexiField field = resolveField(blockName, identifier);
        FlexiFieldInteractor.interact(field, action, resolveValue(value));
    }

    // -- Validation: Presence --

    @Then("I verify flexifield {string} is present in {string}")
    public void i_verify_flexifield_present(String identifier, String blockName) {
        FlexiFieldValidator.validatePresence(resolveField(blockName, identifier));
    }

    @Then("I verify all flexifields are present in {string}")
    public void i_verify_all_flexifields_present(String blockName) {
        List<FlexiField> fields = TestContext.getFlexiFields(blockName);
        for (FlexiField field : fields) {
            FlexiFieldValidator.validatePresence(field);
        }
    }

    // -- Validation: State --

    @Then("I verify flexifield {string} is {string} in {string}")
    public void i_verify_flexifield_state(String identifier, String expectedState, String blockName) {
        FlexiField field = resolveField(blockName, identifier);
        switch (expectedState.toLowerCase()) {
            case "visible":
                FlexiFieldValidator.validateVisibility(field, true);
                break;
            case "hidden":
                FlexiFieldValidator.validateVisibility(field, false);
                break;
            case "editable":
                FlexiFieldValidator.validateEditability(field, true);
                break;
            case "read-only":
            case "readonly":
            case "disabled":
                FlexiFieldValidator.validateEditability(field, false);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + expectedState
                        + ". Expected: visible, hidden, editable, read-only");
        }
    }

    // -- Validation: Value & Type --

    @Then("I verify flexifield {string} has value {string} in {string}")
    public void i_verify_flexifield_value(String identifier, String expectedValue, String blockName) {
        FlexiFieldValidator.validateValue(resolveField(blockName, identifier), resolveValue(expectedValue));
    }

    @Then("I verify flexifield {string} type in {string}")
    public void i_verify_flexifield_type(String identifier, String blockName) {
        FlexiFieldValidator.validateFieldType(resolveField(blockName, identifier));
    }

    // -- Validation: Config-based (Phase 3) --

    @Then("I verify flexifield {string} mandatory flag in {string}")
    public void i_verify_flexifield_mandatory(String identifier, String blockName) {
        FlexiFieldValidator.validateMandatory(resolveField(blockName, identifier));
    }

    @Then("I verify flexifield {string} hidden flag in {string}")
    public void i_verify_flexifield_hidden(String identifier, String blockName) {
        FlexiFieldValidator.validateHidden(resolveField(blockName, identifier));
    }

    @Then("I verify flexifield {string} default value in {string}")
    public void i_verify_flexifield_default_value(String identifier, String blockName) {
        FlexiFieldValidator.validateDefaultValue(resolveField(blockName, identifier));
    }

    @Then("I verify flexifield {string} min max constraints in {string}")
    public void i_verify_flexifield_min_max(String identifier, String blockName) {
        FlexiFieldValidator.validateMinMax(resolveField(blockName, identifier));
    }

    @Then("I verify flexifield {string} date range constraints in {string}")
    public void i_verify_flexifield_date_range(String identifier, String blockName) {
        FlexiFieldValidator.validateDateRange(resolveField(blockName, identifier));
    }

    @Then("I verify flexifield {string} lov relationship in {string}")
    public void i_verify_flexifield_lov(String identifier, String blockName) {
        List<FlexiField> blockFields = TestContext.getFlexiFields(blockName);
        FlexiFieldValidator.validateLovRelationship(resolveField(blockName, identifier), blockFields);
    }

    @Then("I verify flexifield {string} belongs to block {string}")
    public void i_verify_flexifield_block_belonging(String identifier, String blockName) {
        FlexiFieldValidator.validateBlockBelonging(resolveField(blockName, identifier));
    }

    // -- Validation: Composite --

    @Then("I verify all flexifield config for {string} in {string}")
    public void i_verify_all_flexifield_config(String identifier, String blockName) {
        List<FlexiField> blockFields = TestContext.getFlexiFields(blockName);
        FlexiFieldValidator.validateAll(resolveField(blockName, identifier), blockFields);
    }

    @Then("I verify all flexifield config in {string}")
    public void i_verify_all_flexifield_config_in_block(String blockName) {
        List<FlexiField> blockFields = TestContext.getFlexiFields(blockName);
        for (FlexiField field : blockFields) {
            FlexiFieldValidator.validateAll(field, blockFields);
        }
    }

    // -- Field Resolution (key-first, then normalized label -- strict match only) --

    private FlexiField resolveField(String blockName, String identifier) {
        List<FlexiField> fields;
        try {
            fields = TestContext.getFlexiFields(blockName);
        } catch (RuntimeException e) {
            throw new RuntimeException("No flexifields captured for block '" + blockName
                    + "'. Run capture step first.");
        }

        // Attempt 1: exact key match
        for (FlexiField f : fields) {
            if (identifier.equals(f.getFieldKey())) return f;
        }

        // Attempt 2: normalized label match (strict, no partial)
        String normalizedId = normalizeLabel(identifier);
        FlexiField labelMatch = null;
        for (FlexiField f : fields) {
            if (f.getFieldLabel() != null && normalizeLabel(f.getFieldLabel()).equals(normalizedId)) {
                if (labelMatch != null) {
                    throw new RuntimeException("Label '" + identifier + "' matched multiple fields in block '"
                            + blockName + "': " + labelMatch.identity() + " and " + f.identity()
                            + ". Use fieldKey instead.");
                }
                labelMatch = f;
            }
        }
        if (labelMatch != null) return labelMatch;

        throw new RuntimeException("Flexifield '" + identifier + "' not found in block '" + blockName
                + "' -- no match by key or label");
    }

    private static String normalizeLabel(String input) {
        return input.trim().replaceAll("[:\\*\\.]+$", "").replaceAll("\\s+", " ").trim().toLowerCase();
    }

    // -- Value Resolution --

    private String resolveValue(String value) {
        if (value == null) return null;
        if (value.startsWith("TD_")) {
            String module = TestContext.getCurrentModule();
            String testCaseId = CommonSteps.getCurrentTestCaseId();
            Map<String, String> data = TestDataManager.get().getData(module, testCaseId);
            String resolved = data.get(value.substring(3));
            return resolved != null ? resolved : "";
        }
        return value;
    }
}
