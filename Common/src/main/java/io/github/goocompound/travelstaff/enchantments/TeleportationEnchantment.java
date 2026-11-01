// Modified by goocompound.
package io.github.goocompound.travelstaff.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class TeleportationEnchantment extends Enchantment {
    public static TeleportationEnchantment INSTANCE = new TeleportationEnchantment();
    public TeleportationEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.VANISHABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return EnchantmentCategory.WEAPON.canEnchant(stack.getItem()) || EnchantmentCategory.DIGGER.canEnchant(stack.getItem());
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
