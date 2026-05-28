@Demo @GenericHTML @ExpanderEditor
Feature: Generic HTML Table - Expander & Editor Validation
  Atlas v2.2 Phase 2: Row expansion and inline cell editing on Generic HTML tables.
  Uses local fixture: generic-html-expander-editor-demo.html

  Background:
    When I navigate to local fixture "generic-html-expander-editor-demo.html"

  @Demo @GenericHTML @ExpanderEditor @TC_DEMO_GENERIC_EE_001
  Scenario: Expand row and verify detail content
    When I expand the row where "Task ID" is "TD_TaskId" in "taskTable"
    Then the row where "Task ID" is "TD_TaskId" should be expanded in "taskTable"
    And the expanded content of row where "Task ID" is "TD_TaskId" should contain "TD_DetailText" in "taskTable"
    Then I log "Generic HTML: Row expanded and detail content verified" with status "PASS"

  @Demo @GenericHTML @ExpanderEditor @TC_DEMO_GENERIC_EE_002
  Scenario: Expand then collapse row (idempotent behavior)
    When I expand the row where "Task ID" is "TD_TaskId" in "taskTable"
    Then the row where "Task ID" is "TD_TaskId" should be expanded in "taskTable"
    When I expand the row where "Task ID" is "TD_TaskId" in "taskTable"
    Then the row where "Task ID" is "TD_TaskId" should be expanded in "taskTable"
    When I collapse the row where "Task ID" is "TD_TaskId" in "taskTable"
    Then the row where "Task ID" is "TD_TaskId" should not be expanded in "taskTable"
    When I collapse the row where "Task ID" is "TD_TaskId" in "taskTable"
    Then the row where "Task ID" is "TD_TaskId" should not be expanded in "taskTable"
    Then I log "Generic HTML: Expand/collapse idempotency verified" with status "PASS"

  @Demo @GenericHTML @ExpanderEditor @TC_DEMO_GENERIC_EE_003
  Scenario: getExpandedContent on collapsed row throws expected error
    Then getting expanded content of collapsed row where "Task ID" is "TD_TaskId" should fail in "taskTable"
    Then I log "Generic HTML: getExpandedContent correctly throws on collapsed row" with status "PASS"

  @Demo @GenericHTML @ExpanderEditor @TC_DEMO_GENERIC_EE_004
  Scenario: Edit text cell and verify updated value
    When I edit the "Task Name" cell to "TD_NewName" in row where "Task ID" is "TD_TaskId" in "taskTable"
    When I find the row where "Task ID" is "TD_TaskId" in "taskTable"
    Then the found row should have "Task Name" as "TD_NewName"
    Then I log "Generic HTML: Text cell edit + stale recovery verified" with status "PASS"

  @Demo @GenericHTML @ExpanderEditor @TC_DEMO_GENERIC_EE_005
  Scenario: Edit dropdown cell and verify updated value
    When I edit the "Priority" cell as "DROPDOWN" to "TD_NewPriority" in row where "Task ID" is "TD_TaskId" in "taskTable"
    When I find the row where "Task ID" is "TD_TaskId" in "taskTable"
    Then the found row should have "Priority" as "TD_NewPriority"
    Then I log "Generic HTML: Dropdown cell edit verified" with status "PASS"

  @Demo @GenericHTML @ExpanderEditor @TC_DEMO_GENERIC_EE_006
  Scenario: Edit checkbox cell and verify editability
    Then the "Completed" cell in row where "Task ID" is "TD_TaskId" should be editable in "taskTable"
    And the "Task ID" cell in row where "Task ID" is "TD_TaskId" should not be editable in "taskTable"
    When I edit the "Completed" cell as "CHECKBOX" to "true" in row where "Task ID" is "TD_TaskId" in "taskTable"
    Then I log "Generic HTML: Checkbox edit + isCellEditable verified" with status "PASS"
