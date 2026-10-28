package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.block.entity.CircuitEtcherBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.CircuitEtcherMenu;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static io.github.lounode.ae2cs.common.init.AECSBlockProperties.ACTIVE;

public class CircuitEtcherBlock extends AEBaseEntityBlock<CircuitEtcherBlockEntity> {

    private final Supplier<MenuType<CircuitEtcherMenu>> menuType;

    public CircuitEtcherBlock(Properties props) {
        this(props, AECSMenus.CIRCUIT_ETCHER_MENU);
    }

    public CircuitEtcherBlock(Properties props, Supplier<MenuType<CircuitEtcherMenu>> menuType) {
        super(props);
        this.menuType = menuType;
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
            if (level.getBlockEntity(pos) instanceof CircuitEtcherBlockEntity be)
                MenuOpener.open(menuType.get(), player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
