@Demo @AGGrid
Feature: AG Grid Table - Enterprise Table Engine Validation
  Demonstrates Atlas v2.0 Table Engine capabilities on AG Grid.
  Uses local fixture: ag-grid-table-demo.html (3 pages, 15 records)

  Background:
    When I navigate to local fixture "ag-grid-table-demo.html"

  @Demo @AGGrid @TC_DEMO_AGGRID_001
  Scenario: Find order and verify cell values
    When I find the row where "Order ID" is "TD_OrderID" in "agGridDemo"
    Then the row where "Order ID" is "TD_OrderID" should have:
      | Column   | Value      |
      | Customer | TD_Customer |
      | Status   | TD_Status   |
      | Owner    | TD_Owner    |
    Then I log "AG Grid: Row found and cell values verified" with status "PASS"

  @Demo @AGGrid @TC_DEMO_AGGRID_002
  Scenario: Cross-page search finds order on page 2
    When I find the row where "Order ID" is "TD_OrderID" in "agGridDemo"
    Then the row where "Order ID" is "TD_OrderID" should have:
      | Column   | Value       |
      | Customer | TD_Customer |
      | Product  | TD_Product  |
      | Amount   | TD_Amount   |
    Then I log "AG Grid: Cross-page search found record on page 2" with status "PASS"

  @Demo @AGGrid @TC_DEMO_AGGRID_003
  Scenario: Perform Edit action on AG Grid row
    When I find the row where "Order ID" is "TD_OrderID" in "agGridDemo"
    And I perform "Edit" action on the row where "Order ID" is "TD_OrderID" in "agGridDemo"
    Then I log "AG Grid: Edit action executed via title attribute" with status "PASS"

  @Demo @AGGrid @TC_DEMO_AGGRID_004
  Scenario: Multi-column criteria search
    When I find the row matching criteria in "agGridDemo":
      | Column  | Value      |
      | Product | TD_Product |
      | Owner   | TD_Owner   |
    Then the found row should have "Customer" as "TD_Customer"
    Then the found row should have "Status" as "TD_Status"
    Then I log "AG Grid: Multi-column criteria search successful" with status "PASS"

  @Demo @AGGrid @TC_DEMO_AGGRID_005
  Scenario: Verify row existence check
    Then the row where "Customer" is "TD_Customer" should exist in "agGridDemo"
    Then I log "AG Grid: Row existence verified" with status "PASS"

  @Demo @AGGrid @AtlasProof @TC_DEMO_AGGRID_006
  Scenario: Atlas Cross-Framework Proof - Find record on page 3 and verify
    When I find the row where "Order ID" is "TD_OrderID" in "agGridDemo"
    Then the row where "Order ID" is "TD_OrderID" should have:
      | Column   | Value       |
      | Customer | TD_Customer |
      | Product  | TD_Product  |
      | Status   | TD_Status   |
      | Amount   | TD_Amount   |
    Then I log "Atlas Proof: Same Gherkin, AG Grid technology, zero framework change" with status "PASS"
