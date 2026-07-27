# NAFPro AI Nexus — Project Structure

## Root Layout

```
NAFPro_AI_Nexus/
├── NAFPro_Atlas/          # Core reusable automation framework (Maven JAR)
├── STATIM_Automation/     # Concrete BDD test suite consuming the framework
├── OpenWebUI/             # Self-hosted AI platform (SvelteKit + Python FastAPI)
├── Nexus/                 # Supporting infrastructure
├── Runtime/               # Runtime resources
└── .amazonq/rules/        # Amazon Q rules and memory bank
```

---

## NAFPro_Atlas Structure

```
NAFPro_Atlas/
├── pom.xml                          # Maven build: com.novac:nafpro-framework:2.0.0
├── spark-config.json                # AI Spark configuration
├── step-catalog.json                # Step catalog for Spark BRD-to-Gherkin
├── src/main/java/novac/
│   ├── ai/
│   │   ├── defect/                  # AI Defect Writer (DefectWriterAssistant, etc.)
│   │   ├── rca/                     # Failure RCA Assistant (RCAAssistant, rules/)
│   │   └── spark/                   # AI Spark: BRD→Gherkin (SparkOrchestrator, stages/, catalog/)
│   ├── api/
│   │   ├── auth/                    # Auth strategies: ApiKey, Basic, BearerToken
│   │   ├── core/                    # ApiClient, ApiRequest, ApiResponse, ResponseAssert
│   │   ├── reporting/               # ApiStepLogger
│   │   └── utils/                   # SchemaValidator
│   ├── constants/                   # CommonConstants
│   ├── hooks/                       # StepHooks, TestHooks (Cucumber lifecycle)
│   ├── model/                       # FlexiField, FlexiFieldType (universal field model)
│   ├── parallel/                    # ParallelRunner
│   ├── qmetry/                      # QMetry integration (QMetryApiClient, QMetryResultPusher)
│   ├── reporting/
│   │   ├── config/                  # ExtentConfig, ReportPaths
│   │   ├── generators/              # ExcelReportGenerator, HtmlReportGenerator
│   │   ├── managers/                # ReportManager, TestRunReportManager
│   │   └── utils/                   # ReportFileManager
│   ├── utils/
│   │   ├── TestContext.java         # Thread-safe test context (WebDriver, scenario state)
│   │   ├── RunManager.java          # Execution config loader (RunManager.json)
│   │   ├── TestDataManager.java     # JSON/Excel test data access
│   │   ├── SmartLocator.java        # Self-healing element locator
│   │   ├── FlexiFieldInteractor.java # Universal field interaction
│   │   ├── WaitHandler.java         # Explicit wait utilities
│   │   └── ...                      # Other utilities
│   └── wrapper/
│       ├── core/                    # Abstract interfaces: TableEngine, DatePickerHandler, etc.
│       ├── factory/                 # ComponentHandlerFactory, UIFrameworkDetector, Generic* impls
│       ├── aggrid/                  # AG Grid table engine + handlers
│       ├── angularmaterial/         # Angular Material wrappers
│       ├── antd/                    # Ant Design wrappers
│       ├── mui/                     # MUI DataGrid wrappers
│       ├── primeng/                 # PrimeNG wrappers
│       └── desktop/                 # Windows Desktop (WinAppDriver): core/, factory/, session/, table/
└── src/test/java/novac/             # Unit tests for framework components
```

---

## STATIM_Automation Structure

```
STATIM_Automation/
├── pom.xml                          # Maven: com.statim:STATIM_Automation:0.0.1-SNAPSHOT
├── RunManager.json                  # Execution config (browser, env, testDataSource, modules)
├── cucumber.properties              # Cucumber options
├── src/
│   ├── main/java/novac/
│   │   ├── constants/               # App-specific constants
│   │   ├── helpers/                 # Test helper utilities
│   │   ├── pages/                   # Page Object classes
│   │   └── utils/                   # App-specific utilities
│   └── test/
│       ├── java/
│       │   ├── runners/             # TestRunner, CucumberMain, ModuleResolver, SuiteResolver
│       │   └── stepdefinitions/     # Step def classes per module + CommonSteps, FlexiFieldStepDef
│       └── resources/features/      # Cucumber .feature files per module
│           ├── Login/
│           ├── ProductConfig/
│           ├── NewBusiness/
│           ├── Rating/
│           ├── RuleEngine/
│           ├── AccountingSetup/
│           └── SystemParameters/
└── TestDatastore/
    └── json/                        # One JSON file per module (Login.json, ProductConfig.json, etc.)
```

---

## OpenWebUI Structure

```
OpenWebUI/
├── package.json                     # Node: open-webui v0.10.2
├── vite.config.ts                   # Vite build config
├── svelte.config.js                 # SvelteKit adapter config
├── tailwind.config.js               # Tailwind CSS v4
├── postcss.config.js                # PostCSS config
├── tsconfig.json                    # TypeScript config
├── pyproject.toml                   # Python package config (hatch)
├── src/
│   ├── lib/
│   │   ├── apis/                    # API client functions (per backend endpoint)
│   │   ├── components/              # Svelte UI components
│   │   ├── stores/                  # Svelte stores (global state)
│   │   ├── utils/                   # Frontend utilities
│   │   ├── types/                   # TypeScript type definitions
│   │   ├── i18n/                    # Internationalization
│   │   ├── constants/               # Frontend constants
│   │   └── workers/                 # Web workers
│   └── routes/                      # SvelteKit file-based routing
│       ├── (app)/                   # Main app routes
│       ├── auth/                    # Authentication routes
│       ├── s/                       # Share routes
│       └── watch/                   # Watch routes
├── backend/open_webui/
│   ├── main.py                      # FastAPI app entry point
│   ├── config.py                    # App configuration
│   ├── env.py                       # Environment variable handling
│   ├── routers/                     # FastAPI route handlers
│   ├── models/                      # SQLAlchemy/Peewee DB models
│   ├── retrieval/                   # RAG: vector DBs, web search, document loaders
│   ├── socket/                      # WebSocket / Socket.IO handlers
│   ├── utils/                       # Backend utilities
│   ├── tools/                       # Built-in tools
│   ├── storage/                     # File storage providers
│   ├── migrations/                  # Alembic DB migrations
│   └── static/                      # Served static files (loader.js)
└── static/                          # Public static assets (icons, themes, audio)
```

---

## Architectural Patterns

### NAFPro Atlas
- **Factory Pattern:** `ComponentHandlerFactory` + `UIFrameworkDetector` auto-detect the UI framework and return the correct handler implementation.
- **Strategy Pattern:** Each UI framework (AntD, MUI, PrimeNG, etc.) implements the same core interface (e.g., `TableEngine`, `DatePickerHandler`).
- **Template Method:** `AbstractTableEngine` defines the table interaction algorithm; concrete engines override specific steps.
- **Thread-Safe Context:** `TestContext` holds `WebDriver` and scenario state per thread for parallel execution.
- **FlexiField Model:** Universal field descriptor (`FlexiField` + `FlexiFieldType`) decouples step definitions from UI framework specifics.

### STATIM_Automation
- **BDD (Cucumber):** Feature files → Step Definitions → Page Objects → Framework wrappers.
- **Data-Driven:** JSON test data per module, loaded via `TestDataManager`.
- **Module-Based Execution:** `RunManager.json` controls which modules/suites run.

### OpenWebUI
- **SvelteKit SSR/SPA:** File-based routing, Svelte 5 components, Svelte stores for global state.
- **FastAPI Backend:** Python async REST API with SQLAlchemy models and Alembic migrations.
- **RAG Pipeline:** Document ingestion → vector DB → hybrid search (BM25 + vector) → reranking.
- **Plugin Architecture:** Filters, Actions, Pipes, Tools, Skills extend core functionality.
