// Modified by goocompound.
package io.github.goocompound.travelstaff.platform;

import io.github.goocompound.travelstaff.network.AnchorListUpdateSerializer.AnchorListUpdateMessage;
import io.github.goocompound.travelstaff.network.AnchorNameChangeSerializer.AnchorNameChangeMessage;
import io.github.goocompound.travelstaff.network.ClientEventSerializer.ClientEvent;
import io.github.goocompound.travelstaff.network.ClientEventSerializer.ClientEventMessage;
import io.github.goocompound.travelstaff.platform.services.INetworkHelper;
import io.github.goocompound.travelstaff.util.TravelAnchorList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetworkHelper implements INetworkHelper {
    @Environment(EnvType.CLIENT)
    @Override
    public void sendNameChangeToServer(String name, BlockPos pos) {
        ClientPlayNetworking.send(new AnchorNameChangeMessage(pos, name));
    }

    @Override
    public void sendAnchorListToClients(ServerLevel level, TravelAnchorList travelAnchorList) {
        level.players().forEach(this::sendAnchorListToClient);
    }

    @Override
    public void sendAnchorListToClient(ServerPlayer player) {
        ServerPlayNetworking.send(player, new AnchorListUpdateMessage(
                TravelAnchorList.get(player.level()).saveForNetwork()));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void sendClientEventToServer(ClientEvent event) {
        ClientPlayNetworking.send(new ClientEventMessage(event));
    }
}
