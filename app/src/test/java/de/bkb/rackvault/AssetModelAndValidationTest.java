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
        assertEquals(7, ubiquiti.getChildren().size());

        List<String> expectedCategories = Arrays.asList(
                "Cloud Gateways", "Switching", "WiFi", "Physical Security",
                "Door Access", "Integrations", "Accessories"
        );
        for (int i = 0; i < expectedCategories.size(); i++) {
            assertEquals(expectedCategories.get(i), ubiquiti.getChildren().get(i).getLabel());
        }

        // Switching -> alle 7 Serien inkl. Modellanzahl prüfen
        ProductCatalog.CatalogNode switching = ubiquiti.getChildren().get(1);
        assertEquals(7, switching.getChildren().size());

        ProductCatalog.CatalogNode enterprise = switching.getChildren().get(0);
        assertEquals("Enterprise", enterprise.getLabel());
        assertEquals(10, enterprise.getChildren().size());
        Set<String> enterpriseLabels = collectLabels(enterprise);
        assertTrue(enterpriseLabels.contains("Enterprise Campus 24S PoE (ECS-24S-PoE (1050W))"));
        assertTrue(enterpriseLabels.contains("Enterprise Campus 48S PoE (ECS-48S-PoE (2150W))"));
        assertTrue(enterpriseLabels.contains("Enterprise Audio/Video XG 24 PoE (EAV-XG-24-PoE (2150W))"));
        assertTrue(enterpriseLabels.contains("Enterprise Audio/Video Fiber (EAV-Fiber)"));
        assertTrue(enterpriseLabels.contains("Enterprise Campus Aggregation (ECS-Aggregation)"));
        assertTrue(enterpriseLabels.contains("Enterprise Campus Switch Core (ECS-Core)"));

        ProductCatalog.CatalogNode proMaxXg = switching.getChildren().get(1);
        assertEquals("Professional Max & XG", proMaxXg.getLabel());
        assertEquals(13, proMaxXg.getChildren().size());
        Set<String> proMaxXgLabels = collectLabels(proMaxXg);
        assertTrue(proMaxXgLabels.contains("Pro Max 48 PoE (USW-Pro-Max-48-PoE (720W))"));
        assertTrue(proMaxXgLabels.contains("Pro XG 48 PoE (USW-Pro-XG-48-PoE (1080W))"));
        assertTrue(proMaxXgLabels.contains("Pro HD 24 PoE (USW-Pro-HD-24-PoE (600W))"));
        assertTrue(proMaxXgLabels.contains("Pro XG 48 (USW-Pro-XG-48)"));
        assertTrue(proMaxXgLabels.contains("Pro XG 10 PoE (USW-Pro-XG-10-PoE (400W))"));
        assertTrue(proMaxXgLabels.contains("Pro HD 24 (USW-Pro-HD-24)"));

        ProductCatalog.CatalogNode professional = switching.getChildren().get(2);
        assertEquals("Professional", professional.getLabel());
        assertEquals(3, professional.getChildren().size());

        ProductCatalog.CatalogNode standard = switching.getChildren().get(3);
        assertEquals("Standard", standard.getLabel());
        assertEquals(5, standard.getChildren().size());

        ProductCatalog.CatalogNode utility = switching.getChildren().get(4);
        assertEquals("Utility", utility.getLabel());
        assertEquals(20, utility.getChildren().size());

        ProductCatalog.CatalogNode aggregation = switching.getChildren().get(5);
        assertEquals("Aggregation", aggregation.getLabel());
        assertEquals(4, aggregation.getChildren().size());
        Set<String> aggregationLabels = collectLabels(aggregation);
        assertTrue(aggregationLabels.contains("Enterprise Campus Aggregation (ECS-Aggregation)"));
        assertTrue(aggregationLabels.contains("Pro XG Aggregation (USW-Pro-XG-Aggregation)"));
        assertTrue(aggregationLabels.contains("Aggregation (USW-Aggregation)"));
        assertTrue(aggregationLabels.contains("Hi-Capacity Aggregation (USW-Pro-Aggregation)"));

        ProductCatalog.CatalogNode wan = switching.getChildren().get(6);
        assertEquals("WAN", wan.getLabel());
        assertEquals(2, wan.getChildren().size());
        Set<String> wanLabels = collectLabels(wan);
        assertTrue(wanLabels.contains("WAN Switch (USW-WAN)"));
        assertTrue(wanLabels.contains("WAN Switch RJ45 (USW-WAN-RJ45)"));

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

    private Set<String> collectLabels(ProductCatalog.CatalogNode parent) {
        Set<String> labels = new HashSet<>();
        for (ProductCatalog.CatalogNode child : parent.getChildren()) {
            labels.add(child.getLabel());
        }
        return labels;
    }
}
