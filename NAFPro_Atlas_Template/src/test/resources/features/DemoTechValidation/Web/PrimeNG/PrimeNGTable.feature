@Demo @PrimeNG
Feature: PrimeNG Table - Enterprise Table Engine Validation
  Demonstrates Atlas v2.1 Table Engine capabilities on PrimeNG p-datatable.
  Uses local fixture: primeng-table-demo.html (3 pages, 15 records)

  Background:
    When I navigate to local fixture "primeng-table-demo.html"

  @Demo @PrimeNG @TC_DEMO_PRIMENG_001
  Scenario: Find employee and verify row details
    When I find the row where "ID" is "TD_ID" in "primeNGTableDemo"
    Then the row where "ID" is "TD_ID" should have:
      | Column     | Value         |
      | Name       | TD_Name       |
      | Status     | TD_Status     |
      | Department | TD_Department |
      | Amount     | TD_Amount     |
    Then I log "PrimeNG: Row found and details verified" with status "PASS"

  @Demo @PrimeNG @TC_DEMO_PRIMENG_002
  Scenario: Perform Edit action on table row
    When I find the row where "ID" is "TD_ID" in "primeNGTableDemo"
    And I perform "Edit" action on the row where "ID" is "TD_ID" in "primeNGTableDemo"
    Then I log "PrimeNG: Edit action executed successfully" with status "PASS"

  @Demo @PrimeNG @TC_DEMO_PRIMENG_003
  Scenario: Cross-page search finds record on page 2
    When I find the row where "ID" is "TD_ID" in "primeNGTableDemo"
    Then the row where "ID" is "TD_ID" should have:
      | Column     | Value         |
      | Name       | TD_Name       |
      | Department | TD_Department |
      | Amount     | TD_Amount     |
    Then I log "PrimeNG: Cross-page search found record on page 2" with status "PASS"

  @Demo @PrimeNG @TC_DEMO_PRIMENG_004
  Scenario: Multi-column criteria search
    When I find the row matching criteria in "primeNGTableDemo":
      | Column     | Value         |
      | Department | TD_Department |
      | Name       | TD_Name       |
    Then the found row should have "Amount" as "TD_Amount"
    Then the found row should have "Status" as "TD_Status"
    Then I log "PrimeNG: Multi-column criteria search successful" with status "PASS"

  @Demo @PrimeNG @TC_DEMO_PRIMENG_005
  Scenario: Verify row count per page
    Then the row count in "primeNGTableDemo" should be 5
    Then the row where "Name" is "TD_Name" should exist in "primeNGTableDemo"
    Then I log "PrimeNG: Row count and existence verified" with status "PASS"

  @Demo @PrimeNG @AtlasProof @TC_DEMO_PRIMENG_006
  Scenario: Atlas Cross-Framework Proof - Find record on page 3 and verify
    When I find the row where "ID" is "TD_ID" in "primeNGTableDemo"
    Then the row where "ID" is "TD_ID" should have:
      | Column     | Value         |
      | Name       | TD_Name       |
      | Status     | TD_Status     |
      | Department | TD_Department |
      | Amount     | TD_Amount     |
    Then I log "Atlas Proof: Same Gherkin, PrimeNG technology, zero framework change" with status "PASS"
