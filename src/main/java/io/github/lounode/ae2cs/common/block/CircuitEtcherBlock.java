package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.block.entity.CircuitEtcherBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import static io.github.lounode.ae2cs.common.init.AECSBlockProperties.ACTIVE;

public class CircuitEtcherBlock extends AEBaseEntityBlock<CircuitEtcherBlockEntity> {

    public CircuitEtcherBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(ACTIVE, false)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        super.use(state, level, pos, player, hand, hitResult);
        if (!level.isClientSide() && !player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) instanceof CircuitEtcherBlockEntity be)
                MenuOpener.open(AECSMenus.CIRCUIT_ETCHER_MENU.get(), player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.SUCCESS;
    }
}
