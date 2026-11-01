// Modified by goocompound.
package io.github.goocompound.travelstaff.enchantments;

import io.github.goocompound.travelstaff.item.ItemTravelStaff;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class RangeEnchantment extends Enchantment {
    public static RangeEnchantment INSTANCE = new RangeEnchantment();

    public RangeEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.VANISHABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ItemTravelStaff || EnchantmentHelper.getItemEnchantmentLevel(this, stack) > 0;
    }

    @Override
    public int getMinCost(int level) {
        return level * 7 + 3;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) * 2 + 2;
    }
}
