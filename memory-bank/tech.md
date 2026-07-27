# NAFPro AI Nexus — Technology Stack

## NAFPro Atlas (Java Framework)

### Language & Runtime
- Java 17 (source and target)
- Maven 3.8+ build system

### Core Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| selenium-java | 4.25.0 | WebDriver UI automation |
| webdrivermanager | 5.9.2 | Automatic driver management |
| cucumber-java | 7.24.0 | BDD step definitions |
| cucumber-junit-platform-engine | 7.24.0 | Cucumber JUnit 5 integration |
| cucumber-junit | 7.24.0 | Cucumber JUnit 4 runner |
| junit-jupiter | 5.11.0 | Unit testing (test scope) |
| mockito-core | 5.12.0 | Mocking (test scope) |
| extentreports | 5.1.2 | HTML test reporting |
| log4j-core / log4j-api | 2.24.0 | Logging |
| jackson-databind | 2.18.0 | JSON serialization |
| poi-ooxml | 5.3.0 | Excel read/write |
| commons-io | 2.17.0 | File utilities |
| lombok | 1.18.30 | Boilerplate reduction (provided) |
| java-client (Appium) | 9.3.0 | Mobile/Desktop automation |
| rest-assured | 5.5.0 | REST API testing |
| json-schema-validator | 5.5.0 | JSON schema validation |
| xml-path | 5.5.0 | XML path assertions |

### Build Commands
```bash
# Build and install framework to local Maven repo
mvn clean install

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=ClassName

# Migrate Excel → JSON test data
mvn exec:java -Dexec.mainClass=novac.utils.ExcelToJsonMigrator \
    "-Dexec.args=--excel TestDatastore/Testdata.xlsx --outdir TestDatastore/json" \
    -Dexec.classpathScope=compile

# Migrate JSON → Excel
mvn exec:java -Dexec.mainClass=novac.utils.JsonToExcelMigrator \
    "-Dexec.args=--jsondir TestDatastore/json --output TestDatastore/Testdata.xlsx" \
    -Dexec.classpathScope=compile
```

---

## STATIM_Automation (Test Suite)

### Language & Runtime
- Java 17
- Maven 3.8+

### Core Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| nafpro-framework | 1.0.0 | NAFPro Atlas framework JAR |
| cucumber-java | 7.24.0 | Step definitions |
| cucumber-junit | 7.24.0 | Test runner |
| junit-jupiter | 5.11.0 | JUnit 5 (test scope) |

### Maven Plugins
- `maven-surefire-plugin` 3.0.0-M7 — runs `**/TestRunner.java`
- `exec-maven-plugin` 3.1.0 — main class: `runners.TestRunner`
- `maven-assembly-plugin` 3.6.0 — fat JAR with dependencies

### Build Commands
```bash
# Run all tests
mvn test

# Run tests via exec plugin
mvn exec:java

# Build fat JAR
mvn package

# Run specific suite (via RunManager.json config)
mvn test -Dtest=TestRunner
```

---

## OpenWebUI (AI Platform)

### Frontend
| Technology | Version | Purpose |
|-----------|---------|---------|
| Node.js | 18.13.0–22.x | Runtime |
| npm | ≥6.0.0 | Package manager |
| Svelte | ^5.53.10 | UI framework |
| SvelteKit | ^2.5.27 | Full-stack framework |
| TypeScript | ^5.5.4 | Type safety |
| Vite | ^5.4.21 | Build tool |
| Tailwind CSS | ^4.0.0 | Utility-first CSS |
| PostCSS | ^8.4.31 | CSS processing |
| ESLint | ^8.56.0 | Linting |
| Prettier | ^3.3.3 | Code formatting |
| Vitest | ^1.6.1 | Unit testing |
| Cypress | ^13.15.0 | E2E testing |

### Key Frontend Libraries
| Library | Purpose |
|---------|---------|
| socket.io-client ^4.8.3 | Real-time WebSocket |
| i18next ^23.10.0 | Internationalization |
| CodeMirror 6 | Code editor |
| TipTap ^3.0.7 | Rich text editor |
| KaTeX ^0.16.22 | Math rendering |
| Mermaid ^11.10.1 | Diagram rendering |
| highlight.js ^11.9.0 | Syntax highlighting |
| marked ^9.1.0 | Markdown parsing |
| DOMPurify ^3.2.6 | XSS sanitization |
| Chart.js ^4.5.0 | Charts |
| Leaflet ^1.9.4 | Maps |
| Pyodide ^0.28.2 | Python in browser |
| @huggingface/transformers ^3.0.0 | Client-side ML |
| yjs ^13.6.27 | CRDT collaboration |
| pdfjs-dist ^5.4.149 | PDF rendering |
| @azure/msal-browser ^4.5.0 | Azure SSO |

### Backend (Python)
| Technology | Version | Purpose |
|-----------|---------|---------|
| Python | 3.11–3.12 | Runtime |
| FastAPI | 0.136.3 | Async REST API |
| Uvicorn | 0.41.0 | ASGI server |
| Pydantic | 2.13.4 | Data validation |
| SQLAlchemy | 2.0.50 | ORM (async) |
| Alembic | 1.18.4 | DB migrations |
| aiosqlite | 0.22.1 | SQLite async |
| psycopg | 3.3.4 | PostgreSQL |
| Redis | 8.0.0 | Session/cache |
| python-socketio | 5.16.2 | WebSocket |
| LangChain | 1.2.10 | LLM orchestration |
| langchain-community | 0.4.2 | Community integrations |
| ChromaDB | 1.5.9 | Vector DB (default) |
| sentence-transformers | 5.5.1 | Embeddings |
| faster-whisper | 1.2.1 | Speech-to-text |
| openai | 2.29.0 | OpenAI API client |
| anthropic | 0.86.0 | Anthropic API client |
| google-genai | 1.66.0 | Google AI client |
| mcp | 1.27.2 | Model Context Protocol |
| loguru | 0.7.3 | Logging |
| APScheduler | 3.11.2 | Task scheduling |
| hatchling | — | Build backend |

### Frontend Build Commands
```bash
# Development server
npm run dev

# Production build
npm run build

# Type check
npm run check

# Lint (frontend + types + backend)
npm run lint

# Format all files
npm run format

# Run frontend tests
npm run test:frontend

# Parse i18n strings
npm run i18n:parse
```

### Backend Commands
```bash
# Start backend (Linux/Mac)
./backend/start.sh

# Start backend (Windows)
backend\start_windows.bat

# Dev mode
./backend/dev.sh

# Install Python deps
pip install -e ".[all]"
# or
uv sync
```

### Docker
```bash
# Standard run (Ollama on same host)
docker run -d -p 3000:8080 --add-host=host.docker.internal:host-gateway \
  -v open-webui:/app/backend/data --name open-webui --restart always \
  ghcr.io/open-webui/open-webui:main

# Docker Compose
docker compose up -d
```

---

## Code Quality Tools

### Java (NAFPro Atlas / STATIM)
- Log4j2 for structured logging (`log4j2.xml`)
- Lombok for boilerplate reduction
- JUnit 5 + Mockito for unit tests

### Python (OpenWebUI Backend)
- `ruff` — linting and formatting (line-length: 120, single quotes)
- `black` — formatting (line-length: 120)
- `pylint` — additional linting

### TypeScript/Svelte (OpenWebUI Frontend)
- ESLint with `@typescript-eslint` and `eslint-plugin-svelte`
- Prettier with `prettier-plugin-svelte`
- `.prettierrc` and `.eslintrc.cjs` config files
