package novac.helpers;

import novac.model.FlexiField;
import novac.model.FlexiFieldType;
import novac.utils.FlexiFieldResolver;
import novac.utils.TestContext;
import novac.reporting.StepReportingWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Captures flexi field definitions from Product Config UI.
 *
 * Design:
 *   - Wrapper-driven: each component-main-wrapper inside a block = one field
 *   - Label-based extraction is primary; IDs assist as shortcut
 *   - Captures configuration attributes ONLY (not runtime state)
 *   - Called ONCE per block after full configuration is complete
 *   - Fails immediately if key AND label cannot be derived
 *   - Fails immediately on duplicate identity within a block
 */
public class FlexiFieldCaptureHelper {

    private static final Logger logger = LogManager.getLogger(FlexiFieldCaptureHelper.class);
    private static final Pattern KEY_ID_PATTERN = Pattern.compile("(Key\\d+)");

    // ============================================
    // Public API
    // ============================================

    public static List<FlexiField> captureAndStore(
            String blockName, String pageName, String blockLocator) {

        List<FlexiField> fields = captureFromBlock(blockName, pageName, blockLocator);
        TestContext.storeFlexiFields(blockName, fields);

        logCapturedFields(blockName, fields);

        StepReportingWrapper.executeStepWithContext(
                "Captured " + fields.size() + " flexifield definitions from block '" + blockName + "'",
                blockName, pageName, String.valueOf(fields.size()),
                () -> { /* records the step */ });

        return fields;
    }

    public static List<FlexiField> captureFromBlock(
            String blockName, String pageName, String blockLocator) {

        WebElement blockContainer = FlexiFieldResolver.resolveBlock(blockName, blockLocator);
        List<WebElement> wrappers = blockContainer.findElements(
                By.cssSelector("div[class*='component-main-wrapper']"));

        List<FlexiField> fields = new ArrayList<>();
        Set<String> seenIdentities = new LinkedHashSet<>();

        for (WebElement wrapper : wrappers) {
            if (!containsInteractable(wrapper)) continue;

            FlexiField field = buildFromWrapper(wrapper, blockName, pageName, blockLocator);

            String identity = resolveIdentityKey(field);
            if (seenIdentities.contains(identity)) {
                throw new RuntimeException("Duplicate field identity '" + identity
                        + "' in block '" + blockName + "' -- capture cannot proceed.");
            }
            seenIdentities.add(identity);
            fields.add(field);
        }

        return fields;
    }

    // ============================================
    // Build from single wrapper
    // ============================================

    private static FlexiField buildFromWrapper(
            WebElement wrapper, String blockName, String pageName, String blockLocator) {

        // Type from wrapper class
        FlexiFieldType type = FlexiFieldType.fromCssClass(wrapper.getAttribute("class"));

        // Key: try Key\d+ from child IDs
        String fieldKey = extractKeyFromIds(wrapper);

        // Label: structural label from wrapper context
        String fieldLabel = extractLabel(wrapper);

        // FAIL if both missing
        if (isBlank(fieldKey) && isBlank(fieldLabel)) {
            throw new RuntimeException(
                    "Cannot derive fieldKey or fieldLabel from wrapper in block '"
                            + blockName + "' -- capture failed.");
        }

        // Extract all config attribute groups relative to this wrapper
        FlexiField.CoreConfig coreConfig = extractCoreConfig(wrapper);
        FlexiField.MrvGrouping mrvGrouping = extractMrvGrouping(wrapper);
        FlexiField.BusinessRules businessRules = extractBusinessRules(wrapper);
        FlexiField.Advanced advanced = extractAdvanced(wrapper);
        FlexiField.SelectTypes selectTypes = extractSelectTypes(wrapper);
        FlexiField.ApplicableChannels channels = extractApplicableChannels(wrapper);

        return FlexiField.builder()
                .fieldKey(isBlank(fieldKey) ? null : fieldKey)
                .fieldLabel(isBlank(fieldLabel) ? null : fieldLabel)
                .fieldType(type)
                .blockName(blockName)
                .pageName(pageName)
                .blockLocator(blockLocator)
                .coreConfig(coreConfig)
                .mrvGrouping(mrvGrouping)
                .businessRules(businessRules)
                .advanced(advanced)
                .selectTypes(selectTypes)
                .applicableChannels(channels)
                .build();
    }

    // ============================================
    // Identity Extraction
    // ============================================

    private static String extractKeyFromIds(WebElement wrapper) {
        try {
            List<WebElement> idElements = wrapper.findElements(By.cssSelector("[id]"));
            for (WebElement el : idElements) {
                String id = el.getAttribute("id");
                if (id != null) {
                    Matcher m = KEY_ID_PATTERN.matcher(id);
                    if (m.find()) return m.group(1);
                }
            }
        } catch (Exception e) {
            logger.debug("Key extraction from IDs failed: {}", e.getMessage());
        }
        return null;
    }

    private static String extractLabel(WebElement wrapper) {
        // Primary: form-item label relative to wrapper
        String label = findLabelInFormItem(wrapper);
        if (label != null) return label;

        // Fallback: descendant label/span within wrapper
        return findDescendantLabel(wrapper);
    }

    private static String findLabelInFormItem(WebElement wrapper) {
        String[] xpaths = {
                "ancestor::div[contains(@class,'ant-form-item')][1]//label",
                "ancestor::div[contains(@class,'ant-row')][1]//label"
        };
        for (String xpath : xpaths) {
            String text = getVisibleText(wrapper, xpath);
            if (text != null) return cleanLabel(text);
        }
        return null;
    }

    private static String findDescendantLabel(WebElement wrapper) {
        String[] xpaths = { ".//label", ".//span[contains(@class,'label')]" };
        for (String xpath : xpaths) {
            String text = getVisibleText(wrapper, xpath);
            if (text != null) return cleanLabel(text);
        }
        return null;
    }

    // ============================================
    // Config Attribute Extraction -- CoreConfig
    // ============================================

    private static FlexiField.CoreConfig extractCoreConfig(WebElement wrapper) {
        // All extraction is relative to the wrapper's enclosing form context
        WebElement formContext = resolveFormContext(wrapper);

        return FlexiField.CoreConfig.builder()
                .fieldName(readDropdownByLabel(formContext, "Field Name"))
                .sortOrder(readInputByLabel(formContext, "Sort Order"))
                .lovType(readDropdownByLabel(formContext, "LOV Type"))
                .fieldSource(readInputOrDropdownByLabel(formContext, "Field Source"))
                .parentLov(readDropdownByLabel(formContext, "Parent LOV"))
                .configLabel(readInputByLabel(formContext, "Label"))
                .toolTip(readInputByLabel(formContext, "Tool Tip"))
                .minimumValue(readInputByLabel(formContext, "Minimum Value"))
                .maximumValue(readInputByLabel(formContext, "Maximum Value"))
                .dateRangeFrom(readDatePickerByLabel(formContext, "Date Range From"))
                .dateRangeTo(readDatePickerByLabel(formContext, "Date Range To"))
                .build();
    }

    // ============================================
    // Config Attribute Extraction -- MrvGrouping
    // ============================================

    private static FlexiField.MrvGrouping extractMrvGrouping(WebElement wrapper) {
        WebElement formContext = resolveFormContext(wrapper);

        return FlexiField.MrvGrouping.builder()
                .mrv(readCheckboxByLabel(formContext, "MRV"))
                .mrvFieldOrder(readInputByLabel(formContext, "MRV Field Order"))
                .groupName(readInputByLabel(formContext, "Group Name"))
                .groupOrderNum(readInputByLabel(formContext, "Group Order Num"))
                .build();
    }

    // ============================================
    // Config Attribute Extraction -- BusinessRules
    // ============================================

    private static FlexiField.BusinessRules extractBusinessRules(WebElement wrapper) {
        WebElement formContext = resolveFormContext(wrapper);

        return FlexiField.BusinessRules.builder()
                .tariffRange(readDropdownByLabel(formContext, "Tariff Range"))
                .raApplicable(readDropdownByLabel(formContext, "RA Applicable"))
                .mandatoryLevels(readDropdownByLabel(formContext, "Mandatory Levels"))
                .defaultValue(readInputByLabel(formContext, "Default Value"))
                .build();
    }

    // ============================================
    // Config Attribute Extraction -- Advanced
    // ============================================

    private static FlexiField.Advanced extractAdvanced(WebElement wrapper) {
        WebElement formContext = resolveFormContext(wrapper);

        return FlexiField.Advanced.builder()
                .apiDriven(readCheckboxByLabel(formContext, "API Driven"))
                .events(readDropdownByLabel(formContext, "Events"))
                .dependsOn(readDropdownByLabel(formContext, "Depends On"))
                .build();
    }

    // ============================================
    // Config Attribute Extraction -- SelectTypes
    // ============================================

    private static FlexiField.SelectTypes extractSelectTypes(WebElement wrapper) {
        // Select Types are checkboxes identified by their label text
        // Search within the wrapper's page context (sibling sections)
        WebElement formContext = resolveFormContext(wrapper);

        return FlexiField.SelectTypes.builder()
                .mandatory(readCheckboxByLabelText(formContext, "Mandatory"))
                .enterable(readCheckboxByLabelText(formContext, "Enterable"))
                .hide(readCheckboxByLabelText(formContext, "Hide"))
                .claimsLookup(readCheckboxByLabelText(formContext, "Claims Lookup"))
                .tariff(readCheckboxByLabelText(formContext, "Tariff"))
                .excludeForTax(readCheckboxByLabelText(formContext, "Exclude for Tax"))
                .sectionSummary(readCheckboxByLabelText(formContext, "Section Summary"))
                .uwListing(readCheckboxByLabelText(formContext, "UW Listing"))
                .claimListing(readCheckboxByLabelText(formContext, "Claim Listing"))
                .userAuthorization(readCheckboxByLabelText(formContext, "User Authorization"))
                .endorsementChanges(readCheckboxByLabelText(formContext, "Endorsement Changes"))
                .build();
    }

    // ============================================
    // Config Attribute Extraction -- ApplicableChannels
    // ============================================

    private static FlexiField.ApplicableChannels extractApplicableChannels(WebElement wrapper) {
        WebElement formContext = resolveFormContext(wrapper);
        List<String> active = new ArrayList<>();

        String[] channelNames = {"B2B", "B2C", "Core"};
        for (String name : channelNames) {
            Boolean on = readToggleByLabel(formContext, name);
            if (Boolean.TRUE.equals(on)) {
                active.add(name);
            }
        }

        return active.isEmpty() ? null : FlexiField.ApplicableChannels.of(active);
    }

    // ============================================
    // Generic Read Helpers (wrapper-relative)
    // ============================================

    /**
     * Resolve the enclosing form context for a wrapper.
     * This is the nearest ancestor that contains the full field mapping form
     * (all config attributes are siblings within this context).
     */
    private static WebElement resolveFormContext(WebElement wrapper) {
        // Try: nearest ant-card, form-section, collapse-content, or block-container
        String[] ancestors = {
                "ancestor::div[contains(@class,'ant-card-body')][1]",
                "ancestor::div[contains(@class,'ant-collapse-content')][1]",
                "ancestor::div[contains(@class,'form-section')][1]",
                "ancestor::div[contains(@class,'block-container')][1]"
        };
        for (String xpath : ancestors) {
            try {
                WebElement ctx = wrapper.findElement(By.xpath(xpath));
                if (ctx != null) return ctx;
            } catch (NoSuchElementException e) { /* try next */ }
        }
        // Fallback: use wrapper's parent as context
        try {
            return wrapper.findElement(By.xpath(".."));
        } catch (Exception e) {
            return wrapper;
        }
    }

    /**
     * Read text input value by finding the label with matching text,
     * then navigating to the adjacent input within the same form-item.
     */
    private static String readInputByLabel(WebElement context, String labelText) {
        try {
            WebElement formItem = findFormItemByLabel(context, labelText);
            if (formItem == null) return null;

            WebElement input = formItem.findElement(By.xpath(
                    ".//input[@type='text' or @type='number' or not(@type)] | .//textarea"));
            return nullIfBlank(input.getAttribute("value"));
        } catch (NoSuchElementException e) {
            return null;
        } catch (Exception e) {
            logger.debug("Failed to read input for label '{}': {}", labelText, e.getMessage());
            return null;
        }
    }

    /**
     * Read dropdown selected value by label text.
     */
    private static String readDropdownByLabel(WebElement context, String labelText) {
        try {
            WebElement formItem = findFormItemByLabel(context, labelText);
            if (formItem == null) return null;

            // Primary: ant-select-selection-item text
            WebElement selItem = formItem.findElement(
                    By.cssSelector(".ant-select-selection-item"));
            return nullIfBlank(selItem.getText());
        } catch (NoSuchElementException e) {
            return null;
        } catch (Exception e) {
            logger.debug("Failed to read dropdown for label '{}': {}", labelText, e.getMessage());
            return null;
        }
    }

    /**
     * Read a field that could be either input or dropdown.
     */
    private static String readInputOrDropdownByLabel(WebElement context, String labelText) {
        String val = readDropdownByLabel(context, labelText);
        if (val != null) return val;
        return readInputByLabel(context, labelText);
    }

    /**
     * Read date picker value by label text.
     */
    private static String readDatePickerByLabel(WebElement context, String labelText) {
        try {
            WebElement formItem = findFormItemByLabel(context, labelText);
            if (formItem == null) return null;

            WebElement input = formItem.findElement(By.xpath(
                    ".//input[contains(@id,'datePicker') or contains(@class,'ant-picker-input')]"
                    + " | .//div[contains(@class,'ant-picker')]//input"));
            return nullIfBlank(input.getAttribute("value"));
        } catch (NoSuchElementException e) {
            return null;
        } catch (Exception e) {
            logger.debug("Failed to read date picker for label '{}': {}", labelText, e.getMessage());
            return null;
        }
    }

    /**
     * Read checkbox state by label text within a form-item.
     * Returns null if checkbox not found (optional field).
     */
    private static Boolean readCheckboxByLabel(WebElement context, String labelText) {
        try {
            WebElement formItem = findFormItemByLabel(context, labelText);
            if (formItem == null) return null;

            WebElement checkbox = formItem.findElement(By.xpath(
                    ".//input[@type='checkbox'] | .//span[contains(@class,'ant-checkbox')]"));
            return isCheckboxChecked(checkbox);
        } catch (NoSuchElementException e) {
            return null;
        } catch (Exception e) {
            logger.debug("Failed to read checkbox for label '{}': {}", labelText, e.getMessage());
            return null;
        }
    }

    /**
     * Read checkbox state by matching the checkbox label text directly.
     * Used for Select Types where checkboxes are identified by visible text.
     */
    private static Boolean readCheckboxByLabelText(WebElement context, String labelText) {
        try {
            // Find span/label containing the exact text, then find the checkbox within its wrapper
            WebElement labelEl = context.findElement(By.xpath(
                    ".//span[normalize-space()='" + labelText + "']"
                    + " | .//label[normalize-space()='" + labelText + "']"));

            // The checkbox is either a sibling or within the same ant-checkbox-wrapper ancestor
            WebElement checkboxWrapper;
            try {
                checkboxWrapper = labelEl.findElement(By.xpath(
                        "ancestor::label[contains(@class,'ant-checkbox-wrapper')][1]"));
            } catch (NoSuchElementException e) {
                checkboxWrapper = labelEl.findElement(By.xpath(
                        "ancestor::span[contains(@class,'ant-checkbox-wrapper')][1]"
                        + " | preceding-sibling::span[contains(@class,'ant-checkbox')][1]"
                        + " | ../input[@type='checkbox']"));
            }

            String cls = checkboxWrapper.getAttribute("class");
            if (cls != null && cls.contains("ant-checkbox-wrapper-checked")) return true;

            // Fallback: find the actual input inside
            try {
                WebElement input = checkboxWrapper.findElement(By.xpath(
                        ".//input[@type='checkbox'] | .//span[contains(@class,'ant-checkbox')]"));
                return isCheckboxChecked(input);
            } catch (NoSuchElementException e) {
                return cls != null && cls.contains("checked");
            }
        } catch (NoSuchElementException e) {
            return null; // checkbox not present -- optional
        } catch (Exception e) {
            logger.debug("Failed to read select-type checkbox '{}': {}", labelText, e.getMessage());
            return null;
        }
    }

    /**
     * Read toggle switch state by label text.
     */
    private static Boolean readToggleByLabel(WebElement context, String labelText) {
        try {
            // Try by ID pattern first (e.g. B2B_toggle, Core_toggle)
            String idGuess = labelText + "_toggle";
            List<WebElement> byId = context.findElements(By.xpath(
                    ".//*[@id='" + idGuess + "']"));
            WebElement toggleContainer = null;
            if (!byId.isEmpty()) {
                toggleContainer = byId.get(0);
            } else {
                // Fallback: find by label text near a toggle
                WebElement labelEl = context.findElement(By.xpath(
                        ".//*[normalize-space()='" + labelText + "']"));
                toggleContainer = labelEl.findElement(By.xpath(
                        "ancestor::div[.//button[contains(@class,'ant-switch')]][1]"));
            }

            WebElement switchBtn = toggleContainer.findElement(By.xpath(
                    ".//button[contains(@class,'ant-switch')]"));
            String cls = switchBtn.getAttribute("class");
            if (cls != null && cls.contains("ant-switch-checked")) return true;
            return "true".equals(switchBtn.getAttribute("aria-checked"));
        } catch (NoSuchElementException e) {
            return null; // toggle not present -- optional
        } catch (Exception e) {
            logger.debug("Failed to read toggle for '{}': {}", labelText, e.getMessage());
            return null;
        }
    }

    // ============================================
    // Form Item Locator
    // ============================================

    /**
     * Find the ant-form-item (or equivalent row) that contains a label matching the given text.
     * Primary: label text match. Fallback: ID-based match if label contains known ID fragment.
     */
    private static WebElement findFormItemByLabel(WebElement context, String labelText) {
        try {
            // Primary: find label element by normalized text, then traverse to form-item
            WebElement label = context.findElement(By.xpath(
                    ".//label[normalize-space()='" + labelText + "']"
                    + " | .//span[normalize-space()='" + labelText + "']"
                    + "/ancestor::div[contains(@class,'ant-form-item-label')][1]/.."));

            // If we found a label, get its enclosing form-item
            try {
                return label.findElement(By.xpath(
                        "ancestor::div[contains(@class,'ant-form-item')][1]"));
            } catch (NoSuchElementException e) {
                // Fallback: use the label's parent row
                return label.findElement(By.xpath(
                        "ancestor::div[contains(@class,'ant-row')][1]"));
            }
        } catch (NoSuchElementException e) {
            return null; // label not found -- field not present in this context
        } catch (Exception e) {
            logger.debug("Failed to find form-item for label '{}': {}", labelText, e.getMessage());
            return null;
        }
    }

    // ============================================
    // Utility
    // ============================================

    private static boolean containsInteractable(WebElement wrapper) {
        try {
            return !wrapper.findElements(By.xpath(
                    ".//input | .//textarea | .//button | .//*[@role='combobox']"
                    + " | .//div[contains(@class,'ant-select')]"
            )).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isCheckboxChecked(WebElement el) {
        try {
            if (el.isSelected()) return true;
        } catch (Exception e) { /* not an input */ }

        String cls = el.getAttribute("class");
        if (cls != null && cls.contains("ant-checkbox-checked")) return true;
        return "true".equals(el.getAttribute("aria-checked"));
    }

    private static String getVisibleText(WebElement parent, String xpath) {
        try {
            WebElement el = parent.findElement(By.xpath(xpath));
            String text = el.getText();
            if (text != null && !text.trim().isEmpty()) return text.trim();
        } catch (NoSuchElementException e) { /* not found */ }
        return null;
    }

    private static String cleanLabel(String text) {
        if (text == null) return null;
        return text.replaceAll("[*]", "").trim();
    }

    private static String nullIfBlank(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Resolve a unique identity string for duplicate detection.
     * Uses key if available, else normalized label.
     */
    private static String resolveIdentityKey(FlexiField field) {
        if (!isBlank(field.getFieldKey())) return "key:" + field.getFieldKey();
        return "label:" + field.getFieldLabel().trim().toLowerCase();
    }

    private static void logCapturedFields(String blockName, List<FlexiField> fields) {
        logger.info("+-- Captured {} flexifield(s) from block '{}' --", fields.size(), blockName);
        for (int i = 0; i < fields.size(); i++) {
            FlexiField f = fields.get(i);
            String key = isBlank(f.getFieldKey()) ? "(none)" : f.getFieldKey();
            String label = isBlank(f.getFieldLabel()) ? "(none)" : f.getFieldLabel();
            logger.info("| [{}] key={}, label='{}', type={}", i + 1, key, label, f.getFieldType());
        }
        logger.info("+-- End capture for block '{}' --", blockName);
    }
}
