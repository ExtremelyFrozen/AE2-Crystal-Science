package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.block.entity.ResonatingAggregatorFactoryBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

/**
 * 谐振晶能聚合工厂的方块、外观与界面，聚合加工逻辑后续接入。
 * 通电外观由 active 状态控制，默认放置为未通电状态。
 */
public class ResonatingAggregatorFactoryBlock extends AEBaseEntityBlock<ResonatingAggregatorFactoryBlockEntity> {

    public ResonatingAggregatorFactoryBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
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
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        super.useWithoutItem(state, level, pos, player, hitResult);
        if (!level.isClientSide() && !player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof ResonatingAggregatorFactoryBlockEntity host) {
            MenuOpener.open(AECSMenus.RESONATING_AGGREGATOR_FACTORY_MENU.get(), player, MenuLocators.forBlockEntity(host));
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
