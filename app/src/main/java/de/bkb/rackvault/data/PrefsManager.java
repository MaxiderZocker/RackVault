package de.bkb.rackvault.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kapselt den Zugriff auf {@link SharedPreferences} für persistente App-Standardwerte
 * und die Konfiguration der fortlaufenden Inventarnummern.
 */
public class PrefsManager {

    public static final String PREFS_NAME = "rackvault_prefs";
    public static final String KEY_ADMIN_CODE = "pref_admin_code";
    public static final String KEY_DEFAULT_LOCATION = "pref_default_location";
    public static final String KEY_WIFI_ONLY_LOOKUP = "pref_wifi_only_lookup";
    public static final String KEY_INV_PATTERN_GLOBAL = "pref_inv_pattern_global";
    public static final String KEY_INV_PATTERN_CATEGORY_PREFIX = "pref_inv_pattern_cat_";

    public static final String DEFAULT_ADMIN_CODE = "ADM";
    public static final String DEFAULT_LOCATION = "Lager";
    public static final boolean DEFAULT_WIFI_ONLY = true;
    public static final String DEFAULT_INV_PATTERN_GLOBAL = "IT-***";

    private static final Pattern ASTERISK_BLOCK_PATTERN = Pattern.compile("\\*+");

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getAdminCode() {
        String value = prefs.getString(KEY_ADMIN_CODE, DEFAULT_ADMIN_CODE);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : DEFAULT_ADMIN_CODE;
    }

    public String getDefaultLocation() {
        String value = prefs.getString(KEY_DEFAULT_LOCATION, DEFAULT_LOCATION);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : DEFAULT_LOCATION;
    }

    public boolean isWifiOnlyLookup() {
        return prefs.getBoolean(KEY_WIFI_ONLY_LOOKUP, DEFAULT_WIFI_ONLY);
    }

    public void saveSettings(String adminCode, String defaultLocation, boolean wifiOnlyLookup) {
        String sanitizedAdmin = (adminCode != null && !adminCode.trim().isEmpty())
                ? adminCode.trim()
                : DEFAULT_ADMIN_CODE;
        String sanitizedLocation = (defaultLocation != null && !defaultLocation.trim().isEmpty())
                ? defaultLocation.trim()
                : DEFAULT_LOCATION;

        prefs.edit()
                .putString(KEY_ADMIN_CODE, sanitizedAdmin)
                .putString(KEY_DEFAULT_LOCATION, sanitizedLocation)
                .putBoolean(KEY_WIFI_ONLY_LOOKUP, wifiOnlyLookup)
                .apply();
    }

    public String getGlobalInventoryPattern() {
        String value = prefs.getString(KEY_INV_PATTERN_GLOBAL, DEFAULT_INV_PATTERN_GLOBAL);
        return (value != null && !value.trim().isEmpty())
                ? value.trim()
                : DEFAULT_INV_PATTERN_GLOBAL;
    }

    public String getCategoryInventoryPattern(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "";
        }
        String value = prefs.getString(KEY_INV_PATTERN_CATEGORY_PREFIX + category.trim(), "");
        return value != null ? value.trim() : "";
    }

    public boolean hasCategoryInventoryPattern(String category) {
        return !getCategoryInventoryPattern(category).isEmpty();
    }

    public String getEffectiveInventoryPattern(String category) {
        String categoryPattern = getCategoryInventoryPattern(category);
        return !categoryPattern.isEmpty() ? categoryPattern : getGlobalInventoryPattern();
    }

    public void saveInventoryPatterns(String globalPattern, Map<String, String> categoryPatterns) {
        String sanitizedGlobal = isValidInventoryPattern(globalPattern)
                ? globalPattern.trim()
                : DEFAULT_INV_PATTERN_GLOBAL;

        SharedPreferences.Editor editor = prefs.edit()
                .putString(KEY_INV_PATTERN_GLOBAL, sanitizedGlobal);

        if (categoryPatterns != null) {
            for (Map.Entry<String, String> entry : categoryPatterns.entrySet()) {
                String category = entry.getKey();
                if (category == null || category.trim().isEmpty()) {
                    continue;
                }
                String key = KEY_INV_PATTERN_CATEGORY_PREFIX + category.trim();
                String value = entry.getValue() != null ? entry.getValue().trim() : "";
                if (value.isEmpty()) {
                    editor.remove(key);
                } else {
                    editor.putString(key, value);
                }
            }
        }
        editor.apply();
    }

    public static boolean isValidInventoryPattern(String pattern) {
        return pattern != null && !pattern.trim().isEmpty() && pattern.contains("*");
    }

    /**
     * Ersetzt den ersten zusammenhängenden Block aus {@code *}-Zeichen durch {@code sequenceNumber}
     * mit führenden Nullen entsprechend der Platzhalterlänge.
     */
    public static String formatInventoryNumber(String pattern, int sequenceNumber) {
        String effectivePattern = isValidInventoryPattern(pattern)
                ? pattern.trim()
                : DEFAULT_INV_PATTERN_GLOBAL;
        int safeSeq = Math.max(1, sequenceNumber);

        Matcher matcher = ASTERISK_BLOCK_PATTERN.matcher(effectivePattern);
        if (!matcher.find()) {
            return effectivePattern + "-" + safeSeq;
        }

        int width = matcher.end() - matcher.start();
        String formattedNumber = String.format(Locale.ROOT, "%0" + width + "d", safeSeq);
        return effectivePattern.substring(0, matcher.start())
                + formattedNumber
                + effectivePattern.substring(matcher.end());
    }

    /**
     * Extrahiert die laufende Zählernummer aus einer vorhandenen Inventarnummer, sofern sie
     * exakt dem angegebenen Muster (Präfix vor {@code *+} und Suffix nach {@code *+}) entspricht.
     */
    public static int extractSequenceNumber(String pattern, String inventoryNumber) {
        if (!isValidInventoryPattern(pattern) || inventoryNumber == null || inventoryNumber.trim().isEmpty()) {
            return -1;
        }
        Matcher asteriskMatcher = ASTERISK_BLOCK_PATTERN.matcher(pattern.trim());
        if (!asteriskMatcher.find()) {
            return -1;
        }

        String prefix = pattern.trim().substring(0, asteriskMatcher.start());
        String suffix = pattern.trim().substring(asteriskMatcher.end());
        Pattern matchPattern = Pattern.compile(
                "^" + Pattern.quote(prefix) + "(\\d+)" + Pattern.quote(suffix) + "$",
                Pattern.CASE_INSENSITIVE
        );
        Matcher numberMatcher = matchPattern.matcher(inventoryNumber.trim());
        if (!numberMatcher.matches()) {
            return -1;
        }
        try {
            return Integer.parseInt(numberMatcher.group(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
