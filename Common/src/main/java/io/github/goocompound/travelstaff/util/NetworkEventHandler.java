// Modified by goocompound.
package io.github.goocompound.travelstaff.util;

import io.github.goocompound.travelstaff.block.TileTravelAnchor;
import io.github.goocompound.travelstaff.network.ClientEventSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NetworkEventHandler {
    public static void handleClientUpdateAnchorList(Level level, CompoundTag nbt){
        if (level == null) return;

        TravelAnchorList.get(level).load(nbt);
    }

    public static void handleNameChange(ServerLevel level, BlockPos pos, String name){
        if (level == null) return;

        if (level.hasChunkAt(pos)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileTravelAnchor) {
                ((TileTravelAnchor) be).setName(name);
                be.setChanged();
            }
        }
    }


    public static void handleClientEvent(ServerPlayer player, ClientEventSerializer.ClientEvent msg) {
        if (player == null) return;

        switch (msg) {
            case ELEVATOR_UP:
                if (TeleportHandler.canElevate(player)) {
                    if (TeleportHandler.elevateUp(player)) {
                        player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1));
                    }
                }
                break;

            case EMPTY_HAND_INTERACT:
                if (TeleportHandler.canBlockTeleport(player) && !player.isShiftKeyDown()) {
                    TeleportHandler.anchorTeleport(player.getCommandSenderWorld(), player, TeleportHandler.down(player), InteractionHand.MAIN_HAND);
                }
                break;

            case ELEVATOR_DOWN:
                if (TeleportHandler.canElevate(player)) {
                    if (TeleportHandler.elevateDown(player)) {
                        player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1));
                    }
                }
                break;

            case ANCHOR_TP:
                // Client has configured to use jump as telport not elevate
                if (TeleportHandler.canBlockTeleport(player)) {
                    TeleportHandler.anchorTeleport(player.getLevel(), player, TeleportHandler.down(player), null);
                }
                break;
        }
    }

    public static void handleSyncAnchorTile(Level level, CompoundTag nbt, BlockPos pos) {
        if (level == null) return;

        if (level.hasChunkAt(pos)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileTravelAnchor) {
                ((TileTravelAnchor) be).setName(nbt.getString("travel_anchor_name"));
                ((TileTravelAnchor) be).readMimic(nbt);
            }
        }
    }


    // player changes a name on client
    // client -> server handleNameChange
    // then server sends it to all clients
    // server -> client handleSyncAnchorTile
}
