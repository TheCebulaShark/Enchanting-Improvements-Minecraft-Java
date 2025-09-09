package org.blahajenjoyer.enchanting_improvements.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.blahajenjoyer.enchanting_improvements.EnchantingImprovements;
import org.blahajenjoyer.enchanting_improvements.data.EnchantCostData;
import org.blahajenjoyer.enchanting_improvements.data.EnchantCostRule;
import org.blahajenjoyer.enchanting_improvements.data.MaterialCost;
import org.blahajenjoyer.enchanting_improvements.data.conditions.ModLoadedCondition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class EnchantCostJsonBuilder {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private EnchantCostJsonBuilder() {}

    public static MaterialCost matItem(String id, int min, int max) {
        return MaterialCost.ofItem(new ResourceLocation(id), min, max, 1);
    }
    public static MaterialCost matItemIfMod(String id, int min, int max, String modid) {
        var base = MaterialCost.ofItem(new ResourceLocation(id), min, max);
        return new MaterialCost(base.item(), base.tag(), base.count(),
                Optional.of(List.of(new ModLoadedCondition(modid))), 1);
    }

    public static MaterialCost matTag(String id, int min, int max) {
        return MaterialCost.ofTag(new ResourceLocation(id), min, max, 1);
    }
    public static MaterialCost matTagIfMod(String id, int min, int max, String modid) {
        var base = MaterialCost.ofTag(new ResourceLocation(id), min, max);
        return new MaterialCost(base.item(), base.tag(), base.count(),
                Optional.of(List.of(new ModLoadedCondition(modid))), 1);
    }

    public static MaterialCost matItem(String id, int min, int max, int weight) {
        return MaterialCost.ofItem(new ResourceLocation(id), min, max, weight);
    }
    public static MaterialCost matItemIfMod(String id, int min, int max, String modid, int weight) {
        var base = MaterialCost.ofItem(new ResourceLocation(id), min, max);
        return new MaterialCost(base.item(), base.tag(), base.count(),
                Optional.of(List.of(new ModLoadedCondition(modid))), weight);
    }

    public static MaterialCost matTag(String id, int min, int max, int weight) {
        return MaterialCost.ofTag(new ResourceLocation(id), min, max, weight);
    }
    public static MaterialCost matTagIfMod(String id, int min, int max, String modid, int weight) {
        var base = MaterialCost.ofTag(new ResourceLocation(id), min, max);
        return new MaterialCost(base.item(), base.tag(), base.count(),
                Optional.of(List.of(new ModLoadedCondition(modid))), weight);
    }

    public static Path pathSpecific(Path dataRoot, ResourceLocation enchantId) {
        return dataRoot
                .resolve("data").resolve(EnchantingImprovements.MOD_ID)
                .resolve(EnchantCostData.PATH_COSTS)
                .resolve(enchantId.getPath() + ".json");
    }

    public static void writeSpecific(Path dataRoot, ResourceLocation enchantId, List<LevelRule> rules) throws IOException {
        Path file = pathSpecific(dataRoot, enchantId);
        JsonObject root = new JsonObject();
        root.addProperty("enchant", enchantId.toString());

        JsonArray arr = new JsonArray();
        for (LevelRule lr : rules) {
            JsonObject o = new JsonObject();
            o.addProperty("level", lr.level());
            o.add("rule", toJsonRule(lr.rule()));
            arr.add(o);
        }
        root.add("rules", arr);
        writeJson(file, root);
    }

    public static Path pathUniversal(Path dataRoot, int maxLevel) {
        return dataRoot
                .resolve("data").resolve(EnchantingImprovements.MOD_ID)
                .resolve(EnchantCostData.PATH_FALLBACK)
                .resolve("universal_max_level_" + maxLevel + ".json");
    }

    public static void writeUniversal(Path dataRoot, int maxLevel, List<LevelRule> rules) throws IOException {
        Path file = pathUniversal(dataRoot, maxLevel);

        JsonObject root = new JsonObject();
        root.addProperty("max_level", maxLevel);

        JsonArray arr = new JsonArray();
        for (LevelRule lr : rules) {
            JsonObject o = new JsonObject();
            o.addProperty("level", lr.level());
            o.add("rule", toJsonRule(lr.rule()));
            arr.add(o);
        }
        root.add("rules", arr);

        writeJson(file, root);
    }

    public record LevelRule(int level, EnchantCostRule rule) {}

    public static JsonObject toJsonRule(EnchantCostRule r) {
        JsonObject root = new JsonObject();

        r.shelfPower().ifPresent(v -> root.addProperty("shelf_power", v));
        r.playerLevelReq().ifPresent(v -> root.addProperty("player_level_req", v));
        r.xpCost().ifPresent(v -> root.addProperty("xp_cost", v));
        r.lapis().ifPresent(v -> root.addProperty("lapis", v));

        if (r.materials().isPresent() && !r.materials().get().isEmpty()) {
            List<MaterialCost> list = r.materials().get();
            if (list.size() == 1) {
                root.add("materials", materialToJson(list.get(0)));
            } else {
                JsonArray arr = new JsonArray();
                for (MaterialCost m : list) arr.add(materialToJson(m));
                root.add("materials", arr);
            }
        }

        return root;
    }

    private static JsonObject materialToJson(MaterialCost m) {
        JsonObject o = new JsonObject();
        Optional.ofNullable(m.item()).ifPresent(id -> o.addProperty("item", id.toString()));
        Optional.ofNullable(m.tag()).ifPresent(id -> o.addProperty("tag", id.toString()));

        JsonObject cnt = new JsonObject();
        cnt.addProperty("min", m.count().min());
        cnt.addProperty("max", m.count().max());
        o.add("count", cnt);

        if (m.weight() != 1) {
            o.addProperty("weight", m.weight());
        }

        m.conditions().orElse(List.of()).stream().findAny().ifPresent(__ -> {
            JsonArray arr = new JsonArray();
            for (var c : m.conditions().orElse(List.of())) {
                JsonObject co = new JsonObject();
                co.addProperty("type", "enchanting_improvements:mod_loaded");
                co.addProperty("modid", c.modid());
                arr.add(co);
            }
            o.add("conditions", arr);
        });

        return o;
    }

    private static void writeJson(Path file, JsonObject json) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, GSON.toJson(json));
    }
}
