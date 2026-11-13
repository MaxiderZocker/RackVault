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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import de.bkb.rackvault.R;
import de.bkb.rackvault.model.Asset;
import de.bkb.rackvault.model.LoanLog;

/**
 * Startbildschirm der App: Inventar-Übersicht mit Filterung, Kontextmenü, Optionsmenü und Navigation.
 * Enthält für DS 3 einen In-Memory-Demo-Bestand, der in DS 5 durch SQLite ({@code DatabaseHelper}) ersetzt wird.
 */
public class MainActivity extends AppCompatActivity {

    private static final int CTX_MENU_DETAILS = 1;
    private static final int CTX_MENU_EDIT = 2;
    private static final int CTX_MENU_DELETE = 3;

    // --- Temporärer In-Memory-Demo-Speicher für den klickbaren Prototyp (DS 3) ---
    private static final List<Asset> DEMO_ASSETS = new ArrayList<>();
    private static final List<LoanLog> DEMO_LOGS = new ArrayList<>();
    private static long nextAssetId = 1;
    private static long nextLogId = 1;
    private static boolean initialized = false;

    private View rootMain;
    private Spinner spFilterLocation;
    private Spinner spFilterStatus;
    private ListView lvAssets;
    private AssetListAdapter assetAdapter;
    private final List<Asset> displayedAssets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ensureDemoDataInitialized();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rootMain = findViewById(R.id.rootMain);
        UiUtils.setupSystemBars(this, rootMain, toolbar);
        spFilterLocation = findViewById(R.id.spFilterLocation);
        spFilterStatus = findViewById(R.id.spFilterStatus);
        lvAssets = findViewById(R.id.lvAssets);
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
        displayedAssets.addAll(getFilteredAssets(selectedLocation, selectedStatus));
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
            Snackbar.make(rootMain,
                    "CSV-Export wird in DS 6 vollständig angebunden (Vorschau: " + DEMO_ASSETS.size() + " Geräte).",
                    Snackbar.LENGTH_LONG).show();
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
                    deleteAsset(asset.getId());
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

    // --- Statische Demo-Datenzugriffe für DS 3 (werden in DS 5 durch DatabaseHelper ersetzt) ---

    public static synchronized void ensureDemoDataInitialized() {
        if (!initialized) {
            resetToDemoData();
            initialized = true;
        }
    }

    public static synchronized void resetToDemoData() {
        DEMO_ASSETS.clear();
        DEMO_LOGS.clear();
        nextAssetId = 1;
        nextLogId = 1;

        Asset sw1 = new Asset(nextAssetId++, "HP ProCurve 2530-24G", "IT-SW-014", "Switch", "Serverraum",
                "00:1A:2B:3C:4D:5E", "Hewlett Packard Enterprise", Asset.STATUS_VERFUEGBAR, false,
                null, null, "Rack 2, Höheneinheit 14");
        Asset nb1 = new Asset(nextAssetId++, "Dell Latitude 5430", "IT-NB-008", "Laptop", "Büro IT",
                "A4:BB:6D:12:34:56", "Dell Inc.", Asset.STATUS_AUSGELIEHEN, false,
                "M. Müller", "2026-11-11 08:30", "Inkl. 65W USB-C Netzteil");
        Asset ap1 = new Asset(nextAssetId++, "Ubiquiti UniFi AP AC Pro", "IT-AP-003", "Access Point", "Lager",
                "74:83:C2:AA:BB:CC", "Ubiquiti Inc.", Asset.STATUS_DEFEKT, true,
                null, null, "PoE-Port ohne Funktion");
        Asset rt1 = new Asset(nextAssetId++, "MikroTik hEX RB750Gr3", "IT-RT-005", "Router", "Schulungsraum",
                "4C:5E:0C:11:22:33", "MikroTik", Asset.STATUS_VERFUEGBAR, false,
                null, null, "Testlabor VLAN-Übung");

        DEMO_ASSETS.add(sw1);
        DEMO_ASSETS.add(nb1);
        DEMO_ASSETS.add(ap1);
        DEMO_ASSETS.add(rt1);

        DEMO_LOGS.add(new LoanLog(nextLogId++, nb1.getId(), LoanLog.ACTION_AUSLEIHE,
                "M. Müller", "ADM", "2026-11-11 08:30", "Für Raum B104 Aufbau"));
        DEMO_LOGS.add(new LoanLog(nextLogId++, sw1.getId(), LoanLog.ACTION_RUECKGABE,
                "T. Weber", "ADM", "2026-11-10 15:45", "Nach Wartungsfenster zurück im Rack"));
        DEMO_LOGS.add(new LoanLog(nextLogId++, sw1.getId(), LoanLog.ACTION_AUSLEIHE,
                "T. Weber", "ADM", "2026-11-09 09:00", "Firmware-Test"));
        DEMO_LOGS.add(new LoanLog(nextLogId++, ap1.getId(), LoanLog.ACTION_DEFEKT_MELDUNG,
                "—", "ADM", "2026-11-08 11:20", "PoE-Port reagiert nicht mehr"));
    }

    public static synchronized void clearAllData() {
        DEMO_ASSETS.clear();
        DEMO_LOGS.clear();
        initialized = true;
    }

    public static synchronized List<Asset> getFilteredAssets(String locationFilter, String statusFilter) {
        ensureDemoDataInitialized();
        List<Asset> result = new ArrayList<>();
        String mappedStatus = mapFilterStatusToKey(statusFilter);
        for (Asset asset : DEMO_ASSETS) {
            boolean matchesLocation = locationFilter == null || "Alle".equals(locationFilter)
                    || locationFilter.equals(asset.getLocation());
            boolean matchesStatus = mappedStatus == null || mappedStatus.equals(asset.getStatus());
            if (matchesLocation && matchesStatus) {
                result.add(asset);
            }
        }
        return result;
    }

    private static String mapFilterStatusToKey(String statusFilter) {
        if ("Verfügbar".equals(statusFilter)) {
            return Asset.STATUS_VERFUEGBAR;
        }
        if ("Ausgeliehen".equals(statusFilter)) {
            return Asset.STATUS_AUSGELIEHEN;
        }
        if ("Defekt".equals(statusFilter)) {
            return Asset.STATUS_DEFEKT;
        }
        return null;
    }

    public static synchronized Asset findAssetById(long assetId) {
        ensureDemoDataInitialized();
        for (Asset asset : DEMO_ASSETS) {
            if (asset.getId() == assetId) {
                return asset;
            }
        }
        return null;
    }

    public static synchronized void saveAsset(Asset asset) {
        ensureDemoDataInitialized();
        if (asset.getId() <= 0) {
            asset.setId(nextAssetId++);
            DEMO_ASSETS.add(0, asset);
            return;
        }
        for (int i = 0; i < DEMO_ASSETS.size(); i++) {
            if (DEMO_ASSETS.get(i).getId() == asset.getId()) {
                DEMO_ASSETS.set(i, asset);
                return;
            }
        }
        DEMO_ASSETS.add(0, asset);
    }

    public static synchronized void deleteAsset(long assetId) {
        ensureDemoDataInitialized();
        Iterator<Asset> assetIt = DEMO_ASSETS.iterator();
        while (assetIt.hasNext()) {
            if (assetIt.next().getId() == assetId) {
                assetIt.remove();
            }
        }
        Iterator<LoanLog> logIt = DEMO_LOGS.iterator();
        while (logIt.hasNext()) {
            if (logIt.next().getAssetId() == assetId) {
                logIt.remove();
            }
        }
    }

    public static synchronized List<LoanLog> getLoanLogsForAsset(long assetId) {
        ensureDemoDataInitialized();
        List<LoanLog> result = new ArrayList<>();
        for (LoanLog log : DEMO_LOGS) {
            if (log.getAssetId() == assetId) {
                result.add(log);
            }
        }
        return result;
    }

    public static synchronized void borrowAsset(long assetId, String borrower, String comment, String adminCode) {
        Asset asset = findAssetById(assetId);
        if (asset == null) {
            return;
        }
        String now = getCurrentTimestamp();
        asset.setStatus(Asset.STATUS_AUSGELIEHEN);
        asset.setBorrowedBy(borrower);
        asset.setBorrowedSince(now);
        DEMO_LOGS.add(0, new LoanLog(nextLogId++, assetId, LoanLog.ACTION_AUSLEIHE,
                borrower, adminCode, now, comment));
    }

    public static synchronized void returnAsset(long assetId, String adminCode) {
        Asset asset = findAssetById(assetId);
        if (asset == null) {
            return;
        }
        String previousBorrower = asset.getBorrowedBy();
        String now = getCurrentTimestamp();
        asset.setStatus(asset.isDefect() ? Asset.STATUS_DEFEKT : Asset.STATUS_VERFUEGBAR);
        asset.setBorrowedBy(null);
        asset.setBorrowedSince(null);
        DEMO_LOGS.add(0, new LoanLog(nextLogId++, assetId, LoanLog.ACTION_RUECKGABE,
                previousBorrower, adminCode, now, null));
    }

    private static String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMANY).format(new Date());
    }
}
