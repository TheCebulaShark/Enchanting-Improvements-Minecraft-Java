package org.blahajenjoyer.enchanting_improvements.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record EnchantCostRule(
        Optional<Integer> shelfPower,
        Optional<Integer> playerLevelReq,
        Optional<Integer> xpCost,
        Optional<Integer> lapis,
        Optional<List<MaterialCost>> materials,
        Optional<Boolean> requiresEnchantedBook,
        Optional<Boolean> overenchanting
) {
    public static final Codec<EnchantCostRule> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("shelf_power").forGetter(EnchantCostRule::shelfPower),
            Codec.INT.optionalFieldOf("player_level_req").forGetter(EnchantCostRule::playerLevelReq),
            Codec.INT.optionalFieldOf("xp_cost").forGetter(EnchantCostRule::xpCost),
            Codec.INT.optionalFieldOf("lapis").forGetter(EnchantCostRule::lapis),
            MaterialCost.CODEC.listOf().optionalFieldOf("materials").forGetter(EnchantCostRule::materials),
            Codec.BOOL.optionalFieldOf("requires_enchanted_book").forGetter(EnchantCostRule::requiresEnchantedBook),
            Codec.BOOL.optionalFieldOf("overenchanting").forGetter(EnchantCostRule::overenchanting)
    ).apply(i, EnchantCostRule::new));
}