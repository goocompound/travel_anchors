// Modified by goocompound.
package io.github.goocompound.travelstaff;

import io.github.goocompound.travelstaff.network.AnchorNameChangeSerializer;
import io.github.goocompound.travelstaff.network.ClientEventSerializer;
import io.github.goocompound.travelstaff.util.NetworkEventHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class FabricNetworkHandler {
    public static final ResourceLocation SYNC_ANCHOR_TILE = new ResourceLocation(TravelStaffMain.MOD_ID, "sync_anchor_tile");
    public static final ResourceLocation SYNC_ANCHOR_LIST = new ResourceLocation(TravelStaffMain.MOD_ID, "sync_anchor_list");
    public static final ResourceLocation CLIENT_EVENT_TO_SERVER = new ResourceLocation(TravelStaffMain.MOD_ID, "clientevent_to_server");
    public static final ResourceLocation ANCHOR_NAME_CHANGE = new ResourceLocation(TravelStaffMain.MOD_ID, "anchor_name_change");

    public static void init(){
        ServerPlayNetworking.registerGlobalReceiver(CLIENT_EVENT_TO_SERVER, (server, player, handler, data, responseSender) -> {
            if (player == null) return;
            ClientEventSerializer.ClientEvent msg = ClientEventSerializer.decode(data);
            server.execute(() -> NetworkEventHandler.handleClientEvent(player, msg));
        });

        ServerPlayNetworking.registerGlobalReceiver(ANCHOR_NAME_CHANGE, (server, player, handler, data, responseSender) -> {
            if (player == null) return;
            AnchorNameChangeSerializer.AnchorNameChangeMessage msg = AnchorNameChangeSerializer.decode(data);
            server.execute(() -> NetworkEventHandler.handleNameChange(player.getLevel(), msg.pos, msg.name));
        });
    }
}
