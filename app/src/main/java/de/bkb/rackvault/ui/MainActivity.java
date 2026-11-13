package de.bkb.rackvault.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import de.bkb.rackvault.R;
import de.bkb.rackvault.data.CsvExporter;
import de.bkb.rackvault.data.DatabaseHelper;
import de.bkb.rackvault.model.Asset;

/**
 * Startbildschirm der App: Inventar-Übersicht mit SQLite-Filterung, Kontextmenü, Optionsmenü und Navigation.
 */
public class MainActivity extends AppCompatActivity {

    private static final int CTX_MENU_DETAILS = 1;
    private static final int CTX_MENU_EDIT = 2;
    private static final int CTX_MENU_DELETE = 3;

    private View rootMain;
    private Spinner spFilterLocation;
    private Spinner spFilterStatus;
    private AssetListAdapter assetAdapter;
    private final List<Asset> displayedAssets = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rootMain = findViewById(R.id.rootMain);
        UiUtils.setupSystemBars(this, rootMain, toolbar);

        spFilterLocation = findViewById(R.id.spFilterLocation);
        spFilterStatus = findViewById(R.id.spFilterStatus);
        ListView lvAssets = findViewById(R.id.lvAssets);
        TextView tvEmptyAssets = findViewById(R.id.tvEmptyAssets);
        FloatingActionButton fabAddAsset = findViewById(R.id.fabAddAsset);

        lvAssets.setEmptyView(tvEmptyAssets);
        assetAdapter = new AssetListAdapter(this, displayedAssets);
        lvAssets.setAdapter(assetAdapter);
        registerForContextMenu(lvAssets);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshAssetList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                refreshAssetList();
            }
        };
        spFilterLocation.setOnItemSelectedListener(filterListener);
        spFilterStatus.setOnItemSelectedListener(filterListener);

        lvAssets.setOnItemClickListener((parent, view, position, id) -> {
            Asset selected = assetAdapter.getItem(position);
            if (selected != null) {
                openAssetDetail(selected.getId());
            }
        });

        fabAddAsset.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditAssetActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAssetList();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtils.hideNavigationBar(this);
        }
    }

    private void refreshAssetList() {
        String selectedLocation = spFilterLocation.getSelectedItem() != null
                ? spFilterLocation.getSelectedItem().toString()
                : "Alle";
        String selectedStatus = spFilterStatus.getSelectedItem() != null
                ? spFilterStatus.getSelectedItem().toString()
                : "Alle";

        displayedAssets.clear();
        displayedAssets.addAll(dbHelper.getFilteredAssets(selectedLocation, selectedStatus));
        assetAdapter.notifyDataSetChanged();
    }

    private void openAssetDetail(long assetId) {
        Intent intent = new Intent(this, AssetDetailActivity.class);
        intent.putExtra(AssetDetailActivity.EXTRA_ASSET_ID, assetId);
        startActivity(intent);
    }

    private void openEditAsset(long assetId) {
        Intent intent = new Intent(this, AddEditAssetActivity.class);
        intent.putExtra(AddEditAssetActivity.EXTRA_ASSET_ID, assetId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export_csv) {
            CsvExporter.exportAndShowSnackbar(this, rootMain, dbHelper.getAllAssets());
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_info) {
            showInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.lvAssets) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Asset asset = assetAdapter.getItem(info.position);
            if (asset != null) {
                menu.setHeaderTitle(asset.getName());
            }
            menu.add(Menu.NONE, CTX_MENU_DETAILS, 1, "Details & Ausleihe");
            menu.add(Menu.NONE, CTX_MENU_EDIT, 2, "Bearbeiten");
            menu.add(Menu.NONE, CTX_MENU_DELETE, 3, "Löschen");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) {
            return super.onContextItemSelected(item);
        }
        Asset asset = assetAdapter.getItem(info.position);
        if (asset == null) {
            return super.onContextItemSelected(item);
        }

        int itemId = item.getItemId();
        if (itemId == CTX_MENU_DETAILS) {
            openAssetDetail(asset.getId());
            return true;
        }
        if (itemId == CTX_MENU_EDIT) {
            openEditAsset(asset.getId());
            return true;
        }
        if (itemId == CTX_MENU_DELETE) {
            confirmDeleteAsset(asset);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void confirmDeleteAsset(@NonNull Asset asset) {
        new AlertDialog.Builder(this)
                .setTitle("Gerät löschen")
                .setMessage("Soll \"" + asset.getName() + "\" (" + asset.getInventoryNumber() + ") wirklich aus dem Inventar entfernt werden?")
                .setPositiveButton("Löschen", (dialog, which) -> {
                    dbHelper.deleteAsset(asset.getId());
                    refreshAssetList();
                    Snackbar.make(rootMain, "Gerät gelöscht: " + asset.getName(), Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Über RackVault")
                .setMessage("RackVault v1.0\nMobile IT-Inventar- & Hardware-Ausleihverwaltung\n\nProjekt im Fach SWDM DT31 · Berufskolleg Beckum")
                .setPositiveButton("OK", null)
                .show();
    }
}
