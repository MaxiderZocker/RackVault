package de.bkb.rackvault.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hierarchischer Produktkatalog für vorgefertigte IT-Geräte
 * (Hersteller -> Produktbereich -> Serie -> Gerätemodell).
 */
public final class ProductCatalog {

    private static final String VENDOR_UBIQUITI = "Ubiquiti Inc.";
    private static final List<CatalogNode> MANUFACTURERS = buildCatalog();

    private ProductCatalog() {
    }

    /**
     * Repräsentiert einen Knoten im hierarchischen Produktbaum.
     */
    public static final class CatalogNode {
        private final String label;
        private final String fullAssetName;
        private final String defaultVendor;
        private final String defaultCategory;
        private final List<CatalogNode> children;

        private CatalogNode(@NonNull String label,
                            @Nullable String fullAssetName,
                            @Nullable String defaultVendor,
                            @Nullable String defaultCategory,
                            @NonNull List<CatalogNode> children) {
            this.label = label;
            this.fullAssetName = fullAssetName;
            this.defaultVendor = defaultVendor;
            this.defaultCategory = defaultCategory;
            this.children = Collections.unmodifiableList(children);
        }

        @NonNull
        public String getLabel() {
            return label;
        }

        @Nullable
        public String getFullAssetName() {
            return fullAssetName;
        }

        @Nullable
        public String getDefaultVendor() {
            return defaultVendor;
        }

        @Nullable
        public String getDefaultCategory() {
            return defaultCategory;
        }

        @NonNull
        public List<CatalogNode> getChildren() {
            return children;
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }
    }

    @NonNull
    public static List<CatalogNode> getManufacturers() {
        return MANUFACTURERS;
    }

    private static List<CatalogNode> buildCatalog() {
        List<CatalogNode> manufacturers = new ArrayList<>();
        manufacturers.add(buildUbiquitiNode());
        return Collections.unmodifiableList(manufacturers);
    }

    private static CatalogNode buildUbiquitiNode() {
        List<CatalogNode> categories = new ArrayList<>();

        categories.add(branch("Switche (Switching)", VENDOR_UBIQUITI, "Switch",
                branch("Pro Max Serie (Etherlighting / L3)", VENDOR_UBIQUITI, "Switch",
                        ubiquitiLeaves("Switch",
                                "Switch Pro Max 16",
                                "Switch Pro Max 16 PoE",
                                "Switch Pro Max 24",
                                "Switch Pro Max 24 PoE",
                                "Switch Pro Max 48",
                                "Switch Pro Max 48 PoE"
                        )
                ),
                branch("Enterprise & Campus Serie", VENDOR_UBIQUITI, "Switch",
                        ubiquitiLeaves("Switch",
                                "Switch Enterprise 8 PoE",
                                "Switch Enterprise 24 PoE",
                                "Switch Enterprise 48 PoE",
                                "Switch EnterpriseXG 24",
                                "Enterprise Campus 24 PoE (ECS-24-PoE)",
                                "Enterprise Campus 48 PoE (ECS-48-PoE)",
                                "Enterprise Campus Aggregation"
                        )
                ),
                branch("Pro & Aggregation Serie (L3)", VENDOR_UBIQUITI, "Switch",
                        ubiquitiLeaves("Switch",
                                "Switch Pro 8 PoE",
                                "Switch Pro 24",
                                "Switch Pro 24 PoE",
                                "Switch Pro 48",
                                "Switch Pro 48 PoE",
                                "Switch Pro HD 24 PoE",
                                "Switch Aggregation (USW-Aggregation)",
                                "Switch Pro Aggregation (USW-Pro-Aggregation)"
                        )
                ),
                branch("Standard Serie (L2)", VENDOR_UBIQUITI, "Switch",
                        ubiquitiLeaves("Switch",
                                "Switch 16 PoE (USW-16-PoE)",
                                "Switch 24 (USW-24)",
                                "Switch 24 PoE (USW-24-PoE)",
                                "Switch 48 (USW-48)",
                                "Switch 48 PoE (USW-48-PoE)"
                        )
                ),
                branch("Utility & Kompakt (Ultra / Flex / Lite)", VENDOR_UBIQUITI, "Switch",
                        ubiquitiLeaves("Switch",
                                "Switch Ultra (USW-Ultra)",
                                "Switch Ultra 60W",
                                "Switch Ultra 210W",
                                "Switch Flex (USW-Flex)",
                                "Switch Flex 2.5G 5",
                                "Switch Flex 2.5G 8 PoE",
                                "Switch Flex Mini",
                                "Switch Lite 8 PoE",
                                "Switch Lite 16 PoE",
                                "Switch Mission Critical"
                        )
                )
        ));

        categories.add(branch("Access Points (WiFi)", VENDOR_UBIQUITI, "Access Point",
                branch("WiFi 7 Serie (U7 & E7)", VENDOR_UBIQUITI, "Access Point",
                        ubiquitiLeaves("Access Point",
                                "U7 Pro",
                                "U7 Pro Max",
                                "U7 Pro XG",
                                "U7 Pro XGS",
                                "U7 Lite",
                                "Enterprise 7 (E7)",
                                "Enterprise 7 Campus (E7-Campus)"
                        )
                ),
                branch("WiFi 6 / 6E Serie (U6)", VENDOR_UBIQUITI, "Access Point",
                        ubiquitiLeaves("Access Point",
                                "U6+",
                                "U6 Pro",
                                "U6 Long-Range (U6-LR)",
                                "U6 Enterprise"
                        )
                ),
                branch("In-Wall Serie", VENDOR_UBIQUITI, "Access Point",
                        ubiquitiLeaves("Access Point",
                                "U7 In-Wall",
                                "U7 Pro Wall",
                                "U7 Pro XG Wall",
                                "U6 In-Wall",
                                "U6 Enterprise In-Wall"
                        )
                ),
                branch("Outdoor & Mesh Serie", VENDOR_UBIQUITI, "Access Point",
                        ubiquitiLeaves("Access Point",
                                "U7 Outdoor",
                                "U6 Mesh",
                                "U6 Mesh Pro",
                                "Swiss Army Knife Ultra (UK-Ultra)",
                                "U6 Extender"
                        )
                ),
                branch("Building & Device Bridge", VENDOR_UBIQUITI, "Access Point",
                        ubiquitiLeaves("Access Point",
                                "Building Bridge (UBB)",
                                "Building Bridge XG (UBB-XG)",
                                "Device Bridge Pro (UDB-Pro)"
                        )
                )
        ));

        categories.add(branch("Cloud Gateways & Router", VENDOR_UBIQUITI, "Router",
                branch("Dream Machine & Wall Serie", VENDOR_UBIQUITI, "Router",
                        ubiquitiLeaves("Router",
                                "Dream Machine Pro (UDM-Pro)",
                                "Dream Machine Special Edition (UDM-SE)",
                                "Dream Machine Pro Max (UDM-Pro-Max)",
                                "Dream Wall (UDW)",
                                "Dream Router 7 (UDR7)",
                                "UniFi Express 7 (UX7)",
                                "UniFi Express (UX)"
                        )
                ),
                branch("Cloud Gateway Kompakt", VENDOR_UBIQUITI, "Router",
                        ubiquitiLeaves("Router",
                                "Cloud Gateway Fiber (UCG-Fiber)",
                                "Cloud Gateway Max (UCG-Max)",
                                "Cloud Gateway Ultra (UCG-Ultra)"
                        )
                ),
                branch("Enterprise & Unmanaged Gateways", VENDOR_UBIQUITI, "Router",
                        ubiquitiLeaves("Router",
                                "Enterprise Fortress Gateway (EFG)",
                                "Gateway Enterprise (UXG-Enterprise)",
                                "Gateway Pro (UXG-Pro)",
                                "Gateway Max (UXG-Max)",
                                "Gateway Lite (UXG-Lite)"
                        )
                )
        ));

        categories.add(branch("Kameras & NVR (Protect)", VENDOR_UBIQUITI, "Sonstiges",
                branch("NVR (Videorekorder)", VENDOR_UBIQUITI, "Sonstiges",
                        ubiquitiLeaves("Sonstiges",
                                "Network Video Recorder (UNVR)",
                                "Network Video Recorder Pro (UNVR-Pro)",
                                "Enterprise NVR (ENVR)"
                        )
                ),
                branch("G5 Kamera-Serie", VENDOR_UBIQUITI, "Sonstiges",
                        ubiquitiLeaves("Sonstiges",
                                "G5 Bullet",
                                "G5 Dome",
                                "G5 Turret Ultra",
                                "G5 Dome Ultra",
                                "G5 Flex",
                                "G5 Pro",
                                "G5 PTZ"
                        )
                ),
                branch("AI Kamera-Serie", VENDOR_UBIQUITI, "Sonstiges",
                        ubiquitiLeaves("Sonstiges",
                                "AI Pro",
                                "AI Theta",
                                "AI DSLR",
                                "AI 360",
                                "AI LPR",
                                "AI Port"
                        )
                )
        ));

        categories.add(branch("Zutritt, Strom & Rack-Zubehör", VENDOR_UBIQUITI, "Kabel/Zubehör",
                branch("Door Access", VENDOR_UBIQUITI, "Kabel/Zubehör",
                        ubiquitiLeaves("Kabel/Zubehör",
                                "Access Hub",
                                "Access Enterprise Hub",
                                "G2 Reader Pro",
                                "G2 Reader",
                                "Access Intercom"
                        )
                ),
                branch("SmartPower & USV", VENDOR_UBIQUITI, "Kabel/Zubehör",
                        ubiquitiLeaves("Kabel/Zubehör",
                                "SmartPower Redundant Power System (USP-RPS)",
                                "SmartPower PDU Pro (USP-PDU-Pro)",
                                "Uninterruptible Power Supply (USP-Backup)"
                        )
                ),
                branch("Kabel, SFP & PoE-Injektoren", VENDOR_UBIQUITI, "Kabel/Zubehör",
                        ubiquitiLeaves("Kabel/Zubehör",
                                "10G SFP+ Direct Attach Cable (DAC)",
                                "SFP+ to RJ45 10G Modul",
                                "PoE+ Adapter (30W)",
                                "PoE++ Adapter (60W)"
                        )
                )
        ));

        return new CatalogNode("Ubiquiti", null, VENDOR_UBIQUITI, null, categories);
    }

    private static CatalogNode branch(@NonNull String label,
                                      @NonNull String defaultVendor,
                                      @NonNull String defaultCategory,
                                      @NonNull CatalogNode... childNodes) {
        List<CatalogNode> children = new ArrayList<>();
        Collections.addAll(children, childNodes);
        return new CatalogNode(label, null, defaultVendor, defaultCategory, children);
    }

    private static CatalogNode branch(@NonNull String label,
                                      @NonNull String defaultVendor,
                                      @NonNull String defaultCategory,
                                      @NonNull List<CatalogNode> children) {
        return new CatalogNode(label, null, defaultVendor, defaultCategory, children);
    }

    private static List<CatalogNode> ubiquitiLeaves(@NonNull String defaultCategory,
                                                    @NonNull String... models) {
        List<CatalogNode> leaves = new ArrayList<>(models.length);
        for (String model : models) {
            String fullName = "Ubiquiti UniFi " + model;
            leaves.add(new CatalogNode(model, fullName, VENDOR_UBIQUITI, defaultCategory, Collections.emptyList()));
        }
        return leaves;
    }
}
