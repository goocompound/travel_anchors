package io.github.goocompound.travelstaff.config;

import io.github.goocompound.travelstaff.TravelStaffMain;
import io.github.goocompound.travelstaff.TravelStaffMain.ClientConfigData;
import io.github.goocompound.travelstaff.TravelStaffMain.SyncedConfigData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TravelStaffConfig {
    // Existing .json5 files were read by WrapperLib with lenient Gson.
    private static final Gson GSON = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
    private static final String CLIENT_FILE = "travelstaff-client.json5";
    private static final String SERVER_FILE = "travelstaff-synced.json5";

    private static ClientConfigData clientConfig = new ClientConfigData();
    private static SyncedConfigData serverConfig = new SyncedConfigData();
    private static SyncedConfigData syncedConfig = new SyncedConfigData();
    private static boolean clientLoaded;
    private static MinecraftServer activeServer;

    private TravelStaffConfig() {
    }

    public static ClientConfigData getClientConfig() {
        if (!clientLoaded) loadClient();
        return clientConfig;
    }

    public static SyncedConfigData getServerConfig() {
        return serverConfig;
    }

    public static SyncedConfigData getSyncedConfig() {
        return syncedConfig;
    }

    /** Prefer an explicit logical-side getter when a level is available. */
    public static SyncedConfigData getConfig() {
        return activeServer == null ? syncedConfig : serverConfig;
    }

    public static void loadClient() {
        Path path = Path.of("config", CLIENT_FILE);
        clientConfig = read(path, null, clientDefaults(), ClientConfigData.class, clientConfig);
        clientLoaded = true;
    }

    public static void startServer(MinecraftServer server) {
        activeServer = server;
        reloadServer(server);
    }

    public static void reloadServer(MinecraftServer server) {
        Path path = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(SERVER_FILE);
        Path template = Path.of("defaultconfigs", SERVER_FILE);
        serverConfig = read(path, template, serverDefaults(), SyncedConfigData.class, serverConfig);
    }

    public static void stopServer(MinecraftServer server) {
        if (activeServer == server) activeServer = null;
    }

    public static void applySynced(SyncedConfigData received) {
        // Integrated clients must not share a mutable config object with their server.
        SyncedConfigData copy = new SyncedConfigData();
        copy.maxTeleportDistance = received.maxTeleportDistance;
        copy.shortTeleportDistance = received.shortTeleportDistance;
        copy.staffCooldownTicks = received.staffCooldownTicks;
        copy.rangeEnchantScaling = received.rangeEnchantScaling;
        copy.maxAngle = received.maxAngle;
        syncedConfig = copy;
    }

    public static void resetClient() {
        syncedConfig = new SyncedConfigData();
    }

    static <T> T read(Path path, Path template, String defaults, Class<T> type, T previous) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                if (template != null && Files.exists(template)) {
                    Files.copy(template, path);
                } else {
                    Files.writeString(path, defaults, StandardCharsets.UTF_8);
                }
            }
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, type);
            }
        } catch (IOException exception) {
            TravelStaffMain.LOG.error("Failed to load config from {}", path.toAbsolutePath(), exception);
            return previous;
        }
    }

    static String clientDefaults() {
        ClientConfigData defaults = new ClientConfigData();
        return """
                {
                  // When true, while standing on an anchor, you can use space to go to an anchor directly above you or shift to go directly down. When false you must actually look at the target anchor to teleport.
                  "isElevatorMode": %s
                }
                """.formatted(defaults.isElevatorMode);
    }

    static String serverDefaults() {
        SyncedConfigData defaults = new SyncedConfigData();
        return """
                {
                  // The maximum distance you can teleport to an anchor (with no range enchantment).
                  "maxTeleportDistance": %s,
                  // The maximum distance you can short-range teleport with shift-click. This will not be scaled by the range enchantment.
                  "shortTeleportDistance": %s,
                  // How long between uses of the staff of travelling (or item with teleportation enchantment)
                  "staffCooldownTicks": %s,
                  // How much farther does each level of range enchantment allow you to teleport to an anchor. dist = maxTeleportDistance * (1 + (lvl * rangeEnchantScaling))
                  "rangeEnchantScaling": %s,
                  // The maximum angle you can look at the anchor to teleport.
                  "maxAngle": %s
                }
                """.formatted(defaults.maxTeleportDistance, defaults.shortTeleportDistance,
                defaults.staffCooldownTicks, defaults.rangeEnchantScaling, defaults.maxAngle);
    }
}
