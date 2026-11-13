package de.bkb.rackvault.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import de.bkb.rackvault.R;
import de.bkb.rackvault.data.CsvExporter;
import de.bkb.rackvault.data.DatabaseHelper;
import de.bkb.rackvault.data.PrefsManager;

/**
 * Einstellungs-Activity für Standardwerte (SharedPreferences), CSV-Export (DS 6) und DB-Wartung.
 */
public class SettingsActivity extends AppCompatActivity {

    private View rootSettings;
    private TextInputEditText etAdminCode;
    private Spinner spDefaultLocation;
    private SwitchMaterial swWifiOnlyLookup;
    private TextView tvInvPatternSummary;
    private PrefsManager prefsManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefsManager = new PrefsManager(this);
        dbHelper = new DatabaseHelper(this);

        rootSettings = findViewById(R.id.rootSettings);
        UiUtils.setupSystemBars(this, rootSettings, toolbar);
        etAdminCode = findViewById(R.id.etAdminCode);
        spDefaultLocation = findViewById(R.id.spDefaultLocation);
        swWifiOnlyLookup = findViewById(R.id.swWifiOnlyLookup);
        tvInvPatternSummary = findViewById(R.id.tvInvPatternSummary);

        MaterialButton btnSaveSettings = findViewById(R.id.btnSaveSettings);
        MaterialButton btnOpenInvPatternSubmenu = findViewById(R.id.btnOpenInvPatternSubmenu);
        MaterialButton btnOpenLocationsSubmenu = findViewById(R.id.btnOpenLocationsSubmenu);
        MaterialButton btnOpenCategoriesSubmenu = findViewById(R.id.btnOpenCategoriesSubmenu);
        MaterialButton btnExportCsv = findViewById(R.id.btnExportCsv);
        MaterialButton btnLoadDemoData = findViewById(R.id.btnLoadDemoData);
        MaterialButton btnResetDatabase = findViewById(R.id.btnResetDatabase);

        loadCurrentSettings();
        updateInvPatternSummary();

        btnSaveSettings.setOnClickListener(v -> saveSettings());
        btnOpenInvPatternSubmenu.setOnClickListener(v ->
                startActivity(new Intent(this, InventoryPatternActivity.class)));
        btnOpenLocationsSubmenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageListActivity.class);
            intent.putExtra(ManageListActivity.EXTRA_LIST_TYPE, ManageListActivity.TYPE_LOCATIONS);
            startActivity(intent);
        });
        btnOpenCategoriesSubmenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageListActivity.class);
            intent.putExtra(ManageListActivity.EXTRA_LIST_TYPE, ManageListActivity.TYPE_CATEGORIES);
            startActivity(intent);
        });
        btnExportCsv.setOnClickListener(v ->
                CsvExporter.exportAndShowSnackbar(this, rootSettings, dbHelper.getAllAssets()));
        btnLoadDemoData.setOnClickListener(v -> confirmLoadDemoData());
        btnResetDatabase.setOnClickListener(v -> confirmResetDatabase());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLocationSpinner();
        updateInvPatternSummary();
    }

    private void loadCurrentSettings() {
        etAdminCode.setText(prefsManager.getAdminCode());
        swWifiOnlyLookup.setChecked(prefsManager.isWifiOnlyLookup());
        refreshLocationSpinner();
    }

    private void refreshLocationSpinner() {
        List<String> locations = prefsManager.getLocations();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDefaultLocation.setAdapter(adapter);
        selectLocationSpinner(prefsManager.getDefaultLocation());
    }

    private void updateInvPatternSummary() {
        List<String> categories = prefsManager.getCategories();
        int customCount = 0;
        for (String category : categories) {
            if (prefsManager.hasCategoryInventoryPattern(category)) {
                customCount++;
            }
        }
        String summary = "Globales Format: " + prefsManager.getGlobalInventoryPattern()
                + " · Eigene Kategorie-Formate: " + customCount + "/" + categories.size();
        tvInvPatternSummary.setText(summary);
    }

    private void selectLocationSpinner(String location) {
        if (location == null || spDefaultLocation.getAdapter() == null) {
            return;
        }
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spDefaultLocation.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            Object item = adapter.getItem(i);
            if (item != null && location.equals(item.toString())) {
                spDefaultLocation.setSelection(i);
                return;
            }
        }
    }

    private void saveSettings() {
        String adminCode = getTrimmedText(etAdminCode);
        if (TextUtils.isEmpty(adminCode)) {
            etAdminCode.setError("Bitte ein Admin-Kürzel angeben");
            etAdminCode.requestFocus();
            return;
        }
        etAdminCode.setError(null);

        String defaultLocation = spDefaultLocation.getSelectedItem() != null
                ? spDefaultLocation.getSelectedItem().toString()
                : PrefsManager.DEFAULT_LOCATION;
        boolean wifiOnly = swWifiOnlyLookup.isChecked();

        prefsManager.saveSettings(adminCode, defaultLocation, wifiOnly);
        Toast.makeText(this, "Einstellungen dauerhaft gespeichert", Toast.LENGTH_SHORT).show();
    }

    private static String getTrimmedText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void confirmLoadDemoData() {
        new AlertDialog.Builder(this)
                .setTitle("Demo-Daten laden")
                .setMessage("Sollen die Demo-Geräte neu geladen werden? Aktuelle Teständerungen werden dabei zurückgesetzt.")
                .setPositiveButton("Laden", (dialog, which) -> {
                    dbHelper.resetToDemoData();
                    Snackbar.make(rootSettings, "Demo-Daten in SQLite geladen.", Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void confirmResetDatabase() {
        new AlertDialog.Builder(this)
                .setTitle("Datenbank zurücksetzen")
                .setMessage("Sollen wirklich alle Geräte und Ausleih-Einträge unwiderruflich gelöscht werden?")
                .setPositiveButton("Alles löschen", (dialog, which) -> {
                    dbHelper.clearAllData();
                    Snackbar.make(rootSettings, "SQLite-Datenbank vollständig geleert.", Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtils.hideNavigationBar(this);
        }
    }
}
