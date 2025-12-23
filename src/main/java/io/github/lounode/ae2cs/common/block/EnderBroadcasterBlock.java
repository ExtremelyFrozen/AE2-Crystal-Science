package io.github.lounode.ae2cs.common.block;

import appeng.block.AEBaseEntityBlock;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * 末影广播装置（测试）
 */
public class EnderBroadcasterBlock extends AEBaseEntityBlock<EnderBroadcasterBlockEntity>
{

    public EnderBroadcasterBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        super.useWithoutItem(state, level, pos, player, hitResult);

        if (!level.isClientSide())
        {
            if (!(level.getBlockEntity(pos) instanceof EnderBroadcasterBlockEntity be))
            {
                return InteractionResult.PASS;
            }

            BroadcastFrequencyBand band = FrequencyBandManager.tryCreateBand("test", "", true, true);
            if (band == null) return InteractionResult.PASS;

            boolean asSender = player.isShiftKeyDown(); // 潜行=发送端；非潜行=接收端
            be.connectToBand(band.getName(), asSender);
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