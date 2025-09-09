package org.blahajenjoyer.enchanting_improvements.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.blahajenjoyer.enchanting_improvements.data.conditions.ModLoadedCondition;

import java.util.List;
import java.util.Optional;

public record MaterialCost(
        ResourceLocation item,
        ResourceLocation tag,
        CountRange count,
        Optional<List<ModLoadedCondition>> conditions,
        int weight
) {
    public MaterialCost {
        if ((item == null) == (tag == null))
            throw new IllegalStateException("Exactly one of 'item' or 'tag' must be set.");
        if (conditions == null)
            conditions = Optional.empty();
        if (weight <= 0)
            weight = 1;
    }

    public static final Codec<MaterialCost> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.optionalFieldOf("item").forGetter(m -> Optional.ofNullable(m.item)),
            ResourceLocation.CODEC.optionalFieldOf("tag").forGetter(m -> Optional.ofNullable(m.tag)),
            CountRange.CODEC.fieldOf("count").forGetter(MaterialCost::count),
            ModLoadedCondition.CODEC.listOf().optionalFieldOf("conditions").forGetter(MaterialCost::conditions),
            com.mojang.serialization.Codec.INT.optionalFieldOf("weight", 1).forGetter(MaterialCost::weight)
        ).apply(inst, (optItem, optTag, cnt, cond, w) ->
            new MaterialCost(optItem.orElse(null), optTag.orElse(null), cnt, cond, w)
    ));

    public static MaterialCost ofItem(ResourceLocation id, int min, int max) {
        return new MaterialCost(id, null, new CountRange(min, max), null, 1);
    }
    public static MaterialCost ofItem(ResourceLocation id, int min, int max, int weight) {
        return new MaterialCost(id, null, new CountRange(min, max), null, weight);
    }

    public static MaterialCost ofTag(ResourceLocation id, int min, int max) {
        return new MaterialCost(null, id, new CountRange(min, max), null, 1);
    }
    public static MaterialCost ofTag(ResourceLocation id, int min, int max, int weight) {
        return new MaterialCost(null, id, new CountRange(min, max), null, weight);
    }

    public boolean isEnabled() {
        return conditions.isEmpty() || conditions.get().stream().allMatch(ModLoadedCondition::test);
    }
}
