# DemoTechValidation Suite

## NAFPro Atlas — Enterprise Table Engine & Desktop Automation Showcase

This suite demonstrates NAFPro Atlas capabilities across **7 web UI frameworks** and
**Windows Desktop** using local fixtures for 100% deterministic execution.

**54 scenarios total** — all self-contained, no external dependencies.

---

## Structure

```
DemoTechValidation/
├── Web/
│   ├── AGGrid/              → AG Grid (virtualized DOM)
│   ├── AngularMaterial/     → Angular Material (mat-table)
│   ├── AntD/                → Ant Design Table
│   ├── GenericHTML/          → Standard HTML <table>
│   ├── MUI/                 → MUI DataGrid
│   └── PrimeNG/             → PrimeNG Table
└── Desktop/
    └── Calculator/          → Windows Calculator (Preview)
```

---

## Web — Enterprise Table Engine (48 scenarios)

| Technology | Engine | Fixture | Scenarios |
|---|---|---|---|
| AG Grid | `AgGridTableEngine` | `ag-grid-table-demo.html` | 6 |
| Angular Material | `AngularMaterialTableEngine` | `angular-material-table-demo.html` | 6 |
| Ant Design | `AntDTableEngine` | `antd-table-demo.html` | 6 |
| Generic HTML Table | `GenericTableEngine` | `generic-html-table-demo.html` | 6 |
| Generic HTML Sort/Filter | `GenericTableEngine` | `generic-html-table-demo.html` | 6 |
| Generic HTML Selection/State | `GenericTableEngine` | `generic-html-selection-state-demo.html` | 6 |
| Generic HTML Expander/Editor | `GenericTableEngine` | `generic-html-expander-editor-demo.html` | 6 |
| MUI DataGrid | `MUITableEngine` | `mui-datagrid-demo.html` | 6 |
| PrimeNG | `PrimeNGTableEngine` | `primeng-table-demo.html` | 6 |

### Capabilities Proven

- `findRow()` + `getCellValue()` — single-column row lookup with cell verification
- `performRowAction()` — 6-level action resolution (button, link, title, aria-label, data-action, menu)
- Cross-page search — automatic pagination traversal
- `findRowByCriteria()` — multi-column matching with TableRowContext
- `getRowCount()` / `rowExists()` — count and existence verification
- `sortByColumn()` + `verifySortOrder()` — column sorting with data-level validation
- `filterColumnContains()` + `globalSearch()` — column/global filtering
- `selectRow()` / `deselectRow()` / `selectAll()` — row selection
- `isEmpty()` / `isLoading()` — state assertions
- `expandRow()` / `collapseRow()` — row expansion
- `editCell()` — inline cell editing

---

## Desktop — Windows Calculator (Preview) (6 scenarios)

| Scenario | Proves |
|---|---|
| Basic addition | Button click + result verification |
| Mode switch | Navigation + window title assertion |
| Clear/reset | Control button + display state |
| History panel | Multi-step calculation |
| Keyboard input | Expression typing via mapped buttons |
| Memory store/recall | Memory operations |

**Prerequisite:** WinAppDriver installed and running. See `NAFPro/docs/desktop-prerequisites.md`.

---

## Run Commands

```bash
# All 54 scenarios (web runs without prerequisites; desktop requires WinAppDriver)
mvn exec:java

# Web only (48 scenarios, no prerequisites)
# Set selectedSuite to "Demo_Web" in RunManager.json

# Desktop only (6 scenarios, requires WinAppDriver)
# Set selectedSuite to "Demo_Desktop" in RunManager.json

# Individual technology
# Set selectedSuite to "Demo_AGGrid", "Demo_AntD", "Demo_PrimeNG", etc.
```

---

## Adding New Technology Demos

1. Create fixture: `src/test/resources/fixtures/{framework}-table-demo.html`
2. Create feature: `features/DemoTechValidation/Web/{Framework}/{Framework}Table.feature`
3. Create test data: `src/main/resources/testdata/json/{Framework}Table.json`
4. Create constants: `src/main/java/novac/constants/Demo{Framework}Constants.java`
5. Add suite tag to `RunManager.json`
6. No step definition changes needed — `DemoTableSteps.java` is framework-agnostic
