# NAFPro Atlas — Architecture Rules

Hard rules to prevent framework rot. Enforced by code review.

---

## Allowed in Project Test Suites

✅ Project locators in `src/main/java/novac/constants/`
✅ Project test data in `src/main/resources/testdata/json/`
✅ Project-specific step definitions in `src/test/java/stepdefinitions/`
✅ Project page classes in `src/main/java/novac/pages/` (for complex validations only)
✅ Environment configs in `src/main/resources/configs/environments/`

---

## Forbidden in Project Test Suites

❌ Direct Selenium/WebDriver code in step definitions
❌ Direct framework-specific calls (AntDUtils, MUIHandler, PrimeNGHandler, etc.)
❌ `Thread.sleep()` — use `WaitHandler`
❌ Hardcoded waits or timeouts
❌ Business logic in CommonSteps
❌ Modifying GenericActionHandler
❌ Modifying framework hooks (TestHooks, StepHooks)
❌ Static mutable state — use `TestContext`
❌ Importing `org.openqa.selenium.*` in step definitions (use GenericActionHandler)

---

## When NOT to Create New Step Definitions

Do NOT create new steps when:

- ❌ Only the locator changes → add to Constants file instead
- ❌ Only the page changes → use existing parameterized step
- ❌ Only the test data changes → use TD_ prefix
- ❌ Same action pattern already exists → reuse existing step

### Bad:
```gherkin
When I click Login button
When I click Submit button
When I click Save button
```

### Good:
```gherkin
When I click on "LOGIN_BUTTON" on "Login" page
When I click on "SUBMIT_BUTTON" on "Dashboard" page
When I click on "SAVE_BUTTON" on "Settings" page
```

**Rule:** If `GenericActionHandler` already supports the action type, do NOT create a new step.

---

## Reusability Boundaries

| Code Type | Belongs In |
|-----------|-----------|
| Selenium utilities, wait strategies | NAFPro framework |
| UI framework handlers (AntD, MUI, etc.) | NAFPro framework |
| Generic step patterns (click, enter, select) | Template (CommonSteps) |
| Runner infrastructure | Template (runners/) |
| Application locators/XPaths | Project (constants/) |
| Application test data | Project (testdata/json/) |
| Business workflow steps | Project (stepdefinitions/) |
| Page validations | Project (pages/) |

---

## Adding a New Module

1. Create `src/main/java/novac/constants/{Module}Constants.java`
2. Create `src/main/resources/testdata/json/{Module}.json`
3. Create `src/test/resources/features/{Module}/{Module}.feature`
4. Run — no step definition changes needed for standard interactions

---

## Convention-Based Page Resolution

Page names resolve automatically via convention:

- `"Login"` → `LoginPage` → looks up `LoginConstants`
- `"Dashboard"` → `DashboardPage` → looks up `DashboardConstants`
- `"UserManagement"` → `UserManagementPage` → looks up `UserManagementConstants`

**No code changes needed for new modules.**
