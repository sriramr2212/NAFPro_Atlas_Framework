# NAFPro AI Nexus — Product Overview

## Project Purpose

NAFPro AI Nexus is a multi-module AI-augmented test automation platform. It combines a reusable Java automation framework (NAFPro Atlas), a concrete test automation suite (STATIM_Automation), and a self-hosted AI chat interface (OpenWebUI) to deliver end-to-end intelligent test automation capabilities.

## Modules

### 1. NAFPro Atlas (`/NAFPro_Atlas`)
- **Tagline:** Write Once. Run Across Technologies.
- **Role:** Core reusable Java automation framework — no test cases, only framework code.
- **Artifact:** `nafpro-framework-2.0.0.jar` (Maven: `com.novac:nafpro-framework:2.0.0`)
- **Consumed by:** STATIM_Automation and other test projects via Maven dependency.

### 2. STATIM_Automation (`/STATIM_Automation`)
- **Role:** Concrete BDD test automation suite for the STATIM2 application.
- **Depends on:** NAFPro Atlas framework JAR.
- **Contains:** Feature files, step definitions, test data, and test runners.

### 3. OpenWebUI (`/OpenWebUI`)
- **Role:** Self-hosted AI platform (Open WebUI v0.10.2) integrated into the Nexus for AI-assisted automation workflows.
- **Supports:** Ollama, OpenAI-compatible APIs, RAG, plugins, agents, and more.

### 4. Nexus / Runtime (`/Nexus`, `/Runtime`)
- Supporting infrastructure directories.

---

## Key Features

### NAFPro Atlas Framework
- **Multi-Framework UI Wrappers:** AG Grid, Ant Design, PrimeNG, Angular Material, MUI DataGrid, Generic HTML
- **NAFPro Atlas Engine:** find-by-value, cross-page search, row actions, pagination, sort, filter, select, expand, edit
- **File Upload Handler:** Standard input, hidden input, wrapper resolution, multi-file, drop zone (Hard Frozen)
- **Multi-Framework DatePicker:** Calendar popup navigation, direct input, readonly detection (Hard Frozen)
- **Multi-Framework MultiSelect:** Chip/token management, virtualization scroll, deselect, read selected (Soft Frozen)
- **REST API Testing:** Native REST automation with assertions, chaining, schema validation (Soft Frozen)
- **Windows Desktop Support:** WinAppDriver-based desktop automation (Soft Frozen)
- **AI Capabilities:**
  - Failure RCA Assistant — automated root cause analysis
  - AI Defect Writer — structured defect report generation
  - AI Spark — BRD-to-Gherkin feature generation
- **Reporting:** ExtentReports + custom HTML/Excel reports
- **Test Data:** JSON and Excel dual-source support via `RunManager.json`
- **Thread-Safe Execution:** TestContext for parallel test runs

### STATIM_Automation Suite
- BDD Cucumber feature files for STATIM2 modules: Login, ProductConfig, NewBusiness, Rating, RuleEngine, AccountingSetup, SystemParameters
- JSON-based test data store per module
- Custom step definitions and page objects

### OpenWebUI Platform
- Self-hosted LLM chat interface with RAG, web search, image generation
- Plugin system (Filters, Actions, Pipes, Tools, Skills)
- Multi-model conversations, voice/video calls
- RBAC, LDAP/SSO, SCIM 2.0 provisioning
- 9 vector database backends (ChromaDB, PGVector, Qdrant, Milvus, etc.)
- OpenTelemetry observability, horizontal scalability with Redis

---

## Target Users

- **QA/Automation Engineers:** Use NAFPro Atlas to build test suites; use STATIM_Automation as a reference implementation.
- **Test Architects:** Leverage the framework's multi-technology wrappers and AI capabilities.
- **AI/ML Teams:** Use OpenWebUI for self-hosted LLM interactions and RAG workflows.
- **DevOps/Platform Teams:** Deploy OpenWebUI as an enterprise AI platform.

---

## Version History (NAFPro Atlas)

| Version | Milestone |
|---------|-----------|
| v1.0.0 | Initial stable version |
| v2.0.0 | NAFPro Atlas Engine: Enterprise Table Engine |
| v2.3.0 | Desktop Automation + AI Capabilities |
| v2.4.0 | REST API Testing |
| v2.5.0–v2.5.9 | File Upload, DatePicker, MultiSelect, LoadingState, FileDownload, Tree, FormValidation, Toast, DateTime, Wizard, RichText, Cascader handlers |
