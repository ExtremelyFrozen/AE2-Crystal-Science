package io.github.lounode.ae2cs.common.block;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.core.AEConfig;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.VibrationChamberMenu;
import appeng.menu.locator.MenuLocators;
import io.github.lounode.ae2cs.common.block.entity.CrystalVibrationChamberBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class CrystalVibrationChamberBlock extends AEBaseEntityBlock<CrystalVibrationChamberBlockEntity>
{

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public CrystalVibrationChamberBlock()
    {
        super(metalProps().strength(4.2F));
        this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, CrystalVibrationChamberBlockEntity be)
    {
        return currentState.setValue(ACTIVE, be.isOn);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy()
    {
        return OrientationStrategies.full();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (level.getBlockEntity(pos) instanceof VibrationChamberBlockEntity be)
        {
            if (!level.isClientSide)
            {
                MenuOpener.open(VibrationChamberMenu.TYPE, player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r)
    {
        if (!AEConfig.instance().isEnableEffects())
        {
            return;
        }

        var tc = this.getBlockEntity(level, pos);
        if (tc != null && tc.isOn)
        {
            double f1 = pos.getX() + 0.5F;
            double f2 = pos.getY() + 0.5F;
            double f3 = pos.getZ() + 0.5F;

            var front = tc.getFront();
            var top = tc.getTop();

            // Cross-Product of forward/up directional vector
            final int west_x = front.getStepY() * top.getStepZ() - front.getStepZ() * top.getStepY();
            final int west_y = front.getStepZ() * top.getStepX() - front.getStepX() * top.getStepZ();
            final int west_z = front.getStepX() * top.getStepY() - front.getStepY() * top.getStepX();

            f1 += front.getStepX() * 0.6;
            f2 += front.getStepY() * 0.6;
            f3 += front.getStepZ() * 0.6;

            final double ox = r.nextDouble();
            final double oy = r.nextDouble() * 0.2f;

            f1 += top.getStepX() * (-0.3 + oy);
            f2 += top.getStepY() * (-0.3 + oy);
            f3 += top.getStepZ() * (-0.3 + oy);

            f1 += west_x * (0.3 * ox - 0.15);
            f2 += west_y * (0.3 * ox - 0.15);
            f3 += west_z * (0.3 * ox - 0.15);

            level.addParticle(ParticleTypes.SMOKE, f1, f2, f3, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, f1, f2, f3, 0.0D, 0.0D, 0.0D);
        }
    }
}
