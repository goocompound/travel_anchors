package io.github.goocompound.travelstaff.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class StaffUpgradeRecipe extends SimpleSmithingRecipe {
    private static final MapCodec<StaffUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
            Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
            Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition),
            Enchantment.CODEC.fieldOf("teleportation").forGetter(recipe -> recipe.teleportation),
            Enchantment.CODEC.fieldOf("range").forGetter(recipe -> recipe.range)
    ).apply(instance, StaffUpgradeRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, StaffUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
            CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.template,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.base,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.addition,
            Enchantment.STREAM_CODEC, recipe -> recipe.teleportation,
            Enchantment.STREAM_CODEC, recipe -> recipe.range,
            StaffUpgradeRecipe::new);
    public static final RecipeSerializer<StaffUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final Holder<Enchantment> teleportation;
    private final Holder<Enchantment> range;

    private StaffUpgradeRecipe(CommonInfo commonInfo, Ingredient template, Ingredient base, Ingredient addition,
                               Holder<Enchantment> teleportation, Holder<Enchantment> range) {
        super(commonInfo);
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.teleportation = teleportation;
        this.range = range;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!super.matches(input, level)) return false;
        ItemStack tool = input.base();
        boolean addTeleportation = EnchantmentHelper.getItemEnchantmentLevel(teleportation, tool) < 1;
        boolean addRange = transferredRange(input.addition()) > EnchantmentHelper.getItemEnchantmentLevel(range, tool);
        if (!addTeleportation && !addRange) return false;
        if (addTeleportation && !canApply(tool, teleportation)) return false;
        if (addRange && !canApply(tool, range)) return false;
        return !addTeleportation || !addRange || Enchantment.areCompatible(teleportation, range);
    }

    private static boolean canApply(ItemStack tool, Holder<Enchantment> enchantment) {
        return enchantment.value().canEnchant(tool) && EnchantmentHelper.getEnchantmentsForCrafting(tool).keySet().stream()
                .allMatch(existing -> existing.equals(enchantment) || Enchantment.areCompatible(existing, enchantment));
    }

    private int transferredRange(ItemStack staff) {
        return Math.min(EnchantmentHelper.getItemEnchantmentLevel(range, staff), range.value().getMaxLevel());
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack result = input.base().copyWithCount(1);
        int rangeLevel = transferredRange(input.addition());
        EnchantmentHelper.updateEnchantments(result, enchantments -> {
            enchantments.upgrade(teleportation, 1);
            if (rangeLevel > 0) enchantments.upgrade(range, rangeLevel);
        });
        return result;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.of(template);
    }

    @Override
    public Ingredient baseIngredient() {
        return base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return Optional.of(addition);
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(List.of(template, base, addition));
    }

    @Override
    public List<RecipeDisplay> display() {
        List<SlotDisplay> results = base.items().map(item -> {
            ItemStack example = new ItemStack(item);
            example.enchant(teleportation, 1);
            return (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(example));
        }).toList();
        return List.of(new SmithingRecipeDisplay(template.display(), base.display(), addition.display(),
                new SlotDisplay.Composite(results), new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }

    @Override
    public RecipeSerializer<StaffUpgradeRecipe> getSerializer() {
        return SERIALIZER;
    }
}
