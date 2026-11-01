// Modified by goocompound.
package io.github.goocompound.travelstaff.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class SyncAnchorTileSerializer {
    public static void encode(AnchorTileMessage msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(msg.nbt);
        buffer.writeBlockPos(msg.pos);
    }

    public static AnchorTileMessage decode(FriendlyByteBuf buffer) {
        return new AnchorTileMessage(buffer.readNbt(), buffer.readBlockPos());
    }

    public static class AnchorTileMessage {

        public AnchorTileMessage() {

        }

        public AnchorTileMessage(CompoundTag nbt, BlockPos pos) {
            this.nbt = nbt;
            this.pos = pos;
        }

        public CompoundTag nbt;
        public BlockPos pos;
    }
}
