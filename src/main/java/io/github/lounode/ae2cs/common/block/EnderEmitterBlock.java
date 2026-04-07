package io.github.lounode.ae2cs.common.block;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnderEmitterBlock extends AEBaseEntityBlock<EnderEmitterBlockEntity>
{
    public EnderEmitterBlock(Properties props)
    {
        super(props.noOcclusion());
        this.registerDefaultState(this.defaultBlockState()
                .setValue(AECSBlockProperties.ACTIVE, false)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(AECSBlockProperties.ACTIVE);
        builder.add(BlockStateProperties.DOUBLE_BLOCK_HALF);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy()
    {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
        {
            pos = pos.below();
            state = level.getBlockState(pos);
        }

        InteractionResult result = super.use(state, level, pos, player, hand, hitResult);
        if (result.consumesAction())
        {
            return result;
        }

        if (!level.isClientSide() && !player.isShiftKeyDown())
        {
            if (level.getBlockEntity(pos) instanceof EnderEmitterBlockEntity be)
                MenuOpener.open(AECSMenus.ENDER_EMITTER_MENU.get(), player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1) return null;

        BlockPos above = pos.above();
        if (!level.getBlockState(above).canBeReplaced(ctx)) return null;

        return super.getStateForPlacement(ctx);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
        {
            BlockPos above = pos.above();
            BlockState upper = state
                    .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            level.setBlock(above, upper, Block.UPDATE_ALL);
        }
    }

    @Override
    public void playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player)
    {
        if (!level.isClientSide
                && (player.isCreative() || !player.hasCorrectToolForDrops(state)))
        {
            // 放置另一半方块掉落
            preventDropFromBottomPart(level, pos, state, player);
        }
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!level.isClientSide && state.is(this) && !newState.is(this))
        {
            BlockPos basePos = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            if (level.getBlockEntity(basePos) instanceof EnderEmitterBlockEntity be)
            {
                be.cleanConnectionPermanent();
            }
            removeOtherHalfNoDrops(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }


    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction facing, @NotNull BlockState facingState,
                                           @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos)
    {
        DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);

        if (facing.getAxis() == Direction.Axis.Y)
        {
            boolean isOtherHalfDir = (half == DoubleBlockHalf.LOWER) == (facing == Direction.UP);
            if (isOtherHalfDir)
            {
                return facingState.is(this)
                        && facingState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != half
                        ? facingState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, half)
                        : Blocks.AIR.defaultBlockState();
            }
        }

        if (half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(level, currentPos))
        {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }


    @Override
    public boolean canSurvive(BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos)
    {
        DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        if (half == DoubleBlockHalf.UPPER)
        {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
        {
            return null;
        }
        return super.newBlockEntity(pos, state);
    }

    private static void removeOtherHalfNoDrops(Level level, BlockPos pos, BlockState state)
    {
        var half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        BlockPos otherPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPos);

        if (otherState.is(state.getBlock())
                && otherState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != half)
        {

            level.setBlock(otherPos, Blocks.AIR.defaultBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
        }
    }

    protected static void preventDropFromBottomPart(Level level, BlockPos pos, BlockState state, Player player)
    {
        DoubleBlockHalf doubleblockhalf = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        if (doubleblockhalf == DoubleBlockHalf.UPPER)
        {
            BlockPos blockpos = pos.below();
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(state.getBlock()) && blockstate.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
            {
                BlockState blockstate1 = blockstate.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                level.setBlock(blockpos, blockstate1, 35);
                level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
            }
        }

    }
}
