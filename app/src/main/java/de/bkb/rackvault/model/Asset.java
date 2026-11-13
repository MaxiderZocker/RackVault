package de.bkb.rackvault.model;

/**
 * Datenmodell für ein IT-Inventar-Gerät (Tabelle {@code assets}).
 */
public class Asset {

    public static final String STATUS_VERFUEGBAR = "VERFUEGBAR";
    public static final String STATUS_AUSGELIEHEN = "AUSGELIEHEN";
    public static final String STATUS_DEFEKT = "DEFEKT";

    private long id;
    private String name;
    private String inventoryNumber;
    private String category;
    private String location;
    private String macAddress;
    private String vendor;
    private String status;
    private boolean defect;
    private String borrowedBy;
    private String borrowedSince;
    private String notes;

    public Asset() {
        this.status = STATUS_VERFUEGBAR;
        this.defect = false;
    }

    public Asset(long id, String name, String inventoryNumber, String category, String location,
                 String macAddress, String vendor, String status, boolean defect,
                 String borrowedBy, String borrowedSince, String notes) {
        this.id = id;
        this.name = name;
        this.inventoryNumber = inventoryNumber;
        this.category = category;
        this.location = location;
        this.macAddress = macAddress;
        this.vendor = vendor;
        this.status = status;
        this.defect = defect;
        this.borrowedBy = borrowedBy;
        this.borrowedSince = borrowedSince;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDefect() {
        return defect;
    }

    public void setDefect(boolean defect) {
        this.defect = defect;
    }

    public String getBorrowedBy() {
        return borrowedBy;
    }

    public void setBorrowedBy(String borrowedBy) {
        this.borrowedBy = borrowedBy;
    }

    public String getBorrowedSince() {
        return borrowedSince;
    }

    public void setBorrowedSince(String borrowedSince) {
        this.borrowedSince = borrowedSince;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Liefert die lesbare deutsche Bezeichnung für den gespeicherten Status-Schlüssel.
     */
    public String getStatusDisplayName() {
        if (STATUS_AUSGELIEHEN.equals(status)) {
            return "Ausgeliehen";
        }
        if (STATUS_DEFEKT.equals(status)) {
            return "Defekt";
        }
        return "Verfügbar";
    }
}
