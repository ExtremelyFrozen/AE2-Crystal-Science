package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.block.entity.ResonatingGrowthFactoryBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
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

/**
 * 谐振晶体催生工厂的方块、外观与槽位界面，催生加工后续接入。
 */
public class ResonatingGrowthFactoryBlock extends AEBaseEntityBlock<ResonatingGrowthFactoryBlockEntity> {

    public ResonatingGrowthFactoryBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(AECSBlockProperties.ACTIVE, false)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AECSBlockProperties.ACTIVE);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        super.useWithoutItem(state, level, pos, player, hitResult);
        if (!level.isClientSide() && !player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof ResonatingGrowthFactoryBlockEntity host) {
            MenuOpener.open(AECSMenus.RESONATING_GROWTH_FACTORY_MENU.get(), player, MenuLocators.forBlockEntity(host));
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
