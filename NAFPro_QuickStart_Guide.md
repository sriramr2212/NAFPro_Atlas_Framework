# NAFPro Atlas — Quick Start Guide

---

## 1. Supported Technologies (What Can Be Tested)

**Web UI Frameworks:**
- AG Grid
- Ant Design (AntD)
- PrimeNG
- Angular Material
- MUI (Material UI) DataGrid
- Generic HTML Tables
- Any standard web app (Selenium-based)

**Desktop:**
- Windows Desktop apps (Soft Frozen) — runtime enablement via Appium Windows Driver

---

## 2. Key Features

- **Gherkin Test Scripts** — Write tests in plain English steps (Given/When/Then), no coding needed for test cases
- **Dual Test Data Support** — Use JSON or Excel as your test data source (configurable)
- **Smart Locator Recovery** — Retries alternate locators when elements change
- **AI Failure RCA** — Auto root-cause analysis on failure (no LLM needed)
- **AI Defect Writer** — Auto-generates defect drafts from failures
- **AI Spark** — BRD → Gherkin test generation
- **Parallel Execution** — Run tests concurrently for speed
- **QMetry Integration** — Auto-push results to test management
- **Custom HTML + Excel Reporting** — Self-contained reports with screenshot per step

---

## 3. Prerequisites

- Java 17+
- Maven 3.8+
- Chrome / Edge / Firefox installed
- NAFPro Atlas framework installed locally (`mvn clean install` on NAFPro_Atlas repo first)
- Appium 2.x + Windows Driver *(desktop only)*

---

## 4. Steps to Create a New Test Project

1. Copy `NAFPro_Atlas_Template` folder → rename it (e.g., `MyProject_Automation`)
2. Open in IDE (VS Code / IntelliJ)
3. Update `pom.xml`:
   - `groupId` — your team/org identifier (e.g., `com.yourcompany`)
   - `artifactId` — your project name (e.g., `myproject-automation`)
4. Update `RunManager.json` → set `projectName`, `environment`, `selectedSuite`
5. Set environment URL in `src/main/resources/configs/environments/qa.properties`
6. Add your module:
   - Create `src/main/java/novac/constants/{Module}Constants.java` (locators)
   - Create `src/main/resources/testdata/json/{Module}.json` (test data)
   - Create `src/test/resources/features/{Module}/{Module}.feature` (scenarios)
7. Run — no other code changes needed

---

## 5. Configuration

**RunManager.json (key fields):**
- `projectName` — your project name
- `environment` — qa / uat / prod
- `browser` — chrome / firefox / edge
- `selectedSuite` — which suite to run (maps to tags)
- `testSuites` — define your suites with Cucumber tags
- `testDataSource` — `"json"` or `"excel"`
- `rcaEnabled` — true/false
- `defectWriterEnabled` — true/false
- `screenshotsOnFailure` — true/false

**Environment Properties** (`configs/environments/{env}.properties`):
- `base.url` — your app URL

---

## 6. How to Run

**From Command Line:**
```bash
mvn exec:java
```
or
```bash
mvn test
```

**From IDE (IntelliJ / VS Code):**
- Run `runners.TestRunner` as a Java application (main method)

**To run a specific suite:**
- Change `selectedSuite` in `RunManager.json` → run again

**Reports location:**
- `Reports/Run_{timestamp}/` folder after execution

---

## 7. AI Spark (BRD → Gherkin Generation)

Used for: **BRD → Scenario → Automation-ready Gherkin generation**

```bash
mvn exec:java -Dexec.mainClass=novac.ai.spark.SparkRunner
```

Generates feature files, test data, and constants from business requirements using existing reusable steps.
