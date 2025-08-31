package org.blahajenjoyer.enchanting_improvements.client;

import net.fabricmc.api.ClientModInitializer;
import dev.architectury.registry.menu.MenuRegistry;
import org.blahajenjoyer.enchanting_improvements.client.screen.EnchantingScreen;
import org.blahajenjoyer.enchanting_improvements.registry.Registries;

public final class EnchantingImprovementsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuRegistry.registerScreenFactory(Registries.ENCHANT_MENU.get(), EnchantingScreen::new);
    }
}