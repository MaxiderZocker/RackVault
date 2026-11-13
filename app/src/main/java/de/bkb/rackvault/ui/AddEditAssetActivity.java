package de.bkb.rackvault.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import de.bkb.rackvault.R;
import de.bkb.rackvault.data.PrefsManager;
import de.bkb.rackvault.model.Asset;

/**
 * Formular-Activity zum Anlegen neuer sowie Bearbeiten vorhandener IT-Geräte.
 */
public class AddEditAssetActivity extends AppCompatActivity {

    public static final String EXTRA_ASSET_ID = "extra_asset_id";

    private View rootAddEdit;
    private TextInputEditText etAssetName;
    private TextInputEditText etInventoryNumber;
    private Spinner spCategory;
    private Spinner spLocation;
    private TextInputEditText etMacAddress;
    private TextInputEditText etVendor;
    private SwitchMaterial swDefect;
    private TextInputEditText etNotes;
    private PrefsManager prefsManager;

    private long editingAssetId = -1L;
    private Asset existingAsset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_asset);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefsManager = new PrefsManager(this);

        rootAddEdit = findViewById(R.id.rootAddEdit);
        UiUtils.setupSystemBars(this, rootAddEdit, toolbar);
        etAssetName = findViewById(R.id.etAssetName);
        etInventoryNumber = findViewById(R.id.etInventoryNumber);
        spCategory = findViewById(R.id.spCategory);
        spLocation = findViewById(R.id.spLocation);
        etMacAddress = findViewById(R.id.etMacAddress);
        etVendor = findViewById(R.id.etVendor);
        swDefect = findViewById(R.id.swDefect);
        etNotes = findViewById(R.id.etNotes);

        MaterialButton btnLookupVendor = findViewById(R.id.btnLookupVendor);
        MaterialButton btnSaveAsset = findViewById(R.id.btnSaveAsset);

        editingAssetId = getIntent().getLongExtra(EXTRA_ASSET_ID, -1L);
        if (editingAssetId > 0) {
            existingAsset = MainActivity.findAssetById(editingAssetId);
            if (existingAsset != null) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.title_edit_asset);
                }
                populateForm(existingAsset);
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_add_asset);
            }
            selectSpinnerValue(spLocation, prefsManager.getDefaultLocation());
        }

        btnLookupVendor.setOnClickListener(v -> handleMacLookupClick());
        btnSaveAsset.setOnClickListener(v -> saveForm());
    }

    private void populateForm(Asset asset) {
        etAssetName.setText(asset.getName());
        etInventoryNumber.setText(asset.getInventoryNumber());
        selectSpinnerValue(spCategory, asset.getCategory());
        selectSpinnerValue(spLocation, asset.getLocation());
        etMacAddress.setText(asset.getMacAddress());
        etVendor.setText(asset.getVendor());
        swDefect.setChecked(asset.isDefect());
        etNotes.setText(asset.getNotes());
    }

    @SuppressWarnings("unchecked")
    private void selectSpinnerValue(Spinner spinner, String value) {
        if (value == null || spinner.getAdapter() == null) {
            return;
        }
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i) != null ? adapter.getItem(i).toString() : null)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void handleMacLookupClick() {
        String mac = getTrimmedText(etMacAddress);
        if (TextUtils.isEmpty(mac)) {
            etMacAddress.setError("Bitte zuerst eine MAC-Adresse eingeben");
            etMacAddress.requestFocus();
            return;
        }
        etMacAddress.setError(null);
        String wifiInfo = prefsManager.isWifiOnlyLookup() ? " (nur WLAN aktiv)" : " (WLAN & Mobilfunk)";
        Snackbar.make(rootAddEdit,
                "MAC-Vendor-Lookup über api.macvendors.com wird in DS 7 angebunden" + wifiInfo + ".",
                Snackbar.LENGTH_SHORT).show();
    }

    private void saveForm() {
        String name = getTrimmedText(etAssetName);
        String inventoryNumber = getTrimmedText(etInventoryNumber);
        String macAddress = getTrimmedText(etMacAddress);
        String vendor = getTrimmedText(etVendor);
        String notes = getTrimmedText(etNotes);

        boolean hasError = false;
        if (TextUtils.isEmpty(name)) {
            etAssetName.setError("Gerätename ist ein Pflichtfeld");
            etAssetName.requestFocus();
            hasError = true;
        } else {
            etAssetName.setError(null);
        }

        if (TextUtils.isEmpty(inventoryNumber)) {
            etInventoryNumber.setError("Inventarnummer ist ein Pflichtfeld");
            if (!hasError) {
                etInventoryNumber.requestFocus();
            }
            hasError = true;
        } else {
            etInventoryNumber.setError(null);
        }

        if (hasError) {
            return;
        }

        Asset asset = existingAsset != null ? existingAsset : new Asset();
        asset.setName(name);
        asset.setInventoryNumber(inventoryNumber);
        asset.setCategory(spCategory.getSelectedItem() != null ? spCategory.getSelectedItem().toString() : "Sonstiges");
        asset.setLocation(spLocation.getSelectedItem() != null ? spLocation.getSelectedItem().toString() : "Lager");
        asset.setMacAddress(macAddress);
        asset.setVendor(vendor);
        asset.setNotes(notes);

        boolean defect = swDefect.isChecked();
        asset.setDefect(defect);
        if (defect) {
            asset.setStatus(Asset.STATUS_DEFEKT);
        } else if (!Asset.STATUS_AUSGELIEHEN.equals(asset.getStatus())) {
            asset.setStatus(Asset.STATUS_VERFUEGBAR);
        }

        MainActivity.saveAsset(asset);
        Toast.makeText(this,
                existingAsset != null ? "Gerät aktualisiert" : "Gerät gespeichert",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getTrimmedText(TextInputEditText editText) {
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
