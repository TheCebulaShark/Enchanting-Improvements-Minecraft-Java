package org.blahajenjoyer.enchanting_improvements.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CountRange(int min, int max) {
    public static final Codec<CountRange> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("min").forGetter(CountRange::min),
            Codec.INT.fieldOf("max").forGetter(CountRange::max)
    ).apply(inst, CountRange::new));
}