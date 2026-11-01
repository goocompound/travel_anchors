// Modified by goocompound.
package io.github.goocompound.travelstaff.network;

import io.github.goocompound.travelstaff.TravelStaffMain;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class AnchorNameChangeSerializer {
    public record AnchorNameChangeMessage(BlockPos pos, String name) implements CustomPacketPayload {
        public static final Type<AnchorNameChangeMessage> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath(TravelStaffMain.MOD_ID, "anchor_name_change"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AnchorNameChangeMessage> CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, AnchorNameChangeMessage::pos,
                        ByteBufCodecs.stringUtf8(32767), AnchorNameChangeMessage::name,
                        AnchorNameChangeMessage::new);

        @Override
        public Type<AnchorNameChangeMessage> type() {
            return TYPE;
        }
    }
}
