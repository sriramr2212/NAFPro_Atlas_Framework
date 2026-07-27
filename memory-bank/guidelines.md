# NAFPro AI Nexus — Development Guidelines

## Java (NAFPro Atlas & STATIM_Automation)

### Code Quality Standards

- Java 17 features are used throughout: `switch` expressions, `var`, records where appropriate.
- All classes use `package novac.*` namespace; no default package usage.
- Log4j2 is the logging standard — always use `LogManager.getLogger(ClassName.class)` and `private static final Logger logger`.
- Lombok is available (`@Data`, `@Builder`, etc.) but the codebase often uses explicit builders for complex models (see `FlexiField`).
- No test cases in the framework (`NAFPro_Atlas`) — only reusable, generic code.

### Naming Conventions

- Classes: `PascalCase` — e.g., `ComponentHandlerFactory`, `UIFrameworkDetector`, `FlexiField`
- Methods: `camelCase` — e.g., `getDropdownHandler`, `resolveFramework`, `storeFlexiFields`
- Constants: `UPPER_SNAKE_CASE` — e.g., `CONFIG_FILE`, `currentModule`
- Test case tags: `@TC_MODULE_NUMBER` format — e.g., `@TC_LOGIN_001`, `@TC_PRODUCT_CONFIG_002`
- Module names: `PascalCase` matching folder names — e.g., `Login`, `ProductConfig`, `NewBusiness`

### Design Patterns

#### Factory + Strategy (ComponentHandlerFactory)
The primary pattern for UI framework abstraction. `UIFrameworkDetector` detects the framework from a `WebElement`, then `ComponentHandlerFactory` returns the correct handler:

```java
// Usage pattern
DropdownHandler handler = ComponentHandlerFactory.getDropdownHandler(element, driver);
TableEngine engine = ComponentHandlerFactory.getTableEngine(tableRoot, driver);
DatePickerHandler picker = ComponentHandlerFactory.getDatePickerHandler(element, driver);
```

Each factory method uses a `switch` expression over `UIFramework` enum:
```java
return switch (framework) {
    case MUI -> new MUIDropdownHandler();
    case ANTD -> new AntDDropdownAdapter(driver);
    case ANGULAR_MATERIAL -> new AngularMaterialDropdownHandler();
    case PRIMENG -> new PrimeNGDropdownHandler();
    default -> new GenericDropdownHandler();
};
```

#### Thread-Safe Context (TestContext)
All shared test state is stored in `ThreadLocal` fields for parallel execution safety:

```java
private static final ThreadLocal<String> currentModule = new ThreadLocal<>();
private static final ThreadLocal<Map<String, List<FlexiField>>> flexiFieldStore =
        ThreadLocal.withInitial(LinkedHashMap::new);
```

Always call `clear*()` methods in `@After` hooks to prevent thread leaks.

#### Builder Pattern (FlexiField)
Complex models use nested static `Builder` classes with fluent API. Inner config classes also have their own builders:

```java
FlexiField field = FlexiField.builder()
    .fieldKey("premiumAmount")
    .fieldLabel("Premium Amount")
    .fieldType(FlexiFieldType.TEXT)
    .blockName("PolicyDetails")
    .coreConfig(FlexiField.CoreConfig.builder()
        .fieldName("premium_amount")
        .minimumValue("0")
        .build())
    .build();
```

Builder `build()` validates required fields and throws `IllegalStateException` on invalid state.

#### Singleton Config (RunManager)
Configuration is loaded once from `RunManager.json` using lazy initialization with a guard flag:

```java
public static void initialize() {
    if (initialized) return;
    initializeForce();
}
```

System properties override JSON config (e.g., `System.getProperty("browser")` takes priority over `RunManager.json`).

#### Framework Detection (UIFrameworkDetector)
Detection uses CSS class inspection with a `ThreadLocal<WeakHashMap>` cache to avoid repeated DOM queries:

```java
// Detection priority order:
// 1. ag-root/ag-grid class → AG_GRID
// 2. p- tag prefix or p-component class → PRIMENG
// 3. mat- tag prefix or mat-mdc- class → ANGULAR_MATERIAL
// 4. Mui class prefix → MUI
// 5. ant- class prefix → ANTD
// 6. aria/role attributes → detectFromAncestor()
// 7. data-testid → MUI
// 8. matAutocomplete/matInput attributes → ANGULAR_MATERIAL
// fallback → UNKNOWN
```

### Error Handling

- Throw `RuntimeException` with descriptive messages including context (block name, field key, available keys).
- Use fail-safe `try/catch` in detection code — return `UNKNOWN` rather than propagating exceptions.
- Validate configuration eagerly at startup (`validateConfiguration()` in `RunManager`).
- Use `FatalFrameworkException` for unrecoverable framework errors.

### Thread Safety

- All `TestContext` state uses `ThreadLocal` — never use static mutable fields without `ThreadLocal`.
- `UIFrameworkDetector` cache uses `ThreadLocal<WeakHashMap<WebElement, UIFramework>>` — weak references prevent memory leaks.
- Always pair `set*()` with `clear*()` in Cucumber `@After` hooks.

### Test Data

- JSON is the default test data source (`"testDataSource": "json"` in `RunManager.json`).
- One JSON file per module in `TestDatastore/json/` (e.g., `Login.json`, `ProductConfig.json`).
- Access via `TestDataManager` — never read JSON files directly in step definitions.
- `FlexiField` is the universal field descriptor — use `FlexiFieldInteractor` for interactions.

### Cucumber BDD Conventions

- Feature files live in `src/test/resources/features/<ModuleName>/`.
- Step definitions are in `stepdefinitions/` — one class per module plus `CommonSteps` and `FlexiFieldStepDef`.
- Test case tags follow `@TC_MODULE_NUMBER` format (e.g., `@TC_LOGIN_001`).
- `RunManager.json` controls which tags/modules run — do not hardcode tags in runners.
- `TestRunner` is the entry point; `ModuleResolver` and `SuiteResolver` handle dynamic discovery.

### Logging Standards

```java
// Info: significant state changes
logger.info("[TableEngine] Framework detected: {}", framework);
logger.info("RunManager.json loaded successfully");

// Debug: detailed diagnostic info
logger.debug("[DatePicker] Framework detected: {}", framework);

// Warn: recoverable issues
logger.warn("[RunManager] Invalid testDataSource '{}' — falling back to 'json'", value);

// Error: failures with context
logger.error("Error reading includeTags: {}", e.getMessage());
```

Always use parameterized logging (`{}` placeholders) — never string concatenation.

### Reporting

- `ReportManager` and `TestRunReportManager` manage ExtentReports lifecycle.
- `StepHookManager` and `StepReportingWrapper` handle per-step logging.
- Screenshots are captured via `ScreenshotManager` — controlled by `RunManager.json` `reporting` config.
- Both HTML (`HtmlReportGenerator`) and Excel (`ExcelReportGenerator`) reports are generated per run.

---

## Python (OpenWebUI Backend)

### Code Style

- Line length: 120 characters (`ruff`, `black`).
- Single quotes for strings (enforced by `ruff` `flake8-quotes`).
- `ruff` is the primary linter — runs `pycodestyle`, `pyflakes`, `isort`, `pyupgrade`, `mccabe`.
- Import `datetime` as `dt` (enforced by `flake8-import-conventions`).
- Max cyclomatic complexity: 10 (`mccabe`).

### Patterns

- Use `StrEnum` for string-valued enumerations:
  ```python
  class VectorType(StrEnum):
      CHROMA = 'chroma'
      PGVECTOR = 'pgvector'
      QDRANT = 'qdrant'
  ```
- FastAPI async route handlers with Pydantic v2 models for request/response validation.
- SQLAlchemy 2.0 async ORM with Alembic migrations.
- `loguru` for logging (not standard `logging`).

---

## TypeScript/Svelte (OpenWebUI Frontend)

### Code Style

- Prettier with `prettier-plugin-svelte` — run `npm run format` before committing.
- ESLint with `@typescript-eslint` and `eslint-plugin-svelte`.
- TypeScript strict mode via `tsconfig.json`.
- Tailwind CSS v4 utility classes — no custom CSS unless in `app.css` or `tailwind.css`.

### Patterns

- SvelteKit file-based routing: `+page.svelte`, `+layout.svelte`, `+layout.js`.
- Svelte stores in `src/lib/stores/` for global state.
- API calls in `src/lib/apis/` — one file per backend domain.
- Components in `src/lib/components/` — organized by feature area.
- i18n via `i18next` — all user-facing strings must use translation keys.

### Build

- `APP_VERSION` and `APP_BUILD_HASH` are injected at build time via Vite `define`.
- `console.log/debug/error` are stripped in production builds (`esbuild.pure`).
- ONNX runtime WASM files are copied to `wasm/` via `vite-plugin-static-copy`.
- Worker format is `es` modules.

---

## Configuration Files

### RunManager.json (STATIM_Automation)
Controls all execution parameters:
```json
{
  "environmentURL": "https://app.example.com",
  "browser": ["chrome"],
  "executionMode": "web",
  "testDataSource": "json",
  "parallelExecution": false,
  "threads": 1,
  "includeTags": ["@TC_LOGIN_001"],
  "excludeTags": [],
  "failFastEnabled": false,
  "rcaEnabled": true,
  "defectWriterEnabled": true,
  "reporting": {
    "captureScreenshotsOnPass": false,
    "captureScreenshotsOnFailure": true
  }
}
```

### spark-config.json (NAFPro Atlas)
Configures AI Spark BRD-to-Gherkin generation. Referenced by `SparkConfig` and `SparkOrchestrator`.

### step-catalog.json (NAFPro Atlas)
Catalog of available step definitions used by AI Spark for feature generation.

---

## Framework Extension Guidelines

### Adding a New UI Framework Wrapper

1. Create package `novac.wrapper.<framework>/` with handler implementations.
2. Implement core interfaces: `TableEngine`, `DatePickerHandler`, `DropdownHandler`, etc.
3. Add enum value to `UIFrameworkDetector.UIFramework`.
4. Add detection logic in `UIFrameworkDetector.resolveFramework()`.
5. Add `case <FRAMEWORK> -> new <Framework>Handler()` in each `ComponentHandlerFactory` method.
6. Add unit tests in `src/test/java/novac/wrapper/`.

### Adding a New Handler Type

1. Define interface in `novac.wrapper.core/`.
2. Create `Generic<HandlerType>` in `novac.wrapper.factory/` as the default implementation.
3. Add factory method to `ComponentHandlerFactory`.
4. Implement framework-specific versions as needed.
5. Add `get<HandlerType>Handler()` to `ComponentHandlerFactory`.

### Adding a New Test Module (STATIM_Automation)

1. Create feature files in `src/test/resources/features/<ModuleName>/`.
2. Create step definition class in `stepdefinitions/<ModuleName>Steps.java`.
3. Create page objects in `src/main/java/novac/pages/`.
4. Add test data JSON in `TestDatastore/json/<ModuleName>.json`.
5. Tag scenarios with `@TC_<MODULE>_<NUMBER>` format.
6. Update `RunManager.json` to include the new module tags.

---

## Freeze Status Reference

| Component | Status | Meaning |
|-----------|--------|---------|
| Hard Frozen | Stable, no changes | API is locked; do not modify |
| Soft Frozen | Stable, minor additions OK | Core API locked; extensions allowed |
| Active | Under development | API may change |

Hard Frozen components: FileUploadHandler, DatePicker, LoadingStateDetector, FileDownloadVerifier, TreeHandler, FormValidationAssert, ToastWaiter, DateTimeHandler, WizardHandler, RichTextHandler, CascaderHandler.
