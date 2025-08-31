package org.blahajenjoyer.enchanting_improvements.menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.blahajenjoyer.enchanting_improvements.registry.Registries;

import static org.blahajenjoyer.enchanting_improvements.EnchantingImprovements.MOD_ID;

public class EnchantMenu extends AbstractContainerMenu {
    public static final int SLOT_ITEM = 0;
    public static final int SLOT_LAPIS = 1;
    public static final int SLOT_MATERIAL = 2;
    public static final int SLOT_BOOK = 3;

    private static final int MENU_SLOTS = 4;
    private final SimpleContainer container = new SimpleContainer(MENU_SLOTS);
    private final ContainerLevelAccess access;

    public EnchantMenu(int id, Inventory inv) {
        this(id, inv, ContainerLevelAccess.NULL);
    }

    public EnchantMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(Registries.ENCHANT_MENU.get(), id);
        this.access = access;

        // Enchanitng table slots
        this.addSlot(new HintedSlot(container, SLOT_ITEM,     20, 49, null));
        this.addSlot(new HintedSlot(container, SLOT_LAPIS,    30, 73, new ResourceLocation(MOD_ID,"item/empty_slot_lapis_lazuli")));
        this.addSlot(new HintedSlot(container, SLOT_MATERIAL, 10, 73, new ResourceLocation(MOD_ID,"item/empty_slot_material")));
        this.addSlot(new HintedSlot(container, SLOT_BOOK,     20, 94, new ResourceLocation(MOD_ID,"item/empty_slot_enchanted_book")));

        // Players slots
        int startX = 12, startY = 130;
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, startX + col * 18, startY + row * 18));
        for (int hot = 0; hot < 9; ++hot)
            this.addSlot(new Slot(inv, hot, startX + hot * 18, startY + 58));
    }



    @Override public boolean stillValid(Player player) { return true; }

    @Override //not final
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            newStack = stack.copy();
            if (index < MENU_SLOTS) {
                if (!this.moveItemStackTo(stack, MENU_SLOTS, this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(stack, 0, MENU_SLOTS, false))
                    return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return newStack;
    }
}