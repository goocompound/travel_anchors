package io.github.goocompound.travelstaff;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;

import java.util.stream.Stream;

public class EnchantmentGameTest {
    @GameTest
    public void rangeBooksApplyToToolsWithoutTableOffers(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var range = enchantments.getOrThrow(TravelAnchorRegistry.RANGE);
        var teleportation = enchantments.getOrThrow(TravelAnchorRegistry.TELEPORTATION);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.updateEnchantments(book, mutable -> mutable.set(range, 2));

        for (Item item : new Item[]{Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_HOE}) {
            ItemStack tool = new ItemStack(item);
            helper.assertTrue(EnchantmentHelper.getAvailableEnchantmentResults(30, tool, Stream.of(range)).isEmpty(),
                    "Tools must not roll Range at the enchanting table");
            tool.enchant(teleportation, 1);
            AnvilMenu anvil = new AnvilMenu(0, player.getInventory());
            anvil.getSlot(0).set(tool);
            anvil.getSlot(1).set(book.copy());
            anvil.createResult();
            ItemStack result = anvil.getSlot(2).getItem();
            helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(range, result) == 2,
                    "A Range book must apply to " + item);
            helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(teleportation, result) == 1,
                    "Applying Range must preserve Teleportation");
        }

        helper.assertFalse(EnchantmentHelper.getAvailableEnchantmentResults(30,
                new ItemStack(TravelAnchorRegistry.STAFF.get()), Stream.of(range)).isEmpty(),
                "The staff must still roll Range at the enchanting table");
        helper.assertFalse(EnchantmentHelper.getAvailableEnchantmentResults(30,
                new ItemStack(Items.BOOK), Stream.of(range)).isEmpty(),
                "Books must still roll Range at the enchanting table");
        helper.assertFalse(range.value().canEnchant(new ItemStack(Items.BOW)), "Bows must remain ineligible for Range");
        helper.succeed();
    }
}
