// Modified by goocompound.
package io.github.goocompound.travelstaff.network;

import io.github.goocompound.travelstaff.TravelStaffMain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class ClientEventSerializer {
    public enum ClientEvent {
        ELEVATOR_UP,
        EMPTY_HAND_INTERACT,
        ELEVATOR_DOWN,
        ANCHOR_TP
    }

    public record ClientEventMessage(ClientEvent event) implements CustomPacketPayload {
        public static final Type<ClientEventMessage> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath(TravelStaffMain.MOD_ID, "clientevent_to_server"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientEventMessage> CODEC =
                StreamCodec.composite(ByteBufCodecs.STRING_UTF8.map(ClientEvent::valueOf, ClientEvent::name),
                        ClientEventMessage::event, ClientEventMessage::new);

        @Override
        public Type<ClientEventMessage> type() {
            return TYPE;
        }
    }
}
