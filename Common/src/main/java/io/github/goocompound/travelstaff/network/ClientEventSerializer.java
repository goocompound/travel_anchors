// Modified by goocompound.
package io.github.goocompound.travelstaff.network;

import net.minecraft.network.FriendlyByteBuf;

public class ClientEventSerializer{
    public static void encode(ClientEvent msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.name());
    }

    public static ClientEvent decode(FriendlyByteBuf buffer) {
        return ClientEvent.valueOf(buffer.readUtf(32767));
    }

    public enum ClientEvent {
        ELEVATOR_UP,
        EMPTY_HAND_INTERACT,
        ELEVATOR_DOWN,
        ANCHOR_TP
    }
}
