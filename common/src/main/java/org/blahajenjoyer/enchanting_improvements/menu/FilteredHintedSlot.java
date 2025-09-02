package org.blahajenjoyer.enchanting_improvements.menu;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

import java.util.function.Predicate;

public class FilteredHintedSlot extends Slot {
    private final Predicate<ItemStack> allow;
    private final int max;

    private final ResourceLocation hintSprite;

    public FilteredHintedSlot(Container container, int index, int x, int y,
                              Predicate<ItemStack> allow,
                              int maxStack,
                              ResourceLocation hintSprite) {
        super(container, index, x, y);
        this.allow = allow;
        this.max = maxStack;
        this.hintSprite = hintSprite;
    }

    @Override
    public boolean mayPlace(ItemStack stack) { return allow == null || allow.test(stack); }

    @Override
    public int getMaxStackSize() { return max > 0 ? max : super.getMaxStackSize(); }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        int slotCap = getMaxStackSize();
        return Math.min(slotCap, stack.getMaxStackSize());
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return hintSprite == null ? null : Pair.of(InventoryMenu.BLOCK_ATLAS, hintSprite);
    }
}
