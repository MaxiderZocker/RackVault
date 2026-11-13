package de.bkb.rackvault.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.bkb.rackvault.model.Asset;
import de.bkb.rackvault.model.LoanLog;

/**
 * SQLite-Datenbank-Helper für die Tabellen {@code assets} und {@code ausleih_log}.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "rackvault.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_ASSETS = "assets";
    public static final String COL_ASSET_ID = "id";
    public static final String COL_ASSET_NAME = "name";
    public static final String COL_ASSET_INVENTORY_NUMBER = "inventory_number";
    public static final String COL_ASSET_CATEGORY = "category";
    public static final String COL_ASSET_LOCATION = "location";
    public static final String COL_ASSET_MAC_ADDRESS = "mac_address";
    public static final String COL_ASSET_VENDOR = "vendor";
    public static final String COL_ASSET_STATUS = "status";
    public static final String COL_ASSET_IS_DEFECT = "is_defect";
    public static final String COL_ASSET_BORROWED_BY = "borrowed_by";
    public static final String COL_ASSET_BORROWED_SINCE = "borrowed_since";
    public static final String COL_ASSET_NOTES = "notes";

    public static final String TABLE_LOAN_LOG = "ausleih_log";
    public static final String COL_LOG_ID = "id";
    public static final String COL_LOG_ASSET_ID = "asset_id";
    public static final String COL_LOG_ACTION = "action";
    public static final String COL_LOG_BORROWER = "borrower";
    public static final String COL_LOG_ADMIN_CODE = "admin_code";
    public static final String COL_LOG_TIMESTAMP = "timestamp";
    public static final String COL_LOG_COMMENT = "comment";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAssetsTable = "CREATE TABLE " + TABLE_ASSETS + " ("
                + COL_ASSET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ASSET_NAME + " TEXT NOT NULL, "
                + COL_ASSET_INVENTORY_NUMBER + " TEXT NOT NULL UNIQUE, "
                + COL_ASSET_CATEGORY + " TEXT NOT NULL, "
                + COL_ASSET_LOCATION + " TEXT NOT NULL, "
                + COL_ASSET_MAC_ADDRESS + " TEXT, "
                + COL_ASSET_VENDOR + " TEXT, "
                + COL_ASSET_STATUS + " TEXT NOT NULL, "
                + COL_ASSET_IS_DEFECT + " INTEGER NOT NULL DEFAULT 0, "
                + COL_ASSET_BORROWED_BY + " TEXT, "
                + COL_ASSET_BORROWED_SINCE + " TEXT, "
                + COL_ASSET_NOTES + " TEXT"
                + ")";

        String createLoanLogTable = "CREATE TABLE " + TABLE_LOAN_LOG + " ("
                + COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_LOG_ASSET_ID + " INTEGER NOT NULL, "
                + COL_LOG_ACTION + " TEXT NOT NULL, "
                + COL_LOG_BORROWER + " TEXT, "
                + COL_LOG_ADMIN_CODE + " TEXT, "
                + COL_LOG_TIMESTAMP + " TEXT NOT NULL, "
                + COL_LOG_COMMENT + " TEXT, "
                + "FOREIGN KEY(" + COL_LOG_ASSET_ID + ") REFERENCES "
                + TABLE_ASSETS + "(" + COL_ASSET_ID + ") ON DELETE CASCADE"
                + ")";

        db.execSQL(createAssetsTable);
        db.execSQL(createLoanLogTable);

        insertDemoData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOAN_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSETS);
        onCreate(db);
    }

    public List<Asset> getAllAssets() {
        return getFilteredAssets("Alle", "Alle");
    }

    public List<Asset> getFilteredAssets(String locationFilter, String statusFilter) {
        List<Asset> assets = new ArrayList<>();
        StringBuilder selection = new StringBuilder();
        List<String> args = new ArrayList<>();

        if (locationFilter != null && !"Alle".equals(locationFilter)) {
            selection.append(COL_ASSET_LOCATION).append(" = ?");
            args.add(locationFilter);
        }

        String mappedStatus = mapFilterStatusToKey(statusFilter);
        if (mappedStatus != null) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append(COL_ASSET_STATUS).append(" = ?");
            args.add(mappedStatus);
        }

        String whereClause = selection.length() > 0 ? selection.toString() : null;
        String[] whereArgs = args.isEmpty() ? null : args.toArray(new String[0]);

        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_ASSETS,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                COL_ASSET_ID + " DESC")) {
            while (cursor.moveToNext()) {
                assets.add(readAssetFromCursor(cursor));
            }
        }
        return assets;
    }

    public Asset getAssetById(long assetId) {
        if (assetId <= 0) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_ASSETS,
                null,
                COL_ASSET_ID + " = ?",
                new String[]{String.valueOf(assetId)},
                null,
                null,
                null)) {
            if (cursor.moveToFirst()) {
                return readAssetFromCursor(cursor);
            }
        }
        return null;
    }

    public boolean isInventoryNumberTaken(String inventoryNumber, long excludeAssetId) {
        if (inventoryNumber == null || inventoryNumber.trim().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = getReadableDatabase();
        String selection = "LOWER(" + COL_ASSET_INVENTORY_NUMBER + ") = LOWER(?)";
        String[] args;
        if (excludeAssetId > 0) {
            selection += " AND " + COL_ASSET_ID + " != ?";
            args = new String[]{inventoryNumber.trim(), String.valueOf(excludeAssetId)};
        } else {
            args = new String[]{inventoryNumber.trim()};
        }

        try (Cursor cursor = db.query(
                TABLE_ASSETS,
                new String[]{COL_ASSET_ID},
                selection,
                args,
                null,
                null,
                null)) {
            return cursor.moveToFirst();
        }
    }

    public long saveAsset(Asset asset, String adminCode) {
        SQLiteDatabase db = getWritableDatabase();
        long savedId = asset.getId();

        db.beginTransaction();
        try {
            ContentValues values = toAssetContentValues(asset);
            if (asset.getId() <= 0) {
                savedId = db.insertOrThrow(TABLE_ASSETS, null, values);
                asset.setId(savedId);
                if (asset.isDefect()) {
                    insertLogEntry(db, savedId, LoanLog.ACTION_DEFEKT_MELDUNG,
                            null, adminCode, getCurrentTimestamp(), "Bei Erfassung als defekt markiert");
                }
            } else {
                Asset previous = getAssetByIdInternal(db, asset.getId());
                db.update(TABLE_ASSETS, values, COL_ASSET_ID + " = ?",
                        new String[]{String.valueOf(asset.getId())});
                if (previous != null && previous.isDefect() != asset.isDefect()) {
                    String action = asset.isDefect()
                            ? LoanLog.ACTION_DEFEKT_MELDUNG
                            : LoanLog.ACTION_REPARIERT;
                    String comment = asset.isDefect()
                            ? "Gerät als defekt markiert"
                            : "Defekt-Markierung aufgehoben";
                    insertLogEntry(db, asset.getId(), action,
                            previous.getBorrowedBy(), adminCode, getCurrentTimestamp(), comment);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return savedId;
    }

    public void deleteAsset(long assetId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ASSETS, COL_ASSET_ID + " = ?", new String[]{String.valueOf(assetId)});
    }

    public List<LoanLog> getLoanLogsForAsset(long assetId) {
        List<LoanLog> logs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_LOAN_LOG,
                null,
                COL_LOG_ASSET_ID + " = ?",
                new String[]{String.valueOf(assetId)},
                null,
                null,
                COL_LOG_ID + " DESC")) {
            while (cursor.moveToNext()) {
                logs.add(readLoanLogFromCursor(cursor));
            }
        }
        return logs;
    }

    public void borrowAsset(long assetId, String borrower, String comment, String adminCode) {
        SQLiteDatabase db = getWritableDatabase();
        String now = getCurrentTimestamp();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_ASSET_STATUS, Asset.STATUS_AUSGELIEHEN);
            values.put(COL_ASSET_BORROWED_BY, borrower);
            values.put(COL_ASSET_BORROWED_SINCE, now);

            db.update(TABLE_ASSETS, values, COL_ASSET_ID + " = ?",
                    new String[]{String.valueOf(assetId)});

            insertLogEntry(db, assetId, LoanLog.ACTION_AUSLEIHE, borrower, adminCode, now, comment);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void returnAsset(long assetId, String adminCode) {
        SQLiteDatabase db = getWritableDatabase();
        String now = getCurrentTimestamp();

        db.beginTransaction();
        try {
            Asset asset = getAssetByIdInternal(db, assetId);
            if (asset == null) {
                return;
            }

            String nextStatus = asset.isDefect() ? Asset.STATUS_DEFEKT : Asset.STATUS_VERFUEGBAR;
            ContentValues values = new ContentValues();
            values.put(COL_ASSET_STATUS, nextStatus);
            values.putNull(COL_ASSET_BORROWED_BY);
            values.putNull(COL_ASSET_BORROWED_SINCE);

            db.update(TABLE_ASSETS, values, COL_ASSET_ID + " = ?",
                    new String[]{String.valueOf(assetId)});

            insertLogEntry(db, assetId, LoanLog.ACTION_RUECKGABE,
                    asset.getBorrowedBy(), adminCode, now, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void resetToDemoData() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_LOAN_LOG, null, null);
            db.delete(TABLE_ASSETS, null, null);
            insertDemoData(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void clearAllData() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_LOAN_LOG, null, null);
            db.delete(TABLE_ASSETS, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void insertDemoData(SQLiteDatabase db) {
        long swId = insertDemoAsset(db, "HP ProCurve 2530-24G", "IT-SW-014", "Switch", "Serverraum",
                "00:1A:2B:3C:4D:5E", "Hewlett Packard Enterprise", Asset.STATUS_VERFUEGBAR, false,
                null, null, "Rack 2, Höheneinheit 14");

        long nbId = insertDemoAsset(db, "Dell Latitude 5430", "IT-NB-008", "Laptop", "Büro IT",
                "A4:BB:6D:12:34:56", "Dell Inc.", Asset.STATUS_AUSGELIEHEN, false,
                "M. Müller", "2026-11-11 08:30", "Inkl. 65W USB-C Netzteil");

        long apId = insertDemoAsset(db, "Ubiquiti UniFi AP AC Pro", "IT-AP-003", "Access Point", "Lager",
                "74:83:C2:AA:BB:CC", "Ubiquiti Inc.", Asset.STATUS_DEFEKT, true,
                null, null, "PoE-Port ohne Funktion");

        insertDemoAsset(db, "MikroTik hEX RB750Gr3", "IT-RT-005", "Router", "Schulungsraum",
                "4C:5E:0C:11:22:33", "MikroTik", Asset.STATUS_VERFUEGBAR, false,
                null, null, "Testlabor VLAN-Übung");

        insertLogEntry(db, swId, LoanLog.ACTION_AUSLEIHE,
                "T. Weber", "ADM", "2026-11-09 09:00", "Firmware-Test");
        insertLogEntry(db, swId, LoanLog.ACTION_RUECKGABE,
                "T. Weber", "ADM", "2026-11-10 15:45", "Nach Wartungsfenster zurück im Rack");
        insertLogEntry(db, nbId, LoanLog.ACTION_AUSLEIHE,
                "M. Müller", "ADM", "2026-11-11 08:30", "Für Raum B104 Aufbau");
        insertLogEntry(db, apId, LoanLog.ACTION_DEFEKT_MELDUNG,
                null, "ADM", "2026-11-08 11:20", "PoE-Port reagiert nicht mehr");
    }

    private long insertDemoAsset(SQLiteDatabase db, String name, String inventoryNumber,
                                 String category, String location, String macAddress,
                                 String vendor, String status, boolean defect,
                                 String borrowedBy, String borrowedSince, String notes) {
        Asset asset = new Asset(0, name, inventoryNumber, category, location,
                macAddress, vendor, status, defect, borrowedBy, borrowedSince, notes);
        return db.insert(TABLE_ASSETS, null, toAssetContentValues(asset));
    }

    private void insertLogEntry(SQLiteDatabase db, long assetId, String action,
                                String borrower, String adminCode, String timestamp, String comment) {
        ContentValues values = new ContentValues();
        values.put(COL_LOG_ASSET_ID, assetId);
        values.put(COL_LOG_ACTION, action);
        values.put(COL_LOG_BORROWER, borrower);
        values.put(COL_LOG_ADMIN_CODE, adminCode);
        values.put(COL_LOG_TIMESTAMP, timestamp);
        values.put(COL_LOG_COMMENT, comment);
        db.insert(TABLE_LOAN_LOG, null, values);
    }

    private Asset getAssetByIdInternal(SQLiteDatabase db, long assetId) {
        try (Cursor cursor = db.query(
                TABLE_ASSETS,
                null,
                COL_ASSET_ID + " = ?",
                new String[]{String.valueOf(assetId)},
                null,
                null,
                null)) {
            if (cursor.moveToFirst()) {
                return readAssetFromCursor(cursor);
            }
        }
        return null;
    }

    private ContentValues toAssetContentValues(Asset asset) {
        ContentValues values = new ContentValues();
        values.put(COL_ASSET_NAME, asset.getName());
        values.put(COL_ASSET_INVENTORY_NUMBER, asset.getInventoryNumber());
        values.put(COL_ASSET_CATEGORY, asset.getCategory());
        values.put(COL_ASSET_LOCATION, asset.getLocation());
        values.put(COL_ASSET_MAC_ADDRESS, asset.getMacAddress());
        values.put(COL_ASSET_VENDOR, asset.getVendor());
        values.put(COL_ASSET_STATUS, asset.getStatus());
        values.put(COL_ASSET_IS_DEFECT, asset.isDefect() ? 1 : 0);
        values.put(COL_ASSET_BORROWED_BY, asset.getBorrowedBy());
        values.put(COL_ASSET_BORROWED_SINCE, asset.getBorrowedSince());
        values.put(COL_ASSET_NOTES, asset.getNotes());
        return values;
    }

    private Asset readAssetFromCursor(Cursor cursor) {
        Asset asset = new Asset();
        asset.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ASSET_ID)));
        asset.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSET_NAME)));
        asset.setInventoryNumber(cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSET_INVENTORY_NUMBER)));
        asset.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSET_CATEGORY)));
        asset.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSET_LOCATION)));
        asset.setMacAddress(cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSET_MAC_ADDRESS)));
        asset.setVendor(cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSET_VENDOR)));
        asset.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSET_STATUS)));
        asset.setDefect(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ASSET_IS_DEFECT)) == 1);
        asset.setBorrowedBy(cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSET_BORROWED_BY)));
        asset.setBorrowedSince(cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSET_BORROWED_SINCE)));
        asset.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSET_NOTES)));
        return asset;
    }

    private LoanLog readLoanLogFromCursor(Cursor cursor) {
        LoanLog log = new LoanLog();
        log.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOG_ID)));
        log.setAssetId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOG_ASSET_ID)));
        log.setAction(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_ACTION)));
        log.setBorrower(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_BORROWER)));
        log.setAdminCode(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_ADMIN_CODE)));
        log.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_TIMESTAMP)));
        log.setComment(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_COMMENT)));
        return log;
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

    private static String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMANY).format(new Date());
    }
}
