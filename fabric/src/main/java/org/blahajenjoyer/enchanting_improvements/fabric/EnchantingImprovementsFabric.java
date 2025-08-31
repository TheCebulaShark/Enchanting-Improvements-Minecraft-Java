package org.blahajenjoyer.enchanting_improvements.fabric;

import org.blahajenjoyer.enchanting_improvements.EnchantingImprovements;
import net.fabricmc.api.ModInitializer;

public final class EnchantingImprovementsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EnchantingImprovements.init();
    }
}
