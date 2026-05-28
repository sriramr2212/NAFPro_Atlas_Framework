@Demo @MUI
Feature: MUI DataGrid - Enterprise Table Engine Validation
  Demonstrates Atlas v2.1 Table Engine capabilities on MUI DataGrid.
  Uses local fixture: mui-datagrid-demo.html (3 pages, 15 records)

  Background:
    When I navigate to local fixture "mui-datagrid-demo.html"

  @Demo @MUI @TC_DEMO_MUI_001
  Scenario: Find invoice and verify row details
    When I find the row where "ID" is "TD_ID" in "muiDataGridDemo"
    Then the row where "ID" is "TD_ID" should have:
      | Column     | Value         |
      | Name       | TD_Name       |
      | Status     | TD_Status     |
      | Department | TD_Department |
      | Amount     | TD_Amount     |
    Then I log "MUI: Row found and details verified" with status "PASS"

  @Demo @MUI @TC_DEMO_MUI_002
  Scenario: Perform View action on table row
    When I find the row where "ID" is "TD_ID" in "muiDataGridDemo"
    And I perform "View" action on the row where "ID" is "TD_ID" in "muiDataGridDemo"
    Then I log "MUI: View action executed successfully" with status "PASS"

  @Demo @MUI @TC_DEMO_MUI_003
  Scenario: Cross-page search finds record on page 2
    When I find the row where "ID" is "TD_ID" in "muiDataGridDemo"
    Then the row where "ID" is "TD_ID" should have:
      | Column     | Value         |
      | Name       | TD_Name       |
      | Department | TD_Department |
      | Amount     | TD_Amount     |
    Then I log "MUI: Cross-page search found record on page 2" with status "PASS"

  @Demo @MUI @TC_DEMO_MUI_004
  Scenario: Multi-column criteria search
    When I find the row matching criteria in "muiDataGridDemo":
      | Column     | Value         |
      | Department | TD_Department |
      | Name       | TD_Name       |
    Then the found row should have "Amount" as "TD_Amount"
    Then the found row should have "Status" as "TD_Status"
    Then I log "MUI: Multi-column criteria search successful" with status "PASS"

  @Demo @MUI @TC_DEMO_MUI_005
  Scenario: Verify row count per page
    Then the row count in "muiDataGridDemo" should be 5
    Then the row where "Name" is "TD_Name" should exist in "muiDataGridDemo"
    Then I log "MUI: Row count and existence verified" with status "PASS"

  @Demo @MUI @AtlasProof @TC_DEMO_MUI_006
  Scenario: Atlas Cross-Framework Proof - Find record on page 3 and verify
    When I find the row where "ID" is "TD_ID" in "muiDataGridDemo"
    Then the row where "ID" is "TD_ID" should have:
      | Column     | Value         |
      | Name       | TD_Name       |
      | Status     | TD_Status     |
      | Department | TD_Department |
      | Amount     | TD_Amount     |
    Then I log "Atlas Proof: Same Gherkin, MUI technology, zero framework change" with status "PASS"
