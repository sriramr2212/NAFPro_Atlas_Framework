# STATIM — Demo Flow Guide

This is the logical order to demo STATIM to a prospective client, from foundational setup through to live transactions. Each module includes step-by-step click instructions **and functional checks** — what to verify to confirm the system is working correctly.

---

## The Big Picture Flow

```
Masters → User Management → Entity Master → Product Configurator
→ Rating Engine → Rule Engine → New Business (Quote → Policy)
→ Workflow → Claims → Collection → Commission → Reinsurance (RI)
```

---

## 1. Masters

**What it is:** The foundation of the entire platform. All reference data — companies, offices, products, tax codes, currencies, common codes — lives here. Nothing else works without masters.

**Why first:** Every other module pulls from masters. You can't create a product without a Company, COB (Class of Business), LOB (Line of Business), and Office master.

**How to demo:**
1. Click **Masters** in the left nav → opens the Master Dashboard
2. The left panel shows the **Master List** (all master types — Company, Office, Common Codes, Tax, Currency, etc.)
3. Click any master (e.g., **Company** — code `COMPANY`) → middle panel shows the list of entries
4. Click an entry → right panel shows its full details
5. To add: click the **+** icon in the middle panel header → fill the popup form → Save
6. To edit: click the **pencil/edit** icon on the detail panel header
7. Show key masters in order: **Company → Office (OM) → Common Codes (COC) → Currency (CUR) → Tax (TAX) → Product (PRD)**

**Functional checks:**
- After saving a new master entry, verify it immediately appears in the middle panel list without a page refresh
- Click the new entry → confirm all fields you entered are displayed correctly in the right panel
- Try saving with a mandatory field blank → confirm a validation error appears and the record is not saved
- For Tax master: verify the tax rate percentage is stored and displayed correctly (e.g., VAT 5%)
- For Currency master: verify the ISO code is saved (e.g., USD, AED) — this is used in premium display throughout the system

> **Demo tip:** Show the 3-panel layout (Master List | Sub-list | Details). Highlight the search bar and sort arrows. Show that masters are hierarchical — e.g., Office depends on Company.

---

## 2. User Management

**What it is:** Controls who can log in, what roles they have, what menus they see, and what actions they can perform (RBAC + ABAC).

**Why here:** Users need to be set up before they can operate any module.

**How to demo:**
1. Go to **Masters** → scroll to **User Management** section (or navigate via the side menu)
2. Click **User Management** → shows the user list
3. Click **+ Add User** → fill in name, email, role, office, operational hierarchy
4. Show **Roles** — click Roles → shows role list → click a role → shows menu permissions per screen
5. Show **ABAC** — attribute-based rules (e.g., user can only see policies from their own office)
6. Show **Operational Hierarchy** — the org structure that controls data visibility

**Functional checks:**
- After creating a user, log in as that user in a separate browser tab → confirm only the menus assigned to their role are visible
- On the NB Listing screen, verify that action buttons (Approve, Reject, Edit) are enabled or greyed out based on the role's permissions — the system uses `canAccessAction` checks per screen
- Try accessing a restricted menu URL directly → confirm the system blocks access
- Show that a user assigned to Office A cannot see policies created under Office B (ABAC in action)

> **Demo tip:** Show how a role controls which menu items appear and which fields are editable vs read-only.

---

## 3. Entity Master

**What it is:** The centralised customer/party registry. Every insured, claimant, broker, or corporate entity is created here first. Includes KYC, KYB, address, contact, credit facility, and due-dupe checking.

**Why here:** New Business requires an entity (the insured/proposer). You can't issue a policy without one.

**How to demo:**
1. Click **Entity Master** in the left nav → opens the Entity Dashboard
2. Click **+ New Entity** → choose entity type (Individual / Corporate)
3. Fill **Personal Details** — name, DOB, ID type/number
4. Fill **Address** — country, state, city, pincode
5. Fill **KYC** tab — upload ID documents
6. Fill **KYB** tab (for corporate) — business registration, directors
7. Click **Save** → system runs **due-dupe check** automatically (checks for duplicate entities)
8. Show the entity appearing in the list with a unique Entity ID

**Functional checks:**
- After saving, search for the entity by name → confirm it appears in results with the correct Entity ID
- Try creating a second entity with the same name and DOB → confirm the system shows a due-dupe warning before allowing save
- Verify the KYC document upload works — upload a file, save, then reopen the entity and confirm the document is still attached
- Check that the Entity ID generated is unique and follows the configured reference number format

> **Demo tip:** Show the due-dupe alert — if you enter a similar name/DOB, the system warns you before saving.

---

## 4. Product Configurator

**What it is:** Defines insurance products — their sections, Sum Insured (SMI), covers, deductibles, discounts, loadings, charges, fees, and risk metadata (the dynamic form fields). This is what makes STATIM product-agnostic.

**Why here:** Before you can quote, you need a configured product.

**How to demo:**
1. Click **Product Configurator** in the left nav → shows the product listing table
2. Click **+ Product** button (top right) → opens the product creation wizard
3. **Step 1 — Basic Info:** Enter Product Code, Product Name, select Company / COB / LOB, set Effective From/To
4. **Step 2 — Section:** Add sections (e.g., "Own Damage", "Third Party")
5. **Step 3 — SMI & Cover:** Under each section, add SMI (Sum Insured items) and Covers. Set limits, deductibles, conditions
6. **Step 4 — Risk Metadata:** Define the dynamic form fields (e.g., Vehicle Make, Model, Year for motor) — these become the proposal form fields
7. **Step 5 — Properties:** Set underwriting rules, claims settings, instalment options, RI settings
8. **Step 6 — Charges & Fees:** Add stamp duty, policy fee, etc.
9. Click **Save** → status shows **Incomplete**
10. Click the **⋮ menu** on the product row → click **Approve** → status changes to **Approved**

**Functional checks:**
- After approval, go to New Business → click **+ New** → select the product → confirm the risk form fields you defined in Step 4 appear dynamically (e.g., Vehicle Make, Model, Year fields render correctly)
- Verify that mandatory fields marked in Product Configurator show as required (red asterisk) in the New Business form
- Confirm that covers marked as **Mandatory** in the product appear pre-selected and cannot be deselected in the Quick Quote covers section
- Confirm that covers marked as **Default** appear pre-selected but can be toggled off
- Try cloning the product (⋮ → Clone) → verify the cloned product has all sections, covers, and SMIs copied correctly with a new product code

> **Demo tip:** Show the Clone feature — click ⋮ → Clone → select a new product code → instantly copies the entire product structure. Great for creating variants.

---

## 5. Rating Engine

**What it is:** Configures how premiums are calculated — rate tables, tax rules, discount/loading rules, SI tables, and account definitions. Linked to a specific product.

**Why here:** Without a rating setup, the Quick Quote won't calculate any premium.

**How to demo:**
1. Click **Rating Engine** in the left nav → shows the Rating Dashboard
2. Click **+ New Rating** → select Company / COB / LOB / Product
3. **Rate Table:** Add rate logic — e.g., "if Vehicle Age < 3 years, rate = 2.5%"
4. **Tax:** Link the tax master (e.g., VAT 5%) — select applicable covers
5. **Discount:** Add discount rules (e.g., No-Claim Discount 10%)
6. **Loading:** Add loading rules (e.g., High-Risk Zone +15%)
7. **SI Table:** Define Sum Insured bands if needed
8. Click **Save** → rating is now linked to the product

**Functional checks:**
- Go to New Business → Quick Quote → select this product → fill risk details → click **Generate Quick Quote**
- Verify that the **Premium Details panel** (right side of Quick Quote screen) shows a non-zero premium amount after calculation
- Confirm the premium figure changes if you change a risk parameter that affects the rate (e.g., change vehicle age from 2 to 5 years) — click **Update Quick Quote** and verify the premium recalculates
- In Full Quote → Coverage Details → **Price Break-Up** modal: verify the table shows individual line items — SMI rows with Rate and Sum Insured, Cover rows with premiums, Discount rows with percentage and amount, Loading rows, Tax rows, and a **Total Payable Premium** row at the bottom
- Verify the tax amount in Price Break-Up matches the tax rate configured in the Rating Engine (e.g., 5% of base premium)
- Toggle **Functional Currency** switch in Price Break-Up → verify amounts switch between functional and transactional currency correctly

> **Demo tip:** Show the expression builder for rate rules — it's a formula editor where you can write conditions like `VehicleAge <= 3 AND UsageType = "Private"`.

---

## 6. Rule Engine

**What it is:** Business rules that fire at specific events (e.g., on quote save, on policy approval). Rules can validate data, auto-populate fields, or trigger alerts.

**Why here:** Rules enhance the New Business flow with business logic.

**How to demo:**
1. Click **Rule Engine** in the left nav → shows the rule list
2. Click **+ New Rule** → enter Rule Name, select Event (e.g., "On Quote Save")
3. Add conditions (e.g., "If Sum Insured > 1,000,000 then flag for UW review")
4. Add actions (e.g., "Set field X = Y", "Show warning message")
5. Click **Save** → rule is active

**Functional checks:**
- Trigger the rule condition in New Business (e.g., enter a Sum Insured above the threshold) → verify the configured action fires (warning message appears, or field is auto-populated)
- Save the quote without triggering the rule condition → verify no unwanted alerts appear (rule is not firing incorrectly)
- Disable the rule → repeat the trigger condition → confirm the action no longer fires

---

## 7. Workflow

**What it is:** Configures multi-stage approval workflows. Each stage has roles, actions (Approve / Reject / Send Back / Rework), and transitions. Real-time notifications via SignalR.

**Why here:** Before doing New Business, show how approvals are configured.

**How to demo:**
1. Go to **Masters** → find **Workflow (WOF)** → click it
2. Click **+ New Workflow** → enter Workflow Name, select Module (e.g., New Business)
3. **Add Stages:** Stage 1 = "Underwriter Review", Stage 2 = "Manager Approval"
4. For each stage: assign Role, set allowed Actions (Approve / Reject / Send Back)
5. **Add Transitions:** connect stages with arrows (Stage 1 → Approve → Stage 2)
6. Click **Save** → workflow is ready

**Functional checks:**
- After submitting a quote for approval (covered in Section 10), go to **Workflow Tasks** → verify the task appears in the assigned role's task list
- Click the task → verify the correct quote details are shown
- Click **Approve** → verify the task moves to the next stage and disappears from the current approver's list
- Open a second browser tab as the next-stage approver → verify the task notification (bell icon) appears in real-time without page refresh (SignalR push)
- Click **Reject** → enter a rejection reason → verify the quote status changes to **Reject** in the NB Listing and the submitter can see the rejection reason
- Click **Send Back** → verify the quote returns to the previous stage and the original submitter is notified

> **Demo tip:** Show the visual canvas — it's a drag-and-drop flow builder. Very visual and impressive for clients.

---

## 8. New Business — Quick Quote

**What it is:** The first step of the insurance sales journey. A fast premium estimate using minimal risk data. No full proposal form needed.

**Why here:** This is where the product, rating, and entity all come together for the first time.

**How to demo:**
1. Click **New Business** in the left nav → shows the NB Listing (all quotes/policies)
2. Click **Get Quote** button (top right) → search for the entity created earlier → select it
3. Select **Product** (the one you configured earlier) → click **Quick Quote**
4. The form dynamically renders the risk fields you defined in Product Configurator (e.g., Vehicle Make, Model, Year)
5. Fill in the risk details
6. Select a **Plan** (radio buttons at the top of the form) — covers update based on plan selection
7. Toggle add-on covers on/off in the Covers section
8. Click **Generate Quick Quote** → system calls Rating Engine → premium appears in the right-side panel

**Functional checks:**
- Verify the **Premium Details panel** on the right shows a non-zero **Total Premium** after clicking Generate Quick Quote
- Verify the Quick Quote Number is generated and displayed in the header (e.g., "Quick Quote Number - QQ/2025/001")
- Change a risk parameter (e.g., vehicle year) → click **Update Quick Quote** → verify the premium amount changes
- Toggle an add-on cover on → click **Update Quick Quote** → verify the premium increases by the cover's rate
- Try submitting without filling a mandatory field → verify the form shows validation errors and does not proceed
- Click **Save** → go back to NB Listing → verify the Quick Quote appears in the list with status and premium amount visible

---

## 9. New Business — Full Quote

**What it is:** The complete proposal. Adds the proposer/insured details, full risk details, covers, SMI values, co-insurance, broker commission, and payment details.

**How to demo:**
1. From the Quick Quote, click **Proceed to Full Quote** → system converts the QQ to a Full Quote and navigates to the Full Quote screen
2. **Proposer Details tab:** The entity details auto-populate from the entity selected during Get Quote
3. **Risk Details tab:** Fill all dynamic risk fields (the flexi-key form rendered from Product Configurator)
4. **Coverage Details tab:** Review covers, SMI, deductibles — adjust if needed
5. **Price Breakup tab:** Shows premium breakdown
6. **Co-Insurance tab** (if applicable): Add co-insurers and their share %
7. **Broker Commission tab:** Select broker, commission rate auto-calculates
8. Click **Save** → Full Quote saved

**Functional checks:**
- On the **Coverage Details tab**, verify the SMI tab shows Sum Insured values with Rate and Premium columns populated (not all zeros)
- On the **Covers tab**, verify mandatory covers are pre-selected and cannot be deselected; default covers are pre-selected but can be toggled
- On the **Discounts tab**, verify any NCD (No-Claim Discount) configured in the Rating Engine appears with the correct percentage
- On the **Tax tab**, verify the tax rows show the correct tax type and calculated tax amount
- Click **Price Break-Up** button → verify the modal opens and shows a complete breakdown table:
  - SMI section with Rate, Sum Insured, and Premium per item
  - Cover section with premiums
  - Discount section with percentage and deducted amount
  - Loading section (if applicable)
  - Charges & Fees section
  - Tax rows
  - **Total Payable Premium** row in bold at the bottom
- Select a different risk from the **Risk Details dropdown** in Price Break-Up → verify the breakdown updates for that specific risk
- Toggle **Functional Currency** in Price Break-Up → verify amounts switch correctly
- Verify the **Quotation Number** is generated and shown in the NB Listing after save

---

## 10. Quote Approval & Policy Issuance

**What it is:** The workflow-driven approval process that converts a quote into an active policy.

**How to demo:**
1. From the NB Listing, find the Full Quote (status: **Pending**) → click **⋮** → **Approve** (direct approve) OR submit through workflow
2. For workflow path: open the quote → click **Submit for Approval** → workflow triggers
3. The assigned underwriter sees a **task notification** (bell icon, top right — real-time via SignalR)
4. Click **Workflow Tasks** in the nav → shows pending tasks
5. Click the task → review the quote details
6. Click **Approve** → moves to next stage → click **Commit** → checkpoint saved
7. If multi-stage: next approver gets notified automatically
8. Final approval: click **Issue Policy** → system generates Policy Number

**Functional checks:**
- After final approval, go to NB Listing → verify the record's status changes from **QuoteApproved** to **PolicyApproved**
- Verify a **Policy Number** is now populated in the Policy No column (was blank during quote stage)
- Verify the **Premium** column in NB Listing shows the correct total premium
- Click **⋮** on the approved policy → verify the action menu now shows **Endorsement**, **Renewal**, **Cancel Policy**, **History** options (not Edit/Approve — those are gone post-issuance)
- Click **View** on the policy → open **Price Break-Up** → verify the breakdown is still accessible and shows the final premium figures
- Check the **Dashboard** → verify KPI counters (e.g., policies issued today, total premium) have updated — this confirms Kafka events fired and the dashboard subscriber processed them
- Verify the **Accounting** entries were created (if AccountingSetup is configured) — go to Accounting module and check for the staging entry

> **Demo tip:** Open two browser tabs — one as the submitter, one as the approver. Show the real-time notification appearing without page refresh.

---

## 11. Endorsement

**What it is:** Post-issuance policy amendments — change of vehicle, change of sum insured, addition of covers, etc. Full audit trail with field-level diff comparison.

**How to demo:**
1. From the NB Listing, find an issued policy (status: **PolicyApproved**) → click **⋮** → **Endorsement**
2. Select **Endorsement Type** (e.g., "Change of Sum Insured")
3. The system shows the current values — edit the fields you want to change
4. Click **Compare** → system shows a side-by-side diff of old vs new values
5. Click **Save** → endorsement goes through the same workflow approval process
6. On approval: new policy version is created, premium difference calculated

**Functional checks:**
- After saving the endorsement, go to NB Listing → verify the **Endorsement** column now shows an endorsement number (e.g., END/001)
- Open the endorsed policy → go to **Price Break-Up** → use the **Endorsement radio buttons** ("For the Endorsement" / "Up to the Endorsement") → verify the premium shown changes correctly between the two views
- "For the Endorsement" should show only the delta premium (the change)
- "Up to the Endorsement" should show the cumulative premium up to this endorsement
- Verify the original policy version is still accessible via **History** (⋮ → History) and shows the pre-endorsement values
- Confirm the endorsement goes through the workflow (if configured) — check Workflow Tasks for the endorsement approval task

---

## 12. Claims — FNOL

**What it is:** First Notice of Loss — the initial claim registration. Captures incident details, links to the policy, and starts the claims journey.

**How to demo:**
1. Click **Claims** in the left nav → shows the Claims Dashboard (list of all claims)
2. Click **+ New Claim** → opens the FNOL screen
3. **Policy Search:** Enter policy number or insured name → select the policy
4. **FNOL Step:** Fill incident date, location, description, cause of loss
5. Click **Save** → system generates FNOL Reference Number → Kafka event fires in background
6. Status shows **FNOL Registered**

**Functional checks:**
- After saving, verify the **FNOL Reference Number** is generated and displayed (e.g., FNOL/2025/001) — this confirms the Reference No Generator service worked
- Verify the claim appears in the Claims Listing with the correct policy number, insured name, and FNOL date
- Verify the claim status is **N** (New) after FNOL registration
- Check the **Dashboard** → verify the claims KPI counter has incremented — this confirms the `fnol-events` Kafka message was consumed by the dashboard subscriber
- Try saving FNOL without selecting a policy → verify a validation error appears
- Try entering an incident date in the future → verify the system rejects it with a validation message

---

## 13. Claims Journey

**What it is:** The full claims lifecycle — from FNOL through assessment, surveyor appointment, provision, settlement.

**How to demo:**
1. From the FNOL, click **Proceed to Claims Registration**
2. **Claims Register Step:** Assign claim number, set reserve amount
3. **Surveyor/Professional Appointment:** Assign a surveyor → they get notified
4. **Provision Amount:** Enter estimated loss amount
5. **Computation Step:** Calculate settlement amount — applies deductibles, depreciation
6. **Settlement Step:** Enter payment details → approve settlement
7. Each step goes through the workflow (same approval mechanism as New Business)

**Functional checks:**
- After Claims Registration, verify the **Claim Number** is generated (separate from FNOL number) and the status changes to **R** (Registered)
- On the **Computation step**, verify the deductible amount is automatically applied based on the product configuration (e.g., if the product has a 10% deductible, the settlement amount should be 90% of the loss)
- Verify the **reserve amount** set at registration is visible and can be compared against the final settlement amount
- After settlement approval, verify the claim status changes to **Settled** in the Claims Listing
- Verify the workflow tasks appear correctly at each stage — the right role gets the task notification

---

## 14. Collection

**What it is:** Premium collection management — tracks installments, cash/cheque/bank payments, and reconciliation.

**How to demo:**
1. Click **Collection** in the left nav → shows the Payment Collection Listing
2. Search for a policy → click it → shows the installment schedule
3. Click **Collect Payment** → select payment mode (Cash / Cheque / Bank Transfer)
4. Enter amount → click **Save** → payment recorded, receipt generated
5. Show the **CBC (Cash/Bank/Cheque) mapping** — how payments are matched to invoices

**Functional checks:**
- After collecting a payment, verify the installment status changes from **Pending** to **Paid** for that installment
- Verify the **outstanding balance** on the policy reduces by the collected amount
- Try collecting an amount greater than the outstanding balance → verify the system shows a validation error or warning
- Verify the receipt/acknowledgement is generated with the correct amount, date, and payment mode
- Check that the collection appears in the **Dashboard** KPI for collections today

---

## 15. Commission

**What it is:** Calculates and processes broker/agent commissions based on the commission hierarchy and slab rules configured in masters.

**How to demo:**
1. Click **Commission** in the left nav → shows the Commission Process screen
2. Click **New Process** → select Company / COB / LOB / Period
3. Click **Populate** → system fetches all eligible policies for the period
4. Review the commission amounts (calculated from the Commission Master slabs)
5. Click **Process** → commissions are finalised and posted

**Functional checks:**
- After clicking **Populate**, verify the list shows policies from the selected period with commission amounts calculated
- Verify the commission percentage matches the slab configured in the Commission Master for the broker/agent
- For a policy with a known premium (e.g., 10,000), verify the commission amount = premium × commission rate (e.g., 10,000 × 5% = 500)
- After clicking **Process**, verify the commission records are marked as processed and cannot be re-processed for the same period
- Verify the broker/agent can see their commission statement in their profile

---

## 16. Reinsurance (RI Platform)

**What it is:** A separate but integrated platform for managing reinsurance — FAC inward, FAC placement, proportional and XOL treaty setup, RI allocation, and treaty accounting. Accessed via the RI frontend (Next.js app).

**Why last:** RI depends on policies already being issued (for allocation).

**How to demo:**
1. Open the **RI Platform** (separate URL — DEV: `172.23.1.153:3007`)
2. Log in with RI credentials (uses its own login — separate from main GI login)
3. **Masters:** Go to Integration Masters → set up Reinsurer, Product Master, Cover Master
4. **FAC Inward:** Go to Transactions → FAC Inward → + New → fill risk details, coverage, reinsurer share
5. **FAC Placement:** Go to FAC Placement → link to FAC Inward → add placement participants and shares
6. **Treaty Setup:** Go to Setup → Proportional Treaty → create treaty header, add participants, set terms
7. **RI Allocation:** Go to RI Allocation → search for a policy → allocate to treaty → approve
8. **Treaty Accounting:** Go to Treaty Accounting → run batch → generates treaty accounts

**Functional checks:**
- After creating a FAC Inward record, verify the FAC reference number is generated
- In FAC Placement, verify the total placement share across all participants equals 100% — the system should warn if it doesn't
- In RI Allocation, search for a policy that was issued in the GI platform → verify it appears (confirms Kafka sync from GI to RI worked via the RI subscriber)
- After allocating a policy to a treaty, verify the allocated share is deducted from the treaty's available capacity
- After running Treaty Accounting batch, verify the treaty account statement shows the correct premium ceded, commission, and net figures
- Verify that URL query parameters are encrypted in the browser address bar (AES encryption) — this is a security feature of the RI frontend

---

## Summary Cheat Sheet

| Step | Module | Key Action | Output | Key Functional Check |
|---|---|---|---|---|
| 1 | Masters | Create Company, Office, COB, LOB, Tax | Foundation data | New entry appears in list; mandatory field validation works |
| 2 | User Management | Create users, assign roles | Access control | Role-restricted menus visible/hidden on login |
| 3 | Entity Master | Create insured/customer | Entity ID | Due-dupe warning fires on duplicate entry |
| 4 | Product Configurator | Define product, sections, covers | Approved product | Risk form fields render in NB; mandatory covers pre-selected |
| 5 | Rating Engine | Set rate tables, tax, discounts | Premium calculation | Non-zero premium in QQ panel; tax matches configured rate |
| 6 | Rule Engine | Add business rules | Validation logic | Rule action fires when condition is met |
| 7 | Workflow | Build approval stages | Approval process | Task appears in correct role's task list; SignalR notification fires |
| 8 | Quick Quote | Enter risk, calculate premium | Quote reference | Premium recalculates on risk change; QQ number generated |
| 9 | Full Quote | Add proposer, full risk details | Complete proposal | Price Break-Up shows full itemised breakdown with correct totals |
| 10 | Policy Issuance | Approve through workflow | Policy number | Status → PolicyApproved; Policy No populated; Dashboard KPI updates |
| 11 | Endorsement | Amend policy | New policy version | Endorsement No generated; Price Break-Up shows delta vs cumulative |
| 12 | Claims FNOL | Register incident | FNOL reference | FNOL No generated; Dashboard claims KPI increments |
| 13 | Claims Journey | Assess, compute, settle | Settlement payment | Deductible auto-applied; status → Settled after approval |
| 14 | Collection | Collect premium installments | Payment receipt | Installment status → Paid; outstanding balance reduces |
| 15 | Commission | Process broker commissions | Commission statement | Commission amount = premium × rate; period locked after processing |
| 16 | RI Platform | FAC/Treaty setup, allocation | RI accounts | GI policies visible in RI allocation (Kafka sync confirmed) |
