package io.github.lounode.ae2cs.common.block;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.core.AEConfig;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import io.github.lounode.ae2cs.common.block.entity.CrystalVibrationChamberBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
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

public class CrystalVibrationChamberBlock extends AEBaseEntityBlock<CrystalVibrationChamberBlockEntity>
{

    public CrystalVibrationChamberBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(ACTIVE, false)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        // super中已经通过horizontalFacing的Orientation策略把HORIZONTAL_FACING面属性加上了
        // 包括放置时候的面朝向，也已经在AEBaseBlock中做过处理了
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
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
        super.use(state, level, pos, player, hand, hitResult);
        if (!level.isClientSide() && !player.isShiftKeyDown())
        {
            if (level.getBlockEntity(pos) instanceof CrystalVibrationChamberBlockEntity be)
                MenuOpener.open(AECSMenus.CRYSTAL_VIBRATION_CHAMBER_MENU.get(), player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * 在燃烧时添加一些烟雾和粒子特效
     */
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource r)
    {
        if (!AEConfig.instance().isEnableEffects())
        {
            return;
        }

        var tc = this.getBlockEntity(level, pos);
        boolean isActive = state.getValue(ACTIVE);
        if (tc != null && isActive)
        {
            double f1 = pos.getX() + 0.5F;
            double f2 = pos.getY() + 0.5F;
            double f3 = pos.getZ() + 0.5F;

            var front = tc.getFront();
            var top = tc.getTop();

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
