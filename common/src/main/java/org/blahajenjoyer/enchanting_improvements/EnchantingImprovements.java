package org.blahajenjoyer.enchanting_improvements;

import org.blahajenjoyer.enchanting_improvements.registry.Registries;
import org.blahajenjoyer.enchanting_improvements.net.Network;
import org.blahajenjoyer.enchanting_improvements.data.EnchantCostData;

public final class EnchantingImprovements {
    public static final String MOD_ID = "enchanting_improvements";

    public static void init() {
        Registries.init();
        Network.register();
        EnchantCostData.registerReloaders();
    }
}
