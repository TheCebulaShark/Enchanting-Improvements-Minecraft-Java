package org.blahajenjoyer.enchanting_improvements.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;
import java.util.function.Function;

public final class EnchantCostData {
    /** Per-enchant: data/<ns>/costs/<target_ns>/<enchant_name>/<level>.json */
    public static final String PATH_COSTS = "ench_cost";
    /** Global fallback: data/<ns>/fallback/universal_max_level_X.json */
    public static final String PATH_FALLBACK = "fallback";

    private static final Map<ResourceLocation, Map<Integer, EnchantCostRule>> SPECIFIC = new HashMap<>();
    private static final Map<Integer, UniversalCostRule> UNIVERSAL_GLOBAL = new HashMap<>();

    public record Resolved(
            int shelfPower, int playerLevelReq, int xpCost, int lapis,
            List<MaterialCost> materials, boolean requiresEnchantedBook
    ) {}

    public static void registerReloaders() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new CostsReloader());
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new FallbacksReloader());
    }

    public record LevelRule(int level, EnchantCostRule rule) {
        public static final Codec<LevelRule> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("level").forGetter(LevelRule::level),
                EnchantCostRule.CODEC.fieldOf("rule").forGetter(LevelRule::rule)
        ).apply(i, LevelRule::new));
    }
    public record SpecificFile(ResourceLocation enchant, List<LevelRule> rules) {
        public static final Codec<SpecificFile> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("enchant").forGetter(SpecificFile::enchant),
                LevelRule.CODEC.listOf().fieldOf("rules").forGetter(SpecificFile::rules)
        ).apply(i, SpecificFile::new));
    }

    private static final class CostsReloader extends SimpleJsonResourceReloadListener {
        CostsReloader() { super(new com.google.gson.Gson(), PATH_COSTS); }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager rm, ProfilerFiller p) {
            SPECIFIC.clear();
            jsons.forEach((res, json) -> {
                try {
                    SpecificFile file = SpecificFile.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(false, s -> {});
                    var map = SPECIFIC.computeIfAbsent(file.enchant(), k -> new HashMap<>());
                    for (var lr : file.rules()) {
                        map.put(lr.level(), lr.rule());
                    }
                } catch (Exception ignored) {}
            });
        }
    }

    private static final class FallbacksReloader extends SimpleJsonResourceReloadListener {
        FallbacksReloader() { super(new com.google.gson.Gson(), PATH_FALLBACK); }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager rm, ProfilerFiller p) {
            UNIVERSAL_GLOBAL.clear();
            jsons.forEach((res, json) -> {
                try {
                    int maxLvlFromName = parseGlobalMaxLevel(res); // universal_max_level_X.json
                    UniversalCostRule spec = UniversalCostRule.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(false, s -> {});
                    if (spec.maxLevel() != maxLvlFromName) return;
                    UNIVERSAL_GLOBAL.put(maxLvlFromName, spec);
                } catch (Exception ignored) {}
            });
        }
    }

    private static int parseGlobalMaxLevel(ResourceLocation res) {
        String path = res.getPath();
        String base = PATH_FALLBACK + "/";
        if (path.startsWith(base)) path = path.substring(base.length());
        String file = path.replace(".json", "");
        String prefix = "universal_max_level_";
        if (!file.startsWith(prefix)) {
            throw new IllegalArgumentException("Bad fallback filename: " + res);
        }
        return Integer.parseInt(file.substring(prefix.length()));
    }


    public static Optional<Resolved> resolve(ResourceLocation enchantId, int level, int actualMaxLevel) {
        final EnchantCostRule spec = Optional.ofNullable(SPECIFIC.get(enchantId))
                .map(m -> m.get(level))
                .orElse(null);

        final UniversalCostRule uni = pickUniversal(actualMaxLevel);

        Optional<Integer> shelf = firstPresent(opt(spec, EnchantCostRule::shelfPower),
                uniLevel(uni, level, EnchantCostRule::shelfPower));
        Optional<Integer> plevel = firstPresent(opt(spec, EnchantCostRule::playerLevelReq),
                uniLevel(uni, level, EnchantCostRule::playerLevelReq));
        Optional<Integer> xp = firstPresent(opt(spec, EnchantCostRule::xpCost),
                uniLevel(uni, level, EnchantCostRule::xpCost));
        Optional<Integer> lapis = firstPresent(opt(spec, EnchantCostRule::lapis),
                uniLevel(uni, level, EnchantCostRule::lapis));
        Optional<List<MaterialCost>> mats =
                firstPresent(opt(spec, r -> r.materials().map(EnchantCostData::filterEnabled)),
                        uniLevel(uni, level, r -> r.materials().map(EnchantCostData::filterEnabled)));
        Optional<Boolean> bookReq = firstPresent(opt(spec, EnchantCostRule::requiresEnchantedBook),
                uniLevel(uni, level, EnchantCostRule::requiresEnchantedBook));

        if (shelf.isEmpty() || plevel.isEmpty() || xp.isEmpty() || lapis.isEmpty() || bookReq.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Resolved(
                shelf.get(),
                plevel.get(),
                xp.get(),
                lapis.get(),
                mats.orElse(List.of()),
                bookReq.get()
        ));
    }

    public static Optional<Resolved> resolve(net.minecraft.world.item.enchantment.Enchantment ench, int level) {
        return resolve(net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getKey(ench), level, ench.getMaxLevel());
    }

    private static UniversalCostRule pickUniversal(int actualMaxLevel) {
        if (UNIVERSAL_GLOBAL.isEmpty()) return null;

        UniversalCostRule exact = UNIVERSAL_GLOBAL.get(actualMaxLevel);
        if (exact != null) return exact;

        int le = UNIVERSAL_GLOBAL.keySet().stream()
                .filter(k -> k <= actualMaxLevel)
                .max(Integer::compare)
                .orElse(-1);
        if (le != -1) return UNIVERSAL_GLOBAL.get(le);

        int any = UNIVERSAL_GLOBAL.keySet().stream().max(Integer::compare).orElse(-1);
        return any == -1 ? null : UNIVERSAL_GLOBAL.get(any);
    }

    private static <T> Optional<T> firstPresent(Optional<T> a, Optional<T> b) {
        return a.isPresent() ? a : b;
    }

    private static <T> Optional<T> opt(EnchantCostRule r, Function<EnchantCostRule, Optional<T>> f) {
        return r == null ? Optional.empty() : f.apply(r);
    }

    private static <T> Optional<T> uniLevel(UniversalCostRule spec, int lvl, Function<EnchantCostRule, Optional<T>> f) {
        if (spec == null) return Optional.empty();
        return spec.rules().stream()
                .filter(rr -> rr.level() == lvl)
                .findFirst()
                .flatMap(rr -> f.apply(rr.rule()));
    }

    static List<MaterialCost> filterEnabled(List<MaterialCost> in) {
        return in.stream().filter(MaterialCost::isEnabled).toList();
    }
}