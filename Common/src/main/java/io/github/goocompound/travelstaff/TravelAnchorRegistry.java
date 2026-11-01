// Modified by goocompound.
package io.github.goocompound.travelstaff;

import ca.lukegrahamlandry.lib.registry.RegistryWrapper;
import io.github.goocompound.travelstaff.block.BlockTravelAnchor;
import io.github.goocompound.travelstaff.block.TileTravelAnchor;
import io.github.goocompound.travelstaff.enchantments.RangeEnchantment;
import io.github.goocompound.travelstaff.enchantments.TeleportationEnchantment;
import io.github.goocompound.travelstaff.item.ItemTravelStaff;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;
import java.util.List;

public class TravelAnchorRegistry {
    public static final RegistryWrapper<Block> BLOCKS = RegistryWrapper.create(BuiltInRegistries.BLOCK, TravelStaffMain.MOD_ID);
    public static final Supplier<Block> TRAVEL_ANCHOR = BLOCKS.register("travel_anchor", BlockTravelAnchor::new);

    public static final RegistryWrapper<Item> ITEMS = RegistryWrapper.create(BuiltInRegistries.ITEM, TravelStaffMain.MOD_ID);
    public static final Supplier<Item> STAFF = ITEMS.register(new ResourceLocation(TravelStaffMain.MOD_ID, "travel_staff").getPath(), () -> new ItemTravelStaff(new Item.Properties()));
    public static final Supplier<Item> ANCHOR_ITEM = ITEMS.register("travel_anchor", () -> new BlockItem(TRAVEL_ANCHOR.get(), new Item.Properties()));

    public static final RegistryWrapper<Enchantment> ENCHANTMENTS = RegistryWrapper.create(BuiltInRegistries.ENCHANTMENT, TravelStaffMain.MOD_ID);
    public static final Supplier<Enchantment> RANGE = ENCHANTMENTS.register("range", () -> RangeEnchantment.INSTANCE);
    public static final Supplier<Enchantment> TELEPORTATION = ENCHANTMENTS.register("teleportation", () -> TeleportationEnchantment.INSTANCE);

    public static final RegistryWrapper<BlockEntityType<?>> TILE_ENTITY_TYPES = RegistryWrapper.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TravelStaffMain.MOD_ID);
    public static final Supplier<BlockEntityType<TileTravelAnchor>> TRAVEL_ANCHOR_TILE = TILE_ENTITY_TYPES.register("travel_anchor",
            () -> BlockEntityType.Builder.of(TileTravelAnchor::new, TRAVEL_ANCHOR.get()).build(null));

    public static void fillItemCategory(CreativeModeTab tab, List<ItemStack> items) {
	    ComponentContents contents = tab.getDisplayName().getContents();
	    if (!(contents instanceof TranslatableContents)) {
		    return;
	    }

	    String tabName = ((TranslatableContents) contents).getKey();

	    if (tabName.equals("itemGroup.tools")) {
		   items.add(new ItemStack(STAFF.get()));
	    }
	    if (tabName.equals("itemGroup.redstone")) {
		   items.add(new ItemStack(ANCHOR_ITEM.get()));
	    }
    }
}
