// Modified by goocompound.
package io.github.goocompound.travelstaff;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class FabricModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        TravelStaffMain.init();
        FabricNetworkHandler.init();
        FabricEventListener.init();

        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("tools_and_utilities")))
                .register(output -> output.accept(TravelAnchorRegistry.STAFF.get()));
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("redstone_blocks")))
                .register(output -> output.accept(TravelAnchorRegistry.ANCHOR_ITEM.get()));
    }
}
