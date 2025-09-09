package org.blahajenjoyer.enchanting_improvements;

import org.blahajenjoyer.enchanting_improvements.registry.Registries;
import org.blahajenjoyer.enchanting_improvements.network.EnchantingPackets;
import org.blahajenjoyer.enchanting_improvements.data.EnchantCostData;

public final class EnchantingImprovements {
    public static final String MOD_ID = "enchanting_improvements";

    public static void init() {
        Registries.init();
        EnchantingPackets.register();
        EnchantCostData.registerReloaders();
    }
}
