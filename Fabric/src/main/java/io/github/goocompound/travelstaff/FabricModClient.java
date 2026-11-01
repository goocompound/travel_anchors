// Modified by goocompound.
package io.github.goocompound.travelstaff;

import io.github.goocompound.travelstaff.config.TravelStaffConfig;
import io.github.goocompound.travelstaff.network.AnchorListUpdateSerializer.AnchorListUpdateMessage;
import io.github.goocompound.travelstaff.network.SyncedConfigPayload;
import io.github.goocompound.travelstaff.render.TravelAnchorRenderer;
import io.github.goocompound.travelstaff.util.NetworkEventHandler;
import io.github.goocompound.travelstaff.util.TravelAnchorList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class FabricModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TravelStaffConfig.loadClient();
        TravelAnchorRenderer.init();
        ClientPlayNetworking.registerGlobalReceiver(AnchorListUpdateMessage.TYPE, (message, context) ->
                context.client().execute(() -> NetworkEventHandler.handleClientUpdateAnchorList(
                        context.client().level, message.nbt())));
        ClientPlayNetworking.registerGlobalReceiver(SyncedConfigPayload.TYPE, (message, context) ->
                context.client().execute(() -> TravelStaffConfig.applySynced(message.toConfig())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            TravelStaffConfig.resetClient();
            TravelAnchorList.clearClient();
        });
    }
}
