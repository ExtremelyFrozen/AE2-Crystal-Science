package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.block.entity.ResonatingPatternProviderBlockEntity;

import appeng.block.crafting.PatternProviderBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResonatingPatternProviderBlock extends PatternProviderBlock {

    public ResonatingPatternProviderBlock() {
        super();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ResonatingPatternProviderBlockEntity be) {
            be.getResonatingLogic().readDefaultsFromItem(stack);
            be.setChanged();
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof ResonatingPatternProviderBlockEntity resonating) {
            resonating.getResonatingLogic().writeDefaultsToDrops(drops, state.getBlock().asItem());
        }
        return drops;
    }
}
