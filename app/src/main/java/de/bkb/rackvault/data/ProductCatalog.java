package de.bkb.rackvault.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hierarchischer Produktkatalog für vorgefertigte IT-Geräte
 * (Hersteller -> Produktbereich -> Serie -> Gerätemodell) gemäß eu.store.ui.com.
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

        categories.add(branch("Cloud Gateways", VENDOR_UBIQUITI, "Router",
                branch("Enterprise Scale", VENDOR_UBIQUITI, "Router",
                        uLeaf("Router", "Enterprise Firewall Core", "EF-Core"),
                        uLeaf("Router", "Enterprise Firewall", "EFG")
                ),
                branch("Large Scale", VENDOR_UBIQUITI, "Router",
                        uLeaf("Router", "Dream Machine Beast", "UDM-Beast"),
                        uLeaf("Router", "Dream Machine Pro Max", "UDM-Pro-Max"),
                        uLeaf("Router", "Dream Machine Pro", "UDM-Pro"),
                        uLeaf("Router", "Dream Machine Special Edition", "UDM-SE (180W)")
                ),
                branch("Compact", VENDOR_UBIQUITI, "Router",
                        uLeaf("Router", "Cloud Gateway Fiber", "UCG-Fiber (30W)"),
                        uLeaf("Router", "Cloud Gateway Max", "UCG-Max"),
                        uLeaf("Router", "Cloud Gateway Ultra", "UCG-Ultra")
                ),
                branch("WiFi Integrated", VENDOR_UBIQUITI, "Router",
                        uLeaf("Router", "Cloud Gateway Industrial", "UCG-Industrial (270W)"),
                        uLeaf("Router", "Dream Router 5G Max", "UDR-5G-Max (15W)"),
                        uLeaf("Router", "Dream Router 7", "UDR7 (15W)"),
                        uLeaf("Router", "Whole Home WiFi", "UX7"),
                        uLeaf("Router", "Dream Wall", "UDW (420W)")
                ),
                branch("Whole Home WiFi", VENDOR_UBIQUITI, "Router",
                        uLeaf("Router", "Whole Home WiFi", "UX7")
                )
        ));

        categories.add(branch("Switching", VENDOR_UBIQUITI, "Switch",
                branch("Enterprise", VENDOR_UBIQUITI, "Switch",
                        uLeaf("Switch", "Enterprise Campus 24 PoE", "ECS-24-PoE (1050W)"),
                        uLeaf("Switch", "Enterprise Campus 48 PoE", "ECS-48-PoE (2150W)"),
                        uLeaf("Switch", "Enterprise Campus 24S PoE", "ECS-24S-PoE (1050W)"),
                        uLeaf("Switch", "Enterprise Campus 48S PoE", "ECS-48S-PoE (2150W)"),
                        uLeaf("Switch", "Enterprise Campus 24S", "ECS-24S"),
                        uLeaf("Switch", "Enterprise Campus 48S", "ECS-48S"),
                        uLeaf("Switch", "Enterprise Audio/Video XG 24 PoE", "EAV-XG-24-PoE (2150W)"),
                        uLeaf("Switch", "Enterprise Audio/Video Fiber", "EAV-Fiber"),
                        uLeaf("Switch", "Enterprise Campus Aggregation", "ECS-Aggregation"),
                        uLeaf("Switch", "Enterprise Campus Switch Core", "ECS-Core")
                ),
                branch("Professional Max & XG", VENDOR_UBIQUITI, "Switch",
                        uLeaf("Switch", "Pro Max 48 PoE", "USW-Pro-Max-48-PoE (720W)"),
                        uLeaf("Switch", "Pro Max 24 PoE", "USW-Pro-Max-24-PoE (400W)"),
                        uLeaf("Switch", "Pro XG 48 PoE", "USW-Pro-XG-48-PoE (1080W)"),
                        uLeaf("Switch", "Pro XG 24 PoE", "USW-Pro-XG-24-PoE (720W)"),
                        uLeaf("Switch", "Pro HD 24 PoE", "USW-Pro-HD-24-PoE (600W)"),
                        uLeaf("Switch", "Pro XG 48", "USW-Pro-XG-48"),
                        uLeaf("Switch", "Pro XG 24", "USW-Pro-XG-24"),
                        uLeaf("Switch", "Pro XG 10 PoE", "USW-Pro-XG-10-PoE (400W)"),
                        uLeaf("Switch", "Pro Max 16 PoE", "USW-Pro-Max-16-PoE (180W)"),
                        uLeaf("Switch", "Pro HD 24", "USW-Pro-HD-24"),
                        uLeaf("Switch", "Pro Max 48", "USW-Pro-Max-48"),
                        uLeaf("Switch", "Pro Max 24", "USW-Pro-Max-24"),
                        uLeaf("Switch", "Pro Max 16", "USW-Pro-Max-16")
                ),
                branch("Professional", VENDOR_UBIQUITI, "Switch",
                        uLeaf("Switch", "Pro 48 PoE", "USW-Pro-48-POE (600W)"),
                        uLeaf("Switch", "Pro 24 PoE", "USW-Pro-24-POE (400W)"),
                        uLeaf("Switch", "UPS PoE Switch", "USW-Mission-Critical (120W)")
                ),
                branch("Standard", VENDOR_UBIQUITI, "Switch",
                        uLeaf("Switch", "Standard 48 PoE", "USW-48-POE (195W)"),
                        uLeaf("Switch", "Standard 24 PoE", "USW-24-POE (95W)"),
                        uLeaf("Switch", "Standard 16 PoE", "USW-16-POE (42W)"),
                        uLeaf("Switch", "Standard 48", "USW-48"),
                        uLeaf("Switch", "Standard 24", "USW-24")
                ),
                branch("Utility", VENDOR_UBIQUITI, "Switch",
                        uLeaf("Switch", "Long-Range Switch 8 PoE", "USW-LR-Switch-8-PoE (630W)"),
                        uLeaf("Switch", "Lite 16 PoE", "USW-Lite-16-PoE (45W)"),
                        uLeaf("Switch", "Lite 8 PoE", "USW-Lite-8-PoE (52W)"),
                        uLeaf("Switch", "Pro XG 8 PoE", "USW-Pro-XG-8-PoE (155W)"),
                        uLeaf("Switch", "Enterprise 8 PoE (Vintage)", "USW-Enterprise-8-PoE (120W)"),
                        uLeaf("Switch", "Pro 8 PoE", "USW-Pro-8-PoE (120W)"),
                        uLeaf("Switch", "Ultra 210W", "USW-Ultra-210W (202W)"),
                        uLeaf("Switch", "Ultra 60W", "USW-Ultra-60W (52W)"),
                        uLeaf("Switch", "Ultra", "USW-Ultra (42W)"),
                        uLeaf("Switch", "Flex 2.5G PoE", "USW-Flex-2.5G-8-PoE (196W)"),
                        uLeaf("Switch", "Flex 2.5G", "USW-Flex-2.5G-8"),
                        uLeaf("Switch", "Flex Mini 2.5G", "USW-Flex-2.5G-5"),
                        uLeaf("Switch", "Flex Mini", "USW-Flex-Mini"),
                        uLeaf("Switch", "Flex", "USW-Flex (46W)"),
                        uLeaf("Switch", "Flex Utility", "USW-Flex-Utility"),
                        uLeaf("Switch", "Flex Utility Pro", "UACC-Flex-Utility-Pro"),
                        uLeaf("Switch", "Flex 10 GbE", "USW-Flex-XG"),
                        uLeaf("Switch", "Industrial DIN 2.5G 8 PoE", "USW-Industrial-DIN-2.5G-8-PoE (240W)"),
                        uLeaf("Switch", "Industrial DIN 8 PoE", "USW-Industrial-DIN-8-PoE (480W)"),
                        uLeaf("Switch", "Industrial DIN 8", "USW-Industrial-DIN-8")
                ),
                branch("Aggregation", VENDOR_UBIQUITI, "Switch",
                        uLeaf("Switch", "Enterprise Campus Aggregation", "ECS-Aggregation"),
                        uLeaf("Switch", "Pro XG Aggregation", "USW-Pro-XG-Aggregation"),
                        uLeaf("Switch", "Aggregation", "USW-Aggregation"),
                        uLeaf("Switch", "Hi-Capacity Aggregation", "USW-Pro-Aggregation")
                ),
                branch("WAN", VENDOR_UBIQUITI, "Switch",
                        uLeaf("Switch", "WAN Switch", "USW-WAN"),
                        uLeaf("Switch", "WAN Switch RJ45", "USW-WAN-RJ45")
                )
        ));

        categories.add(branch("WiFi", VENDOR_UBIQUITI, "Access Point",
                branch("Enterprise", VENDOR_UBIQUITI, "Access Point",
                        uLeaf("Access Point", "E7", "E7"),
                        uLeaf("Access Point", "E7 Audience", "E7-Audience"),
                        uLeaf("Access Point", "E7 Campus", "E7-Campus")
                ),
                branch("Flagship", VENDOR_UBIQUITI, "Access Point",
                        uLeaf("Access Point", "U7 Pro XGS", "U7-Pro-XGS"),
                        uLeaf("Access Point", "U7 Pro XG", "U7-Pro-XG"),
                        uLeaf("Access Point", "U7 Pro Max", "U7-Pro-Max"),
                        uLeaf("Access Point", "U7 Pro", "U7-Pro"),
                        uLeaf("Access Point", "U7 Long-Range", "U7-LR"),
                        uLeaf("Access Point", "U7 Lite", "U7-Lite"),
                        uLeaf("Access Point", "U6 Enterprise", "U6-Enterprise"),
                        uLeaf("Access Point", "U6 Pro", "U6-Pro"),
                        uLeaf("Access Point", "U6+", "U6+"),
                        uLeaf("Access Point", "AC Pro", "UAP-AC-PRO")
                ),
                branch("Wall", VENDOR_UBIQUITI, "Access Point",
                        uLeaf("Access Point", "U7 Pro XG Wall", "U7-Pro-XG-Wall"),
                        uLeaf("Access Point", "U7 Pro Wall", "U7-Pro-Wall"),
                        uLeaf("Access Point", "U7 In-Wall", "U7-IW"),
                        uLeaf("Access Point", "U6 Enterprise In-Wall", "U6-Enterprise-IW"),
                        uLeaf("Access Point", "U6 Extender", "U6-Extender"),
                        uLeaf("Access Point", "U6 In-Wall", "U6-IW")
                ),
                branch("Outdoor", VENDOR_UBIQUITI, "Access Point",
                        uLeaf("Access Point", "U7 Pro Outdoor", "U7-Pro-Outdoor"),
                        uLeaf("Access Point", "U7 Outdoor", "U7-Outdoor"),
                        uLeaf("Access Point", "U7 Mesh", "U7-Mesh"),
                        uLeaf("Access Point", "U7 Industrial", "U7-Industrial"),
                        uLeaf("Access Point", "U6 Mesh", "U6-Mesh"),
                        uLeaf("Access Point", "U6 Mesh Pro", "U6-Mesh-Pro"),
                        uLeaf("Access Point", "AC Mesh", "UAP-AC-M"),
                        uLeaf("Access Point", "Swiss Army Knife", "UK-Ultra")
                ),
                branch("Mega Capacity", VENDOR_UBIQUITI, "Access Point",
                        uLeaf("Access Point", "E7 Audience", "E7-Audience"),
                        uLeaf("Access Point", "WiFi BaseStation XG", "UWB-XG")
                ),
                branch("Bridging", VENDOR_UBIQUITI, "Access Point",
                        uLeaf("Access Point", "Device Bridge Pro Sector", "UDB-Pro-Sector"),
                        uLeaf("Access Point", "Device Bridge Pro", "UDB-Pro"),
                        uLeaf("Access Point", "Device Bridge", "UDB"),
                        uLeaf("Access Point", "Device Bridge IoT", "UDB-IoT"),
                        uLeaf("Access Point", "Device Bridge Switch", "UDB-Switch"),
                        uLeaf("Access Point", "Building Bridge XG", "UBB-XG"),
                        uLeaf("Access Point", "Building Bridge", "UBB")
                ),
                branch("Special Devices", VENDOR_UBIQUITI, "Access Point",
                        uLeaf("Access Point", "AirWire", "U-AirWire"),
                        uLeaf("Access Point", "UniFi Travel Router Long-Range", "UTR-LR"),
                        uLeaf("Access Point", "UniFi Travel Router", "UTR")
                ),
                branch("Whole Home WiFi", VENDOR_UBIQUITI, "Access Point",
                        uLeaf("Access Point", "Whole Home WiFi", "UX7")
                )
        ));

        categories.add(branch("Physical Security", VENDOR_UBIQUITI, "Sonstiges",
                branch("NVRs and Edge Devices", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "Enterprise NVR Core", "ENVR-Core"),
                        uLeaf("Sonstiges", "Enterprise NVR", "ENVR"),
                        uLeaf("Sonstiges", "Network Video Recorder G2 Pro", "UNVR-G2-Pro"),
                        uLeaf("Sonstiges", "Network Video Recorder G2", "UNVR-G2"),
                        uLeaf("Sonstiges", "Network Video Recorder Pro", "UNVR-Pro"),
                        uLeaf("Sonstiges", "Network Video Recorder", "UNVR"),
                        uLeaf("Sonstiges", "Network Video Recorder Instant", "UNVR-Instant"),
                        uLeaf("Sonstiges", "Network Video Recorder Instant Kit", "UNVR-Instant-Kit"),
                        uLeaf("Sonstiges", "AI Key", "AI-Key"),
                        uLeaf("Sonstiges", "AI Port", "UP-AI-Port"),
                        uLeaf("Sonstiges", "Protect Viewport", "UP-Viewport"),
                        uLeaf("Sonstiges", "CloudKey+", "UCK-G2-SSD")
                ),
                branch("Dome & Turret", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "G6 Edge Turret", "UVC-G6-Edge-Turret"),
                        uLeaf("Sonstiges", "G6 Edge Dome", "UVC-G6-Edge-Dome"),
                        uLeaf("Sonstiges", "G6 Pro Turret", "UVC-G6-Pro-Turret"),
                        uLeaf("Sonstiges", "G6 Pro Dome", "UVC-G6-Pro-Dome"),
                        uLeaf("Sonstiges", "G6 Turret", "UVC-G6-Turret"),
                        uLeaf("Sonstiges", "G6 Dome", "UVC-G6-Dome"),
                        uLeaf("Sonstiges", "G6 Mini Dome", "UVC-G6-Mini-Dome"),
                        uLeaf("Sonstiges", "G6 Pro 360", "UVC-G6-Pro-360"),
                        uLeaf("Sonstiges", "G6 180", "UVC-G6-180"),
                        uLeaf("Sonstiges", "AI Multi Sensor 4", "UVC-AI-MS-4"),
                        uLeaf("Sonstiges", "AI Multi Sensor 2", "UVC-AI-MS-2"),
                        uLeaf("Sonstiges", "AI Turret", "UVC-AI-Turret"),
                        uLeaf("Sonstiges", "AI Dome", "UVC-AI-Dome"),
                        uLeaf("Sonstiges", "AI 360", "UVC-AI-360"),
                        uLeaf("Sonstiges", "G5 Turret Ultra", "UVC-G5-Turret-Ultra"),
                        uLeaf("Sonstiges", "G5 Dome", "UVC-G5-Dome"),
                        uLeaf("Sonstiges", "G5 Dome Ultra", "UVC-G5-Dome-Ultra")
                ),
                branch("Bullet", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "G6 Pro Bullet", "UVC-G6-Pro-Bullet"),
                        uLeaf("Sonstiges", "G6 Bullet", "UVC-G6-Bullet"),
                        uLeaf("Sonstiges", "G6 Edge Bullet", "UVC-G6-Edge-Bullet"),
                        uLeaf("Sonstiges", "AI LPR", "UVC-AI-LPR"),
                        uLeaf("Sonstiges", "AI DSLR", "UVC-AI-DSLR"),
                        uLeaf("Sonstiges", "AI Pro", "UVC-AI-Pro"),
                        uLeaf("Sonstiges", "G5 Pro", "UVC-G5-Pro"),
                        uLeaf("Sonstiges", "G5 Bullet", "UVC-G5-Bullet")
                ),
                branch("Compact", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "G6 Instant", "UVC-G6-INS"),
                        uLeaf("Sonstiges", "Camera AI Theta Pro", "UVC-AI-Theta-Pro"),
                        uLeaf("Sonstiges", "G5 Flex", "UVC-G5-Flex"),
                        uLeaf("Sonstiges", "G4 Instant", "UVC-G4-INS"),
                        uLeaf("Sonstiges", "AI Theta Audio", "UVC-AI-Theta-Audio"),
                        uLeaf("Sonstiges", "AI Theta Pro Long-Distance Lens", "UVC-AI-Theta-ProLens50"),
                        uLeaf("Sonstiges", "G3 Instant", "UVC-G3-INS")
                ),
                branch("PTZ", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "G6 PTZ", "UVC-G6-PTZ"),
                        uLeaf("Sonstiges", "AI PTZ Industrial", "UVC-AI-PTZ"),
                        uLeaf("Sonstiges", "AI PTZ Precision", "UVC-AI-PTZ-Precision"),
                        uLeaf("Sonstiges", "G5 PTZ", "UVC-G5-PTZ")
                ),
                branch("Doorbells", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "G4 Doorbell Pro PoE Kit", "UVC-G4-Doorbell-Pro-PoE-Kit"),
                        uLeaf("Sonstiges", "G4 Doorbell Pro PoE", "UVC-G4-Doorbell-Pro-PoE"),
                        uLeaf("Sonstiges", "G4 Doorbell Pro", "UVC-G4-Doorbell-Pro"),
                        uLeaf("Sonstiges", "Doorbell Lite", "UVC-Doorbell-Lite-B"),
                        uLeaf("Sonstiges", "PoE Smart Chime", "UACC-Chime-PoE"),
                        uLeaf("Sonstiges", "Smart Chime (WiFi)", "UP-Chime")
                ),
                branch("Sensors & Alarms", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "SuperLink Gateway", "USL-Gateway"),
                        uLeaf("Sonstiges", "Entry Sensor", "USL-Entry"),
                        uLeaf("Sonstiges", "Recessed Entry Sensor", "USL-Entry-R"),
                        uLeaf("Sonstiges", "Motion Sensor", "USL-Motion"),
                        uLeaf("Sonstiges", "Glass Break Sensor", "USL-GlassBreak"),
                        uLeaf("Sonstiges", "Environmental Sensor", "USL-Environmental"),
                        uLeaf("Sonstiges", "Smoke and CO Alarm", "USL-Smoke"),
                        uLeaf("Sonstiges", "Vape Detection & Air Quality Sensor", "UP-AirQuality"),
                        uLeaf("Sonstiges", "Relay", "USL-Relay"),
                        uLeaf("Sonstiges", "Remote Control KeyFob", "USL-Fob"),
                        uLeaf("Sonstiges", "Alarm Hub Kit", "UP-AlarmHub-Kit"),
                        uLeaf("Sonstiges", "Siren PoE", "UP-Siren-PoE"),
                        uLeaf("Sonstiges", "Siren", "USL-Siren"),
                        uLeaf("Sonstiges", "AI Speaker", "UP-AI-Speaker"),
                        uLeaf("Sonstiges", "AI Horn Speaker", "UP-AI-Horn-Speaker"),
                        uLeaf("Sonstiges", "All-In-One Sensor", "UP-Sense"),
                        uLeaf("Sonstiges", "SuperLink High-Gain Antenna", "UACC-USL-ANT-HG")
                )
        ));

        categories.add(branch("Door Access", VENDOR_UBIQUITI, "Sonstiges",
                branch("Readers", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "G6 Pro Entry", "UVC-G6-Pro-Entry"),
                        uLeaf("Sonstiges", "G6 Entry", "UVC-G6-Entry"),
                        uLeaf("Sonstiges", "G3 Reader Pro", "UA-G3-Pro"),
                        uLeaf("Sonstiges", "G2 Reader Pro", "UA-G2-Pro"),
                        uLeaf("Sonstiges", "G3 Reader Flex", "UA-G3-Flex"),
                        uLeaf("Sonstiges", "G3 Reader Fingerprint", "UA-G3-Fingerprint"),
                        uLeaf("Sonstiges", "G3 Reader", "UA-G3"),
                        uLeaf("Sonstiges", "G2 Reader", "UA-G2"),
                        uLeaf("Sonstiges", "Retrofit Reader", "UA-Retrofit-Reader"),
                        uLeaf("Sonstiges", "Retrofit Reader Fingerprint", "UA-Retrofit-Reader-Fingerprint"),
                        uLeaf("Sonstiges", "G3 Intercom", "UA-G3-Intercom"),
                        uLeaf("Sonstiges", "Intercom", "UA-Intercom"),
                        uLeaf("Sonstiges", "Access Ultra", "UA-Ultra")
                ),
                branch("Hubs", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "Enterprise Access Hub", "EAH-8"),
                        uLeaf("Sonstiges", "Retrofit Hub", "UA-Retrofit-Hub-2"),
                        uLeaf("Sonstiges", "Door Hub", "UA-Hub-Door"),
                        uLeaf("Sonstiges", "Door Hub Mini", "UA-Hub-Door-Mini"),
                        uLeaf("Sonstiges", "Gate Hub", "UA-Hub-Gate"),
                        uLeaf("Sonstiges", "Access Ultra", "UA-Ultra")
                ),
                branch("Kits", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "G3 Starter Kit Pro", "UA-G3-SK-Pro"),
                        uLeaf("Sonstiges", "G2 Starter Kit Pro", "UA-G2-SK-Pro"),
                        uLeaf("Sonstiges", "G3 Starter Kit", "UA-G3-SK"),
                        uLeaf("Sonstiges", "G2 Starter Kit", "UA-G2-SK"),
                        uLeaf("Sonstiges", "G3 Gate Starter Kit", "UA-G3-SK-Gate"),
                        uLeaf("Sonstiges", "Gate Starter Kit", "UA-SK-Gate"),
                        uLeaf("Sonstiges", "G3 Elevator Starter Kit", "UA-G3-SK-Elevator")
                ),
                branch("Intercoms", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "G3 Intercom", "UA-G3-Intercom"),
                        uLeaf("Sonstiges", "Intercom", "UA-Intercom"),
                        uLeaf("Sonstiges", "Intercom Viewer", "UA-Intercom-Viewer")
                ),
                branch("NVRs", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "Enterprise NVR Core", "ENVR-Core"),
                        uLeaf("Sonstiges", "Enterprise NVR", "ENVR"),
                        uLeaf("Sonstiges", "Network Video Recorder G2 Pro", "UNVR-G2-Pro"),
                        uLeaf("Sonstiges", "Network Video Recorder G2", "UNVR-G2"),
                        uLeaf("Sonstiges", "Network Video Recorder Pro", "UNVR-Pro"),
                        uLeaf("Sonstiges", "Network Video Recorder", "UNVR"),
                        uLeaf("Sonstiges", "Network Video Recorder Instant", "UNVR-Instant"),
                        uLeaf("Sonstiges", "Network Video Recorder Instant Kit", "UNVR-Instant-Kit")
                ),
                branch("Door Access Accessories", VENDOR_UBIQUITI, "Kabel/Zubehör",
                        uLeaf("Kabel/Zubehör", "Access Card", "UA-Card"),
                        uLeaf("Kabel/Zubehör", "PoE Over 2-Wire Retrofit Extender", "UACC-Retrofit-PoE-2Wire"),
                        uLeaf("Kabel/Zubehör", "Reader Junction Box", "UACC-Reader-JB"),
                        uLeaf("Kabel/Zubehör", "Reader Pro Junction Box", "UACC-Reader-Pro-JB"),
                        uLeaf("Kabel/Zubehör", "Reader Pro Angle Mount", "UACC-Reader-Pro-AM"),
                        uLeaf("Kabel/Zubehör", "Intercom Surface Angle Mount", "UACC-Intercom-SAM"),
                        uLeaf("Kabel/Zubehör", "Intercom Flush Mount", "UACC-Intercom-FM"),
                        uLeaf("Kabel/Zubehör", "Intercom Sunshield", "UACC-Intercom-Sunshield"),
                        uLeaf("Kabel/Zubehör", "Intercom Wedge Mount", "UACC-Intercom-WM"),
                        uLeaf("Kabel/Zubehör", "Intercom Viewer Table Stand", "UACC-Intercom-Viewer-TS"),
                        uLeaf("Kabel/Zubehör", "Junction Utility", "UACC-Junction-Utility"),
                        uLeaf("Kabel/Zubehör", "Retrofit PSU 12V", "UACC-Retrofit-PSU-12V"),
                        uLeaf("Kabel/Zubehör", "Access Button", "UA-Button"),
                        uLeaf("Kabel/Zubehör", "Door Closer", "UACC-DoorCloser"),
                        uLeaf("Kabel/Zubehör", "Panic Bar", "UACC-PanicBar"),
                        uLeaf("Kabel/Zubehör", "Magnetic Lock", "UA-Lock-Magnetic"),
                        uLeaf("Kabel/Zubehör", "Electric Locks", "Electric Locks"),
                        uLeaf("Kabel/Zubehör", "Door Lock Relay Cable", "UACC-Cable-DoorLockRelay"),
                        uLeaf("Kabel/Zubehör", "Pocket Keyfob, 10-Pack", "UA-Pocket"),
                        uLeaf("Kabel/Zubehör", "Access Rescue KeySwitch", "UA-Rescue")
                )
        ));

        categories.add(branch("Integrations", VENDOR_UBIQUITI, "Sonstiges",
                branch("Network Storage", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "Enterprise NAS", "ENAS"),
                        uLeaf("Sonstiges", "UNAS Pro 8", "UNAS-Pro-8"),
                        uLeaf("Sonstiges", "UNAS Pro 4", "UNAS-Pro-4"),
                        uLeaf("Sonstiges", "UNAS Pro", "UNAS-Pro"),
                        uLeaf("Sonstiges", "UNAS 4", "UNAS-4"),
                        uLeaf("Sonstiges", "UNAS 2", "UNAS-2")
                ),
                branch("Power Tech", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "UPS 2U Pro", "UPS-2U-Pro"),
                        uLeaf("Sonstiges", "UPS 2U", "UPS-2U"),
                        uLeaf("Sonstiges", "UPS Tower", "UPS-Tower"),
                        uLeaf("Sonstiges", "UPS PoE Switch (Mission Critical)", "USW-Mission-Critical"),
                        uLeaf("Sonstiges", "Redundant Power", "USP-RPS")
                ),
                branch("LTE/5G", VENDOR_UBIQUITI, "Router",
                        uLeaf("Router", "UniFi 5G Max", "U5G-Max"),
                        uLeaf("Router", "UniFi 5G Max Outdoor", "U5G-Max-Outdoor"),
                        uLeaf("Router", "UniFi 5G Backup", "U5G"),
                        uLeaf("Router", "Mobile Router Industrial", "UMR-Industrial"),
                        uLeaf("Router", "Mobile Router Ultra", "UMR-Ultra"),
                        uLeaf("Router", "Mobile Router", "UMR"),
                        uLeaf("Router", "LTE Backup Pro", "U-LTE-Backup-Pro")
                ),
                branch("Managed VoIP", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "G3 Touch Enterprise", "UTP-G3-Touch-Enterprise"),
                        uLeaf("Sonstiges", "G3 Touch Pro", "UTP-G3-Touch-Pro"),
                        uLeaf("Sonstiges", "G3 Touch Wall", "UTP-G3-Touch-Wall"),
                        uLeaf("Sonstiges", "G2 Touch Max", "UTP-TouchMax"),
                        uLeaf("Sonstiges", "G2 Touch", "UTP-Touch"),
                        uLeaf("Sonstiges", "G3 Wireless Handset", "UT-G3-Handset")
                ),
                branch("Premium IoT", VENDOR_UBIQUITI, "Sonstiges",
                        uLeaf("Sonstiges", "Enterprise Audio/Video Bridge", "EAV-Bridge"),
                        uLeaf("Sonstiges", "EV Station Lite", "UC-EV-Station-Lite"),
                        uLeaf("Sonstiges", "PowerAmp", "UPL-Amp"),
                        uLeaf("Sonstiges", "PoE Audio Port", "UPL-Port"),
                        uLeaf("Sonstiges", "Display Cast Pro", "UC-Cast-Pro"),
                        uLeaf("Sonstiges", "Display Cast Lite", "UC-Cast-Lite")
                )
        ));

        categories.add(branch("Accessories", VENDOR_UBIQUITI, "Kabel/Zubehör",
                branch("SFP & Fiber", VENDOR_UBIQUITI, "Kabel/Zubehör",
                        uLeaf("Kabel/Zubehör", "10G SFP+ Direct Attach Cable", "UACC-DAC-SFP10"),
                        uLeaf("Kabel/Zubehör", "25G SFP28 Direct Attach Cable", "UACC-DAC-SFP28"),
                        uLeaf("Kabel/Zubehör", "100G QSFP28 Direct Attach Cable", "UACC-DAC-QSFP28"),
                        uLeaf("Kabel/Zubehör", "10G SFP+ to RJ45 Adapter", "UACC-CM-RJ45-MG"),
                        uLeaf("Kabel/Zubehör", "10G Multi-Mode Optical Module", "UACC-OM-MM-10G"),
                        uLeaf("Kabel/Zubehör", "10G Single-Mode Optical Module", "UACC-OM-SM-10G"),
                        uLeaf("Kabel/Zubehör", "25G Multi-Mode Optical Module", "UACC-OM-SFP28-SR")
                ),
                branch("RJ45 & Copper", VENDOR_UBIQUITI, "Kabel/Zubehör",
                        uLeaf("Kabel/Zubehör", "Etherlighting Patch Cable", "UACC-Cable-Patch-EL"),
                        uLeaf("Kabel/Zubehör", "Ultra Thin Patch Cable", "UACC-Cable-Patch-Ultra"),
                        uLeaf("Kabel/Zubehör", "Outdoor Patch Cable", "UACC-Cable-Patch-Outdoor"),
                        uLeaf("Kabel/Zubehör", "Cat6A CMP Cable", "UACC-Cable-C6A-CMP"),
                        uLeaf("Kabel/Zubehör", "Ethernet Surge Protector", "ETH-SP-G2")
                ),
                branch("PoE & Power", VENDOR_UBIQUITI, "Kabel/Zubehör",
                        uLeaf("Kabel/Zubehör", "PoE++ Adapter 60W", "U-PoE++"),
                        uLeaf("Kabel/Zubehör", "PoE+ Adapter 30W", "U-PoE+"),
                        uLeaf("Kabel/Zubehör", "PoE Adapter 15W", "U-PoE"),
                        uLeaf("Kabel/Zubehör", "2.5G PoE+ Adapter 30W", "UACC-PoE+-2.5G"),
                        uLeaf("Kabel/Zubehör", "10G PoE++ Adapter 60W", "UACC-PoE++-10G"),
                        uLeaf("Kabel/Zubehör", "SmartPower Strip", "USP-Strip"),
                        uLeaf("Kabel/Zubehör", "SmartPower Cable", "USP-Cable")
                ),
                branch("Rack Mount", VENDOR_UBIQUITI, "Kabel/Zubehör",
                        uLeaf("Kabel/Zubehör", "Toolless Mini Rack", "U-Rack-6U-TL"),
                        uLeaf("Kabel/Zubehör", "24-Port Blank Keystone Patch Panel", "UACC-Rack-Panel-Patch-Blank-24"),
                        uLeaf("Kabel/Zubehör", "1U OCD Brush Panel", "UACC-Rack-Panel-Brush-1U"),
                        uLeaf("Kabel/Zubehör", "1U Blank Vented Panel", "UACC-Rack-Panel-Vented-1U"),
                        uLeaf("Kabel/Zubehör", "Rack Mount Blank Shelf", "UACC-Rack-Shelf-TL")
                ),
                branch("Storage & Hosting", VENDOR_UBIQUITI, "Kabel/Zubehör",
                        uLeaf("Kabel/Zubehör", "CloudKey+", "UCK-G2-SSD"),
                        uLeaf("Kabel/Zubehör", "CloudKey Enterprise", "CK-Enterprise"),
                        uLeaf("Kabel/Zubehör", "8 TB HDD Storage", "UACC-HDD-E-8TB"),
                        uLeaf("Kabel/Zubehör", "16 TB HDD Storage", "UACC-HDD-E-16TB"),
                        uLeaf("Kabel/Zubehör", "24 TB HDD Storage", "UACC-HDD-E-24TB")
                )
        ));

        return new CatalogNode("Ubiquiti", null, VENDOR_UBIQUITI, null, categories);
    }

    private static CatalogNode branch(@NonNull String label,
                                      @NonNull String defaultVendor,
                                      @NonNull String defaultCategory,
                                      @NonNull CatalogNode... childNodes) {
        List<CatalogNode> children = new ArrayList<>(childNodes.length);
        Collections.addAll(children, childNodes);
        return new CatalogNode(label, null, defaultVendor, defaultCategory, children);
    }

    private static CatalogNode uLeaf(@NonNull String defaultCategory,
                                     @NonNull String modelName,
                                     @NonNull String sku) {
        String label = modelName.equalsIgnoreCase(sku)
                ? modelName
                : modelName + " (" + sku + ")";
        String fullName = modelName.startsWith("UniFi ")
                ? "Ubiquiti " + label
                : "Ubiquiti UniFi " + label;
        return new CatalogNode(label, fullName, VENDOR_UBIQUITI, defaultCategory, Collections.emptyList());
    }
}
