package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.block.entity.CrystalGrowthChamberBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;

import net.minecraft.client.Minecraft;
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

public class CrystalGrowthChamberBlock extends AEBaseEntityBlock<CrystalGrowthChamberBlockEntity> {

    public CrystalGrowthChamberBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(AECSBlockProperties.ACTIVE, Boolean.FALSE));
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
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!state.is(AECSBlocks.CRYSTAL_GROWTH_ACCELERATOR_BLOCK.get()) || !state.getValue(ACTIVE) || !AEConfig.instance().isEnableEffects() || !AppEngClient.instance().shouldAddParticles(random)) {
            return;
        }

        double x = pos.getX() + 0.5 + (random.nextFloat() - 0.5F) * 0.7;
        double y = pos.getY() + 1.01;
        double z = pos.getZ() + 0.5 + (random.nextFloat() - 0.5F) * 0.7;
        BlockPos above = pos.above();
        if (level.getBlockState(above).isAir()) {
            Minecraft.getInstance().particleEngine.createParticle(
                    ParticleTypes.LIGHTNING, x, y, z, 0.0D, 0.0D, 0.0D);
        }
        if (level.getBlockEntity(pos) instanceof CrystalGrowthChamberBlockEntity be && be.getAcceleratorDisplayBlock() instanceof CrystalMotherRockBlock) {
            double motherRockX = pos.getX() + 0.5 + (random.nextFloat() - 0.5F) * 0.35;
            double motherRockY = pos.getY() + 1.12;
            double motherRockZ = pos.getZ() + 0.5 + (random.nextFloat() - 0.5F) * 0.35;
            Minecraft.getInstance().particleEngine.createParticle(
                    ParticleTypes.LIGHTNING, motherRockX, motherRockY, motherRockZ, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        super.useWithoutItem(state, level, pos, player, hitResult);
        if (!level.isClientSide() && !player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) instanceof CrystalGrowthChamberBlockEntity be) {
                var menu = state.is(AECSBlocks.CRYSTAL_GROWTH_ACCELERATOR_BLOCK.get()) ? AECSMenus.CRYSTAL_GROWTH_ACCELERATOR_MENU.get() : AECSMenus.CRYSTAL_GROWTH_CHAMBER_MENU.get();
                MenuOpener.open(menu, player, MenuLocators.forBlockEntity(be));
            }
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CrystalGrowthChamberBlockEntity be)
            be.updateGrowthNum();
    }
}
