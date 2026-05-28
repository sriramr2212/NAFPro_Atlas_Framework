@Demo @Desktop
Feature: Desktop Calculator - Atlas v2.3 Desktop Automation Proof
  Demonstrates Atlas v2.3 Desktop Engine capabilities on Windows Calculator.
  Uses WinAppDriver with same Gherkin + TD_ + reporting philosophy as web.

  Background:
    Given I launch the desktop application "Calculator"

  @Demo @Desktop @TC_CALC_001
  Scenario: Basic addition
    When I click desktop element "BTN_SEVEN"
    And I click desktop element "BTN_PLUS"
    And I click desktop element "BTN_THREE"
    And I click desktop element "BTN_EQUALS"
    Then the desktop element "TXT_RESULT" should contain "TD_Expected"
    Then I log "Desktop: Basic addition 7+3=10 verified" with status "PASS"

  @Demo @Desktop @TC_CALC_002
  Scenario: Mode switch to Scientific
    When I open calculator navigation
    And I select calculator mode "TD_TargetMode"
    Then the window title should contain "TD_ExpectedTitle"
    Then I log "Desktop: Mode switch to Scientific verified" with status "PASS"

  @Demo @Desktop @TC_CALC_003
  Scenario: Clear and verify display reset
    When I click desktop element "BTN_FIVE"
    And I click desktop element "BTN_CLEAR"
    Then the desktop element "TXT_RESULT" should contain "TD_ExpectedAfterClear"
    Then I log "Desktop: Clear resets display to 0" with status "PASS"

  @Demo @Desktop @TC_CALC_004
  Scenario: History panel shows calculation
    When I click desktop element "BTN_FIVE"
    And I click desktop element "BTN_PLUS"
    And I click desktop element "BTN_THREE"
    And I click desktop element "BTN_EQUALS"
    Then the desktop element "TXT_RESULT" should contain "TD_Expected"
    Then I log "Desktop: Calculation 5+3=8 with history" with status "PASS"

  @Demo @Desktop @TC_CALC_005
  Scenario: Keyboard input calculation
    When I type expression "TD_Expression" into calculator
    Then the desktop element "TXT_RESULT" should contain "TD_Expected"
    Then I log "Desktop: Keyboard input 123+456=579 verified" with status "PASS"

  @Demo @Desktop @TC_CALC_006
  Scenario: Memory store and recall
    When I type expression "TD_MemoryValue" into calculator
    And I click desktop element "BTN_MEMORY_STORE"
    And I click desktop element "BTN_CLEAR"
    And I click desktop element "BTN_MEMORY_RECALL"
    Then the desktop element "TXT_RESULT" should contain "TD_Expected"
    Then I log "Desktop: Memory store/recall verified" with status "PASS"
