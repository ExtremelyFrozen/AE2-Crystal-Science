package io.github.lounode.ae2cs.common.block;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * 末影广播装置（测试）
 */
public class EnderBroadcasterBlock extends AEBaseEntityBlock<EnderBroadcasterBlockEntity>
{

    public EnderBroadcasterBlock(BlockBehaviour.Properties properties)
    {
        super(properties.noOcclusion());
        this.registerDefaultState(this.defaultBlockState()
                .setValue(AECSBlockProperties.ACTIVE, false)
                .setValue(AECSBlockProperties.BROADCASTER_SENDER, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(AECSBlockProperties.ACTIVE);
        builder.add(AECSBlockProperties.BROADCASTER_SENDER);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        super.useWithoutItem(state, level, pos, player, hitResult);
        if (!level.isClientSide() && !player.isShiftKeyDown())
        {
            if (level.getBlockEntity(pos) instanceof EnderBroadcasterBlockEntity be)
                MenuOpener.open(AECSMenus.ENDER_BROADCASTER_MENU.get(), player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    /**
     * 方块真正被替换/移除时，永久清理
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (!level.isClientSide() && state.getBlock() != newState.getBlock())
        {
            if (level.getBlockEntity(pos) instanceof EnderBroadcasterBlockEntity be)
            {
                be.cleanConnectionPermanent();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}