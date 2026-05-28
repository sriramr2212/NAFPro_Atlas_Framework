package runners;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleResolver {

    private static final String FEATURES_DIR = "src/test/resources/features";
    private static final Pattern TAG_PATTERN = Pattern.compile("@TC_([A-Z_0-9]+)_\\d+");

    public static Set<String> resolveModules(String includeTags) {
        if (includeTags == null || includeTags.isEmpty()) {
            return Collections.emptySet();
        }

        List<String> folderNames = listFeatureFolders();
        Set<String> derivedNames = extractDerivedNames(includeTags);
        Set<String> resolved = new LinkedHashSet<>();

        for (String derived : derivedNames) {
            String match = findLongestPrefixMatch(derived, folderNames);
            if (match != null) {
                resolved.add(match);
            } else {
                System.out.println("[WARN] No matching feature folder for derived module: " + derived);
                resolved.add(derived);
            }
        }

        return resolved;
    }

    private static Set<String> extractDerivedNames(String includeTags) {
        Set<String> names = new LinkedHashSet<>();
        Matcher m = TAG_PATTERN.matcher(includeTags);
        while (m.find()) {
            String body = m.group(1); // e.g. PRODUCT_CONFIG_BASIC_INFO
            names.add(toPascalCase(body));
        }
        return names;
    }

    private static String toPascalCase(String underscored) {
        StringBuilder sb = new StringBuilder();
        for (String part : underscored.split("_")) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    private static List<String> listFeatureFolders() {
        List<String> folders = new ArrayList<>();
        File dir = new File(FEATURES_DIR);
        if (dir.isDirectory()) {
            File[] children = dir.listFiles(File::isDirectory);
            if (children != null) {
                for (File child : children) {
                    folders.add(child.getName());
                }
            }
        }
        return folders;
    }

    /**
     * Finds the folder whose name is the longest prefix of the derived name (case-insensitive).
     * Exact matches are returned immediately.
     */
    private static String findLongestPrefixMatch(String derived, List<String> folderNames) {
        String derivedLower = derived.toLowerCase();
        String bestMatch = null;
        int bestLen = 0;

        for (String folder : folderNames) {
            String folderLower = folder.toLowerCase();
            if (derivedLower.equals(folderLower)) {
                return folder; // exact match — return immediately
            }
            if (derivedLower.startsWith(folderLower) && folderLower.length() > bestLen) {
                bestLen = folderLower.length();
                bestMatch = folder;
            }
        }
        return bestMatch;
    }
}
