package novac.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Convention-based page and module name resolution.
 * Eliminates hardcoded page-name switches — any new module works automatically.
 *
 * Conventions:
 *   "Login"       → "LoginPage"     → looks up LoginConstants via ConstantsResolver
 *   "LoginPage"   → "LoginPage"
 *   "login"       → "LoginPage"
 *   "login page"  → "LoginPage"
 *   "ProductConfig" → "ProductConfigPage"
 */
public class PageResolver {

    private static final Logger logger = LogManager.getLogger(PageResolver.class);

    public static String resolve(String page) {
        if (page == null || page.trim().isEmpty()) {
            String module = TestContext.getCurrentModule();
            if (module != null && !module.isEmpty()) {
                return capitalize(module) + "Page";
            }
            throw new IllegalArgumentException("Page name cannot be null or empty and no current module is set");
        }

        String normalized = page.trim().replace(" ", "");

        if (normalized.toLowerCase().endsWith("page")) {
            String result = capitalize(normalized);
            logger.debug("Page resolved: '{}' → '{}'", page, result);
            return result;
        }

        String result = capitalize(normalized) + "Page";
        logger.debug("Page resolved: '{}' → '{}'", page, result);
        return result;
    }

    public static String getModuleName(String pageName) {
        if (pageName == null || pageName.trim().isEmpty()) {
            String module = TestContext.getCurrentModule();
            if (module != null && !module.isEmpty()) {
                return module;
            }
            throw new IllegalArgumentException("Page name cannot be null or empty for module resolution");
        }

        String name = pageName.trim();
        if (name.endsWith("Page") && name.length() > 4) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }

    private static String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
}
