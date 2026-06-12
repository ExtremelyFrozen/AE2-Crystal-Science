package io.github.lounode.ae2cs.common.block;

import appeng.block.AEBaseBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CertusQuartzOreBlock extends AEBaseBlock {

    public CertusQuartzOreBlock(Properties props) {
        super(props);
    }

    @Override
    public int getExpDrop(@NotNull BlockState state, @NotNull LevelAccessor level, @NotNull BlockPos pos,
                          @Nullable BlockEntity blockEntity, @Nullable Entity breaker, @NotNull ItemStack tool) {
        if (!(level instanceof ServerLevelAccessor serverLevel)) return 0;

        var lookup = serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        if (tool.getEnchantmentLevel(lookup.getOrThrow(Enchantments.SILK_TOUCH)) > 0) {
            return 0;
        }
        RandomSource rand = serverLevel.getRandom();
        return Mth.nextInt(rand, 2, 5);
    }
}
