package org.blahajenjoyer.enchanting_improvements.datagen;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.blahajenjoyer.enchanting_improvements.data.EnchantCostRule;
import org.blahajenjoyer.enchanting_improvements.data.MaterialCost;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class BuilderDev {
    /** TODO: cost rules for all vanilla enchants
     *  + some popular mods (Create, Supplementaries, Fossils & Archeology, Biome Makeover,
     *  Farmer's Delight etc.) */
    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        Path outRoot = args.length > 0 ? Path.of(args[0]) : Path.of("src/generated/resources");
        generateAll(outRoot);
        System.out.println("[EI] JSON enchanting costs generated into: " + outRoot.toAbsolutePath());
    }

    public static void generateAll(Path outRoot) throws Exception {

        writePower(outRoot);

        writeUniversal(outRoot);
    }

    private static void writePower(Path outRoot) throws Exception {
        ResourceLocation POWER = new ResourceLocation("minecraft", "power");

        List<EnchantCostJsonBuilder.LevelRule> rules = List.of(
                // level 1
                new EnchantCostJsonBuilder.LevelRule(1, rule(
                        3, 3, 1, 1,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:flint", 6, 16),
                                EnchantCostJsonBuilder.matItem("minecraft:iron_nugget", 12, 36),
                                EnchantCostJsonBuilder.matItemIfMod("create:copper_nugget", 18, 40, "create")
                        ),
                        false
                )),
                // level 2
                new EnchantCostJsonBuilder.LevelRule(2, rule(
                        6, 7, 1, 2,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:flint", 12, 24),
                                EnchantCostJsonBuilder.matItem("minecraft:copper_ingot", 10, 20),
                                EnchantCostJsonBuilder.matItem("minecraft:iron_ingot", 8, 12),
                                EnchantCostJsonBuilder.matItem("minecraft:gold_ingot", 4, 10),
                                EnchantCostJsonBuilder.matItem("minecraft:emerald", 1, 4)
                        ),
                        false
                )),
                // level 3
                new EnchantCostJsonBuilder.LevelRule(3, rule(
                        9, 15, 2, 3,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:flint", 52, 64),
                                EnchantCostJsonBuilder.matItem("minecraft:copper_ingot", 24, 48),
                                EnchantCostJsonBuilder.matItem("minecraft:iron_ingot", 18, 32),
                                EnchantCostJsonBuilder.matItem("minecraft:gold_ingot", 12, 24),
                                EnchantCostJsonBuilder.matItem("minecraft:emerald", 8, 20)
                        ),
                        false
                )),
                // level 4
                new EnchantCostJsonBuilder.LevelRule(4, rule(
                        12, 21, 4, 4,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:gold_ingot", 24, 48),
                                EnchantCostJsonBuilder.matItem("minecraft:emerald", 16, 28),
                                EnchantCostJsonBuilder.matItem("minecraft:diamond", 4, 8)
                        ),
                        false
                )),
                // level 5
                new EnchantCostJsonBuilder.LevelRule(5, rule(
                        12, 30, 5, 5,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:diamond", 12, 16),
                                EnchantCostJsonBuilder.matItem("minecraft:emerald", 48, 64),
                                EnchantCostJsonBuilder.matItem("minecraft:nether_star", 1, 1)
                        ),
                        false
                ))
        );

        // zapis: data/<modid>/ench_cost/power.json
        EnchantCostJsonBuilder.writeSpecific(outRoot, POWER, rules);
    }


    private static void writeUniversal(Path outRoot) throws Exception {
        // TODO: Change materials
        // max_level = 1
        EnchantCostJsonBuilder.writeUniversal(outRoot, 1, List.of(
                new EnchantCostJsonBuilder.LevelRule(1, rule(
                        1, 1, 1, 1,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:diamond", 16, 48),
                                EnchantCostJsonBuilder.matItem("minecraft:netherite_ingot", 1, 3),
                                EnchantCostJsonBuilder.matItem("minecraft:nether_star", 1, 1)
                        ),
                        false
                ))
        ));

        // max_level = 2
        EnchantCostJsonBuilder.writeUniversal(outRoot, 2, List.of(
                new EnchantCostJsonBuilder.LevelRule(1, rule(
                        2, 2, 1, 1,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:iron_ingot", 32, 48),
                                EnchantCostJsonBuilder.matItem("minecraft:copper_ingot", 40, 52)
                        ),
                        false
                )),
                new EnchantCostJsonBuilder.LevelRule(2, rule(
                        4, 4, 2, 1,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:diamond", 12, 36),
                                EnchantCostJsonBuilder.matItem("minecraft:emerald", 32, 64)
                        ),
                        false
                ))
        ));

        // max_level = 3
        EnchantCostJsonBuilder.writeUniversal(outRoot, 3, List.of(
                new EnchantCostJsonBuilder.LevelRule(1, rule(
                        2, 2, 1, 1,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:copper_ingot", 8, 24),
                                EnchantCostJsonBuilder.matItem("minecraft:iron_ingot", 6, 20),
                                EnchantCostJsonBuilder.matItem("minecraft:gold_ingot", 4, 16)
                        ),
                        false
                )),
                new EnchantCostJsonBuilder.LevelRule(2, rule(
                        5, 6, 2, 2,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:copper_ingot", 40, 64),
                                EnchantCostJsonBuilder.matItem("minecraft:iron_ingot", 38, 54),
                                EnchantCostJsonBuilder.matItem("minecraft:gold_ingot", 30, 42),
                                EnchantCostJsonBuilder.matItem("minecraft:emerald", 8, 24)
                        ),
                        false
                )),
                new EnchantCostJsonBuilder.LevelRule(3, rule(
                        8, 12, 3, 2,
                        List.of(
                                EnchantCostJsonBuilder.matItem("minecraft:diamond", 12, 24),
                                EnchantCostJsonBuilder.matItem("minecraft:emerald", 48, 64)
                        ),
                        false
                ))
        ));

        // max_level = 4
        EnchantCostJsonBuilder.writeUniversal(outRoot, 4, List.of(
                new EnchantCostJsonBuilder.LevelRule(1, rule(
                        2, 2, 1, 1,
                        List.of(EnchantCostJsonBuilder.matItem("minecraft:copper_ingot", 8, 16)),
                        false
                )),
                new EnchantCostJsonBuilder.LevelRule(2, rule(
                        6, 7, 2, 2,
                        List.of(EnchantCostJsonBuilder.matItem("minecraft:iron_ingot", 8, 16)),
                        false
                )),
                new EnchantCostJsonBuilder.LevelRule(3, rule(
                        10, 14, 3, 3,
                        List.of(EnchantCostJsonBuilder.matItem("minecraft:gold_ingot", 8, 16)),
                        false
                )),
                new EnchantCostJsonBuilder.LevelRule(4, rule(
                        14, 18, 4, 3,
                        List.of(EnchantCostJsonBuilder.matItem("minecraft:emerald", 4, 8)),
                        false
                ))
        ));

        // max_level = 5
        EnchantCostJsonBuilder.writeUniversal(outRoot, 5, List.of(
                new EnchantCostJsonBuilder.LevelRule(1, rule(
                        2, 2, 1, 1,
                        List.of(EnchantCostJsonBuilder.matItem("minecraft:copper_ingot", 8, 16)),
                        false
                )),
                new EnchantCostJsonBuilder.LevelRule(2, rule(
                        6, 7, 2, 2,
                        List.of(EnchantCostJsonBuilder.matItem("minecraft:iron_ingot", 8, 16)),
                        false
                )),
                new EnchantCostJsonBuilder.LevelRule(3, rule(
                        10, 14, 3, 3,
                        List.of(EnchantCostJsonBuilder.matItem("minecraft:gold_ingot", 8, 16)),
                        false
                )),
                new EnchantCostJsonBuilder.LevelRule(4, rule(
                        14, 18, 4, 3,
                        List.of(EnchantCostJsonBuilder.matItem("minecraft:emerald", 4, 8)),
                        false
                )),
                new EnchantCostJsonBuilder.LevelRule(5, rule(
                        18, 30, 5, 4,
                        List.of(EnchantCostJsonBuilder.matItem("minecraft:diamond", 2, 4)),
                        false
                ))
        ));
    }

    private static EnchantCostRule rule(
            int shelfPower,
            int playerLevelReq,
            int xpCost,
            int lapis,
            List<MaterialCost> materials,
            boolean requiresBook
    ) {
        return new EnchantCostRule(
                Optional.of(shelfPower),
                Optional.of(playerLevelReq),
                Optional.of(xpCost),
                Optional.of(lapis),
                (materials == null || materials.isEmpty()) ? Optional.empty() : Optional.of(materials),
                Optional.of(requiresBook),
                Optional.empty()
        );
    }
}
