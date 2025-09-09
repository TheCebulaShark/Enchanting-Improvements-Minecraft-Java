package org.blahajenjoyer.enchanting_improvements.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.Enchantment;
import org.blahajenjoyer.enchanting_improvements.data.tags.EnchantingTags;

public class EnchantingHelper {
    public static boolean isTreasure(ServerLevel level, Enchantment ench) {
        boolean forced = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(BuiltInRegistries.ENCHANTMENT.getResourceKey(ench).orElseThrow())
                .map(h -> h.is(EnchantingTags.TREASURE_BOOK_WHITELIST)).orElse(false);

        boolean blocked = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(BuiltInRegistries.ENCHANTMENT.getResourceKey(ench).orElseThrow())
                .map(h -> h.is(EnchantingTags.TREASURE_BOOK_BLACKLIST)).orElse(false);

        return (ench.isTreasureOnly() || forced) && !blocked;
    }

    public static boolean isEnchantedBookRequired(ServerLevel level, Enchantment ench) {
        return isTreasure(level, ench);
    }
}
