@Demo @GenericHTML
Feature: Generic HTML Table - Enterprise Table Engine Validation
  Demonstrates Atlas v2.0 Table Engine capabilities on standard HTML tables.
  Uses local fixture: generic-html-table-demo.html (3 pages, 15 records)

  Background:
    When I navigate to local fixture "generic-html-table-demo.html"

  @Demo @GenericHTML @TC_DEMO_GENERIC_001
  Scenario: Find and verify customer row on first page
    When I find the row where "Customer Name" is "TD_CustomerName" in "customerTable"
    Then the row where "Customer Name" is "TD_CustomerName" should have:
      | Column  | Value      |
      | Status  | TD_Status  |
      | Product | TD_Product |
      | Premium | TD_Premium |
    Then I log "Generic HTML: Row found and verified on page 1" with status "PASS"

  @Demo @GenericHTML @TC_DEMO_GENERIC_002
  Scenario: Perform Edit action on table row
    When I find the row where "Customer Name" is "TD_CustomerName" in "customerTable"
    And I perform "Edit" action on the row where "Customer Name" is "TD_CustomerName" in "customerTable"
    Then I log "Generic HTML: Edit action executed successfully" with status "PASS"

  @Demo @GenericHTML @TC_DEMO_GENERIC_003
  Scenario: Cross-page search finds record on page 2
    When I find the row where "Customer Name" is "TD_CustomerName" in "customerTable"
    Then the row where "Customer Name" is "TD_CustomerName" should have:
      | Column  | Value      |
      | Status  | TD_Status  |
      | Region  | TD_Region  |
      | Premium | TD_Premium |
    Then I log "Generic HTML: Cross-page search found record on page 2" with status "PASS"

  @Demo @GenericHTML @TC_DEMO_GENERIC_004
  Scenario: Multi-column criteria search
    When I find the row matching criteria in "customerTable":
      | Column        | Value      |
      | Product       | TD_Product |
      | Region        | TD_Region  |
    Then the found row should have "Customer Name" as "TD_CustomerName"
    Then I log "Generic HTML: Multi-column criteria search successful" with status "PASS"

  @Demo @GenericHTML @TC_DEMO_GENERIC_005
  Scenario: Verify row count on current page
    Then the row count in "customerTable" should be 5
    Then I log "Generic HTML: Row count verified (5 per page)" with status "PASS"

  @Demo @GenericHTML @AtlasProof @TC_DEMO_GENERIC_006
  Scenario: Atlas Cross-Framework Proof - Find record on page 3 and verify
    When I find the row where "Customer Name" is "TD_CustomerName" in "customerTable"
    Then the row where "Customer Name" is "TD_CustomerName" should have:
      | Column  | Value      |
      | Status  | TD_Status  |
      | Product | TD_Product |
      | Premium | TD_Premium |
    Then I log "Atlas Proof: Same Gherkin, Generic HTML technology, zero framework change" with status "PASS"
