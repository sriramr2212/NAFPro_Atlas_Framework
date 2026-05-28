@Demo @DefectWriter
Feature: AI Defect Writer - Runtime Proof
  Validates end-to-end defect draft generation from intentional failures.
  Each scenario triggers a specific failure type and verifies the AI Defect Writer output.

  Background:
    When I navigate to local fixture "generic-html-table-demo.html"

  @DefectWriter @TC_DW_LOCATOR_001
  Scenario: DW_001 - Locator failure generates FUNCTIONAL_DEFECT draft (stable locator, missing element)
    When I trigger a locator failure on element "nonExistentButton_XYZ_12345"
    Then the defect draft should have classification "FUNCTIONAL_DEFECT"
    And the defect draft should have owner "Application"
    And the defect draft should have severity "Medium" or "High"
    And the defect draft title should contain "application defect"
    And the defect draft should be renderable in all formats
    Then I log "DW_001: Stable locator + missing element → FUNCTIONAL_DEFECT draft generated ✓" with status "PASS"

  @DefectWriter @TC_DW_ASSERTION_002
  Scenario: DW_002 - Assertion failure generates FUNCTIONAL_DEFECT draft
    When I trigger an assertion failure expecting "Active" but finding "Inactive"
    Then the defect draft should have classification "FUNCTIONAL_DEFECT"
    And the defect draft should have owner "Application"
    And the defect draft should have severity "Medium" or "High"
    And the defect draft title should contain "assertion failure"
    And the defect draft should be renderable in all formats
    Then I log "DW_002: Assertion failure → FUNCTIONAL_DEFECT draft generated ✓" with status "PASS"

  @DefectWriter @TC_DW_TIMING_003
  Scenario: DW_003 - Timing issue generates AUTOMATION_DEFECT draft
    When I trigger a stale element failure on the table
    Then the defect draft should have classification "AUTOMATION_DEFECT"
    And the defect draft should have owner "Automation"
    And the defect draft should have severity "Low" or "Medium"
    And the defect draft title should contain "timing"
    And the defect draft should be renderable in all formats
    Then I log "DW_003: Timing issue → AUTOMATION_DEFECT draft generated ✓" with status "PASS"

  @DefectWriter @TC_DW_ENVIRONMENT_004
  Scenario: DW_004 - Environment/Driver issue generates AUTOMATION_DEFECT draft
    When I trigger an environment failure by navigating to "http://localhost:99999/nonexistent"
    Then the defect draft should have classification "AUTOMATION_DEFECT"
    And the defect draft should have owner "Infrastructure"
    And the defect draft should have severity "Critical" or "High"
    And the defect draft title should contain "driver" or "browser"
    And the defect draft should be renderable in all formats
    Then I log "DW_004: WebDriverException → AUTOMATION_DEFECT [Critical] draft generated ✓" with status "PASS"
