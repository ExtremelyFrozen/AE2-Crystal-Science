package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.AE2CrystalScience;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.fml.ModList;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Grows a configured sequence of crystal buds on adjacent air blocks or water sources.
 */
public class CrystalMotherRockBlock extends Block {

    public static final int GROWTH_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();

    private final CrystalGrowthSequence growthSequence;
    private final boolean externalStages;
    private final AtomicBoolean missingStageLogged = new AtomicBoolean();

    public CrystalMotherRockBlock(Properties properties, CrystalGrowthSequence growthSequence, boolean externalStages) {
        super(properties);
        this.growthSequence = growthSequence;
        this.externalStages = externalStages;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos,
                           @NotNull RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) != 0) {
            return;
        }

        Direction direction = Util.getRandom(DIRECTIONS, random);
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        Optional<ResourceLocation> nextStageId = selectNextStageId(targetState, direction);
        if (nextStageId.isEmpty()) {
            return;
        }

        Optional<Block> nextStage = resolveStage(nextStageId.get());
        if (nextStage.isEmpty()) {
            return;
        }

        BlockState nextState = nextStage.get().defaultBlockState();
        if (!nextState.hasProperty(AmethystClusterBlock.FACING) || !nextState.hasProperty(AmethystClusterBlock.WATERLOGGED)) {
            logInvalidStage(nextStageId.get(), "does not expose FACING and WATERLOGGED");
            return;
        }

        nextState = nextState
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, targetState.getFluidState().getType() == Fluids.WATER);
        level.setBlockAndUpdate(targetPos, nextState);
    }

    Optional<ResourceLocation> selectNextStageId(BlockState targetState, Direction direction) {
        if (canClusterGrowAtState(targetState)) {
            return Optional.of(growthSequence.firstStageId());
        }

        ResourceLocation currentStageId = BuiltInRegistries.BLOCK.getKey(targetState.getBlock());
        Optional<ResourceLocation> nextStageId = growthSequence.nextStageId(currentStageId);
        if (nextStageId.isEmpty() || !targetState.hasProperty(AmethystClusterBlock.FACING) || targetState.getValue(AmethystClusterBlock.FACING) != direction) {
            return Optional.empty();
        }
        return nextStageId;
    }

    public CrystalGrowthSequence growthSequence() {
        return growthSequence;
    }

    public static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return PushReaction.DESTROY;
    }

    private Optional<Block> resolveStage(ResourceLocation stageId) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(stageId);
        if (block.isPresent()) {
            return block;
        }
        if (!externalStages || ModList.get().isLoaded(stageId.getNamespace())) {
            logInvalidStage(stageId, "is not registered");
        }
        return Optional.empty();
    }

    private void logInvalidStage(ResourceLocation stageId, String reason) {
        if (missingStageLogged.compareAndSet(false, true)) {
            AE2CrystalScience.LOGGER.error("Crystal growth stage {} {}. The mother rock will remain inert.", stageId,
                    reason);
        }
    }
}
