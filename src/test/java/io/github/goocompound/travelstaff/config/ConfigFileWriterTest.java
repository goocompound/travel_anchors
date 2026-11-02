package io.github.goocompound.travelstaff.config;

import io.github.goocompound.travelstaff.TravelStaffMain.ClientConfigData;
import io.github.goocompound.travelstaff.TravelStaffMain.SyncedConfigData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigFileWriterTest {
    private static final Gson GSON = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
    @TempDir Path directory;

    @Test
    void changesOnlyValuesAndKeepsCommentsUnknownFieldsAndCrLf() throws Exception {
        Path path = directory.resolve("client.json5");
        String source = "{\r\n  // isElevatorMode: true, nameplateScale: 9 — élévateur\r\n"
                + "  isElevatorMode: true, /* keep this */\r\n  'nameplateScale': 0.8,\r\n"
                + "  unknown: {text: 'nameplateScale: 9 // not a comment', list: [1, {x: '}'}]}\r\n}\r\n";
        Files.writeString(path, source);
        ClientConfigData values = new ClientConfigData();
        values.isElevatorMode = false;
        values.nameplateScale = 0.65;
        ConfigFileWriter.write(path, TravelStaffConfig.clientDefaults(), values);
        assertEquals(source.replace("isElevatorMode: true, /*", "isElevatorMode: false, /*")
                .replace("'nameplateScale': 0.8", "'nameplateScale': 0.65"), Files.readString(path));
    }

    @Test
    void addsMissingKeysWithoutSwallowingThemInTrailingComments() {
        for (String source : new String[]{"{}", "{isElevatorMode:false}",
                "{isElevatorMode:false // keep\n}", "{ /* empty config */ }",
                "{isElevatorMode:false /* keep */}", "{isElevatorMode:false # keep\r\n}",
                "{unknown:[{nested:true}, 'text']}"}) {
            ClientConfigData values = new ClientConfigData();
            values.isElevatorMode = false;
            String updated = ConfigFileWriter.update(source, GSON.toJsonTree(values).getAsJsonObject());
            ClientConfigData read = GSON.fromJson(updated, ClientConfigData.class);
            assertFalse(read.isElevatorMode, source);
            assertEquals(0.7, read.nameplateScale, source);
            if (source.contains("keep")) assertTrue(updated.contains("keep"), source);
            if (source.contains("unknown")) assertTrue(GSON.fromJson(updated, JsonObject.class).has("unknown"));
        }
    }

    @Test
    void supportsLegacySeparatorsEscapedKeysAndCommentLikeStrings() {
        String source = "{isElevatorMode=>true; 'nameplate\\u0053cale'=0.8;"
                + "unknown: ['quoted \\\" text', '//', '#', '/*', {deep:[1,2]}]}";
        ClientConfigData values = new ClientConfigData();
        values.nameplateScale = 1.2;
        String updated = ConfigFileWriter.update(source, GSON.toJsonTree(values).getAsJsonObject());
        assertTrue(updated.contains("'nameplate\\u0053cale'=1.2"));
        assertEquals(GSON.fromJson(source, JsonObject.class).get("unknown"), GSON.fromJson(updated, JsonObject.class).get("unknown"));
    }

    @Test
    void leavesUnchangedFilesByteForByteIncludingNumberFormatting() throws Exception {
        Path path = directory.resolve("client.json5");
        String source = "\ufeff{ /* retain */ isElevatorMode:true, nameplateScale:8e-1 }\r\n";
        Files.writeString(path, source);
        byte[] original = Files.readAllBytes(path);
        ClientConfigData values = new ClientConfigData();
        values.nameplateScale = 0.8;
        ConfigFileWriter.write(path, TravelStaffConfig.clientDefaults(), values);
        assertArrayEquals(original, Files.readAllBytes(path));
    }

    @Test
    void updatesDuplicateSettingsConsistently() {
        String source = "{nameplateScale:0.6, nameplateScale:0.7, isElevatorMode:true}";
        String updated = ConfigFileWriter.update(source, GSON.toJsonTree(new ClientConfigData()).getAsJsonObject());
        assertEquals("{nameplateScale:0.7, nameplateScale:0.7, isElevatorMode:true}", updated);
    }

    @Test
    void refusesMalformedOrNonObjectFilesWithoutReplacingThem() throws Exception {
        Path path = directory.resolve("client.json5");
        for (String source : new String[]{"{", "null", "[]", "{isElevatorMode:true,}", "{a:1} trailing"}) {
            Files.writeString(path, source);
            assertThrows(IOException.class, () -> ConfigFileWriter.write(path, "{}", new ClientConfigData()), source);
            assertEquals(source, Files.readString(path));
        }
        try (var entries = Files.list(directory)) { assertEquals(1, entries.count()); }
    }

    @Test
    void createsCommentedDefaultsAndKeepsWorldFilesSeparate() throws Exception {
        Path first = directory.resolve("first/serverconfig/travelstaff-synced.json5");
        Path second = directory.resolve("second/serverconfig/travelstaff-synced.json5");
        SyncedConfigData values = new SyncedConfigData();
        values.maxTeleportDistance = 96;
        ConfigFileWriter.write(first, TravelStaffConfig.serverDefaults(), values);
        ConfigFileWriter.write(second, TravelStaffConfig.serverDefaults(), new SyncedConfigData());
        assertTrue(Files.readString(first).contains("//"));
        assertEquals(96, GSON.fromJson(Files.readString(first), SyncedConfigData.class).maxTeleportDistance);
        assertEquals(64, GSON.fromJson(Files.readString(second), SyncedConfigData.class).maxTeleportDistance);
    }

    @Test
    void validatesUiWorldValuesWithoutChangingLegacyDefaults() {
        SyncedConfigData values = new SyncedConfigData();
        assertDoesNotThrow(() -> TravelStaffConfig.validateServer(values));
        values.shortTeleportDistance = 0;
        values.staffCooldownTicks = 0;
        assertDoesNotThrow(() -> TravelStaffConfig.validateServer(values));
        values.rangeEnchantScaling = Float.NaN;
        assertThrows(IllegalArgumentException.class, () -> TravelStaffConfig.validateServer(values));
        values.rangeEnchantScaling = 0.5F;
        values.maxAngle = Double.POSITIVE_INFINITY;
        assertThrows(IllegalArgumentException.class, () -> TravelStaffConfig.validateServer(values));
        values.maxAngle = 181;
        assertThrows(IllegalArgumentException.class, () -> TravelStaffConfig.validateServer(values));
        values.maxAngle = 30;
        values.maxTeleportDistance = -1;
        assertThrows(IllegalArgumentException.class, () -> TravelStaffConfig.validateServer(values));
    }

    @Test
    void syncedSettingsDoNotAliasTheServerDraft() {
        SyncedConfigData values = new SyncedConfigData();
        values.maxTeleportDistance = 96;
        TravelStaffConfig.applySynced(values);
        values.maxTeleportDistance = 128;
        assertEquals(96, TravelStaffConfig.getSyncedConfig().maxTeleportDistance);
        TravelStaffConfig.resetClient();
    }
}
