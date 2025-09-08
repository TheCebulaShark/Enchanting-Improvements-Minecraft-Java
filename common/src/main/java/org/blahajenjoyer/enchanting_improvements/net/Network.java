package org.blahajenjoyer.enchanting_improvements.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.blahajenjoyer.enchanting_improvements.menu.EnchantMenu;

import java.util.HashMap;
import java.util.Map;

import static org.blahajenjoyer.enchanting_improvements.EnchantingImprovements.MOD_ID;

public final class Network {
    private Network() {}
    public static final ResourceLocation APPLY_ENCHANT = new ResourceLocation(MOD_ID, "apply_enchant");

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, APPLY_ENCHANT, Network::handleApplyEnchant);
    }

    /** CLIENT -> SERVER */
    public static void sendApplyEnchant(Enchantment ench, int level) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ResourceLocation key = BuiltInRegistries.ENCHANTMENT.getKey(ench);
        if (key == null) return;
        buf.writeResourceLocation(key);
        buf.writeVarInt(level);
        NetworkManager.sendToServer(APPLY_ENCHANT, buf);
    }

    /* SERVER handler */
    private static void handleApplyEnchant(FriendlyByteBuf buf, PacketContext ctx) {
        final ResourceLocation enchId = buf.readResourceLocation();
        final int reqLevel = buf.readVarInt();

        ctx.queue(() -> {
            ServerPlayer player = (ServerPlayer) ctx.getPlayer();
            if (!(player.containerMenu instanceof EnchantMenu menu)) return;

            Enchantment ench = BuiltInRegistries.ENCHANTMENT.get(enchId);
            if (ench == null) return;

            int lvl = Math.max(1, Math.min(reqLevel, ench.getMaxLevel()));

            Slot itemSlot = menu.getSlot(EnchantMenu.SLOT_ITEM);
            ItemStack stack = itemSlot.getItem();
            if (stack.isEmpty()) return;

            boolean can = stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK) || ench.canEnchant(stack);
            if (!can) return;

            applyOrOverwrite(player, menu, itemSlot, stack, ench, lvl);
            menu.broadcastChanges();
        });
    }

    private static void applyOrOverwrite(ServerPlayer player, EnchantMenu menu,
                                         Slot itemSlot, ItemStack stack,
                                         Enchantment ench, int level) {

        if (stack.is(Items.BOOK)) {
            ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(newBook, new EnchantmentInstance(ench, level));

            // zabierz 1 szt. z itemSlot
            stack.shrink(1);

            // spróbuj wstawić nową książkę w SLOT_BOOK
            Slot bookSlot = menu.getSlot(EnchantMenu.SLOT_BOOK);
            if (!bookSlot.hasItem()) {
                bookSlot.set(newBook);
            } else if (!itemSlot.hasItem()) {
                // jeżeli akurat slot itemu się opróżnił – można wstawić tam
                itemSlot.set(newBook);
            } else if (!player.getInventory().add(newBook)) {
                // brak miejsca – wyrzuć
                player.drop(newBook, false);
            }
            return;
        }

        // 2) Zaczarowana książka – nadpisz/ustaw poziom w NBT
        if (stack.is(Items.ENCHANTED_BOOK)) {
            Map<Enchantment, Integer> map = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
            map.put(ench, level);

            ItemStack out = new ItemStack(Items.ENCHANTED_BOOK);
            for (var e : map.entrySet()) {
                EnchantedBookItem.addEnchantment(out, new EnchantmentInstance(e.getKey(), e.getValue()));
            }
            itemSlot.set(out); // podmień zawartość slotu
            return;
        }

        // 3) Zwykły item – ustaw/zmień mapę enchantów (nadpisuje poziom, jeśli już był)
        Map<Enchantment, Integer> map = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        map.put(ench, level);
        EnchantmentHelper.setEnchantments(map, stack);
        itemSlot.setChanged();
    }
}
