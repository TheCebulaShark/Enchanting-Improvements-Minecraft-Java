package org.blahajenjoyer.enchanting_improvements.data.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.blahajenjoyer.enchanting_improvements.EnchantingImprovements;

public final class EnchantingTags {
    public static final TagKey<Enchantment> TREASURE_BOOK_WHITELIST =
            TagKey.create(Registries.ENCHANTMENT,
                    new ResourceLocation(EnchantingImprovements.MOD_ID, "treasure_enchant_whitelist"));

    public static final TagKey<Enchantment> TREASURE_BOOK_BLACKLIST =
            TagKey.create(Registries.ENCHANTMENT,
                    new ResourceLocation(EnchantingImprovements.MOD_ID, "treasure_enchant_blacklist"));

    private EnchantingTags() {}
}
