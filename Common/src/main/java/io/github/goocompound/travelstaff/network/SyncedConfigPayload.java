package io.github.goocompound.travelstaff.network;

import io.github.goocompound.travelstaff.TravelStaffMain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncedConfigPayload(int maxTeleportDistance, int shortTeleportDistance,
                                  int staffCooldownTicks, float rangeEnchantScaling,
                                  double maxAngle) implements CustomPacketPayload {
    public static final Type<SyncedConfigPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TravelStaffMain.MOD_ID, "synced_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncedConfigPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeInt(payload.maxTeleportDistance);
                buffer.writeInt(payload.shortTeleportDistance);
                buffer.writeInt(payload.staffCooldownTicks);
                buffer.writeFloat(payload.rangeEnchantScaling);
                buffer.writeDouble(payload.maxAngle);
            },
            buffer -> new SyncedConfigPayload(buffer.readInt(), buffer.readInt(), buffer.readInt(),
                    buffer.readFloat(), buffer.readDouble()));

    public static SyncedConfigPayload from(TravelStaffMain.SyncedConfigData config) {
        return new SyncedConfigPayload(config.maxTeleportDistance, config.shortTeleportDistance,
                config.staffCooldownTicks, config.rangeEnchantScaling, config.maxAngle);
    }

    public TravelStaffMain.SyncedConfigData toConfig() {
        var config = new TravelStaffMain.SyncedConfigData();
        config.maxTeleportDistance = maxTeleportDistance;
        config.shortTeleportDistance = shortTeleportDistance;
        config.staffCooldownTicks = staffCooldownTicks;
        config.rangeEnchantScaling = rangeEnchantScaling;
        config.maxAngle = maxAngle;
        return config;
    }

    @Override
    public Type<SyncedConfigPayload> type() {
        return TYPE;
    }
}
