package de.bkb.rackvault.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import de.bkb.rackvault.R;

/**
 * Einstellungs-Activity für Standardwerte (SharedPreferences in DS 4), CSV-Export (DS 6) und DB-Wartung.
 */
public class SettingsActivity extends AppCompatActivity {

    private View rootSettings;
    private TextInputEditText etAdminCode;
    private Spinner spDefaultLocation;
    private SwitchMaterial swWifiOnlyLookup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rootSettings = findViewById(R.id.rootSettings);
        etAdminCode = findViewById(R.id.etAdminCode);
        spDefaultLocation = findViewById(R.id.spDefaultLocation);
        swWifiOnlyLookup = findViewById(R.id.swWifiOnlyLookup);

        MaterialButton btnSaveSettings = findViewById(R.id.btnSaveSettings);
        MaterialButton btnExportCsv = findViewById(R.id.btnExportCsv);
        MaterialButton btnLoadDemoData = findViewById(R.id.btnLoadDemoData);
        MaterialButton btnResetDatabase = findViewById(R.id.btnResetDatabase);

        // Initialwerte für den Prototyp (werden in DS 4 über PrefsManager geladen)
        etAdminCode.setText("ADM");
        swWifiOnlyLookup.setChecked(true);
        if (spDefaultLocation.getCount() > 1) {
            spDefaultLocation.setSelection(1); // "Lager"
        }

        btnSaveSettings.setOnClickListener(v -> saveSettings());
        btnExportCsv.setOnClickListener(v -> Snackbar.make(rootSettings,
                "CSV-Export inkl. Teilen-Option wird in DS 6 implementiert.",
                Snackbar.LENGTH_SHORT).show());
        btnLoadDemoData.setOnClickListener(v -> confirmLoadDemoData());
        btnResetDatabase.setOnClickListener(v -> confirmResetDatabase());
    }

    private void saveSettings() {
        String adminCode = etAdminCode.getText() != null
                ? etAdminCode.getText().toString().trim()
                : "";
        if (TextUtils.isEmpty(adminCode)) {
            etAdminCode.setError("Bitte ein Admin-Kürzel angeben");
            etAdminCode.requestFocus();
            return;
        }
        etAdminCode.setError(null);
        Toast.makeText(this, "Einstellungen gespeichert (DS 4 ergänzt Persistenz)", Toast.LENGTH_SHORT).show();
    }

    private void confirmLoadDemoData() {
        new AlertDialog.Builder(this)
                .setTitle("Demo-Daten laden")
                .setMessage("Sollen die Demo-Geräte neu geladen werden? Aktuelle Teständerungen werden dabei zurückgesetzt.")
                .setPositiveButton("Laden", (dialog, which) -> {
                    MainActivity.resetToDemoData();
                    Snackbar.make(rootSettings, "Demo-Daten geladen.", Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void confirmResetDatabase() {
        new AlertDialog.Builder(this)
                .setTitle("Datenbank zurücksetzen")
                .setMessage("Sollen wirklich alle Geräte und Ausleih-Einträge unwiderruflich gelöscht werden?")
                .setPositiveButton("Alles löschen", (dialog, which) -> {
                    MainActivity.clearAllData();
                    Snackbar.make(rootSettings, "Inventar vollständig geleert.", Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
