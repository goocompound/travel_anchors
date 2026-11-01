// Modified by goocompound.
package io.github.goocompound.travelstaff;

import io.github.goocompound.travelstaff.config.TravelStaffConfig;
import io.github.goocompound.travelstaff.platform.Services;
import io.github.goocompound.travelstaff.util.EventHandlers;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionResult;

public final class FabricEventListener {
    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(TravelStaffConfig::startServer);
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resources) -> {
            TravelStaffConfig.reloadServer(server);
            server.getPlayerList().getPlayers().forEach(FabricNetworkHandler::sendConfig);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(TravelStaffConfig::stopServer);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            FabricNetworkHandler.sendConfig(handler.player);
            Services.NETWORK.sendAnchorListToClient(handler.player);
        });
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) ->
                Services.NETWORK.sendAnchorListToClient(player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                Services.NETWORK.sendAnchorListToClient(newPlayer));

        UseItemCallback.EVENT.register((player, level, hand) ->
                EventHandlers.onRightClick(player, hand, player.getItemInHand(hand)));
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            var stack = player.getItemInHand(hand);
            return stack.isEmpty() ? EventHandlers.onEmptyBlockClick(player, hand, stack) : InteractionResult.PASS;
        });
    }
}
