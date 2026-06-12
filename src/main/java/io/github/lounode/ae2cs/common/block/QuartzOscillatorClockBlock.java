package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.block.entity.QuartzOscillatorClockBlockEntity;
import io.github.lounode.ae2cs.common.me.logic.QuartzOscillatorClockHost;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.locator.MenuLocators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import static io.github.lounode.ae2cs.common.init.AECSBlockProperties.ACTIVE;

public class QuartzOscillatorClockBlock extends AEBaseEntityBlock<QuartzOscillatorClockBlockEntity> {

    public QuartzOscillatorClockBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        super.useWithoutItem(state, level, pos, player, hitResult);
        if (!level.isClientSide() && !player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) instanceof QuartzOscillatorClockBlockEntity be)
                be.openMenu(player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    @Override
    public boolean isSignalSource(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction side) {
        if (!state.getValue(ACTIVE)) {
            return 0;
        }

        var be = level.getBlockEntity(pos);
        if (be instanceof QuartzOscillatorClockHost host) {
            return host.getTargets().contains(side) ? 15 : 0;
        }

        return 0;
    }

    @Override
    public int getDirectSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction side) {
        return getSignal(state, level, pos, side);
    }

    @Override
    public boolean canConnectRedstone(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, Direction side) {
        if (side == null) {
            return true;
        }

        var be = level.getBlockEntity(pos);
        if (be instanceof QuartzOscillatorClockHost host) {
            return host.getTargets().contains(side);
        }
        return true;
    }
}
