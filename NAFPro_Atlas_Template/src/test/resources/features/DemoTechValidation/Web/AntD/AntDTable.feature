@Demo @AntD
Feature: Ant Design Table - Enterprise Table Engine Validation
  Demonstrates Atlas v2.0 Table Engine capabilities on AntD tables.
  Uses local fixture: antd-table-demo.html (3 pages, 15 records, meatball menus)

  Background:
    When I navigate to local fixture "antd-table-demo.html"

  @Demo @AntD @TC_DEMO_ANTD_001
  Scenario: Find request and verify row details
    When I find the row where "Request ID" is "TD_RequestID" in "antdTableDemo"
    Then the row where "Request ID" is "TD_RequestID" should have:
      | Column     | Value         |
      | Requester  | TD_Requester  |
      | Department | TD_Department |
      | Status     | TD_Status     |
    Then I log "AntD: Row found and details verified" with status "PASS"

  @Demo @AntD @TC_DEMO_ANTD_002
  Scenario: Perform Approve action via meatball menu
    When I find the row where "Request ID" is "TD_RequestID" in "antdTableDemo"
    And I perform "Approve" action on the row where "Request ID" is "TD_RequestID" in "antdTableDemo"
    Then I log "AntD: Approve action executed via meatball menu" with status "PASS"

  @Demo @AntD @TC_DEMO_ANTD_003
  Scenario: Cross-page search finds request on page 3
    When I find the row where "Request ID" is "TD_RequestID" in "antdTableDemo"
    Then the row where "Request ID" is "TD_RequestID" should have:
      | Column    | Value        |
      | Requester | TD_Requester |
      | Priority  | TD_Priority  |
      | Type      | TD_Type      |
    Then I log "AntD: Cross-page search found record on page 3" with status "PASS"

  @Demo @AntD @TC_DEMO_ANTD_004
  Scenario: Multi-column criteria search
    When I find the row matching criteria in "antdTableDemo":
      | Column     | Value         |
      | Department | TD_Department |
      | Type       | TD_Type       |
    Then the found row should have "Requester" as "TD_Requester"
    Then the found row should have "Priority" as "TD_Priority"
    Then I log "AntD: Multi-column criteria search successful" with status "PASS"

  @Demo @AntD @TC_DEMO_ANTD_005
  Scenario: Verify row count per page
    Then the row count in "antdTableDemo" should be 5
    Then I log "AntD: Row count verified (5 per page)" with status "PASS"

  @Demo @AntD @AtlasProof @TC_DEMO_ANTD_006
  Scenario: Atlas Cross-Framework Proof - Find record on page 3 and verify
    When I find the row where "Request ID" is "TD_RequestID" in "antdTableDemo"
    Then the row where "Request ID" is "TD_RequestID" should have:
      | Column     | Value         |
      | Requester  | TD_Requester  |
      | Department | TD_Department |
      | Type       | TD_Type       |
      | Priority   | TD_Priority   |
    Then I log "Atlas Proof: Same Gherkin, AntD technology, zero framework change" with status "PASS"
