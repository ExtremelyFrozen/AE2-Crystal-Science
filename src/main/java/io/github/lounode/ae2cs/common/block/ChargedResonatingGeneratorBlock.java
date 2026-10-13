package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.block.entity.ChargedResonatingGeneratorBlockEntity;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.api.orientation.RelativeSide;
import appeng.block.AEBaseEntityBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 充能共振发电机。
 *
 * <p>
 * 外观借用 AE2 的水晶谐振发电机：模型与换色贴图都在本模组资源里，见
 * {@code models/block/charged_resonating_generator/}。
 * </p>
 *
 * <p>
 * 这不是满格方块：模型主体只有 12/16 宽，朝向前方的一侧另有凸出，因此必须声明
 * {@code noOcclusion}（否则相邻方块朝它的面会被错误剔除）并给出收窄的碰撞箱。朝向、碰撞箱与
 * 含水行为都对齐 AE2 原件，六面可放。
 * </p>
 */
public class ChargedResonatingGeneratorBlock extends AEBaseEntityBlock<ChargedResonatingGeneratorBlockEntity>
                                             implements SimpleWaterloggedBlock {

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ChargedResonatingGeneratorBlock(Properties properties) {
        super(properties);
        // AE2 原件不设默认朝向（取枚举首个值 DOWN）；这里显式给 NORTH，方便无放置上下文时阅读默认态
        this.registerDefaultState(this.defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facing();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getVoxelShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getVoxelShape(state);
    }

    /**
     * 碰撞箱随朝向变化：机体收窄到 2/16~14/16，朝向那一侧则顶到方块边缘（凸出的出风口）。
     */
    @NotNull
    private VoxelShape getVoxelShape(BlockState state) {
        var forward = getOrientation(state).getSide(RelativeSide.FRONT);

        double minX = 0;
        double minY = 0;
        double minZ = 0;
        double maxX = 1;
        double maxY = 1;
        double maxZ = 1;

        switch (forward) {
            case DOWN -> {
                minX = minZ = 2.0 / 16.0;
                maxX = maxZ = 14.0 / 16.0;
                minY = 1.0 / 16.0;
            }
            case UP -> {
                minX = minZ = 2.0 / 16.0;
                maxX = maxZ = 14.0 / 16.0;
                maxY = 15.0 / 16.0;
            }
            case NORTH -> {
                minX = minY = 2.0 / 16.0;
                maxX = maxY = 14.0 / 16.0;
                minZ = 1.0 / 16.0;
            }
            case SOUTH -> {
                minX = minY = 2.0 / 16.0;
                maxX = maxY = 14.0 / 16.0;
                maxZ = 15.0 / 16.0;
            }
            case WEST -> {
                minY = minZ = 2.0 / 16.0;
                maxY = maxZ = 14.0 / 16.0;
                minX = 1.0 / 16.0;
            }
            case EAST -> {
                minY = minZ = 2.0 / 16.0;
                maxY = maxZ = 14.0 / 16.0;
                maxX = 15.0 / 16.0;
            }
        }

        return Shapes.create(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
                                  BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }
}
