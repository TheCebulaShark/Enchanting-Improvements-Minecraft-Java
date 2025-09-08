package org.blahajenjoyer.enchanting_improvements.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.blahajenjoyer.enchanting_improvements.logic.EnchantingHelper;
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
        this.addSlot(new FilteredHintedSlot(
                container, SLOT_ITEM, 20, 49,
                EnchantingHelper::isEnchantable, 1,
                null));

        this.addSlot(new FilteredHintedSlot(
                container, SLOT_LAPIS, 30, 73,
                EnchantingHelper::isLapis, 64,
                new ResourceLocation(MOD_ID,"item/empty_slot_lapis_lazuli")));

        this.addSlot(new FilteredHintedSlot(
                container, SLOT_MATERIAL, 10, 73,
                EnchantingHelper::isMaterial, 64,
                new ResourceLocation(MOD_ID,"item/empty_slot_material")));

        this.addSlot(new FilteredHintedSlot(container, SLOT_BOOK, 20, 94,
                EnchantingHelper::isEnchantedBook, 1,
                new ResourceLocation(MOD_ID,"item/empty_slot_enchanted_book")));

        // Players slots
        int startX = 12, startY = 130;
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, startX + col * 18, startY + row * 18));
        for (int hot = 0; hot < 9; ++hot)
            this.addSlot(new Slot(inv, hot, startX + hot * 18, startY + 58));
    }

    public BlockPos getTablePos() {
        return this.access.evaluate((level, pos) -> pos, BlockPos.ZERO);
    }

    private static int getPowerAt(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.ENCHANTMENT_POWER_PROVIDER) ? 1 : 0;
    }

    public static int computeShelfPower(net.minecraft.world.level.Level level, BlockPos tablePos) {
        int power = 0;
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                if ((dx != 0 || dz != 0)
                        && level.isEmptyBlock(tablePos.offset(dx, 0, dz))
                        && level.isEmptyBlock(tablePos.offset(dx, 1, dz))) {

                    power += getPowerAt(level, tablePos.offset(dx * 2, 0, dz * 2));
                    if (dx != 0 && dz != 0) {
                        power += getPowerAt(level, tablePos.offset(dx * 2, 0, dz));
                        power += getPowerAt(level, tablePos.offset(dx, 0, dz * 2));
                    }
                }
            }
        }
        return power;
    }

    @Override public boolean stillValid(Player player) { return true; }

    private boolean moveToSlot(ItemStack stack, int slotIndex) {
        Slot dst = this.slots.get(slotIndex);
        if (!dst.mayPlace(stack)) return false;

        ItemStack existing = dst.getItem();
        int limit = dst.getMaxStackSize(stack);
        if (limit <= 0) return false;

        if (existing.isEmpty()) {
            int toMove = Math.min(limit, stack.getCount());
            if (toMove <= 0) return false;
            ItemStack moved = stack.split(toMove);
            dst.set(moved);
            dst.setChanged();
            return true;
        } else {
            if (!ItemStack.isSameItemSameTags(existing, stack)) return false;
            int free = limit - existing.getCount();
            if (free <= 0) return false;
            int toMove = Math.min(free, stack.getCount());
            existing.grow(toMove);
            stack.shrink(toMove);
            dst.setChanged();
            return true;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack newStack = stack.copy();

        if (index < MENU_SLOTS) {
            if (!this.moveItemStackTo(stack, MENU_SLOTS, this.slots.size(), true))
                return ItemStack.EMPTY;
            slot.onQuickCraft(stack, newStack);
        }
        else {
            if (EnchantingHelper.isEnchantedBook(stack)) {
                if (!moveToSlot(stack, SLOT_BOOK)) {
                    if (!moveToSlot(stack, SLOT_ITEM))
                        return ItemStack.EMPTY;
                }
            } else if (EnchantingHelper.isLapis(stack)) {
                if (!moveToSlot(stack, SLOT_LAPIS)) {
                    if (!moveToSlot(stack, SLOT_MATERIAL))
                        return ItemStack.EMPTY;
                }
            } else if (EnchantingHelper.isEnchantable(stack)) {
                if (!moveToSlot(stack, SLOT_ITEM))
                    return ItemStack.EMPTY;
            } else if (EnchantingHelper.isMaterial(stack)) {
                if (!moveToSlot(stack, SLOT_MATERIAL))
                    return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return newStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide) return;

        for (int i = 0; i < this.container.getContainerSize(); ++i) {
            ItemStack stack = this.container.removeItemNoUpdate(i);
            if (stack.isEmpty()) continue;

            if (!player.isAlive() || (player instanceof ServerPlayer sp && sp.hasDisconnected())) {
                player.drop(stack, false);
            } else {
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }
}