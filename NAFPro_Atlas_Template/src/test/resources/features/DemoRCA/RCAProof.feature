@RCA @Demo
Feature: NAFPro Atlas AI v1 — Failure RCA Runtime Proof
  Intentional failures to validate RCA classification, ownership, and HTML report rendering.

  @TC_RCA_001
  Scenario: RCA_001 — Locator Issue (Invalid element)
    Given I navigate to local fixture "generic-html-table-demo.html"
    When I attempt to click element with invalid locator "//div[3]/div[2]/span[99]/button[@class='nonexistent-class-xyz']"
    Then the step should fail with LOCATOR_ISSUE classification

  @TC_RCA_002
  Scenario: RCA_002 — Timing Issue (Stale element reference)
    Given I navigate to local fixture "generic-html-table-demo.html"
    When I trigger a stale element reference by navigating away mid-interaction
    Then the step should fail with TIMING_SYNC_ISSUE classification

  @TC_RCA_003
  Scenario: RCA_003 — Assertion Failure (Expected vs Actual mismatch)
    Given I navigate to local fixture "generic-html-table-demo.html"
    When I find the row where "Customer Name" is "Alice Johnson" in "demo-table"
    Then the found row should have "Status" as "Expired"

  @TC_RCA_004
  Scenario: RCA_004 — Environment Failure (Unreachable URL)
    When I navigate to unreachable URL "http://192.0.2.1:9999/nonexistent"
    Then the page should load successfully
