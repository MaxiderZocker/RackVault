package de.bkb.rackvault.model;

/**
 * Datenmodell für einen Eintrag im Ausleih- und Status-Protokoll (Tabelle {@code ausleih_log}).
 */
public class LoanLog {

    public static final String ACTION_AUSLEIHE = "AUSLEIHE";
    public static final String ACTION_RUECKGABE = "RUECKGABE";
    public static final String ACTION_DEFEKT_MELDUNG = "DEFEKT_MELDUNG";
    public static final String ACTION_REPARIERT = "REPARIERT";

    private long id;
    private long assetId;
    private String action;
    private String borrower;
    private String adminCode;
    private String timestamp;
    private String comment;

    public LoanLog() {
    }

    public LoanLog(long id, long assetId, String action, String borrower,
                   String adminCode, String timestamp, String comment) {
        this.id = id;
        this.assetId = assetId;
        this.action = action;
        this.borrower = borrower;
        this.adminCode = adminCode;
        this.timestamp = timestamp;
        this.comment = comment;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAssetId() {
        return assetId;
    }

    public void setAssetId(long assetId) {
        this.assetId = assetId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBorrower() {
        return borrower;
    }

    public void setBorrower(String borrower) {
        this.borrower = borrower;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
