package org.blahajenjoyer.enchanting_improvements.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.entity.player.Inventory;
import org.blahajenjoyer.enchanting_improvements.menu.EnchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentTableBlock.class)
public class EnchantmentTableBlockMixin {
    @Inject(method = "getMenuProvider", at = @At("HEAD"), cancellable = true)
    private void ei$replaceMenu(BlockState state, Level level, BlockPos pos,
                                CallbackInfoReturnable<MenuProvider> cir) {
        MenuProvider provider = new SimpleMenuProvider(
                (int syncId, Inventory inv, net.minecraft.world.entity.player.Player player) ->
                        new EnchantMenu(syncId, inv, ContainerLevelAccess.create(level, pos)),
                Component.translatable("container.enchant")
        );
        cir.setReturnValue(provider);
    }
}