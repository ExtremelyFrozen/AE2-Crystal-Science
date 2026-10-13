package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.block.entity.ResonatingPulverizerFactoryBlockEntity;
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
 * 谐振粉碎工厂。
 *
 * <p>
 * 晶能粉碎机（{@code crystal_pulverizer}）的升级型：输入输出各三乘三格，并支持最高 32 份的并行处理。
 * 它是一台独立机器，与晶能粉碎机各自注册、各自有方块实体与菜单。
 * </p>
 */
public class ResonatingPulverizerFactoryBlock extends AEBaseEntityBlock<ResonatingPulverizerFactoryBlockEntity> {

    public ResonatingPulverizerFactoryBlock(Properties props) {
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
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        super.useWithoutItem(state, level, pos, player, hitResult);
        if (!level.isClientSide() && !player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) instanceof ResonatingPulverizerFactoryBlockEntity be)
                MenuOpener.open(AECSMenus.RESONATING_PULVERIZER_FACTORY_MENU.get(), player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
