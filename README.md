# RackVault

Android-App (Programmiersprache **Java**) zur mobilen IT-Inventar- und Hardware-Ausleihverwaltung (Projekt im Fach SWDM DT31 / AWE, Berufskolleg Beckum).

## Umgesetzte Features
- **Inventar-Übersicht (`MainActivity`):**
  - Klassische `ListView` mit eigenem `AssetListAdapter`, Status-Badges (**Grün** = Verfügbar, **Orange** = Ausgeliehen, **Rot** = Defekt) und Live-Filterung über Standort- und Status-`Spinner`.
  - Toolbar-`OptionsMenu` (CSV-Export, Einstellungen, App-Info), `ContextMenu` bei langem Druck auf Listeneinträge (Details, Bearbeiten, Löschen mit Sicherheits-`AlertDialog`) und FAB (`+`).
  - Auto Light/Dark Mode (`DayNight`) sowie Edge-to-Edge-Statusleisten-Insets inkl. ausgeblendeter System-Navigationsleiste.
- **Geräte anlegen & bearbeiten (`AddEditAssetActivity`):**
  - Pflichtfeldprüfung (`setError`), Prüfung auf eindeutige Inventarnummern und Defekt-Switch.
  - Automatische MAC-Adressen-Formatierung während der Eingabe (`A` → `A2` → `A2:3`, beim Löschen verschwindet der Doppelpunkt mit).
  - Asynchroner MAC-Vendor-Lookup über `https://api.macvendors.com/{mac}` (`MacVendorClient`) inkl. WLAN-Prüfung, Ladeanzeige und manuellem Fallback.
- **Detailansicht & Ausleihe (`AssetDetailActivity`):**
  - Übersicht aller Gerätestammdaten inkl. Ausleih-Status.
  - Modaler `AlertDialog` für Ausleihe (Ausleiher + Bemerkung), Rückgabe-Buchung und chronologische Historie (`LoanLogAdapter`).
- **Einstellungen & Inventur-Export (`SettingsActivity`):**
  - Persistente Standardwerte via `SharedPreferences` (`PrefsManager`: Admin-Kürzel, Standard-Standort, WLAN-Only-Option).
  - UTF-8-CSV-Export mit BOM in den öffentlichen `Downloads`-Ordner und ins App-Export-Verzeichnis inkl. `Snackbar` mit direkter **„Teilen“**-Option (`FileProvider`).
  - Wartungsfunktionen mit Bestätigungs-`AlertDialog` (Demo-Daten laden / SQLite-Datenbank zurücksetzen).

## Architektur & Paketstruktur (`de.bkb.rackvault`)
```text
app/src/main/java/de/bkb/rackvault/
├── model/
│   ├── Asset.java                 # Datenmodell für Inventargeräte
│   └── LoanLog.java               # Datenmodell für Ausleih-/Status-Protokoll
├── data/
│   ├── DatabaseHelper.java        # SQLiteOpenHelper (Tabellen assets & ausleih_log)
│   ├── PrefsManager.java          # SharedPreferences-Verwaltung (rackvault_prefs)
│   └── CsvExporter.java           # UTF-8-CSV-Export (Downloads + FileProvider-Share)
├── network/
│   └── MacVendorClient.java       # Asynchroner HTTP-GET-Lookup inkl. WLAN-Check
└── ui/
    ├── MainActivity.java          # Inventar-Übersicht & Filter
    ├── AddEditAssetActivity.java  # Geräteformular, MAC-Formatter & Lookup
    ├── AssetDetailActivity.java   # Detailansicht, Ausleih-Dialog & Verlauf
    ├── SettingsActivity.java      # Einstellungen, Export & DB-Wartung
    ├── AssetListAdapter.java      # ArrayAdapter<Asset>
    ├── LoanLogAdapter.java        # ArrayAdapter<LoanLog>
    └── UiUtils.java               # System-Bar-Insets & Immersive Navigation Bar
```

## Datenhaltung
- **SQLite (`rackvault.db`, Version 1):**
  - `assets`: `id`, `name`, `inventory_number` (`UNIQUE`), `category`, `location`, `mac_address`, `vendor`, `status`, `is_defect`, `borrowed_by`, `borrowed_since`, `notes`.
  - `ausleih_log`: `id`, `asset_id` (`FOREIGN KEY ... ON DELETE CASCADE`), `action`, `borrower`, `admin_code`, `timestamp`, `comment`.
- **`SharedPreferences` (`rackvault_prefs`):**
  - `pref_admin_code` (Default: `"ADM"`), `pref_default_location` (Default: `"Lager"`), `pref_wifi_only_lookup` (Default: `true`).

## Build, Tests & Demo-Checkliste
- **Debug-APK bauen & auf verbundenem Gerät installieren:**
  ```powershell
  .\gradlew.bat installDebug
  ```
- **Unit-Tests ausführen:**
  ```powershell
  .\gradlew.bat testDebugUnitTest
  ```
- **Demo-Ablauf für die Abnahme:**
  1. App starten → 4 Demo-Geräte in der `MainActivity` zeigen & nach Standort/Status filtern.
  2. Neues Gerät per `+` anlegen, MAC ohne Doppelpunkte eingeben (z. B. `001A2B3C4D5E`) und per **„Hersteller?“** auflösen.
  3. Gerät öffnen, über **„Gerät ausleihen“** per `AlertDialog` verleihen, Verlauf prüfen und anschließend zurückbuchen.
  4. In den **Einstellungen** Admin-Kürzel ändern, **„Inventar als CSV exportieren“** ausführen und Teilen-Dialog zeigen.
  5. App komplett schließen und neu öffnen → Nachweis der SQLite- und `SharedPreferences`-Persistenz.
