// Modified by goocompound.
package io.github.goocompound.travelstaff;

import io.github.goocompound.travelstaff.block.BlockTravelAnchor;
import io.github.goocompound.travelstaff.block.TileTravelAnchor;
import io.github.goocompound.travelstaff.item.ItemTravelStaff;
import io.github.goocompound.travelstaff.item.ItemTravelAnchor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.Set;
import java.util.function.Supplier;

public final class TravelAnchorRegistry {
    private static final ResourceKey<Block> ANCHOR_KEY = ResourceKey.create(Registries.BLOCK, id("travel_anchor"));
    private static final ResourceKey<Item> STAFF_KEY = ResourceKey.create(Registries.ITEM, id("travel_staff"));
    private static final ResourceKey<Item> ANCHOR_ITEM_KEY = ResourceKey.create(Registries.ITEM, id("travel_anchor"));

    private static final Block ANCHOR = Registry.register(BuiltInRegistries.BLOCK, ANCHOR_KEY,
            new BlockTravelAnchor(BlockBehaviour.Properties.of().setId(ANCHOR_KEY)
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    private static final Item STAFF_ITEM = Registry.register(BuiltInRegistries.ITEM, STAFF_KEY,
            new ItemTravelStaff(new Item.Properties().setId(STAFF_KEY).enchantable(20)));
    private static final Item ANCHOR_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, ANCHOR_ITEM_KEY,
            new ItemTravelAnchor(ANCHOR, new Item.Properties().setId(ANCHOR_ITEM_KEY).useBlockDescriptionPrefix()));
    private static final BlockEntityType<TileTravelAnchor> ANCHOR_TYPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, id("travel_anchor"),
            new BlockEntityType<>(TileTravelAnchor::new, Set.of(ANCHOR)));

    public static final Supplier<Block> TRAVEL_ANCHOR = () -> ANCHOR;
    public static final Supplier<Item> STAFF = () -> STAFF_ITEM;
    public static final Supplier<Item> ANCHOR_ITEM = () -> ANCHOR_BLOCK_ITEM;
    public static final Supplier<BlockEntityType<TileTravelAnchor>> TRAVEL_ANCHOR_TILE = () -> ANCHOR_TYPE;

    // Enchantments are supplied by data packs and resolved against the current world.
    public static final ResourceKey<Enchantment> RANGE = ResourceKey.create(Registries.ENCHANTMENT, id("range"));
    public static final ResourceKey<Enchantment> TELEPORTATION = ResourceKey.create(Registries.ENCHANTMENT, id("teleportation"));

    private TravelAnchorRegistry() {}

    public static void init() {
        // Loading this class registers the fixed block, item and block-entity types.
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TravelStaffMain.MOD_ID, path);
    }

}
