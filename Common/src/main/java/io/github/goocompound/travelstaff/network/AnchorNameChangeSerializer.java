// Modified by goocompound.
package io.github.goocompound.travelstaff.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class AnchorNameChangeSerializer {
    public static void encode(AnchorNameChangeMessage msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeUtf(msg.name);
    }

    public static AnchorNameChangeMessage decode(FriendlyByteBuf buffer) {
        return new AnchorNameChangeMessage(buffer.readBlockPos(), buffer.readUtf(32767));
    }

    public static class AnchorNameChangeMessage {
        public BlockPos pos;
        public String name;

        public AnchorNameChangeMessage() {

        }

        public AnchorNameChangeMessage(BlockPos pos, String name) {
            this.pos = pos;
            this.name = name;
        }
    }
}
