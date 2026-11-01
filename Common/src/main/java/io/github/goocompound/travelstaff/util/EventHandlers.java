// Modified by goocompound.
package io.github.goocompound.travelstaff.util;

import io.github.goocompound.travelstaff.TravelStaffMain;
import io.github.goocompound.travelstaff.network.ClientEventSerializer;
import io.github.goocompound.travelstaff.platform.Services;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EventHandlers {
    // not in use on item because has to check for teleport enchant as well
    public static InteractionResult onRightClick(Player player, InteractionHand hand, ItemStack stack) {
        Level level = player.level;
        if (TeleportHandler.canPlayerTeleport(player, hand) && !stack.isEmpty()) {
            if (player.isShiftKeyDown() && TeleportHandler.canItemTeleport(player, hand)) {
                if (player.getCooldowns().isOnCooldown(stack.getItem())) return InteractionResult.PASS;
                if (TeleportHandler.shortTeleport(level, player, hand)) {
                    player.getCooldowns().addCooldown(stack.getItem(), TravelStaffMain.CONFIG.get().staffCooldownTicks);
                    return InteractionResult.SUCCESS;
                }
            } else {
                if (TeleportHandler.anchorTeleport(level, player, player.blockPosition().immutable().below(), hand)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    public static InteractionResult onEmptyClick(Player player, InteractionHand hand, ItemStack stack) {
        if (TeleportHandler.canBlockTeleport(player) && !player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND && player.getItemInHand(InteractionHand.OFF_HAND).isEmpty() && stack.isEmpty()) {
            Services.NETWORK.sendClientEventToServer(ClientEventSerializer.ClientEvent.EMPTY_HAND_INTERACT);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult onEmptyBlockClick(Player player, InteractionHand hand, ItemStack stack) {
        if (hand == InteractionHand.MAIN_HAND) {
            // Empty offhand does not count. In that case the main hand will either produce
            // this event or PlayerInteractEvent.RightClickItem
            if (TeleportHandler.canPlayerTeleport(player, hand)) {
                if (!player.isShiftKeyDown()) {
                    if (player.level.isClientSide()){
                        // fabric
                        Services.NETWORK.sendClientEventToServer(ClientEventSerializer.ClientEvent.EMPTY_HAND_INTERACT);
                        return InteractionResult.FAIL; // stops the block from processing the interaction
                    } else {
                        // forge
                        if (TeleportHandler.anchorTeleport(player.level, player, TeleportHandler.down(player), hand)) {
                            return InteractionResult.FAIL; // stops the block from processing the interaction
                        }
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    public static void onJump(Player player) {
        if (player != null) {
            if (TravelStaffMain.CLIENT_CONFIG.get().isElevatorMode) {
                if (TeleportHandler.canElevate(player) && !player.isShiftKeyDown()) {
                    Services.NETWORK.sendClientEventToServer(ClientEventSerializer.ClientEvent.ELEVATOR_UP);
                }
            } else {
                if (TeleportHandler.canBlockTeleport(player) && !player.isShiftKeyDown()) {
                    Services.NETWORK.sendClientEventToServer(ClientEventSerializer.ClientEvent.ANCHOR_TP);
                }
            }
        }
    }

    public static void onSneak(Player player) {
        if (player != null) {
            if (TravelStaffMain.CLIENT_CONFIG.get().isElevatorMode) {
                if (TeleportHandler.canElevate(player)) {
                    Services.NETWORK.sendClientEventToServer(ClientEventSerializer.ClientEvent.ELEVATOR_DOWN);
                }
            } else {
                if (TeleportHandler.canBlockTeleport(player)) {
                    Services.NETWORK.sendClientEventToServer(ClientEventSerializer.ClientEvent.ANCHOR_TP);
                }
            }
        }
    }
}
