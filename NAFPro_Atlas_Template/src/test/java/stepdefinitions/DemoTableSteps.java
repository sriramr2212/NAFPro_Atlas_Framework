package stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import novac.reporting.StepDescription;
import novac.reporting.StepReportingWrapper;
import novac.utils.TestContext;
import novac.utils.TestDataManager;
import novac.utils.MissingDataTracker;
import novac.wrapper.GenericWrapper;
import novac.wrapper.core.TableEngine;
import novac.wrapper.core.TableEditor;
import novac.wrapper.core.TableExpander;
import novac.wrapper.core.TableFilter;
import novac.wrapper.core.TableRowContext;
import novac.wrapper.core.TableSelector;
import novac.wrapper.core.TableSorter;
import novac.wrapper.core.TableSorter.SortOrder;
import novac.wrapper.core.TableStateAssert;
import novac.wrapper.factory.ComponentHandlerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DemoTableSteps {

    private static final Logger logger = LogManager.getLogger(DemoTableSteps.class);

    @When("I navigate to local fixture {string}")
    @StepDescription("Navigate to local HTML fixture file")
    public void i_navigate_to_local_fixture(String fixtureName) {
        StepReportingWrapper.executeStep("Navigate to fixture: " + fixtureName, () -> {
            WebDriver driver = GenericWrapper.getDriver();
            String fixturePath = new File("src/test/resources/fixtures/" + fixtureName).getAbsolutePath();
            String fileUrl = "file:///" + fixturePath.replace("\\", "/");
            driver.get(fileUrl);
            logger.info("Navigated to fixture: {}", fileUrl);
        });
    }

    @When("I find the row where {string} is {string} in {string}")
    @StepDescription("Find row by column value using Table Engine")
    public void i_find_row_where(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Find row where %s='%s' in %s", column, resolvedValue, tableLocator), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            WebElement row = engine.findRow(driver, column, resolvedValue);
            TestContext.setCurrentTableRow(TableRowContext.of(tableRoot, column, resolvedValue, row));
            logger.info("Row found where {}='{}'", column, resolvedValue);
        });
    }

    @Then("the row where {string} is {string} should have:")
    @StepDescription("Verify row cell values using Table Engine")
    public void the_row_should_have(String column, String value, DataTable dataTable) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Verify row where %s='%s' has expected values", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            TableRowContext ctx = TestContext.hasCurrentTableRow() ? TestContext.getCurrentTableRowContext() : null;
            WebElement tableRoot = ctx != null ? ctx.getTableRoot() : resolveTableRoot(driver, "");
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            WebElement row = engine.findRow(driver, column, resolvedValue);

            List<Map<String, String>> rows = dataTable.asMaps();
            for (Map<String, String> expected : rows) {
                String colName = expected.get("Column");
                String expectedVal = resolveTestData(expected.get("Value"));
                String actualVal = engine.getCellValue(driver, row, colName);
                if (!expectedVal.equalsIgnoreCase(actualVal)) {
                    throw new AssertionError(String.format(
                            "Cell mismatch in column '%s': expected '%s', actual '%s'",
                            colName, expectedVal, actualVal));
                }
                logger.info("Verified: {}='{}' ✓", colName, actualVal);
            }
        });
    }

    @When("I perform {string} action on the row where {string} is {string} in {string}")
    @StepDescription("Perform action on row using Table Engine")
    public void i_perform_action_on_row(String action, String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Perform '%s' on row where %s='%s'", action, column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            WebElement row = engine.findRow(driver, column, resolvedValue);
            engine.performRowAction(driver, row, action);
            logger.info("Action '{}' performed on row where {}='{}'", action, column, resolvedValue);
        });
    }

    @Then("the row count in {string} should be {int}")
    @StepDescription("Verify table row count")
    public void the_row_count_should_be(String tableLocator, int expectedCount) {
        StepReportingWrapper.executeStep(
                String.format("Verify row count in %s is %d", tableLocator, expectedCount), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            int actual = engine.getRowCount(driver);
            if (actual != expectedCount) {
                throw new AssertionError(String.format(
                        "Row count mismatch: expected %d, actual %d", expectedCount, actual));
            }
            logger.info("Row count verified: {}", actual);
        });
    }

    @Then("the row where {string} is {string} should exist in {string}")
    @StepDescription("Verify row exists using Table Engine")
    public void the_row_should_exist(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Verify row exists where %s='%s'", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            boolean exists = engine.rowExists(driver, column, resolvedValue);
            if (!exists) {
                throw new AssertionError("Row not found where " + column + "='" + resolvedValue + "'");
            }
            logger.info("Row exists: {}='{}' ✓", column, resolvedValue);
        });
    }

    @When("I find the row matching criteria in {string}:")
    @StepDescription("Find row by multi-column criteria using Table Engine")
    public void i_find_row_by_criteria(String tableLocator, DataTable dataTable) {
        StepReportingWrapper.executeStep("Find row by multi-column criteria", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);

            Map<String, String> criteria = new LinkedHashMap<>();
            List<Map<String, String>> rows = dataTable.asMaps();
            for (Map<String, String> row : rows) {
                String col = row.get("Column");
                String val = resolveTestData(row.get("Value"));
                criteria.put(col, val);
            }

            WebElement row = engine.findRowByCriteria(driver, criteria);
            TestContext.setCurrentTableRow(TableRowContext.ofCriteria(tableRoot, criteria, row));
            logger.info("Row found matching criteria: {}", criteria);
        });
    }

    @Then("the found row should have {string} as {string}")
    @StepDescription("Verify cell value on previously found row")
    public void the_found_row_should_have(String column, String expectedValue) {
        String resolvedValue = resolveTestData(expectedValue);
        StepReportingWrapper.executeStep(
                String.format("Verify found row has %s='%s'", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            TableRowContext ctx = TestContext.getCurrentTableRowContext();
            TableEngine engine = ComponentHandlerFactory.getTableEngine(ctx.getTableRoot(), driver);
            String actual = engine.getCellValue(driver, ctx.getCurrentRow(), column);
            if (!resolvedValue.equalsIgnoreCase(actual)) {
                throw new AssertionError(String.format(
                        "Cell mismatch in '%s': expected '%s', actual '%s'", column, resolvedValue, actual));
            }
            logger.info("Verified: {}='{}' ✓", column, actual);
        });
    }

    @When("I perform {string} action on the found row in {string}")
    @StepDescription("Perform action on previously found row")
    public void i_perform_action_on_found_row(String action, String tableLocator) {
        StepReportingWrapper.executeStep("Perform '" + action + "' on found row", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            TableRowContext ctx = TestContext.getCurrentTableRowContext();
            TableEngine engine = ComponentHandlerFactory.getTableEngine(ctx.getTableRoot(), driver);
            engine.performRowAction(driver, ctx.getCurrentRow(), action);
            logger.info("Action '{}' performed on found row", action);
        });
    }

    // --- Sort/Filter Steps (Atlas v2.1 Phase 2) ---

    @When("I sort the table by {string} in {string} order in {string}")
    @StepDescription("Sort table by column using Table Engine")
    public void i_sort_table_by_column(String column, String order, String tableLocator) {
        StepReportingWrapper.executeStep(
                String.format("Sort table by '%s' in %s order", column, order), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableSorter sorter)) {
                throw new UnsupportedOperationException("Table engine does not support sorting");
            }
            SortOrder sortOrder = SortOrder.valueOf(order.toUpperCase());
            sorter.sortByColumn(driver, column, sortOrder);
            logger.info("Table sorted by '{}' in {} order", column, order);
        });
    }

    @Then("the {string} column should be sorted in {string} order in {string}")
    @StepDescription("Verify column sort order using actual data validation")
    public void column_should_be_sorted(String column, String order, String tableLocator) {
        StepReportingWrapper.executeStep(
                String.format("Verify '%s' column is sorted %s", column, order), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableSorter sorter)) {
                throw new UnsupportedOperationException("Table engine does not support sorting");
            }
            SortOrder expected = SortOrder.valueOf(order.toUpperCase());
            boolean valid = sorter.verifySortOrder(driver, column, expected);
            if (!valid) {
                throw new AssertionError("Column '" + column + "' is NOT sorted in " + order + " order");
            }
            logger.info("Verified: '{}' column sorted {} ✓", column, order);
        });
    }

    @When("I filter the table by {string} with value {string} in {string}")
    @StepDescription("Filter table column contains value")
    public void i_filter_table_by_column(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Filter '%s' contains '%s'", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableFilter filter)) {
                throw new UnsupportedOperationException("Table engine does not support filtering");
            }
            filter.filterColumnContains(driver, column, resolvedValue);
            logger.info("Filtered '{}' contains '{}'", column, resolvedValue);
        });
    }

    @When("I clear the filter on {string} in {string}")
    @StepDescription("Clear column filter")
    public void i_clear_column_filter(String column, String tableLocator) {
        StepReportingWrapper.executeStep("Clear filter on '" + column + "'", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableFilter filter)) {
                throw new UnsupportedOperationException("Table engine does not support filtering");
            }
            filter.clearColumnFilter(driver, column);
            logger.info("Cleared filter on '{}'", column);
        });
    }

    @When("I clear all filters in {string}")
    @StepDescription("Clear all table filters")
    public void i_clear_all_filters(String tableLocator) {
        StepReportingWrapper.executeStep("Clear all filters", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableFilter filter)) {
                throw new UnsupportedOperationException("Table engine does not support filtering");
            }
            filter.clearAllFilters(driver);
            logger.info("All filters cleared");
        });
    }

    @When("I search globally for {string} in {string}")
    @StepDescription("Global search in table")
    public void i_search_globally(String searchText, String tableLocator) {
        String resolvedText = resolveTestData(searchText);
        StepReportingWrapper.executeStep("Global search: '" + resolvedText + "'", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableFilter filter)) {
                throw new UnsupportedOperationException("Table engine does not support filtering");
            }
            filter.globalSearch(driver, resolvedText);
            logger.info("Global search applied: '{}'", resolvedText);
        });
    }

    @When("I clear the global search in {string}")
    @StepDescription("Clear global search")
    public void i_clear_global_search(String tableLocator) {
        StepReportingWrapper.executeStep("Clear global search", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableFilter filter)) {
                throw new UnsupportedOperationException("Table engine does not support filtering");
            }
            filter.clearGlobalSearch(driver);
            logger.info("Global search cleared");
        });
    }

    @Then("a row where {string} is {string} should not exist in {string}")
    @StepDescription("Verify row does NOT exist after filter")
    public void row_should_not_exist(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Verify row where %s='%s' does NOT exist", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            boolean exists = engine.rowExists(driver, column, resolvedValue);
            if (exists) {
                throw new AssertionError("Row should NOT exist where " + column + "='" + resolvedValue + "' but it was found");
            }
            logger.info("Confirmed: row where {}='{}' does NOT exist ✓", column, resolvedValue);
        });
    }

    // --- Selection Steps (Atlas v2.2 Phase 1) ---

    @When("I select the row where {string} is {string} in {string}")
    @StepDescription("Select row by checkbox using Table Engine")
    public void i_select_row(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Select row where %s='%s'", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableSelector selector)) {
                throw new UnsupportedOperationException("Table engine does not support selection");
            }
            selector.selectRow(driver, column, resolvedValue);
            logger.info("Row selected where {}='{}'", column, resolvedValue);
        });
    }

    @When("I deselect the row where {string} is {string} in {string}")
    @StepDescription("Deselect row by checkbox using Table Engine")
    public void i_deselect_row(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Deselect row where %s='%s'", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableSelector selector)) {
                throw new UnsupportedOperationException("Table engine does not support selection");
            }
            selector.deselectRow(driver, column, resolvedValue);
            logger.info("Row deselected where {}='{}'", column, resolvedValue);
        });
    }

    @When("I select all rows in {string}")
    @StepDescription("Select all rows via header checkbox")
    public void i_select_all_rows(String tableLocator) {
        StepReportingWrapper.executeStep("Select all rows", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableSelector selector)) {
                throw new UnsupportedOperationException("Table engine does not support selection");
            }
            selector.selectAll(driver);
            logger.info("All rows selected");
        });
    }

    @When("I deselect all rows in {string}")
    @StepDescription("Deselect all rows via header checkbox")
    public void i_deselect_all_rows(String tableLocator) {
        StepReportingWrapper.executeStep("Deselect all rows", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableSelector selector)) {
                throw new UnsupportedOperationException("Table engine does not support selection");
            }
            selector.deselectAll(driver);
            logger.info("All rows deselected");
        });
    }

    @Then("the selected row count in {string} should be {int}")
    @StepDescription("Verify selected row count")
    public void the_selected_count_should_be(String tableLocator, int expectedCount) {
        StepReportingWrapper.executeStep(
                String.format("Verify selected count is %d", expectedCount), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableSelector selector)) {
                throw new UnsupportedOperationException("Table engine does not support selection");
            }
            int actual = selector.getSelectedCount(driver);
            if (actual != expectedCount) {
                throw new AssertionError(String.format(
                        "Selected count mismatch: expected %d, actual %d", expectedCount, actual));
            }
            logger.info("Selected count verified: {}", actual);
        });
    }

    @Then("the row where {string} is {string} should be selected in {string}")
    @StepDescription("Verify row is selected")
    public void row_should_be_selected(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Verify row where %s='%s' is selected", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableSelector selector)) {
                throw new UnsupportedOperationException("Table engine does not support selection");
            }
            boolean selected = selector.isRowSelected(driver, column, resolvedValue);
            if (!selected) {
                throw new AssertionError("Row where " + column + "='" + resolvedValue + "' should be selected but is not");
            }
            logger.info("Verified: row where {}='{}' is selected ✓", column, resolvedValue);
        });
    }

    @Then("the row where {string} is {string} should not be selected in {string}")
    @StepDescription("Verify row is NOT selected")
    public void row_should_not_be_selected(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Verify row where %s='%s' is NOT selected", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableSelector selector)) {
                throw new UnsupportedOperationException("Table engine does not support selection");
            }
            boolean selected = selector.isRowSelected(driver, column, resolvedValue);
            if (selected) {
                throw new AssertionError("Row where " + column + "='" + resolvedValue + "' should NOT be selected but is");
            }
            logger.info("Verified: row where {}='{}' is NOT selected ✓", column, resolvedValue);
        });
    }

    // --- State Assertion Steps (Atlas v2.2 Phase 1) ---

    @Then("the table {string} should be loading")
    @StepDescription("Verify table is in loading state")
    public void table_should_be_loading(String tableLocator) {
        StepReportingWrapper.executeStep("Verify table is loading", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableStateAssert stateAssert)) {
                throw new UnsupportedOperationException("Table engine does not support state assertions");
            }
            boolean loading = stateAssert.isLoading(driver);
            if (!loading) {
                throw new AssertionError("Table should be in loading state but is not");
            }
            logger.info("Verified: table is loading ✓");
        });
    }

    @Then("the table {string} should not be loading")
    @StepDescription("Verify table is NOT in loading state")
    public void table_should_not_be_loading(String tableLocator) {
        StepReportingWrapper.executeStep("Verify table is NOT loading", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableStateAssert stateAssert)) {
                throw new UnsupportedOperationException("Table engine does not support state assertions");
            }
            boolean loading = stateAssert.isLoading(driver);
            if (loading) {
                throw new AssertionError("Table should NOT be loading but is");
            }
            logger.info("Verified: table is NOT loading ✓");
        });
    }

    @Then("the table {string} should be empty")
    @StepDescription("Verify table is empty")
    public void table_should_be_empty(String tableLocator) {
        StepReportingWrapper.executeStep("Verify table is empty", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableStateAssert stateAssert)) {
                throw new UnsupportedOperationException("Table engine does not support state assertions");
            }
            boolean empty = stateAssert.isEmpty(driver);
            if (!empty) {
                throw new AssertionError("Table should be empty but has data");
            }
            logger.info("Verified: table is empty ✓");
        });
    }

    @Then("the table {string} should not be empty")
    @StepDescription("Verify table is NOT empty")
    public void table_should_not_be_empty(String tableLocator) {
        StepReportingWrapper.executeStep("Verify table is NOT empty", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableStateAssert stateAssert)) {
                throw new UnsupportedOperationException("Table engine does not support state assertions");
            }
            boolean empty = stateAssert.isEmpty(driver);
            if (empty) {
                throw new AssertionError("Table should NOT be empty but is");
            }
            logger.info("Verified: table is NOT empty ✓");
        });
    }

    @Then("the empty state message in {string} should be {string}")
    @StepDescription("Verify empty state message text")
    public void empty_state_message_should_be(String tableLocator, String expectedMessage) {
        String resolvedMessage = resolveTestData(expectedMessage);
        StepReportingWrapper.executeStep(
                String.format("Verify empty message is '%s'", resolvedMessage), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableStateAssert stateAssert)) {
                throw new UnsupportedOperationException("Table engine does not support state assertions");
            }
            String actual = stateAssert.getEmptyStateMessage(driver);
            if (!resolvedMessage.equalsIgnoreCase(actual)) {
                throw new AssertionError(String.format(
                        "Empty state message mismatch: expected '%s', actual '%s'", resolvedMessage, actual));
            }
            logger.info("Verified: empty state message='{}' ✓", actual);
        });
    }

    // --- Expander Steps (Atlas v2.2 Phase 2) ---

    @When("I expand the row where {string} is {string} in {string}")
    @StepDescription("Expand row to show detail content")
    public void i_expand_row(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Expand row where %s='%s'", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableExpander expander)) {
                throw new UnsupportedOperationException("Table engine does not support expansion");
            }
            expander.expandRow(driver, column, resolvedValue);
            logger.info("Row expanded where {}='{}'", column, resolvedValue);
        });
    }

    @When("I collapse the row where {string} is {string} in {string}")
    @StepDescription("Collapse expanded row")
    public void i_collapse_row(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Collapse row where %s='%s'", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableExpander expander)) {
                throw new UnsupportedOperationException("Table engine does not support expansion");
            }
            expander.collapseRow(driver, column, resolvedValue);
            logger.info("Row collapsed where {}='{}'", column, resolvedValue);
        });
    }

    @Then("the row where {string} is {string} should be expanded in {string}")
    @StepDescription("Verify row is expanded")
    public void row_should_be_expanded(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Verify row where %s='%s' is expanded", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableExpander expander)) {
                throw new UnsupportedOperationException("Table engine does not support expansion");
            }
            if (!expander.isRowExpanded(driver, column, resolvedValue)) {
                throw new AssertionError("Row where " + column + "='" + resolvedValue + "' should be expanded but is not");
            }
            logger.info("Verified: row where {}='{}' is expanded \u2713", column, resolvedValue);
        });
    }

    @Then("the row where {string} is {string} should not be expanded in {string}")
    @StepDescription("Verify row is NOT expanded")
    public void row_should_not_be_expanded(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Verify row where %s='%s' is NOT expanded", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableExpander expander)) {
                throw new UnsupportedOperationException("Table engine does not support expansion");
            }
            if (expander.isRowExpanded(driver, column, resolvedValue)) {
                throw new AssertionError("Row where " + column + "='" + resolvedValue + "' should NOT be expanded but is");
            }
            logger.info("Verified: row where {}='{}' is NOT expanded \u2713", column, resolvedValue);
        });
    }

    @Then("the expanded content of row where {string} is {string} should contain {string} in {string}")
    @StepDescription("Verify expanded content contains text")
    public void expanded_content_should_contain(String column, String value, String expectedText, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        String resolvedText = resolveTestData(expectedText);
        StepReportingWrapper.executeStep(
                String.format("Verify expanded content contains '%s'", resolvedText), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableExpander expander)) {
                throw new UnsupportedOperationException("Table engine does not support expansion");
            }
            WebElement content = expander.getExpandedContent(driver, column, resolvedValue);
            String actual = content.getText();
            if (!actual.contains(resolvedText)) {
                throw new AssertionError("Expanded content does not contain '" + resolvedText + "'. Actual: " + actual);
            }
            logger.info("Verified: expanded content contains '{}' \u2713", resolvedText);
        });
    }

    @Then("getting expanded content of collapsed row where {string} is {string} should fail in {string}")
    @StepDescription("Verify getExpandedContent throws on collapsed row")
    public void get_expanded_content_should_fail(String column, String value, String tableLocator) {
        String resolvedValue = resolveTestData(value);
        StepReportingWrapper.executeStep(
                String.format("Verify getExpandedContent throws for collapsed row %s='%s'", column, resolvedValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableExpander expander)) {
                throw new UnsupportedOperationException("Table engine does not support expansion");
            }
            try {
                expander.getExpandedContent(driver, column, resolvedValue);
                throw new AssertionError("Expected exception but getExpandedContent succeeded on collapsed row");
            } catch (RuntimeException e) {
                if (!e.getMessage().contains("Row is not expanded")) {
                    throw new AssertionError("Expected 'Row is not expanded' message but got: " + e.getMessage());
                }
                logger.info("Verified: getExpandedContent correctly throws on collapsed row \u2713");
            }
        });
    }

    // --- Editor Steps (Atlas v2.2 Phase 2) ---

    @When("I edit the {string} cell to {string} in row where {string} is {string} in {string}")
    @StepDescription("Edit cell value using auto-detected type")
    public void i_edit_cell(String targetColumn, String newValue, String rowColumn, String rowValue, String tableLocator) {
        String resolvedRowValue = resolveTestData(rowValue);
        String resolvedNewValue = resolveTestData(newValue);
        StepReportingWrapper.executeStep(
                String.format("Edit '%s' to '%s' in row where %s='%s'", targetColumn, resolvedNewValue, rowColumn, resolvedRowValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableEditor editor)) {
                throw new UnsupportedOperationException("Table engine does not support editing");
            }
            editor.editCell(driver, rowColumn, resolvedRowValue, targetColumn, resolvedNewValue);
            logger.info("Edited '{}' to '{}' in row where {}='{}'", targetColumn, resolvedNewValue, rowColumn, resolvedRowValue);
        });
    }

    @When("I edit the {string} cell as {string} to {string} in row where {string} is {string} in {string}")
    @StepDescription("Edit cell value with explicit type")
    public void i_edit_cell_as(String targetColumn, String editType, String newValue, String rowColumn, String rowValue, String tableLocator) {
        String resolvedRowValue = resolveTestData(rowValue);
        String resolvedNewValue = resolveTestData(newValue);
        StepReportingWrapper.executeStep(
                String.format("Edit '%s' as %s to '%s'", targetColumn, editType, resolvedNewValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableEditor editor)) {
                throw new UnsupportedOperationException("Table engine does not support editing");
            }
            TableEditor.CellEditType type = TableEditor.CellEditType.valueOf(editType.toUpperCase());
            editor.editCellAs(driver, rowColumn, resolvedRowValue, targetColumn, resolvedNewValue, type);
            logger.info("Edited '{}' as {} to '{}'", targetColumn, editType, resolvedNewValue);
        });
    }

    @Then("the {string} cell in row where {string} is {string} should be editable in {string}")
    @StepDescription("Verify cell is editable")
    public void cell_should_be_editable(String targetColumn, String rowColumn, String rowValue, String tableLocator) {
        String resolvedRowValue = resolveTestData(rowValue);
        StepReportingWrapper.executeStep(
                String.format("Verify '%s' is editable in row where %s='%s'", targetColumn, rowColumn, resolvedRowValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableEditor editor)) {
                throw new UnsupportedOperationException("Table engine does not support editing");
            }
            if (!editor.isCellEditable(driver, rowColumn, resolvedRowValue, targetColumn)) {
                throw new AssertionError("Cell '" + targetColumn + "' should be editable but is not");
            }
            logger.info("Verified: '{}' is editable \u2713", targetColumn);
        });
    }

    @Then("the {string} cell in row where {string} is {string} should not be editable in {string}")
    @StepDescription("Verify cell is NOT editable")
    public void cell_should_not_be_editable(String targetColumn, String rowColumn, String rowValue, String tableLocator) {
        String resolvedRowValue = resolveTestData(rowValue);
        StepReportingWrapper.executeStep(
                String.format("Verify '%s' is NOT editable in row where %s='%s'", targetColumn, rowColumn, resolvedRowValue), () -> {
            WebDriver driver = GenericWrapper.getDriver();
            WebElement tableRoot = resolveTableRoot(driver, tableLocator);
            TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
            if (!(engine instanceof TableEditor editor)) {
                throw new UnsupportedOperationException("Table engine does not support editing");
            }
            if (editor.isCellEditable(driver, rowColumn, resolvedRowValue, targetColumn)) {
                throw new AssertionError("Cell '" + targetColumn + "' should NOT be editable but is");
            }
            logger.info("Verified: '{}' is NOT editable \u2713", targetColumn);
        });
    }

    // --- Helpers ---

    private WebElement resolveTableRoot(WebDriver driver, String tableLocator) {
        // Try by ID first
        List<WebElement> byId = driver.findElements(By.id(tableLocator));
        if (!byId.isEmpty()) return byId.get(0);

        // Try common table container patterns
        String[] selectors = {
                "#" + tableLocator,
                "[data-testid='" + tableLocator + "']",
                ".ant-table-wrapper",
                ".ag-root-wrapper",
                "table[role='grid']",
                "table"
        };
        for (String sel : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(sel));
                if (!elements.isEmpty()) return elements.get(0);
            } catch (Exception ignored) {}
        }
        throw new RuntimeException("Table not found for locator: " + tableLocator);
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
        return tcId != null ? tcId : "TC_DEMO_001";
    }
}
