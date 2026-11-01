// Modified by goocompound.
package io.github.goocompound.travelstaff;

import io.github.goocompound.travelstaff.config.TravelStaffConfig;
import io.github.goocompound.travelstaff.network.AnchorListUpdateSerializer.AnchorListUpdateMessage;
import io.github.goocompound.travelstaff.network.AnchorNameChangeSerializer.AnchorNameChangeMessage;
import io.github.goocompound.travelstaff.network.ClientEventSerializer.ClientEventMessage;
import io.github.goocompound.travelstaff.network.SyncedConfigPayload;
import io.github.goocompound.travelstaff.util.NetworkEventHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class FabricNetworkHandler {
    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(ClientEventMessage.TYPE, ClientEventMessage.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(AnchorNameChangeMessage.TYPE, AnchorNameChangeMessage.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AnchorListUpdateMessage.TYPE, AnchorListUpdateMessage.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncedConfigPayload.TYPE, SyncedConfigPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ClientEventMessage.TYPE, (message, context) ->
                context.server().execute(() -> NetworkEventHandler.handleClientEvent(context.player(), message.event())));
        ServerPlayNetworking.registerGlobalReceiver(AnchorNameChangeMessage.TYPE, (message, context) ->
                context.server().execute(() -> NetworkEventHandler.handleNameChange(
                        context.player(), message.pos(), message.name())));
    }

    public static void sendConfig(ServerPlayer player) {
        ServerPlayNetworking.send(player, SyncedConfigPayload.from(TravelStaffConfig.getServerConfig()));
    }
}
