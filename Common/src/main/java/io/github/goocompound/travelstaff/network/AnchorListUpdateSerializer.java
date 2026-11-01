// Modified by goocompound.
package io.github.goocompound.travelstaff.network;

import io.github.goocompound.travelstaff.TravelStaffMain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class AnchorListUpdateSerializer {
    public record AnchorListUpdateMessage(CompoundTag nbt) implements CustomPacketPayload {
        public static final Type<AnchorListUpdateMessage> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath(TravelStaffMain.MOD_ID, "sync_anchor_list"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AnchorListUpdateMessage> CODEC =
                StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, AnchorListUpdateMessage::nbt,
                        AnchorListUpdateMessage::new);

        @Override
        public Type<AnchorListUpdateMessage> type() {
            return TYPE;
        }
    }
}
