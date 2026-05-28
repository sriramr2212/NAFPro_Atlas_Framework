@Demo @GenericHTML @SelectionState
Feature: Generic HTML Table - Selection & State Validation
  Atlas v2.2 Phase 1: Row selection and table state assertions on Generic HTML tables.
  Uses local fixture: generic-html-selection-state-demo.html

  Background:
    When I navigate to local fixture "generic-html-selection-state-demo.html"

  @Demo @GenericHTML @SelectionState @TC_DEMO_GENERIC_SS_001
  Scenario: Select and verify single row
    When I select the row where "Order ID" is "TD_OrderId" in "orderTable"
    Then the row where "Order ID" is "TD_OrderId" should be selected in "orderTable"
    And the selected row count in "orderTable" should be 1
    Then I log "Generic HTML: Single row selection verified" with status "PASS"

  @Demo @GenericHTML @SelectionState @TC_DEMO_GENERIC_SS_002
  Scenario: Select multiple rows and deselect one
    When I select the row where "Order ID" is "TD_OrderId1" in "orderTable"
    And I select the row where "Order ID" is "TD_OrderId2" in "orderTable"
    And I select the row where "Order ID" is "TD_OrderId3" in "orderTable"
    Then the selected row count in "orderTable" should be 3
    When I deselect the row where "Order ID" is "TD_OrderId2" in "orderTable"
    Then the selected row count in "orderTable" should be 2
    And the row where "Order ID" is "TD_OrderId2" should not be selected in "orderTable"
    Then I log "Generic HTML: Multi-select + deselect verified" with status "PASS"

  @Demo @GenericHTML @SelectionState @TC_DEMO_GENERIC_SS_003
  Scenario: Select all and deselect all
    When I select all rows in "orderTable"
    Then the selected row count in "orderTable" should be 5
    When I deselect all rows in "orderTable"
    Then the selected row count in "orderTable" should be 0
    Then I log "Generic HTML: Select all / deselect all verified" with status "PASS"

  @Demo @GenericHTML @SelectionState @TC_DEMO_GENERIC_SS_004
  Scenario: Verify table is not loading and not empty
    Then the table "orderTable" should not be loading
    And the table "orderTable" should not be empty
    Then I log "Generic HTML: Normal table state verified (not loading, not empty)" with status "PASS"

  @Demo @GenericHTML @SelectionState @TC_DEMO_GENERIC_SS_005
  Scenario: Verify empty table state and message
    Then the table "emptyTableContainer" should be empty
    And the empty state message in "emptyTableContainer" should be "TD_EmptyMessage"
    Then I log "Generic HTML: Empty state and message verified" with status "PASS"

  @Demo @GenericHTML @SelectionState @TC_DEMO_GENERIC_SS_006
  Scenario: Verify loading table state
    Then the table "loadingTableContainer" should be loading
    Then I log "Generic HTML: Loading state verified" with status "PASS"
