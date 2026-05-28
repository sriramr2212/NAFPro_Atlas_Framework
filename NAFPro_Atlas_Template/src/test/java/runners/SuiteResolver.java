package runners;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;

public class SuiteResolver {

    private static final String CONFIG_FILE = "RunManager.json";

    /**
     * Reads RunManager.json. If selectedSuite is set and found in testSuites,
     * returns the corresponding tag expression. Otherwise returns null.
     */
    public static String resolveSelectedSuiteTags() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (!root.has("selectedSuite") || !root.has("testSuites")) {
                return null;
            }

            String selectedSuite = root.get("selectedSuite").getAsString().trim();
            if (selectedSuite.isEmpty()) {
                return null;
            }

            JsonObject suites = root.getAsJsonObject("testSuites");
            JsonElement suiteElement = suites.get(selectedSuite);

            if (suiteElement == null) {
                System.err.println("[WARN] Suite '" + selectedSuite + "' not found in testSuites. Available: " + suites.keySet());
                return null;
            }

            String tags = suiteElement.getAsString().trim();
            System.out.println("[SUITE] Resolved suite '" + selectedSuite + "' -> " + tags);
            return tags;

        } catch (Exception e) {
            System.err.println("[WARN] Could not resolve suite: " + e.getMessage());
            return null;
        }
    }
}
