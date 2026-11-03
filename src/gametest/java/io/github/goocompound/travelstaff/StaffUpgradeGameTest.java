package io.github.goocompound.travelstaff;

import io.github.goocompound.travelstaff.recipe.StaffUpgradeRecipe;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class StaffUpgradeGameTest {
    @GameTest
    public void smithingPreservesToolAndConsumesOnePearlAndStaff(GameTestHelper helper) {
        var level = helper.getLevel();
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var teleportation = enchantments.getOrThrow(TravelAnchorRegistry.TELEPORTATION);
        var range = enchantments.getOrThrow(TravelAnchorRegistry.RANGE);
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.setDamageValue(137);
        tool.set(DataComponents.CUSTOM_NAME, Component.literal("Named pickaxe"));
        tool.set(DataComponents.REPAIR_COST, 7);
        CompoundTag customData = new CompoundTag();
        customData.putString("other_mod_data", "preserved");
        tool.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
        tool.enchant(enchantments.getOrThrow(Enchantments.EFFICIENCY), 5);
        tool.enchant(enchantments.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        tool.enchant(range, 1);
        ItemStack staff = new ItemStack(TravelAnchorRegistry.STAFF.get(), 2);
        staff.enchant(range, 2);
        staff.enchant(enchantments.getOrThrow(Enchantments.MENDING), 1);

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlockAndUpdate(pos, Blocks.SMITHING_TABLE.defaultBlockState());
        SmithingMenu menu = new SmithingMenu(0, player.getInventory(), ContainerLevelAccess.create(level, pos));
        ItemStack pearls = new ItemStack(Items.ENDER_PEARL, 3);
        helper.assertTrue(menu.getSlot(SmithingMenu.TEMPLATE_SLOT).mayPlace(pearls), "Template slot must accept ender pearls");
        helper.assertTrue(menu.getSlot(SmithingMenu.BASE_SLOT).mayPlace(tool), "Equipment slot must accept the tool");
        helper.assertTrue(menu.getSlot(SmithingMenu.ADDITIONAL_SLOT).mayPlace(staff), "Addition slot must accept the staff");
        menu.getSlot(SmithingMenu.TEMPLATE_SLOT).set(pearls);
        menu.getSlot(SmithingMenu.BASE_SLOT).set(tool.copy());
        menu.getSlot(SmithingMenu.ADDITIONAL_SLOT).set(staff.copy());
        menu.createResult();

        ItemStack expected = tool.copy();
        expected.enchant(teleportation, 1);
        expected.enchant(range, 2);
        var output = menu.getSlot(SmithingMenu.RESULT_SLOT);
        helper.assertTrue(ItemStack.isSameItemSameComponents(expected, output.getItem()),
                "Only Teleportation and Range may change; all other tool data must survive");
        helper.assertTrue(output.mayPickup(player), "Survival players must be able to take the upgrade without XP");
        ItemStack taken = output.remove(1);
        output.onTake(player, taken);
        helper.assertTrue(taken.getCount() == 1, "Smithing must produce exactly one tool");
        helper.assertTrue(menu.getSlot(SmithingMenu.TEMPLATE_SLOT).getItem().getCount() == 2, "Consume one pearl");
        helper.assertTrue(menu.getSlot(SmithingMenu.ADDITIONAL_SLOT).getItem().getCount() == 1, "Consume one staff");
        helper.assertTrue(menu.getSlot(SmithingMenu.BASE_SLOT).getItem().isEmpty(), "Consume the original tool");
        helper.assertTrue(output.getItem().isEmpty(), "Taking the result must not leave a duplicate");
        helper.succeed();
    }

    @GameTest
    public void rangeKeepsTheHigherLevelWithoutCombiningEqualLevels(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var range = enchantments.getOrThrow(TravelAnchorRegistry.RANGE);
        var teleportation = enchantments.getOrThrow(TravelAnchorRegistry.TELEPORTATION);
        for (int toolLevel = 0; toolLevel <= 3; toolLevel++) {
            for (int staffLevel = 0; staffLevel <= 3; staffLevel++) {
                ItemStack tool = new ItemStack(Items.IRON_AXE);
                ItemStack staff = new ItemStack(TravelAnchorRegistry.STAFF.get());
                if (toolLevel > 0) tool.enchant(range, toolLevel);
                if (staffLevel > 0) staff.enchant(range, staffLevel);
                SmithingRecipeInput input = new SmithingRecipeInput(new ItemStack(Items.ENDER_PEARL), tool, staff);
                StaffUpgradeRecipe recipe = recipe(helper, input);
                ItemStack result = recipe.assemble(input);
                helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(teleportation, result) == 1, "Add Teleportation I");
                helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(range, result) == Math.max(toolLevel, staffLevel),
                        "Keep the higher Range level without increasing equal levels");
                helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(teleportation, tool) == 0,
                        "Previewing a result must not modify the input tool");
                helper.assertFalse(recipe.matches(new SmithingRecipeInput(input.template(), result, staff), helper.getLevel()),
                        "A combination that cannot improve the tool must have no result");
            }
        }
        ItemStack tool = new ItemStack(Items.IRON_SHOVEL);
        tool.enchant(teleportation, 1);
        tool.enchant(range, 1);
        ItemStack staff = new ItemStack(TravelAnchorRegistry.STAFF.get());
        staff.enchant(range, 3);
        SmithingRecipeInput input = new SmithingRecipeInput(new ItemStack(Items.ENDER_PEARL), tool, staff);
        helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(range, recipe(helper, input).assemble(input)) == 3,
                "An existing Teleportation tool must accept a higher Range staff");
        helper.succeed();
    }

    @GameTest
    public void invalidIngredientsDoNotMatch(GameTestHelper helper) {
        ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
        ItemStack tool = new ItemStack(Items.IRON_SWORD);
        ItemStack staff = new ItemStack(TravelAnchorRegistry.STAFF.get());
        StaffUpgradeRecipe recipe = recipe(helper, new SmithingRecipeInput(pearl, tool, staff));
        for (SmithingRecipeInput input : new SmithingRecipeInput[]{
                new SmithingRecipeInput(ItemStack.EMPTY, tool, staff),
                new SmithingRecipeInput(new ItemStack(Items.LEATHER), tool, staff),
                new SmithingRecipeInput(pearl, new ItemStack(Items.BOW), staff),
                new SmithingRecipeInput(pearl, staff, tool),
                new SmithingRecipeInput(pearl, tool, new ItemStack(Items.STICK))}) {
            helper.assertFalse(recipe.matches(input, helper.getLevel()), "All three correct ingredients are required");
        }
        helper.succeed();
    }

    @GameTest
    public void recipeAndDisplaySurviveNetworkEncoding(GameTestHelper helper) {
        ItemStack staff = new ItemStack(TravelAnchorRegistry.STAFF.get());
        staff.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(TravelAnchorRegistry.RANGE), 2);
        SmithingRecipeInput input = new SmithingRecipeInput(new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.IRON_HOE), staff);
        StaffUpgradeRecipe recipe = recipe(helper, input);
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            StaffUpgradeRecipe.SERIALIZER.streamCodec().encode(buffer, recipe);
            StaffUpgradeRecipe decoded = StaffUpgradeRecipe.SERIALIZER.streamCodec().decode(buffer);
            helper.assertTrue(decoded.matches(input, helper.getLevel()), "Decoded recipe must recognize the same ingredients");
            helper.assertTrue(ItemStack.isSameItemSameComponents(recipe.assemble(input), decoded.assemble(input)),
                    "Decoded recipe must produce the same enchantments");
            RecipeDisplay display = decoded.display().getFirst();
            RecipeDisplay.STREAM_CODEC.encode(buffer, display);
            helper.assertTrue(display.equals(RecipeDisplay.STREAM_CODEC.decode(buffer)), "Recipe preview must survive synchronization");
            helper.assertTrue(buffer.readableBytes() == 0, "Recipe encodings must consume their complete payloads");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    private static StaffUpgradeRecipe recipe(GameTestHelper helper, SmithingRecipeInput input) {
        var match = helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, helper.getLevel());
        helper.assertTrue(match.isPresent() && match.get().value() instanceof StaffUpgradeRecipe,
                "The registered smithing recipe must match");
        return (StaffUpgradeRecipe) match.orElseThrow().value();
    }
}
