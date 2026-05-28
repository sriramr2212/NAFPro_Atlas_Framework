<p align="center">
  <img src="../NAFPro_Atlas/src/main/resources/assets/nafpro_logo.png" alt="NAFPro Atlas" width="320">
</p>

<h1 align="center">NAFPro Atlas Template</h1>

<p align="center">
  Showcase and starter template for the <strong>NAFPro Atlas</strong> automation framework.
</p>

**54 self-contained demo scenarios** across 7 web UI frameworks + Windows Desktop.
Clone, build, run — zero external dependencies for web demos.

---

## Prerequisites

- Java 17+
- Maven 3.8+
- NAFPro Atlas framework installed locally (`mvn clean install` from NAFPro_Atlas repo)
- *(Desktop only)* WinAppDriver — see `NAFPro_Atlas/docs/desktop-prerequisites.md`

---

## Quick Start

```bash
# 1. Install NAFPro Atlas framework
cd ../NAFPro_Atlas
mvn clean install

# 2. Run all demos (web scenarios run immediately; desktop requires WinAppDriver)
cd ../NAFPro_Atlas_Template
mvn exec:java
```

Default suite is `Demo_All` (54 scenarios). For web-only (no WinAppDriver needed):

```json
"selectedSuite": "Demo_Web"
```

---

## Demo Suites

| Suite | Tag | Scenarios | Notes |
|---|---|---|---|
| `Demo_All` | `@Demo` | 54 | All web + desktop |
| `Demo_Web` | `@Demo and not @Desktop` | 48 | Web only, no prerequisites |
| `Demo_Desktop` | `@Desktop` | 6 | Requires WinAppDriver |
| `Demo_AGGrid` | `@AGGrid` | 6 | AG Grid |
| `Demo_AntD` | `@AntD` | 6 | Ant Design |
| `Demo_PrimeNG` | `@PrimeNG` | 6 | PrimeNG |
| `Demo_AngularMaterial` | `@AngularMaterial` | 6 | Angular Material |
| `Demo_MUI` | `@MUI` | 6 | MUI DataGrid |
| `Demo_GenericHTML` | `@GenericHTML` | 6 | Generic HTML Table |
| `Demo_SortFilter` | `@SortFilter` | 6 | Sort & Filter |
| `Demo_Selection_State` | `@SelectionState` | 6 | Selection & State |
| `Demo_Expander_Editor` | `@ExpanderEditor` | 6 | Expand & Edit |

Switch suite: edit `selectedSuite` in `RunManager.json`.

---

## Project Structure

```
├── src/main/java/novac/
│   ├── constants/              # Element locators per demo module
│   ├── helpers/                # Project-specific helpers
│   └── utils/                  # Shared utilities (PageResolver, ScrollHelper)
├── src/main/resources/
│   ├── testdata/json/          # Test data per module (TD_ resolution)
│   └── configs/environments/   # Environment properties
├── src/test/java/
│   ├── runners/                # TestRunner, SuiteResolver, ModuleResolver
│   └── stepdefinitions/        # DemoTableSteps, DesktopCalculatorSteps, CommonSteps
├── src/test/resources/
│   ├── features/DemoTechValidation/
│   │   ├── Web/                # 6 framework folders (48 scenarios)
│   │   └── Desktop/            # Calculator demo (6 scenarios)
│   └── fixtures/               # Local HTML fixtures (deterministic)
├── RunManager.json             # Execution configuration
└── ARCHITECTURE_RULES.md       # Guardrails
```

---

## What It Proves

### NAFPro Atlas Engine (Web)
- Row lookup by business data across 6 UI frameworks
- Cross-page search with automatic pagination
- 6-level action resolution (button → link → title → aria-label → data-action → menu)
- Sort, filter, select, expand, edit operations
- Framework auto-detection — same step definitions for all frameworks
- Stale DOM recovery with retry

### Desktop Automation — Windows Desktop (Preview)
- Windows Calculator automation via WinAppDriver
- Same Gherkin + TD_ + reporting philosophy as web
- Button clicks, keyboard input, window assertions, memory operations

---

## Using as a Starter Template

### 1. Copy & Rename

```bash
cp -r NAFPro_Atlas_Template MyProject_Automation
```

### 2. Update pom.xml

```xml
<groupId>com.yourcompany</groupId>
<artifactId>myproject-automation</artifactId>
```

### 3. Configure RunManager.json

```json
{
  "projectName": "MyProject",
  "environment": "qa",
  "selectedSuite": "Smoke"
}
```

### 4. Add Your Modules

1. Create `src/main/java/novac/constants/{Module}Constants.java`
2. Create `src/main/resources/testdata/json/{Module}.json`
3. Create `src/test/resources/features/{Module}/{Module}.feature`
4. Run — convention-based resolution handles everything

---

## Available Generic Steps

### Interaction
```gherkin
When I click on "{ELEMENT}" on "{PAGE}" page
When I enter "{value}" in "{FIELD}" field on "{PAGE}" page
When I select "{value}" from "{DROPDOWN}" on "{PAGE}" page
When I set "{TOGGLE}" to "{state}" on "{PAGE}" page
When I set "{FIELD}" date to "{value}" on "{PAGE}" page
```

### Validation
```gherkin
Then I verify element "{ELEMENT}" is {state} on "{PAGE}" page
Then I verify "{FIELD}" has value "{expected}" and is "{state}" on "{PAGE}" page
Then I verify toast message "{expected}"
```

### Table (NAFPro Atlas Engine)
```gherkin
Then I verify record with "{column}" = "{value}" exists in "{TABLE}" table
When I perform "{action}" action on record with "{column}" = "{value}" in "{TABLE}" table
```

### Flow Control
```gherkin
When I wait for "{seconds}" seconds
When I log "{message}" with status "{status}"
```

See [ARCHITECTURE_RULES.md](ARCHITECTURE_RULES.md) for guardrails on when NOT to create new steps.
