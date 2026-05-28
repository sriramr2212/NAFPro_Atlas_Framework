@Demo @AtlasProof
Feature: NAFPro Atlas - DemoTechValidation Suite Entry
  This feature serves as the module entry point for the DemoTechValidation suite.
  Individual technology demos are organized under Web/ and Desktop/ subfolders.

  @Demo @AtlasProof @TC_DEMO_ENTRY_001
  Scenario: Atlas Demo Suite initialized
    Then I log "NAFPro Atlas DemoTechValidation Suite - 54 scenarios across 7 web frameworks + Desktop" with status "PASS"
