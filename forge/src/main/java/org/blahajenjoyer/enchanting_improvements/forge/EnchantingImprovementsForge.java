package org.blahajenjoyer.enchanting_improvements.forge;

import org.blahajenjoyer.enchanting_improvements.EnchantingImprovements;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EnchantingImprovements.MOD_ID)
public final class EnchantingImprovementsForge {
    public EnchantingImprovementsForge(FMLJavaModLoadingContext context) {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(
                EnchantingImprovements.MOD_ID,
                context.getModEventBus()
        );

        // Run our common setup.
        EnchantingImprovements.init();
    }
}
