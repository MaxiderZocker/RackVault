# RackVault

Android-App (Programmiersprache **Java**) zur mobilen IT-Inventar- und Hardware-Ausleihverwaltung (Projekt im Fach SWDM DT31 / AWE, Berufskolleg Beckum).

## Umgesetzte Features
- **Inventar-Übersicht (`MainActivity`):**
  - Klassische `ListView` mit eigenem `AssetListAdapter`, Status-Badges (**Grün** = Verfügbar, **Orange** = Ausgeliehen, **Rot** = Defekt) und Live-Filterung über dynamischen Standort- und Status-`Spinner`.
  - Toolbar-`OptionsMenu` (CSV-Export, Einstellungen, App-Info), `ContextMenu` bei langem Druck auf Listeneinträge (Details, Bearbeiten, Löschen mit Sicherheits-`AlertDialog`) und FAB (`+`).
  - Auto Light/Dark Mode (`DayNight`) sowie Edge-to-Edge-Statusleisten-Insets inkl. ausgeblendeter System-Navigationsleiste.
- **Geräte anlegen & bearbeiten (`AddEditAssetActivity`):**
  - **Gerätename – Freitext oder Produktkatalog (`ProductCatalog`):** Wahl zwischen freier Texteingabe und einer mehrstufigen Baumstruktur (Hersteller → Produktkategorie → Unterkategorie/Serie → Modell) mit der aktuellen **Ubiquiti UniFi / UISP / AmpliFi**-Produktpalette (Cloud Gateways, Switching inkl. Pro Max / Enterprise / XG / Aggregation / WAN, WiFi, Camera Security, Door Access, Integrations, Power Tech, UISP & Accessories). Automatische Übernahme passender Gerätekategorie und des Herstellers (`Ubiquiti Inc.`).
  - **Automatische fortlaufende Inventarnummer:** Bleibt das Feld „Inventarnummer“ beim Anlegen leer, generiert `DatabaseHelper.generateNextInventoryNumber(...)` automatisch die nächste freie Nummer anhand des konfigurierten Kategorie- oder globalen Formats (Platzhalter `*` für führende Nullen, z. B. `IT-***-SWT` → `IT-010-SWT`).
  - **Dynamische Kategorien & Standorte:** Auswahl aus den vom Nutzer verwalteten Kategorien und Standorten.
  - Pflichtfeldprüfung (`setError`), Prüfung auf eindeutige Inventarnummern und Defekt-Switch.
  - Automatische MAC-Adressen-Formatierung während der Eingabe (`A` → `A2` → `A2:3`, beim Löschen verschwindet der Doppelpunkt mit).
  - Asynchroner MAC-Vendor-Lookup über `https://api.macvendors.com/{mac}` (`MacVendorClient`) inkl. WLAN-Prüfung, Ladeanzeige und manuellem Fallback.
- **Detailansicht & Ausleihe (`AssetDetailActivity`):**
  - Übersicht aller Gerätestammdaten inkl. Ausleih-Status.
  - Modaler `AlertDialog` für Ausleihe (Ausleiher + Bemerkung), Rückgabe-Buchung und chronologische Historie (`LoanLogAdapter`).
- **Einstellungen, Untermenüs & Inventur-Export (`SettingsActivity`, `InventoryPatternActivity`, `ManageListActivity`):**
  - Persistente Standardwerte via `SharedPreferences` (`PrefsManager`: Admin-Kürzel, Standard-Standort, WLAN-Only-Option).
  - **Untermenü „Inventarnummer-Formatierung“ (`InventoryPatternActivity`):** Anpassung des globalen Formats (Fallback, z. B. `IT-***`) sowie kategoriespezifischer Formate über ein dynamisches Dropdown (`spAddCategoryPattern`), das nur noch nicht konfigurierte Kategorien anbietet. Konfigurierte Kategorien lassen sich per **„Löschen“**-Button oder durch Leeren des Feldes wieder entfernen und wandern zurück ins Dropdown.
  - **Untermenüs „Standorte verwalten“ & „Kategorien verwalten“ (`ManageListActivity`):** Eigene Standorte und Kategorien anlegen, bestehende Einträge per Dialog umbenennen (inkl. automatischer Aktualisierung bestehender SQLite-Datensätze und Kategorie-Formate) sowie Einträge löschen.
  - UTF-8-CSV-Export mit BOM in den öffentlichen `Downloads`-Ordner und ins App-Export-Verzeichnis inkl. `Snackbar` mit direkter **„Teilen“**-Option (`FileProvider`).
  - Wartungsfunktionen mit Bestätigungs-`AlertDialog` (Demo-Daten laden / SQLite-Datenbank zurücksetzen).

## Architektur & Paketstruktur (`de.bkb.rackvault`)
```text
app/src/main/java/de/bkb/rackvault/
├── model/
│   ├── Asset.java                     # Datenmodell für Inventargeräte
│   └── LoanLog.java                   # Datenmodell für Ausleih-/Status-Protokoll
├── data/
│   ├── DatabaseHelper.java            # SQLiteOpenHelper (Tabellen assets & ausleih_log, Auto-Inventarnummern, Rename-Sync)
│   ├── PrefsManager.java              # SharedPreferences-Verwaltung (Standards, Inventarnummer-Formate, eigene Standorte/Kategorien)
│   ├── ProductCatalog.java            # Hierarchischer Produktkatalog (Ubiquiti-Baumstruktur)
│   └── CsvExporter.java               # UTF-8-CSV-Export (Downloads + FileProvider-Share)
├── network/
│   └── MacVendorClient.java           # Asynchroner HTTP-GET-Lookup inkl. WLAN-Check
└── ui/
    ├── MainActivity.java              # Inventar-Übersicht & dynamische Filter
    ├── AddEditAssetActivity.java      # Geräteformular, Katalog-Baum, Auto-Inv.-Nr., MAC-Formatter & Lookup
    ├── AssetDetailActivity.java       # Detailansicht, Ausleih-Dialog & Verlauf
    ├── SettingsActivity.java          # Einstellungen, Navigation in Untermenüs, Export & DB-Wartung
    ├── InventoryPatternActivity.java  # Untermenü: Globale & dynamische Kategorie-Inventarnummer-Formate
    ├── ManageListActivity.java        # Untermenü: Eigene Standorte & Kategorien anlegen, umbenennen & löschen
    ├── AssetListAdapter.java          # ArrayAdapter<Asset>
    ├── LoanLogAdapter.java            # ArrayAdapter<LoanLog>
    └── UiUtils.java                   # System-Bar-Insets & Immersive Navigation Bar
```

## Datenhaltung
- **SQLite (`rackvault.db`, Version 1):**
  - `assets`: `id`, `name`, `inventory_number` (`UNIQUE`), `category`, `location`, `mac_address`, `vendor`, `status`, `is_defect`, `borrowed_by`, `borrowed_since`, `notes`.
  - `ausleih_log`: `id`, `asset_id` (`FOREIGN KEY ... ON DELETE CASCADE`), `action`, `borrower`, `admin_code`, `timestamp`, `comment`.
- **`SharedPreferences` (`rackvault_prefs`):**
  - `pref_admin_code` (Default: `"ADM"`), `pref_default_location` (Default: `"Lager"`), `pref_wifi_only_lookup` (Default: `true`).
  - `pref_inv_pattern_global` (Default: `"IT-***"`) sowie `pref_inv_pattern_cat_<kategorie>` für kategoriespezifische Inventarnummer-Muster.
  - `pref_custom_locations` und `pref_custom_categories` (JSON-Arrays für benutzerdefinierte Standorte und Kategorien).

## Build, Tests & Demo-Checkliste
- **Debug-APK bauen & auf verbundenem Gerät installieren:**
  ```powershell
  .\gradlew.bat installDebug
  ```
- **Unit-Tests ausführen (6 JUnit-Tests für Modelle, MAC-Validierung, Katalog-Baum & Inventarnummer-Formatierung):**
  ```powershell
  .\gradlew.bat testDebugUnitTest
  ```
- **Demo-Ablauf für die Abnahme:**
  1. App starten → Demo-Geräte in der `MainActivity` zeigen & nach Standort/Status filtern.
  2. Neues Gerät per `+` anlegen, wahlweise über den **Ubiquiti-Produktkatalog** (`ProductCatalog`) auswählen oder frei benennen, Inventarnummer für die automatische Nummerierung leer lassen, MAC ohne Doppelpunkte eingeben (z. B. `001A2B3C4D5E`) und per **„Hersteller?“** auflösen.
  3. Gerät öffnen, über **„Gerät ausleihen“** per `AlertDialog` verleihen, Verlauf prüfen und anschließend zurückbuchen.
  4. In den **Einstellungen** die Untermenüs **„Inventarnummer-Formatierung anpassen“**, **„Standorte verwalten“** und **„Kategorien verwalten“** zeigen, danach **„Inventar als CSV exportieren“** ausführen und Teilen-Dialog präsentieren.
  5. App komplett schließen und neu öffnen → Nachweis der SQLite- und `SharedPreferences`-Persistenz.
