package de.bkb.rackvault.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.LinkedHashMap;
import java.util.Map;

import de.bkb.rackvault.R;
import de.bkb.rackvault.data.PrefsManager;

/**
 * Untermenü zur Konfiguration des globalen sowie der kategoriespezifischen
 * Formate für fortlaufende Inventarnummern (* als Zähler-Platzhalter).
 */
public class InventoryPatternActivity extends AppCompatActivity {

    private TextInputEditText etInvPatternGlobal;
    private final Map<String, TextInputEditText> categoryPatternFields = new LinkedHashMap<>();
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_pattern);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefsManager = new PrefsManager(this);

        View rootInvPattern = findViewById(R.id.rootInvPattern);
        UiUtils.setupSystemBars(this, rootInvPattern, toolbar);

        etInvPatternGlobal = findViewById(R.id.etInvPatternGlobal);
        categoryPatternFields.put("Switch", findViewById(R.id.etInvPatternSwitch));
        categoryPatternFields.put("Router", findViewById(R.id.etInvPatternRouter));
        categoryPatternFields.put("Access Point", findViewById(R.id.etInvPatternAp));
        categoryPatternFields.put("Laptop", findViewById(R.id.etInvPatternLaptop));
        categoryPatternFields.put("Kabel/Zubehör", findViewById(R.id.etInvPatternCable));
        categoryPatternFields.put("Sonstiges", findViewById(R.id.etInvPatternOther));

        loadPatterns();

        MaterialButton btnSaveInvPatterns = findViewById(R.id.btnSaveInvPatterns);
        btnSaveInvPatterns.setOnClickListener(v -> savePatterns());
    }

    private void loadPatterns() {
        etInvPatternGlobal.setText(prefsManager.getGlobalInventoryPattern());
        for (Map.Entry<String, TextInputEditText> entry : categoryPatternFields.entrySet()) {
            entry.getValue().setText(prefsManager.getCategoryInventoryPattern(entry.getKey()));
        }
    }

    private void savePatterns() {
        String globalPattern = getTrimmedText(etInvPatternGlobal);
        if (!PrefsManager.isValidInventoryPattern(globalPattern)) {
            etInvPatternGlobal.setError("Mindestens ein * als Zähler erforderlich (z. B. IT-***)");
            etInvPatternGlobal.requestFocus();
            return;
        }
        etInvPatternGlobal.setError(null);

        Map<String, String> categoryPatterns = new LinkedHashMap<>();
        for (Map.Entry<String, TextInputEditText> entry : categoryPatternFields.entrySet()) {
            TextInputEditText field = entry.getValue();
            String value = getTrimmedText(field);
            if (!TextUtils.isEmpty(value) && !PrefsManager.isValidInventoryPattern(value)) {
                field.setError("Muster muss mindestens ein * enthalten (z. B. IT-***-SWT)");
                field.requestFocus();
                return;
            }
            field.setError(null);
            categoryPatterns.put(entry.getKey(), value);
        }

        prefsManager.saveInventoryPatterns(globalPattern, categoryPatterns);
        Toast.makeText(this, "Inventarnummer-Formatierung gespeichert", Toast.LENGTH_SHORT).show();
        finish();
    }

    private static String getTrimmedText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
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
