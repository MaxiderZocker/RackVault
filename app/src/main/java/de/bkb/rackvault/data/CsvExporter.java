package de.bkb.rackvault.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.bkb.rackvault.model.Asset;

/**
 * Erzeugt UTF-8-CSV-Inventurberichte im Gerätespeicher (Downloads &amp; App-Export-Ordner)
 * und stellt einen direkten Share-Intent über {@link FileProvider} bereit.
 */
public final class CsvExporter {

    private static final String CSV_HEADER =
            "ID;Geraetename;Inventarnummer;Kategorie;Standort;MAC_Adresse;Hersteller;Status;Defekt;Ausgeliehen_An;Ausgeliehen_Seit;Notizen";

    private CsvExporter() {
    }

    /**
     * Exportiert alle übergebenen Geräte als CSV und zeigt eine Snackbar mit Pfad und Teilen-Aktion.
     */
    public static void exportAndShowSnackbar(@NonNull Context context,
                                             @NonNull View rootView,
                                             @NonNull List<Asset> assets) {
        if (assets.isEmpty()) {
            Snackbar.make(rootView, "Keine Geräte für den CSV-Export vorhanden.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(new Date());
        String fileName = "rackvault_export_" + timestamp + ".csv";
        String csvContent = buildCsvContent(assets);
        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        try {
            File shareFile = writeToAppExportDir(context, fileName, csvBytes);
            boolean savedToDownloads = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savedToDownloads = writeToPublicDownloads(context, fileName, csvBytes);
            }

            String displayPath = savedToDownloads
                    ? "Downloads/" + fileName
                    : shareFile.getAbsolutePath();

            Snackbar.make(rootView, "CSV gespeichert: " + displayPath, Snackbar.LENGTH_LONG)
                    .setAction("Teilen", v -> shareCsvFile(context, shareFile))
                    .show();
        } catch (IOException e) {
            Snackbar.make(rootView, "CSV-Export fehlgeschlagen: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private static String buildCsvContent(@NonNull List<Asset> assets) {
        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM für korrekte Umlaut-Darstellung in Microsoft Excel
        sb.append('\uFEFF');
        sb.append(CSV_HEADER).append("\r\n");

        for (Asset asset : assets) {
            sb.append(asset.getId()).append(';')
                    .append(escapeCsv(asset.getName())).append(';')
                    .append(escapeCsv(asset.getInventoryNumber())).append(';')
                    .append(escapeCsv(asset.getCategory())).append(';')
                    .append(escapeCsv(asset.getLocation())).append(';')
                    .append(escapeCsv(asset.getMacAddress())).append(';')
                    .append(escapeCsv(asset.getVendor())).append(';')
                    .append(escapeCsv(asset.getStatus())).append(';')
                    .append(asset.isDefect() ? "Ja" : "Nein").append(';')
                    .append(escapeCsv(asset.getBorrowedBy())).append(';')
                    .append(escapeCsv(asset.getBorrowedSince())).append(';')
                    .append(escapeCsv(asset.getNotes()))
                    .append("\r\n");
        }
        return sb.toString();
    }

    private static String escapeCsv(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        boolean needsQuotes = value.contains(";") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }

    private static File writeToAppExportDir(@NonNull Context context,
                                            @NonNull String fileName,
                                            @NonNull byte[] csvBytes) throws IOException {
        File exportDir = context.getExternalFilesDir(null);
        if (exportDir == null) {
            exportDir = context.getCacheDir();
        }
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            throw new IOException("Export-Verzeichnis konnte nicht erstellt werden.");
        }

        File outFile = new File(exportDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(csvBytes);
            fos.flush();
        }
        return outFile;
    }

    private static boolean writeToPublicDownloads(@NonNull Context context,
                                                  @NonNull String fileName,
                                                  @NonNull byte[] csvBytes) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            return false;
        }

        try (OutputStream os = resolver.openOutputStream(uri)) {
            if (os == null) {
                return false;
            }
            os.write(csvBytes);
            os.flush();
            return true;
        } catch (IOException e) {
            resolver.delete(uri, null, null);
            return false;
        }
    }

    /**
     * Öffnet den Android-Teilen-Dialog für die erzeugte CSV-Datei via {@link FileProvider}.
     */
    public static void shareCsvFile(@NonNull Context context, @NonNull File csvFile) {
        try {
            String authority = context.getPackageName() + ".fileprovider";
            Uri contentUri = FileProvider.getUriForFile(context, authority, csvFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "RackVault Inventur-Export (" + csvFile.getName() + ")");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "CSV-Inventurbericht teilen"));
        } catch (Exception e) {
            Toast.makeText(context, "Teilen fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
