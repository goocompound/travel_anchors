package io.github.goocompound.travelstaff.config;

import io.github.goocompound.travelstaff.TravelStaffMain.ClientConfigData;
import io.github.goocompound.travelstaff.TravelStaffMain.SyncedConfigData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TravelStaffConfigTest {
    @TempDir
    Path directory;

    @Test
    void readsLegacyCommentsAndUnquotedKeysWithDefaultsForMissingFields() throws Exception {
        Path path = directory.resolve("travelstaff-synced.json5");
        Files.writeString(path, """
                {
                  // Existing WrapperLib files use lenient Gson syntax.
                  maxTeleportDistance: 96,
                  'staffCooldownTicks': 12,
                  extraSetting: 'ignored'
                }
                """);

        SyncedConfigData config = TravelStaffConfig.read(path, null,
                TravelStaffConfig.serverDefaults(), SyncedConfigData.class, new SyncedConfigData());

        assertEquals(96, config.maxTeleportDistance);
        assertEquals(12, config.staffCooldownTicks);
        assertEquals(7, config.shortTeleportDistance);
        assertEquals(0.5F, config.rangeEnchantScaling);
        assertEquals(30.0, config.maxAngle);
    }

    @Test
    void keepsExistingFileBytesAndCommentsEvenWhenATemplateExists() throws Exception {
        Path path = directory.resolve("travelstaff-client.json5");
        byte[] existing = "{\r\n  // My preference: élévateur disabled\r\n  isElevatorMode: false\r\n}\r\n"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(path, existing);
        Path template = directory.resolve("template.json5");
        Files.writeString(template, "{isElevatorMode: true}");

        ClientConfigData config = TravelStaffConfig.read(path, template,
                TravelStaffConfig.clientDefaults(), ClientConfigData.class, new ClientConfigData());

        assertFalse(config.isElevatorMode);
        assertArrayEquals(existing, Files.readAllBytes(path));
    }

    @Test
    void copiesDefaultConfigTemplateIntoANewWorldWithoutReformatting() throws Exception {
        Path template = directory.resolve("defaultconfigs/travelstaff-synced.json5");
        Files.createDirectories(template.getParent());
        byte[] defaults = "{ // Pack defaults\n  maxTeleportDistance: 128, shortTeleportDistance: 9\n}\n"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(template, defaults);
        Path path = directory.resolve("new-world/serverconfig/travelstaff-synced.json5");

        SyncedConfigData config = TravelStaffConfig.read(path, template,
                TravelStaffConfig.serverDefaults(), SyncedConfigData.class, new SyncedConfigData());

        assertEquals(128, config.maxTeleportDistance);
        assertEquals(9, config.shortTeleportDistance);
        assertEquals(20, config.staffCooldownTicks);
        assertArrayEquals(defaults, Files.readAllBytes(path));
        assertArrayEquals(defaults, Files.readAllBytes(template));
    }

    @Test
    void createsReadableCommentedDefaultFilesWhenNoTemplateExists() throws Exception {
        Path path = directory.resolve("new-world/serverconfig/travelstaff-synced.json5");
        Path missingTemplate = directory.resolve("defaultconfigs/travelstaff-synced.json5");

        SyncedConfigData config = TravelStaffConfig.read(path, missingTemplate,
                TravelStaffConfig.serverDefaults(), SyncedConfigData.class, new SyncedConfigData());

        assertTrue(Files.readString(path).contains("//"));
        assertEquals(64, config.maxTeleportDistance);
        assertEquals(7, config.shortTeleportDistance);
        assertEquals(20, config.staffCooldownTicks);
        assertEquals(0.5F, config.rangeEnchantScaling);
        assertEquals(30.0, config.maxAngle);
        assertFalse(Files.exists(missingTemplate));

        Path clientPath = directory.resolve("config/travelstaff-client.json5");
        ClientConfigData client = TravelStaffConfig.read(clientPath, null,
                TravelStaffConfig.clientDefaults(), ClientConfigData.class, new ClientConfigData());

        assertTrue(client.isElevatorMode);
        assertTrue(Files.readString(clientPath).contains("//"));
    }
}
