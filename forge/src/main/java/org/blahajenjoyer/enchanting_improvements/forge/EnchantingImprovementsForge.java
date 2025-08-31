package org.blahajenjoyer.enchanting_improvements.forge;

import org.blahajenjoyer.enchanting_improvements.EnchantingImprovements;
import org.blahajenjoyer.enchanting_improvements.client.screen.EnchantingScreen;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.blahajenjoyer.enchanting_improvements.registry.Registries;

@Mod(EnchantingImprovements.MOD_ID)
public final class EnchantingImprovementsForge {
    public EnchantingImprovementsForge(FMLJavaModLoadingContext context) {
        EventBuses.registerModEventBus(EnchantingImprovements.MOD_ID, context.getModEventBus());
        EnchantingImprovements.init();
        context.getModEventBus().addListener(this::clientSetup);
    }

    private void clientSetup(final net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e) {
        e.enqueueWork(() ->
                dev.architectury.registry.menu.MenuRegistry.registerScreenFactory(
                        Registries.ENCHANT_MENU.get(),
                        EnchantingScreen::new
                )
        );
    }
}
