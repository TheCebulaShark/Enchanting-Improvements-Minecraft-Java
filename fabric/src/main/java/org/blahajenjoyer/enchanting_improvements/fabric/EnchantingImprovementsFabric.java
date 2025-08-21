package org.blahajenjoyer.enchanting_improvements.fabric;

import org.blahajenjoyer.enchanting_improvements.EnchantingImprovements;
import net.fabricmc.api.ModInitializer;

public final class EnchantingImprovementsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        EnchantingImprovements.init();
    }
}
