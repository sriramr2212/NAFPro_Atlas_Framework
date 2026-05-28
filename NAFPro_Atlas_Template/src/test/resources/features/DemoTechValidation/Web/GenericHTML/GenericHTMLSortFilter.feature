@Demo @GenericHTML @SortFilter
Feature: Generic HTML Table - Sort & Filter Validation
  Atlas v2.1 Phase 2: Sort and Filter behaviors on Generic HTML tables.
  Uses local fixture: generic-html-table-demo.html (sortable headers + column filters + global search)

  Background:
    When I navigate to local fixture "generic-html-table-demo.html"

  @Demo @GenericHTML @SortFilter @TC_DEMO_GENERIC_SF_001
  Scenario: Sort table by Customer Name ascending
    When I sort the table by "Customer Name" in "ASC" order in "customerTable"
    Then the "Customer Name" column should be sorted in "ASC" order in "customerTable"
    Then I log "Generic HTML: Customer Name sorted ASC verified via actual data" with status "PASS"

  @Demo @GenericHTML @SortFilter @TC_DEMO_GENERIC_SF_002
  Scenario: Sort table by Premium descending (numeric)
    When I sort the table by "Premium" in "DESC" order in "customerTable"
    Then the "Premium" column should be sorted in "DESC" order in "customerTable"
    Then I log "Generic HTML: Premium sorted DESC verified (numeric comparison)" with status "PASS"

  @Demo @GenericHTML @SortFilter @TC_DEMO_GENERIC_SF_003
  Scenario: Filter table by Status and verify filtered row exists
    When I filter the table by "Status" with value "TD_FilterStatus" in "customerTable"
    Then the row where "Customer Name" is "TD_VisibleCustomer" should exist in "customerTable"
    Then I log "Generic HTML: Column filter applied - filtered row found" with status "PASS"

  @Demo @GenericHTML @SortFilter @TC_DEMO_GENERIC_SF_004
  Scenario: Global search filters across all columns
    When I search globally for "TD_SearchTerm" in "customerTable"
    Then the row where "Customer Name" is "TD_ExpectedCustomer" should exist in "customerTable"
    Then I log "Generic HTML: Global search verified - matching row found" with status "PASS"

  @Demo @GenericHTML @SortFilter @TC_DEMO_GENERIC_SF_005
  Scenario: Clear filter restores original row count
    When I filter the table by "Product" with value "Enterprise Suite" in "customerTable"
    When I clear the filter on "Product" in "customerTable"
    Then the row count in "customerTable" should be 5
    Then I log "Generic HTML: Clear filter restores original page data" with status "PASS"

  @Demo @GenericHTML @SortFilter @AtlasProof @TC_DEMO_GENERIC_SF_006
  Scenario: Atlas Proof - Sort + Filter combined workflow
    When I sort the table by "Premium" in "DESC" order in "customerTable"
    Then the "Premium" column should be sorted in "DESC" order in "customerTable"
    When I filter the table by "Status" with value "Active" in "customerTable"
    Then the row where "Customer Name" is "TD_VisibleCustomer" should exist in "customerTable"
    When I clear all filters in "customerTable"
    Then the row count in "customerTable" should be 5
    Then I log "Atlas Proof: Sort+Filter same Gherkin, Generic HTML, zero framework change" with status "PASS"
