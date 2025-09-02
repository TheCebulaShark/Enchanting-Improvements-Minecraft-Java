package org.blahajenjoyer.enchanting_improvements.logic;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class EnchantingHelper {
    private EnchantingHelper() {}

    public static boolean isEnchantable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.BOOK)) return true;
        if (isEnchantedBook(stack)) return true;
        if (stack.isEnchanted()) return true;
        // TODO: new enchatnting rules
        return stack.isEnchantable();
    }

    public static boolean isLapis(ItemStack stack) {
        return stack.is(Items.LAPIS_LAZULI);
    }

    public static boolean isEnchantedBook(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK);
    }

    public static boolean isMaterial(ItemStack stack) {
        return !isEnchantable(stack) && stack.getMaxStackSize() > 1;
    }
}
