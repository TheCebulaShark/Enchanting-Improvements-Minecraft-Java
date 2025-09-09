package org.blahajenjoyer.enchanting_improvements.enchanting;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.blahajenjoyer.enchanting_improvements.data.*;

import java.util.List;
import java.util.Optional;

public final class EnchantCosts {
    private EnchantCosts() {}

    public record Rolled(int shelfPower, int lapis, int xp, MaterialCost material, int materialCount) {}

    public static Optional<Rolled> roll(Player player, ItemStack stack, Enchantment ench, int level, int availableShelfPower, BlockPos tablePos) {
        Optional<EnchantCostData.Resolved> resolved = EnchantCostData.resolve(ench, level);

        if (resolved.isEmpty()) return Optional.empty();
        var r = resolved.get();

        if (availableShelfPower < r.shelfPower()) return Optional.empty();

        RandomSource rng = seededRng(player, stack, ench, level, tablePos);

        MaterialCost chosenMat = null;
        int chosenCount = 0;
        List<MaterialCost> mats = r.materials();

        if (!mats.isEmpty()) {
            chosenMat = pickWeighted(mats, rng);
            int min = chosenMat.count().min();
            int max = chosenMat.count().max();
            chosenCount = biasedBetween(rng, min, max, stack.getItem().getEnchantmentValue());
        }

        return Optional.of(new Rolled(r.shelfPower(), r.lapis(), r.xpCost(), chosenMat, chosenCount));
    }

    private static MaterialCost pickWeighted(java.util.List<MaterialCost> mats, RandomSource rng) {
        int total = 0;
        for (var m : mats) total += Math.max(1, m.weight());
        int r = rng.nextInt(Math.max(1, total));
        for (var m : mats) {
            r -= Math.max(1, m.weight());
            if (r < 0) return m;
        }
        return mats.get(0);
    }

    private static net.minecraft.util.RandomSource seededRng(
            net.minecraft.world.entity.player.Player p,
            net.minecraft.world.item.ItemStack stack,
            net.minecraft.world.item.enchantment.Enchantment ench,
            int level,
            net.minecraft.core.BlockPos pos
    ) {
        long seed = 0L;
        if (p.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            seed = sl.getSeed();
        }

        // per-player + reroll after enchanting
        var u = p.getUUID();
        seed ^= u.getMostSignificantBits() ^ u.getLeastSignificantBits();
        seed = Long.rotateLeft(seed, 21) ^ p.getEnchantmentSeed();

        var enchId = net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getKey(ench);
        seed = Long.rotateLeft(seed, 21) ^ enchId.hashCode();
        seed = Long.rotateLeft(seed, 21) ^ level;
        seed = Long.rotateLeft(seed, 21) ^ pos.asLong();
        seed = Long.rotateLeft(seed, 21) ^ net.minecraft.world.item.Item.getId(stack.getItem());

        return net.minecraft.util.RandomSource.create(seed);
    }

    private static int biasedBetween(RandomSource rng, int min, int max, int enchantability) {
        if (min >= max) return min;
        double u = rng.nextDouble();
        double k = exponentFor(enchantability);
        double t = Math.pow(u, k);
        int span = max - min;
        return min + (int)Math.round(t * span);
    }

    public static double exponentFor(int enchantability) {
        int e = Mth.clamp(enchantability, 1, 40);
        if (e <= 10) return 0.9;
        if (e >= 25) return 1.8;
        double t = (e - 10) / 15.0;
        return 0.9 + t * (1.8 - 1.0);
    }
}
