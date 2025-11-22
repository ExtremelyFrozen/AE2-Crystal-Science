package io.github.lounode.ae2cs.common.block;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import io.github.lounode.ae2cs.common.block.entity.CrystalGrowthChamberBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrystalGrowthChamberBlock extends AEBaseEntityBlock<CrystalGrowthChamberBlockEntity>
{

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;


    public CrystalGrowthChamberBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.FALSE));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, false);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        super.useWithoutItem(state, level, pos, player, hitResult);
        if (!level.isClientSide() && !player.isShiftKeyDown())
        {
            if (level.getBlockEntity(pos) instanceof CrystalGrowthChamberBlockEntity be)
                MenuOpener.open(AECSMenus.CRYSTAL_GROWTH_CHAMBER_MENU.get(), player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    private void checkLitState(Level level, BlockPos pos, BlockState state)
    {

    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation)
    {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, LIT);
    }
}
