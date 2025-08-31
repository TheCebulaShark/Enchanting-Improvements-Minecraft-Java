package org.blahajenjoyer.enchanting_improvements.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.blahajenjoyer.enchanting_improvements.EnchantingImprovements;
import org.blahajenjoyer.enchanting_improvements.menu.EnchantMenu;

public final class Registries {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(EnchantingImprovements.MOD_ID, net.minecraft.core.registries.Registries.MENU);

    public static final RegistrySupplier<MenuType<EnchantMenu>> ENCHANT_MENU =
            MENUS.register("enchanting_table",
                    () -> new MenuType<>(EnchantMenu::new, FeatureFlags.VANILLA_SET));

    public static void init() {
        MENUS.register();
    }
}
