package org.blahajenjoyer.enchanting_improvements.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record UniversalCostRule(
        int maxLevel,
        List<PerLevelRule> rules
) {
    public static final Codec<UniversalCostRule> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("max_level").forGetter(UniversalCostRule::maxLevel),
            PerLevelRule.CODEC.listOf().fieldOf("rules").forGetter(UniversalCostRule::rules)
    ).apply(i, UniversalCostRule::new));

    public record PerLevelRule(int level, EnchantCostRule rule) {
        public static final Codec<PerLevelRule> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("level").forGetter(PerLevelRule::level),
                EnchantCostRule.CODEC.fieldOf("rule")
                        .orElse(new EnchantCostRule(
                                Optional.empty(), Optional.empty(), Optional.empty(),
                                Optional.empty(), Optional.empty()
                        ))
                        .forGetter(PerLevelRule::rule)
        ).apply(i, PerLevelRule::new));
    }
}