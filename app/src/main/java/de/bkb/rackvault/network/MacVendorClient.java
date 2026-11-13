package de.bkb.rackvault.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Asynchroner HTTP-Client für den MAC-Vendor-Lookup über {@code https://api.macvendors.com/{mac}}.
 */
public final class MacVendorClient {

    private static final String API_BASE_URL = "https://api.macvendors.com/";
    private static final int TIMEOUT_MS = 5000;

    public interface Callback {
        void onSuccess(@NonNull String vendor);

        void onError(@NonNull String errorMessage);
    }

    private MacVendorClient() {
    }

    public static boolean isInternetAvailable(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public static boolean isWifiConnected(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    public static void lookupVendor(@NonNull Context context,
                                    @NonNull String macAddress,
                                    boolean wifiOnly,
                                    @NonNull Callback callback) {
        if (!isInternetAvailable(context)) {
            callback.onError("Keine Internetverbindung – Hersteller bitte manuell eintragen.");
            return;
        }
        if (wifiOnly && !isWifiConnected(context)) {
            callback.onError("MAC-Lookup ist auf WLAN beschränkt (Einstellungen) – aktuell kein WLAN aktiv.");
            return;
        }

        final Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String encodedMac = URLEncoder.encode(macAddress.trim(), "UTF-8");
                URL url = new URL(API_BASE_URL + encodedMac);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setRequestProperty("User-Agent", "RackVault-Android/1.0");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String vendor = readResponseBody(connection);
                    if (!vendor.isEmpty()) {
                        mainHandler.post(() -> callback.onSuccess(vendor));
                    } else {
                        mainHandler.post(() -> callback.onError(
                                "Leere Antwort erhalten – Hersteller bitte manuell eintragen."));
                    }
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    mainHandler.post(() -> callback.onError(
                            "Kein Hersteller zu dieser MAC gefunden – bitte manuell eintragen."));
                } else if (responseCode == 429) {
                    mainHandler.post(() -> callback.onError(
                            "Abfragelimit erreicht – bitte kurz warten oder Hersteller manuell eintragen."));
                } else {
                    mainHandler.post(() -> callback.onError(
                            "MAC-Lookup fehlgeschlagen (HTTP " + responseCode + ") – bitte manuell eintragen."));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError(
                        "MAC-Lookup fehlgeschlagen – Hersteller bitte manuell eintragen."));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private static String readResponseBody(@NonNull HttpURLConnection connection) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString().trim();
    }
}
