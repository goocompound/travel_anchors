// Modified by goocompound.
package io.github.goocompound.travelstaff;

import ca.lukegrahamlandry.lib.event.fabric.WrapperLibModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class FabricModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        new WrapperLibModInitializer().onInitialize();
        TravelStaffMain.init();
        FabricEventListener.init();
        FabricNetworkHandler.init();

	ItemGroupEvents.MODIFY_ENTRIES_ALL.register((tab, entries) -> {
		TravelAnchorRegistry.fillItemCategory(tab, entries.getDisplayStacks());
		TravelAnchorRegistry.fillItemCategory(tab, entries.getSearchTabStacks());
	});
    }
}
