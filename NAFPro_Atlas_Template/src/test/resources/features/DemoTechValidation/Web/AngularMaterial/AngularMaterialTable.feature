@Demo @AngularMaterial
Feature: Angular Material Table - Enterprise Table Engine Validation
  Demonstrates Atlas v2.1 Table Engine capabilities on Angular Material mat-table.
  Uses local fixture: angular-material-table-demo.html (3 pages, 15 records)

  Background:
    When I navigate to local fixture "angular-material-table-demo.html"

  @Demo @AngularMaterial @TC_DEMO_ANGMAT_001
  Scenario: Find issue and verify row details
    When I find the row where "ID" is "TD_ID" in "angularMaterialTableDemo"
    Then the row where "ID" is "TD_ID" should have:
      | Column     | Value         |
      | Name       | TD_Name       |
      | Status     | TD_Status     |
      | Department | TD_Department |
      | Amount     | TD_Amount     |
    Then I log "Angular Material: Row found and details verified" with status "PASS"

  @Demo @AngularMaterial @TC_DEMO_ANGMAT_002
  Scenario: Perform Edit action on table row
    When I find the row where "ID" is "TD_ID" in "angularMaterialTableDemo"
    And I perform "Edit" action on the row where "ID" is "TD_ID" in "angularMaterialTableDemo"
    Then I log "Angular Material: Edit action executed successfully" with status "PASS"

  @Demo @AngularMaterial @TC_DEMO_ANGMAT_003
  Scenario: Cross-page search finds record on page 2
    When I find the row where "ID" is "TD_ID" in "angularMaterialTableDemo"
    Then the row where "ID" is "TD_ID" should have:
      | Column     | Value         |
      | Name       | TD_Name       |
      | Department | TD_Department |
      | Amount     | TD_Amount     |
    Then I log "Angular Material: Cross-page search found record on page 2" with status "PASS"

  @Demo @AngularMaterial @TC_DEMO_ANGMAT_004
  Scenario: Multi-column criteria search
    When I find the row matching criteria in "angularMaterialTableDemo":
      | Column     | Value         |
      | Department | TD_Department |
      | Name       | TD_Name       |
    Then the found row should have "Amount" as "TD_Amount"
    Then the found row should have "Status" as "TD_Status"
    Then I log "Angular Material: Multi-column criteria search successful" with status "PASS"

  @Demo @AngularMaterial @TC_DEMO_ANGMAT_005
  Scenario: Verify row count per page
    Then the row count in "angularMaterialTableDemo" should be 5
    Then the row where "Name" is "TD_Name" should exist in "angularMaterialTableDemo"
    Then I log "Angular Material: Row count and existence verified" with status "PASS"

  @Demo @AngularMaterial @AtlasProof @TC_DEMO_ANGMAT_006
  Scenario: Atlas Cross-Framework Proof - Find record on page 3 and verify
    When I find the row where "ID" is "TD_ID" in "angularMaterialTableDemo"
    Then the row where "ID" is "TD_ID" should have:
      | Column     | Value         |
      | Name       | TD_Name       |
      | Status     | TD_Status     |
      | Department | TD_Department |
      | Amount     | TD_Amount     |
    Then I log "Atlas Proof: Same Gherkin, Angular Material technology, zero framework change" with status "PASS"
