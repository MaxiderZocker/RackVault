package de.bkb.rackvault.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Kapselt den Zugriff auf {@link SharedPreferences} für persistente App-Standardwerte.
 */
public class PrefsManager {

    public static final String PREFS_NAME = "rackvault_prefs";
    public static final String KEY_ADMIN_CODE = "pref_admin_code";
    public static final String KEY_DEFAULT_LOCATION = "pref_default_location";
    public static final String KEY_WIFI_ONLY_LOOKUP = "pref_wifi_only_lookup";

    public static final String DEFAULT_ADMIN_CODE = "ADM";
    public static final String DEFAULT_LOCATION = "Lager";
    public static final boolean DEFAULT_WIFI_ONLY = true;

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
}
