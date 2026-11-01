// Modified by goocompound.
package io.github.goocompound.travelstaff;

import ca.lukegrahamlandry.lib.event.fabric.WrapperLibClientInitializer;
import io.github.goocompound.travelstaff.block.RenderTravelAnchor;
import io.github.goocompound.travelstaff.network.AnchorListUpdateSerializer;
import io.github.goocompound.travelstaff.network.SyncAnchorTileSerializer;
import io.github.goocompound.travelstaff.util.NetworkEventHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.Minecraft;

import static io.github.goocompound.travelstaff.FabricNetworkHandler.SYNC_ANCHOR_LIST;
import static io.github.goocompound.travelstaff.FabricNetworkHandler.SYNC_ANCHOR_TILE;

@Environment(EnvType.CLIENT)
public class FabricModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new WrapperLibClientInitializer().onInitializeClient();
        BlockEntityRendererRegistry.register(TravelAnchorRegistry.TRAVEL_ANCHOR_TILE.get(), (ctx) -> new RenderTravelAnchor());
        initClientPackets();
    }

    @Environment(EnvType.CLIENT)
    public static void initClientPackets(){
        ClientPlayNetworking.registerGlobalReceiver(SYNC_ANCHOR_LIST, (client, handler, data, responseSender) -> {
            AnchorListUpdateSerializer.AnchorListUpdateMessage msg = AnchorListUpdateSerializer.decode(data);
            client.execute(() -> NetworkEventHandler.handleClientUpdateAnchorList(Minecraft.getInstance().level, msg.nbt));
        });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_ANCHOR_TILE, (client, handler, data, responseSender) -> {
            SyncAnchorTileSerializer.AnchorTileMessage msg = SyncAnchorTileSerializer.decode(data);
            client.execute(() -> NetworkEventHandler.handleSyncAnchorTile(Minecraft.getInstance().level, msg.nbt, msg.pos));
        });
    }
}