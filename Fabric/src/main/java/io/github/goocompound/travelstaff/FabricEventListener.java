// Modified by goocompound.
package io.github.goocompound.travelstaff;

import io.github.goocompound.travelstaff.platform.Services;
import io.github.goocompound.travelstaff.util.EventHandlers;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class FabricEventListener {
    public static void init(){
        ServerPlayConnectionEvents.JOIN.register(FabricEventListener::onJoin);
        UseItemCallback.EVENT.register(FabricEventListener::onUseItem);
        UseBlockCallback.EVENT.register(FabricEventListener::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult blockHitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) return InteractionResult.PASS;
        return EventHandlers.onEmptyBlockClick(player, hand, stack);
    }

    private static InteractionResultHolder<ItemStack> onUseItem(Player player, Level level, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResult result = EventHandlers.onRightClick(player, hand, stack);
        return result == InteractionResult.PASS ? InteractionResultHolder.pass(stack) : InteractionResultHolder.success(stack);
    }

    private static void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server){
        Services.NETWORK.sendAnchorListToClient(handler.player);
    }
}
