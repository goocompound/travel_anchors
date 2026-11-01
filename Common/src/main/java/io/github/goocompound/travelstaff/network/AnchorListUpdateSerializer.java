// Modified by goocompound.
package io.github.goocompound.travelstaff.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class AnchorListUpdateSerializer {
    public static void encode(AnchorListUpdateMessage msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(msg.nbt);
    }

    public static AnchorListUpdateMessage decode(FriendlyByteBuf buffer) {
        return new AnchorListUpdateMessage(buffer.readNbt());
    }

    public static class AnchorListUpdateMessage {

        public AnchorListUpdateMessage() {

        }

        public AnchorListUpdateMessage(CompoundTag nbt) {
            this.nbt = nbt;
        }

        public CompoundTag nbt;
    }
}
