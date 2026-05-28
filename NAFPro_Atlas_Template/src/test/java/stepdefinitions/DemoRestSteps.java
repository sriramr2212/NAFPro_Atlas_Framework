package stepdefinitions;

import io.cucumber.java.en.*;
import novac.ai.defect.DefectContext;
import novac.ai.defect.DefectDraft;
import novac.ai.defect.DefectWriterAssistant;
import novac.ai.rca.*;
import novac.api.core.ApiClient;
import novac.api.core.ApiContext;
import novac.api.core.ApiRequest;
import novac.api.core.ApiResponse;
import novac.reporting.StepDescription;
import novac.reporting.StepReportingWrapper;
import novac.utils.TestContext;
import novac.utils.TestDataManager;
import novac.wrapper.GenericWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class DemoRestSteps {

    private static final Logger logger = LogManager.getLogger(DemoRestSteps.class);

    // ── Request Execution ─────────────────────────────────────────────────────

    @Given("I send a GET request to {string}")
    @StepDescription("Execute GET request")
    public void sendGet(String endpoint) {
        StepReportingWrapper.executeStep("GET " + endpoint, () -> {
            ApiClient.execute(ApiRequest.get(resolveVariables(endpoint)));
        });
    }

    @Given("I send a POST request to {string} with body:")
    @StepDescription("Execute POST request with JSON body")
    public void sendPostWithBody(String endpoint, String body) {
        StepReportingWrapper.executeStep("POST " + endpoint, () -> {
            ApiClient.execute(ApiRequest.post(resolveVariables(endpoint))
                    .body(resolveVariables(body)));
        });
    }

    @Given("I send a PUT request to {string} with body:")
    @StepDescription("Execute PUT request with JSON body")
    public void sendPutWithBody(String endpoint, String body) {
        StepReportingWrapper.executeStep("PUT " + endpoint, () -> {
            ApiClient.execute(ApiRequest.put(resolveVariables(endpoint))
                    .body(resolveVariables(body)));
        });
    }

    @Given("I send a PATCH request to {string} with body:")
    @StepDescription("Execute PATCH request with JSON body")
    public void sendPatchWithBody(String endpoint, String body) {
        StepReportingWrapper.executeStep("PATCH " + endpoint, () -> {
            ApiClient.execute(ApiRequest.patch(resolveVariables(endpoint))
                    .body(resolveVariables(body)));
        });
    }

    @Given("I send a DELETE request to {string}")
    @StepDescription("Execute DELETE request")
    public void sendDelete(String endpoint) {
        StepReportingWrapper.executeStep("DELETE " + endpoint, () -> {
            ApiClient.execute(ApiRequest.delete(resolveVariables(endpoint)));
        });
    }

    // ── TD_ Test Data Integration ─────────────────────────────────────────────

    @Given("I send a POST request to {string} with TD_ body {string}")
    @StepDescription("Execute POST with test data resolved body")
    public void sendPostWithTestData(String endpoint, String fieldName) {
        StepReportingWrapper.executeStep("POST " + endpoint + " with TD_" + fieldName, () -> {
            String module = TestContext.getCurrentModule();
            String tcId = TestContext.getCurrentTestCaseId();
            Map<String, String> data = TestDataManager.get().getData(module, tcId);
            String body = data.get(fieldName);
            if (body == null) {
                throw new RuntimeException("[DemoRestSteps] TD field '" + fieldName +
                        "' not found for " + tcId + " in module " + module);
            }
            ApiClient.execute(ApiRequest.post(resolveVariables(endpoint))
                    .body(resolveVariables(body)));
        });
    }

    // ── Authentication ────────────────────────────────────────────────────────

    @Given("I set the bearer token {string}")
    @StepDescription("Set bearer token for session")
    public void setBearerToken(String token) {
        StepReportingWrapper.executeStep("Set bearer token", () -> {
            ApiContext.setBearerToken(resolveVariables(token));
        });
    }

    @Given("I set the bearer token from saved value {string}")
    @StepDescription("Set bearer token from previously saved value")
    public void setBearerTokenFromSaved(String key) {
        StepReportingWrapper.executeStep("Set bearer token from saved '" + key + "'", () -> {
            String token = ApiContext.getSavedValue(key);
            ApiContext.setBearerToken(token);
        });
    }

    @Given("I clear the bearer token")
    @StepDescription("Clear bearer token from session")
    public void clearBearerToken() {
        StepReportingWrapper.executeStep("Clear bearer token", () -> {
            ApiContext.clearBearerToken();
        });
    }

    @Given("I set request header {string} to {string}")
    @StepDescription("Set session header")
    public void setSessionHeader(String name, String value) {
        StepReportingWrapper.executeStep("Set header " + name, () -> {
            ApiContext.setSessionHeader(name, resolveVariables(value));
        });
    }

    @Given("I set the API base URL to {string}")
    @StepDescription("Override API base URL for this scenario")
    public void setBaseUrl(String url) {
        StepReportingWrapper.executeStep("Set base URL: " + url, () -> {
            ApiContext.setBaseUrl(url);
        });
    }

    // ── Hybrid: Cookie Extraction ─────────────────────────────────────────────

    @Given("I extract session cookies from browser")
    @StepDescription("Extract cookies from browser session for REST API calls")
    public void extractSessionCookies() {
        StepReportingWrapper.executeStep("Extract session cookies from browser", () -> {
            WebDriver driver = GenericWrapper.getDriver();
            if (driver == null) {
                throw new RuntimeException("[DemoRestSteps] WebDriver is null — cannot extract cookies. Ensure browser is initialized (hybrid mode).");
            }
            Set<Cookie> cookies = driver.manage().getCookies();
            if (cookies.isEmpty()) {
                throw new RuntimeException("[DemoRestSteps] No cookies found in browser session. Login may have failed.");
            }
            StringBuilder cookieHeader = new StringBuilder();
            for (Cookie cookie : cookies) {
                if (cookieHeader.length() > 0) cookieHeader.append("; ");
                cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue());
            }
            ApiContext.setSessionHeader("Cookie", cookieHeader.toString());
            logger.info("[REST Hybrid] Extracted {} cookies from browser session", cookies.size());
        });
    }

    // ── Response Assertions ───────────────────────────────────────────────────

    @Then("the response status code should be {int}")
    @StepDescription("Assert response status code")
    public void assertStatusCode(int expected) {
        StepReportingWrapper.executeStep("Assert status " + expected, () -> {
            ApiContext.getCurrentResponse().assertThat().statusCode(expected);
        });
    }

    @Then("the response should be successful")
    @StepDescription("Assert response is 2xx")
    public void assertSuccessful() {
        StepReportingWrapper.executeStep("Assert successful response", () -> {
            ApiContext.getCurrentResponse().assertThat().isSuccessful();
        });
    }

    @Then("the response content type should be JSON")
    @StepDescription("Assert Content-Type is application/json")
    public void assertContentTypeJson() {
        StepReportingWrapper.executeStep("Assert Content-Type JSON", () -> {
            ApiContext.getCurrentResponse().assertThat().contentTypeIsJson();
        });
    }

    @Then("the response body should contain {string}")
    @StepDescription("Assert body contains text")
    public void assertBodyContains(String text) {
        StepReportingWrapper.executeStep("Assert body contains '" + text + "'", () -> {
            ApiContext.getCurrentResponse().assertThat().bodyContains(resolveVariables(text));
        });
    }

    @Then("the response body should not contain {string}")
    @StepDescription("Assert body does not contain text")
    public void assertBodyNotContains(String text) {
        StepReportingWrapper.executeStep("Assert body not contains '" + text + "'", () -> {
            ApiContext.getCurrentResponse().assertThat().bodyNotContains(text);
        });
    }

    @Then("the JSON path {string} should equal {string}")
    @StepDescription("Assert JSON path value equals expected")
    public void assertJsonPathEquals(String path, String expected) {
        StepReportingWrapper.executeStep("Assert $." + path + " == '" + expected + "'", () -> {
            ApiContext.getCurrentResponse().assertThat()
                    .jsonPathEquals(path, resolveVariables(expected));
        });
    }

    @Then("the JSON path {string} should not be null")
    @StepDescription("Assert JSON path value is not null")
    public void assertJsonPathNotNull(String path) {
        StepReportingWrapper.executeStep("Assert $." + path + " not null", () -> {
            ApiContext.getCurrentResponse().assertThat().jsonPathNotNull(path);
        });
    }

    @Then("the JSON path {string} should contain {string}")
    @StepDescription("Assert JSON path value contains substring")
    public void assertJsonPathContains(String path, String substring) {
        StepReportingWrapper.executeStep("Assert $." + path + " contains '" + substring + "'", () -> {
            ApiContext.getCurrentResponse().assertThat().jsonPathContains(path, substring);
        });
    }

    @Then("the JSON array {string} should not be empty")
    @StepDescription("Assert JSON array is not empty")
    public void assertJsonArrayNotEmpty(String path) {
        StepReportingWrapper.executeStep("Assert $." + path + " array not empty", () -> {
            ApiContext.getCurrentResponse().assertThat().jsonArrayNotEmpty(path);
        });
    }

    @Then("the response time should be below {long} milliseconds")
    @StepDescription("Assert response time within threshold")
    public void assertResponseTime(long maxMs) {
        StepReportingWrapper.executeStep("Assert response time < " + maxMs + "ms", () -> {
            ApiContext.getCurrentResponse().assertThat().responseTimeBelow(maxMs);
        });
    }

    @Then("the response should match JSON schema {string}")
    @StepDescription("Validate response against JSON schema")
    public void assertJsonSchema(String schemaFile) {
        StepReportingWrapper.executeStep("Validate schema: " + schemaFile, () -> {
            ApiContext.getCurrentResponse().assertThat().matchesJsonSchema(schemaFile);
        });
    }

    @Then("the response header {string} should equal {string}")
    @StepDescription("Assert response header value")
    public void assertHeader(String name, String expected) {
        StepReportingWrapper.executeStep("Assert header " + name + " == '" + expected + "'", () -> {
            ApiContext.getCurrentResponse().assertThat().headerEquals(name, expected);
        });
    }

    // ── Saved Values ──────────────────────────────────────────────────────────

    @Then("I save the JSON path {string} as {string}")
    @StepDescription("Save JSON path value for later use")
    public void saveJsonPath(String path, String key) {
        StepReportingWrapper.executeStep("Save $." + path + " as '" + key + "'", () -> {
            String value = ApiContext.getCurrentResponse().jsonPathAsString(path);
            if (value == null) {
                throw new RuntimeException(String.format(
                        "[DemoRestSteps] JSON path '%s' returned null — cannot save as '%s'",
                        path, key));
            }
            ApiContext.saveValue(key, value);
            logger.info("[REST] Saved '{}' = '{}' (from $.{})", key, value, path);
        });
    }

    @Then("the saved value {string} should not be empty")
    @StepDescription("Assert saved value is not empty")
    public void assertSavedValueNotEmpty(String key) {
        StepReportingWrapper.executeStep("Assert saved '" + key + "' not empty", () -> {
            String value = ApiContext.getSavedValue(key);
            assertFalse(value.isEmpty(), "Saved value '" + key + "' is empty");
        });
    }

    @Then("the saved value {string} should equal {string}")
    @StepDescription("Assert saved value equals expected")
    public void assertSavedValue(String key, String expected) {
        StepReportingWrapper.executeStep("Assert saved '" + key + "' == '" + expected + "'", () -> {
            String actual = ApiContext.getSavedValue(key);
            assertEquals(expected, actual, "Saved value '" + key + "' mismatch");
        });
    }

    // ── Failure Simulation (RCA + Defect Writer) ──────────────────────────────

    @Given("I trigger a REST assertion failure expecting status {int} on GET {string}")
    @StepDescription("Intentionally trigger status code assertion failure for RCA/Defect validation")
    public void triggerStatusAssertionFailure(int expectedStatus, String endpoint) {
        simulateRestFailureAndGenerateDefect(() -> {
            ApiResponse response = ApiClient.execute(ApiRequest.get(endpoint));
            response.assertThat().statusCode(expectedStatus);
        });
    }

    @Given("I trigger a REST JSON path failure expecting {string} at {string} on GET {string}")
    @StepDescription("Intentionally trigger JSON path assertion failure for RCA/Defect validation")
    public void triggerJsonPathFailure(String expectedValue, String path, String endpoint) {
        simulateRestFailureAndGenerateDefect(() -> {
            ApiResponse response = ApiClient.execute(ApiRequest.get(endpoint));
            response.assertThat().jsonPathEquals(path, expectedValue);
        });
    }

    @Given("I trigger a REST connection failure to {string}")
    @StepDescription("Intentionally trigger connection failure for RCA/Defect validation")
    public void triggerConnectionFailure(String url) {
        simulateRestFailureAndGenerateDefect(() -> {
            ApiClient.execute(ApiRequest.get("/test").baseUrl(url).timeout(3));
        });
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private void simulateRestFailureAndGenerateDefect(Runnable failureAction) {
        Throwable captured = null;
        String stepName = "REST API failure simulation";
        try {
            failureAction.run();
        } catch (Throwable t) {
            captured = t;
        }

        if (captured == null) {
            throw new RuntimeException("Expected REST failure did not occur");
        }

        // Run RCA
        RCAContext context = RCAContext.builder(stepName, captured)
                .moduleName(safeGetModule())
                .testCaseId(safeGetTestCaseId())
                .build();

        RCAAssistant assistant = new RCAAssistant();
        RCAResult rcaResult = assistant.analyze(context);
        TestContext.setLastRCAResult(rcaResult);

        String tcId = safeGetTestCaseId();
        if (tcId != null) {
            novac.reporting.managers.TestRunReportManager.getInstance().storeRCAResult(tcId, rcaResult);
        }

        // Run Defect Writer
        DefectContext defectContext = DefectContext.builder(rcaResult)
                .failedStep(stepName)
                .testCaseId(tcId != null ? tcId : "")
                .moduleName(safeGetModule())
                .exceptionMessage(captured.getMessage())
                .build();

        DefectWriterAssistant writer = new DefectWriterAssistant();
        DefectDraft draft = writer.generate(defectContext);
        TestContext.setLastDefectDraft(draft);

        if (tcId != null) {
            novac.reporting.managers.TestRunReportManager.getInstance().storeDefectDraft(tcId, draft);
        }

        logger.info("REST failure simulated → RCA: {} ({}%) → Defect: {} [{}]",
                rcaResult.getCategory().getDisplayName(),
                rcaResult.getConfidenceScore(),
                draft.getDefectClassification().getDisplayName(),
                draft.getSeverity());

        StepReportingWrapper.recordManualStep(
                String.format("RCA: %s (%d%%) | Defect: %s [%s] | Owner: %s",
                        rcaResult.getCategory().getDisplayName(),
                        rcaResult.getConfidenceScore(),
                        draft.getDefectClassification().getDisplayName(),
                        draft.getSeverity(),
                        draft.getLikelyOwner()),
                "INFO");
    }

    private String resolveVariables(String input) {
        if (input == null) return null;
        String result = input;
        for (Map.Entry<String, String> entry : ApiContext.getAllSavedValues().entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    private String safeGetModule() {
        try { return TestContext.getCurrentModule(); }
        catch (Exception e) { return "REST"; }
    }

    private String safeGetTestCaseId() {
        try { return TestContext.getCurrentTestCaseId(); }
        catch (Exception e) { return null; }
    }
}
