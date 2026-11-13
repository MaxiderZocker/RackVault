package de.bkb.rackvault.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import de.bkb.rackvault.R;
import de.bkb.rackvault.data.DatabaseHelper;
import de.bkb.rackvault.data.PrefsManager;
import de.bkb.rackvault.data.ProductCatalog;
import de.bkb.rackvault.model.Asset;
import de.bkb.rackvault.network.MacVendorClient;

/**
 * Formular-Activity zum Anlegen neuer sowie Bearbeiten vorhandener IT-Geräte.
 */
public class AddEditAssetActivity extends AppCompatActivity {

    public static final String EXTRA_ASSET_ID = "extra_asset_id";
    private static final Pattern MAC_PATTERN =
            Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");

    private View rootAddEdit;
    private TextInputEditText etAssetName;
    private Spinner spTreeManufacturer;
    private Spinner spTreeCategory;
    private Spinner spTreeSeries;
    private Spinner spTreeModel;
    private View layoutTreeCategory;
    private View layoutTreeSeries;
    private View layoutTreeModel;
    private TextInputEditText etInventoryNumber;
    private Spinner spCategory;
    private Spinner spLocation;
    private TextInputEditText etMacAddress;
    private TextInputEditText etVendor;
    private SwitchMaterial swDefect;
    private TextInputEditText etNotes;
    private MaterialButton btnLookupVendor;
    private ProgressBar pbMacLookup;
    private PrefsManager prefsManager;
    private DatabaseHelper dbHelper;

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
        dbHelper = new DatabaseHelper(this);

        rootAddEdit = findViewById(R.id.rootAddEdit);
        UiUtils.setupSystemBars(this, rootAddEdit, toolbar);
        etAssetName = findViewById(R.id.etAssetName);
        spTreeManufacturer = findViewById(R.id.spTreeManufacturer);
        spTreeCategory = findViewById(R.id.spTreeCategory);
        spTreeSeries = findViewById(R.id.spTreeSeries);
        spTreeModel = findViewById(R.id.spTreeModel);
        layoutTreeCategory = findViewById(R.id.layoutTreeCategory);
        layoutTreeSeries = findViewById(R.id.layoutTreeSeries);
        layoutTreeModel = findViewById(R.id.layoutTreeModel);
        etInventoryNumber = findViewById(R.id.etInventoryNumber);
        spCategory = findViewById(R.id.spCategory);
        spLocation = findViewById(R.id.spLocation);
        etMacAddress = findViewById(R.id.etMacAddress);
        etVendor = findViewById(R.id.etVendor);
        swDefect = findViewById(R.id.swDefect);
        etNotes = findViewById(R.id.etNotes);

        btnLookupVendor = findViewById(R.id.btnLookupVendor);
        pbMacLookup = findViewById(R.id.pbMacLookup);
        MaterialButton btnSaveAsset = findViewById(R.id.btnSaveAsset);

        editingAssetId = getIntent().getLongExtra(EXTRA_ASSET_ID, -1L);
        if (editingAssetId > 0) {
            existingAsset = dbHelper.getAssetById(editingAssetId);
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

        setupProductCatalogTree();
        setupMacAddressFormatter();
        btnLookupVendor.setOnClickListener(v -> handleMacLookupClick());
        btnSaveAsset.setOnClickListener(v -> saveForm());
    }

    /**
     * Richtet die stufenweise aufklappende Produktbaum-Auswahl (Hersteller -> Produktbereich ->
     * Serie -> Gerätemodell) ein. Das Textfeld {@code etAssetName} bleibt parallel jederzeit frei editierbar.
     */
    private void setupProductCatalogTree() {
        List<ProductCatalog.CatalogNode> manufacturers = ProductCatalog.getManufacturers();
        populateTreeSpinner(spTreeManufacturer, getString(R.string.prompt_tree_custom), manufacturers);

        spTreeManufacturer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0) {
                    hideTreeLevels(layoutTreeCategory, layoutTreeSeries, layoutTreeModel);
                    return;
                }
                ProductCatalog.CatalogNode manufacturer = manufacturers.get(position - 1);
                if (TextUtils.isEmpty(getTrimmedText(etVendor)) && manufacturer.getDefaultVendor() != null) {
                    etVendor.setText(manufacturer.getDefaultVendor());
                }
                populateTreeSpinner(spTreeCategory, getString(R.string.prompt_tree_category), manufacturer.getChildren());
                layoutTreeCategory.setVisibility(View.VISIBLE);
                hideTreeLevels(layoutTreeSeries, layoutTreeModel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                hideTreeLevels(layoutTreeCategory, layoutTreeSeries, layoutTreeModel);
            }
        });

        spTreeCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0) {
                    hideTreeLevels(layoutTreeSeries, layoutTreeModel);
                    return;
                }
                ProductCatalog.CatalogNode manufacturer = getSelectedCatalogNode(spTreeManufacturer, manufacturers);
                if (manufacturer == null || position - 1 >= manufacturer.getChildren().size()) {
                    hideTreeLevels(layoutTreeSeries, layoutTreeModel);
                    return;
                }
                ProductCatalog.CatalogNode categoryNode = manufacturer.getChildren().get(position - 1);
                selectSpinnerValue(spCategory, categoryNode.getDefaultCategory());
                populateTreeSpinner(spTreeSeries, getString(R.string.prompt_tree_series), categoryNode.getChildren());
                layoutTreeSeries.setVisibility(View.VISIBLE);
                hideTreeLevels(layoutTreeModel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                hideTreeLevels(layoutTreeSeries, layoutTreeModel);
            }
        });

        spTreeSeries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0) {
                    hideTreeLevels(layoutTreeModel);
                    return;
                }
                ProductCatalog.CatalogNode manufacturer = getSelectedCatalogNode(spTreeManufacturer, manufacturers);
                ProductCatalog.CatalogNode categoryNode = getSelectedCatalogNode(spTreeCategory,
                        manufacturer != null ? manufacturer.getChildren() : Collections.emptyList());
                if (categoryNode == null) {
                    hideTreeLevels(layoutTreeModel);
                    return;
                }
                ProductCatalog.CatalogNode seriesNode = categoryNode.getChildren().get(position - 1);
                populateTreeSpinner(spTreeModel, getString(R.string.prompt_tree_model), seriesNode.getChildren());
                layoutTreeModel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                hideTreeLevels(layoutTreeModel);
            }
        });

        spTreeModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0) {
                    return;
                }
                ProductCatalog.CatalogNode manufacturer = getSelectedCatalogNode(spTreeManufacturer, manufacturers);
                ProductCatalog.CatalogNode categoryNode = getSelectedCatalogNode(spTreeCategory,
                        manufacturer != null ? manufacturer.getChildren() : Collections.emptyList());
                ProductCatalog.CatalogNode seriesNode = getSelectedCatalogNode(spTreeSeries,
                        categoryNode != null ? categoryNode.getChildren() : Collections.emptyList());
                if (seriesNode == null || position - 1 >= seriesNode.getChildren().size()) {
                    return;
                }
                ProductCatalog.CatalogNode modelNode = seriesNode.getChildren().get(position - 1);
                String assetName = modelNode.getFullAssetName() != null
                        ? modelNode.getFullAssetName()
                        : modelNode.getLabel();
                etAssetName.setText(assetName);
                etAssetName.setSelection(assetName.length());
                etAssetName.setError(null);

                if (modelNode.getDefaultVendor() != null) {
                    etVendor.setText(modelNode.getDefaultVendor());
                }
                if (modelNode.getDefaultCategory() != null) {
                    selectSpinnerValue(spCategory, modelNode.getDefaultCategory());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void populateTreeSpinner(Spinner spinner, String placeholder, List<ProductCatalog.CatalogNode> nodes) {
        List<String> items = new ArrayList<>(nodes.size() + 1);
        items.add(placeholder);
        for (ProductCatalog.CatalogNode node : nodes) {
            items.add(node.getLabel());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private ProductCatalog.CatalogNode getSelectedCatalogNode(Spinner spinner, List<ProductCatalog.CatalogNode> nodes) {
        int pos = spinner.getSelectedItemPosition();
        if (pos <= 0 || pos - 1 >= nodes.size()) {
            return null;
        }
        return nodes.get(pos - 1);
    }

    private void hideTreeLevels(View... views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    /**
     * Formatiert die MAC-Adresse während der Eingabe automatisch mit Doppelpunkten
     * (z. B. A -> A2 -> A2:3) und entfernt überflüssige Doppelpunkte beim Löschen automatisch mit.
     */
    private void setupMacAddressFormatter() {
        etMacAddress.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) {
                    return;
                }
                isFormatting = true;

                String current = s.toString();
                String formattedStr = formatMacInput(current);
                if (!current.equals(formattedStr)) {
                    s.replace(0, s.length(), formattedStr);
                    etMacAddress.setSelection(Math.min(formattedStr.length(), s.length()));
                }

                isFormatting = false;
            }
        });
    }

    /**
     * Bereinigt eine beliebige MAC-Eingabe auf bis zu 12 Großbuchstaben-Hex-Zeichen
     * und fügt nach jedem zweiten Zeichen automatisch einen Doppelpunkt ein.
     */
    public static String formatMacInput(String current) {
        if (current == null || current.isEmpty()) {
            return "";
        }
        String rawHex = current.replaceAll("[^0-9A-Fa-f]", "").toUpperCase(Locale.ROOT);
        if (rawHex.length() > 12) {
            rawHex = rawHex.substring(0, 12);
        }

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < rawHex.length(); i++) {
            if (i > 0 && i % 2 == 0) {
                formatted.append(':');
            }
            formatted.append(rawHex.charAt(i));
        }
        return formatted.toString();
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
        if (!isValidMacAddress(mac)) {
            etMacAddress.setError("Ungültiges MAC-Format (z. B. 00:1A:2B:3C:4D:5E)");
            etMacAddress.requestFocus();
            return;
        }
        etMacAddress.setError(null);

        String normalizedMac = mac.toUpperCase(Locale.ROOT).replace('-', ':');
        etMacAddress.setText(normalizedMac);

        pbMacLookup.setVisibility(View.VISIBLE);
        btnLookupVendor.setEnabled(false);

        MacVendorClient.lookupVendor(this, normalizedMac, prefsManager.isWifiOnlyLookup(),
                new MacVendorClient.Callback() {
                    @Override
                    public void onSuccess(@NonNull String vendor) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        pbMacLookup.setVisibility(View.GONE);
                        btnLookupVendor.setEnabled(true);
                        etVendor.setText(vendor);
                        Snackbar.make(rootAddEdit, "Hersteller erkannt: " + vendor, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        pbMacLookup.setVisibility(View.GONE);
                        btnLookupVendor.setEnabled(true);
                        Snackbar.make(rootAddEdit, errorMessage, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void saveForm() {
        String name = getTrimmedText(etAssetName);
        String selectedCategory = spCategory.getSelectedItem() != null
                ? spCategory.getSelectedItem().toString()
                : "Sonstiges";
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
            inventoryNumber = dbHelper.generateNextInventoryNumber(prefsManager, selectedCategory, editingAssetId);
            etInventoryNumber.setText(inventoryNumber);
            etInventoryNumber.setError(null);
        } else if (dbHelper.isInventoryNumberTaken(inventoryNumber, editingAssetId)) {
            etInventoryNumber.setError("Inventarnummer ist bereits vergeben");
            if (!hasError) {
                etInventoryNumber.requestFocus();
            }
            hasError = true;
        } else {
            etInventoryNumber.setError(null);
        }

        if (!TextUtils.isEmpty(macAddress) && !isValidMacAddress(macAddress)) {
            etMacAddress.setError("Ungültiges MAC-Format (z. B. 00:1A:2B:3C:4D:5E)");
            if (!hasError) {
                etMacAddress.requestFocus();
            }
            hasError = true;
        } else {
            etMacAddress.setError(null);
        }

        if (hasError) {
            return;
        }

        String normalizedMac = !TextUtils.isEmpty(macAddress)
                ? macAddress.toUpperCase(Locale.ROOT).replace('-', ':')
                : "";

        Asset asset = existingAsset != null ? existingAsset : new Asset();
        asset.setName(name);
        asset.setInventoryNumber(inventoryNumber);
        asset.setCategory(selectedCategory);
        asset.setLocation(spLocation.getSelectedItem() != null ? spLocation.getSelectedItem().toString() : "Lager");
        asset.setMacAddress(normalizedMac);
        asset.setVendor(vendor);
        asset.setNotes(notes);

        boolean defect = swDefect.isChecked();
        asset.setDefect(defect);
        if (defect) {
            asset.setStatus(Asset.STATUS_DEFEKT);
        } else if (!Asset.STATUS_AUSGELIEHEN.equals(asset.getStatus())) {
            asset.setStatus(Asset.STATUS_VERFUEGBAR);
        }

        dbHelper.saveAsset(asset, prefsManager.getAdminCode());
        Toast.makeText(this,
                existingAsset != null ? "Gerät aktualisiert" : "Gerät gespeichert",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    public static boolean isValidMacAddress(String mac) {
        return mac != null && MAC_PATTERN.matcher(mac).matches();
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
