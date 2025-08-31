package org.blahajenjoyer.enchanting_improvements.menu;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;

public class HintedSlot extends Slot {
    private final ResourceLocation hintSprite;

    public HintedSlot(Container container, int index, int x, int y, ResourceLocation hintSprite) {
        super(container, index, x, y);
        this.hintSprite = hintSprite;
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return hintSprite == null ? null : Pair.of(InventoryMenu.BLOCK_ATLAS, hintSprite);
    }
}
