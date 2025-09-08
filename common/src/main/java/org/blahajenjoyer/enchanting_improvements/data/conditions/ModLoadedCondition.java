package org.blahajenjoyer.enchanting_improvements.data.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;

public record ModLoadedCondition(String modid) {
    public static final Codec<ModLoadedCondition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("type")
                    .orElse("enchanting_improvements:mod_loaded").forGetter(x -> "enchanting_improvements:mod_loaded"),
            Codec.STRING.fieldOf("modid").forGetter(ModLoadedCondition::modid)
    ).apply(i, (ignored, modid) -> new ModLoadedCondition(modid)));

    public boolean test() {
        return Platform.isModLoaded(modid);
    }
}