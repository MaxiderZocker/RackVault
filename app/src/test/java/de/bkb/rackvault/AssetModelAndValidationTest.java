package de.bkb.rackvault;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.bkb.rackvault.data.ProductCatalog;
import de.bkb.rackvault.model.Asset;
import de.bkb.rackvault.model.LoanLog;
import de.bkb.rackvault.ui.AddEditAssetActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit-Tests für Datenmodelle, MAC-Auto-Formatierung und MAC-Formatvalidierung.
 */
public class AssetModelAndValidationTest {

    @Test
    public void assetStatusDisplayNamesMatchExpectedLabels() {
        Asset asset = new Asset();
        assertEquals("Verfügbar", asset.getStatusDisplayName());

        asset.setStatus(Asset.STATUS_AUSGELIEHEN);
        assertEquals("Ausgeliehen", asset.getStatusDisplayName());

        asset.setStatus(Asset.STATUS_DEFEKT);
        assertEquals("Defekt", asset.getStatusDisplayName());
    }

    @Test
    public void loanLogStoresFieldsCorrectly() {
        LoanLog log = new LoanLog(7L, 3L, LoanLog.ACTION_AUSLEIHE,
                "M. Müller", "ADM", "2026-11-13 12:00", "Raum B104");
        assertEquals(7L, log.getId());
        assertEquals(3L, log.getAssetId());
        assertEquals(LoanLog.ACTION_AUSLEIHE, log.getAction());
        assertEquals("M. Müller", log.getBorrower());
        assertEquals("ADM", log.getAdminCode());
        assertEquals("2026-11-13 12:00", log.getTimestamp());
        assertEquals("Raum B104", log.getComment());
    }

    @Test
    public void formatMacInputInsertsAndRemovesColonsAutomatically() {
        assertEquals("", AddEditAssetActivity.formatMacInput(""));
        assertEquals("A", AddEditAssetActivity.formatMacInput("a"));
        assertEquals("A2", AddEditAssetActivity.formatMacInput("a2"));
        assertEquals("A2:3", AddEditAssetActivity.formatMacInput("a23"));
        // Nach dem Löschen der '3' aus "A2:3" bleibt "A2:" übrig -> Doppelpunkt wird mit entfernt
        assertEquals("A2", AddEditAssetActivity.formatMacInput("A2:"));
        assertEquals("00:1A:2B:3C:4D:5E", AddEditAssetActivity.formatMacInput("001a2b3c4d5e"));
        // Begrenzung auf maximal 12 Hex-Zeichen und Ignorieren ungültiger Zeichen
        assertEquals("00:1A:2B:3C:4D:5E", AddEditAssetActivity.formatMacInput("00-1a-2b-3c-4d-5e-ff"));
    }

    @Test
    public void isValidMacAddressValidatesFormatCorrectly() {
        assertTrue(AddEditAssetActivity.isValidMacAddress("00:1A:2B:3C:4D:5E"));
        assertTrue(AddEditAssetActivity.isValidMacAddress("a4-bb-6d-12-34-56"));
        assertFalse(AddEditAssetActivity.isValidMacAddress(null));
        assertFalse(AddEditAssetActivity.isValidMacAddress(""));
        assertFalse(AddEditAssetActivity.isValidMacAddress("A2:3"));
        assertFalse(AddEditAssetActivity.isValidMacAddress("00:1A:2B:3C:4D:ZZ"));
    }

    @Test
    public void productCatalogContainsUbiquitiTreeStructure() {
        List<ProductCatalog.CatalogNode> manufacturers = ProductCatalog.getManufacturers();
        assertFalse(manufacturers.isEmpty());

        ProductCatalog.CatalogNode ubiquiti = null;
        for (ProductCatalog.CatalogNode node : manufacturers) {
            if ("Ubiquiti".equals(node.getLabel())) {
                ubiquiti = node;
                break;
            }
        }
        assertNotNull(ubiquiti);
        assertEquals("Ubiquiti Inc.", ubiquiti.getDefaultVendor());
        assertEquals(5, ubiquiti.getChildren().size());

        Set<String> validCategories = new HashSet<>(Arrays.asList(
                "Switch", "Router", "Access Point", "Laptop", "Kabel/Zubehör", "Sonstiges"
        ));

        for (ProductCatalog.CatalogNode category : ubiquiti.getChildren()) {
            assertFalse(category.isLeaf());
            assertTrue(validCategories.contains(category.getDefaultCategory()));
            for (ProductCatalog.CatalogNode series : category.getChildren()) {
                assertFalse(series.isLeaf());
                for (ProductCatalog.CatalogNode model : series.getChildren()) {
                    assertTrue(model.isLeaf());
                    assertNotNull(model.getFullAssetName());
                    assertFalse(model.getFullAssetName().trim().isEmpty());
                    assertEquals("Ubiquiti Inc.", model.getDefaultVendor());
                    assertTrue(validCategories.contains(model.getDefaultCategory()));
                }
            }
        }
    }
}
