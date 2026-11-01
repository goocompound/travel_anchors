// Modified by goocompound.
package io.github.goocompound.travelstaff.platform.services;

import io.github.goocompound.travelstaff.block.TileTravelAnchor;
import io.github.goocompound.travelstaff.network.ClientEventSerializer;
import io.github.goocompound.travelstaff.util.TravelAnchorList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface INetworkHelper {
    void sendNameChangeToServer(String trim, BlockPos pos);

    void syncAnchorToClients(TileTravelAnchor tile);

    void sendAnchorListToClients(ServerLevel level, TravelAnchorList travelAnchorList);

    void sendAnchorListToClient(ServerPlayer player);

    void sendClientEventToServer(ClientEventSerializer.ClientEvent event);
}
