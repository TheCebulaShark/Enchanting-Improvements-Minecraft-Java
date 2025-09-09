package org.blahajenjoyer.enchanting_improvements.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import io.netty.buffer.Unpooled;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.blahajenjoyer.enchanting_improvements.data.MaterialCost;
import org.blahajenjoyer.enchanting_improvements.menu.EnchantMenu;
import org.blahajenjoyer.enchanting_improvements.util.EnchantingHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.blahajenjoyer.enchanting_improvements.EnchantingImprovements.MOD_ID;

public final class EnchantingPackets {
    private EnchantingPackets() {}
    public static final ResourceLocation APPLY_ENCHANT = new ResourceLocation(MOD_ID, "apply_enchant");

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, APPLY_ENCHANT, EnchantingPackets::handleApplyEnchant);
    }

    /** CLIENT -> SERVER */
    public static void sendApplyEnchant(Enchantment ench, int level) {
        sendApplyEnchant(ench, level, false);
    }
    public static void sendApplyEnchant(Enchantment ench, int level, boolean shift) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ResourceLocation key = BuiltInRegistries.ENCHANTMENT.getKey(ench);
        if (key == null) return;
        buf.writeResourceLocation(key);
        buf.writeVarInt(level);
        buf.writeBoolean(shift);
        NetworkManager.sendToServer(APPLY_ENCHANT, buf);
    }

    /* SERVER handler */
    private static void handleApplyEnchant(FriendlyByteBuf buf, PacketContext ctx) {
        ResourceLocation enchId = buf.readResourceLocation();
        int level = buf.readVarInt();
        boolean shiftFill = buf.isReadable() ? buf.readBoolean() : false;

        ctx.queue(() -> {
            ServerPlayer player = (ServerPlayer) ctx.getPlayer();
            if (!(player.containerMenu instanceof EnchantMenu menu)) return;

            Enchantment ench = player.server.registryAccess().registryOrThrow(Registries.ENCHANTMENT).get(enchId);
            if (ench == null) return;

            Slot itemSlot = menu.getSlot(EnchantMenu.SLOT_ITEM);
            ItemStack stack = itemSlot.getItem();
            if (stack.isEmpty()) return;

            final boolean creative = player.getAbilities().instabuild;

            var tablePos = menu.getTablePos();
            int shelf = EnchantMenu.computeShelfPower(player.level(), tablePos);

            var rolledOpt = org.blahajenjoyer.enchanting_improvements.enchanting.EnchantCosts.roll(player, stack, ench, level, shelf, tablePos);
            if (rolledOpt.isEmpty()) return;
            var rolled = rolledOpt.get();

            Slot bookSlot = menu.getSlot(EnchantMenu.SLOT_BOOK);
            boolean hasOverrideBook = isBookWithEnchant(bookSlot.getItem(), ench, level);

            boolean requiresBook = EnchantingHelper.isEnchantedBookRequired(player.serverLevel(), ench);

            boolean movedAnything = false;
            boolean requirementsInitiallyMet =
                    creative || requirementsMet(rolled, hasOverrideBook, requiresBook, ench, level, menu);

            if (!creative) {
                if (requiresBook) {
                    var res = ensureSlotMatches(player, menu, EnchantMenu.SLOT_BOOK,
                            s -> isBookWithEnchant(s, ench, level), 1, false);
                    movedAnything |= res.moved;
                    if (!res.ok) { menu.broadcastChanges(); return; }
                    hasOverrideBook = true;
                }

                if (!hasOverrideBook && rolled.lapis() > 0) {
                    var res = ensureSlotMatches(player, menu, EnchantMenu.SLOT_LAPIS,
                            s -> s.is(Items.LAPIS_LAZULI), rolled.lapis(), shiftFill);
                    movedAnything |= res.moved;
                    if (!res.ok) { menu.broadcastChanges(); return; }
                }
                if (!hasOverrideBook && rolled.material() != null && rolled.materialCount() > 0) {
                    var res = ensureSlotMatches(player, menu, EnchantMenu.SLOT_MATERIAL,
                            materialMatcher(rolled.material()), rolled.materialCount(), shiftFill);
                    movedAnything |= res.moved;
                    if (!res.ok) { menu.broadcastChanges(); return; }
                }

                if (!requirementsInitiallyMet && movedAnything) {
                    menu.broadcastChanges();
                    return;
                }

                if (!creative && player.experienceLevel < rolled.xp()) return;
            }

            if (!creative) {
                if (requiresBook || hasOverrideBook) {
                    ItemStack book = bookSlot.getItem();
                    if (book.isEmpty() || !isBookWithEnchant(book, ench, level)) return;
                    book.shrink(1);
                    bookSlot.setChanged();

                    player.giveExperienceLevels(-rolled.xp());
                } else {
                    player.giveExperienceLevels(-rolled.xp());
                    if (rolled.lapis() > 0) {
                        Slot lapSlot = menu.getSlot(EnchantMenu.SLOT_LAPIS);
                        ItemStack lap = lapSlot.getItem();
                        if (lap.getCount() < rolled.lapis()) return;
                        lap.shrink(rolled.lapis());
                        lapSlot.setChanged();
                    }
                    if (rolled.material() != null && rolled.materialCount() > 0) {
                        Slot matSlot = menu.getSlot(EnchantMenu.SLOT_MATERIAL);
                        ItemStack mat = matSlot.getItem();
                        if (!materialMatcher(rolled.material()).test(mat) || mat.getCount() < rolled.materialCount()) return;
                        mat.shrink(rolled.materialCount());
                        matSlot.setChanged();
                    }
                }
            }

            applyOrOverwrite(player, menu, itemSlot, stack, ench, level);

            player.level().playSound(null, tablePos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F,
                    player.level().random.nextFloat() * 0.1F + 0.9F);
            player.onEnchantmentPerformed(stack, level);

            player.awardStat(Stats.ENCHANT_ITEM);

            CriteriaTriggers.ENCHANTED_ITEM.trigger(player, stack, rolled.xp());

            menu.broadcastChanges();
        });
    }

    private static final class EnsureResult {
        final boolean ok, moved;
        EnsureResult(boolean ok, boolean moved) { this.ok = ok; this.moved = moved; }
    }

    private static EnsureResult ensureSlotMatches(ServerPlayer player, EnchantMenu menu, int slotIdx,
                                                  Predicate<ItemStack> matcher, int needCount, boolean fillMax) {
        boolean moved = false;
        Slot slot = menu.getSlot(slotIdx);
        ItemStack inSlot = slot.getItem();

        if (!inSlot.isEmpty() && !matcher.test(inSlot)) {
            if (!moveWholeStackToInv(player, inSlot.copy())) {
                int idx = findInventoryIndex(player, matcher);
                if (idx == -1) return new EnsureResult(false, moved);
                ItemStack fromInv = getInventoryStack(player, idx).copy();
                if (!slot.mayPlace(fromInv)) return new EnsureResult(false, moved);

                setInventoryStack(player, idx, inSlot.copy());
                slot.set(fromInv.copy());
                slot.setChanged();
            } else {
                slot.set(ItemStack.EMPTY);
                slot.setChanged();
            }
            moved = true;
            inSlot = slot.getItem();
        }

        int have = (!inSlot.isEmpty() && matcher.test(inSlot)) ? inSlot.getCount() : 0;
        int limit = slot.getMaxStackSize(inSlot.isEmpty() ? ItemStack.EMPTY : inSlot);
        int target = fillMax ? limit : Math.max(have, needCount);
        int toAdd = Math.max(0, target - have);
        if (toAdd <= 0) return new EnsureResult(!inSlot.isEmpty() && matcher.test(inSlot) && inSlot.getCount() >= needCount, moved);

        int added = moveFromInvToSlot(player, slot, matcher, toAdd);
        moved |= (added > 0);

        ItemStack now = slot.getItem();
        boolean ok = !now.isEmpty() && matcher.test(now) && now.getCount() >= needCount;
        return new EnsureResult(ok, moved);
    }

    private static int moveFromInvToSlot(ServerPlayer player, Slot slot,
                                         Predicate<ItemStack> matcher, int maxToMove) {
        int left = maxToMove;
        for (var list : List.of(player.getInventory().items, player.getInventory().offhand)) {
            for (int i = 0; i < list.size() && left > 0; i++) {
                ItemStack s = list.get(i);
                if (s.isEmpty() || !matcher.test(s)) continue;

                int limit = slot.getMaxStackSize(slot.getItem());
                ItemStack inSlot = slot.getItem();
                int free = (inSlot.isEmpty() ? Math.min(limit, s.getMaxStackSize()) : (limit - inSlot.getCount()));
                if (free <= 0) return maxToMove - left;

                int willMove = Math.min(Math.min(left, s.getCount()), free);

                if (inSlot.isEmpty()) {
                    ItemStack moved = s.copy();
                    moved.setCount(willMove);
                    s.shrink(willMove);
                    slot.set(moved);
                    slot.setChanged();
                } else {
                    inSlot.grow(willMove);
                    s.shrink(willMove);
                    slot.setChanged();
                }
                left -= willMove;
            }
        }
        return maxToMove - left;
    }

    private static boolean moveWholeStackToInv(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return true;
        ItemStack copy = stack.copy();
        boolean added = player.getInventory().add(copy);
        if (!added) return false;
        return true;
    }

    private static int findInventoryIndex(ServerPlayer player, Predicate<ItemStack> matcher) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack s = player.getInventory().items.get(i);
            if (!s.isEmpty() && matcher.test(s)) return i;
        }
        int off = player.getInventory().items.size();
        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            ItemStack s = player.getInventory().offhand.get(i);
            if (!s.isEmpty() && matcher.test(s)) return off + i;
        }
        return -1;
    }
    private static ItemStack getInventoryStack(ServerPlayer player, int flatIndex) {
        int main = player.getInventory().items.size();
        if (flatIndex < main) return player.getInventory().items.get(flatIndex);
        return player.getInventory().offhand.get(flatIndex - main);
    }
    private static void setInventoryStack(ServerPlayer player, int flatIndex, ItemStack stack) {
        int main = player.getInventory().items.size();
        if (flatIndex < main) player.getInventory().items.set(flatIndex, stack);
        else player.getInventory().offhand.set(flatIndex - main, stack);
    }

    private static boolean isBookWithEnchant(ItemStack s, Enchantment ench, int level) {
        if (!s.is(Items.ENCHANTED_BOOK)) return false;
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(s);
        Integer lv = map.get(ench);
        return lv != null && lv >= level;
    }

    private static Predicate<ItemStack> materialMatcher(MaterialCost m) {
        if (m.item() != null) {
            Item item = BuiltInRegistries.ITEM.get(m.item());
            return s -> !s.isEmpty() && s.is(item);
        } else {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, m.tag());
            return s -> !s.isEmpty() && s.is(tag);
        }
    }

    private static boolean requirementsMet(
            org.blahajenjoyer.enchanting_improvements.enchanting.EnchantCosts.Rolled rolled,
            boolean hasOverrideBook,
            boolean requiresBook,
            Enchantment ench,
            int level,
            EnchantMenu menu
    ) {
        if (hasOverrideBook || requiresBook) {
            ItemStack book = menu.getSlot(EnchantMenu.SLOT_BOOK).getItem();
            return isBookWithEnchant(book, ench, level);
        }
        if (rolled.lapis() > 0) {
            ItemStack lap = menu.getSlot(EnchantMenu.SLOT_LAPIS).getItem();
            if (lap.isEmpty() || lap.getCount() < rolled.lapis()) return false;
        }
        if (rolled.material() != null && rolled.materialCount() > 0) {
            ItemStack mat = menu.getSlot(EnchantMenu.SLOT_MATERIAL).getItem();
            if (mat.isEmpty() || mat.getCount() < rolled.materialCount()) return false;
        }
        return true;
    }

    private static void applyOrOverwrite(ServerPlayer player, EnchantMenu menu, Slot itemSlot, ItemStack stack, Enchantment ench, int level) {
        if (stack.is(Items.BOOK)) {
            ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(newBook, new EnchantmentInstance(ench, level));
            stack.shrink(1);

            Slot bookSlot = menu.getSlot(EnchantMenu.SLOT_BOOK);
            if (!bookSlot.hasItem()) {
                bookSlot.set(newBook);
            } else if (!itemSlot.hasItem()) {
                itemSlot.set(newBook);
            } else if (!player.getInventory().add(newBook)) {
                player.drop(newBook, false);
            }
            return;
        }

        if (stack.is(Items.ENCHANTED_BOOK)) {
            Map<Enchantment, Integer> map = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
            map.put(ench, level);
            ItemStack out = new ItemStack(Items.ENCHANTED_BOOK);
            for (var e : map.entrySet()) {
                EnchantedBookItem.addEnchantment(out, new EnchantmentInstance(e.getKey(), e.getValue()));
            }
            itemSlot.set(out);
            return;
        }

        Map<Enchantment, Integer> map = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        map.put(ench, level);
        EnchantmentHelper.setEnchantments(map, stack);
        itemSlot.setChanged();
    }
}
