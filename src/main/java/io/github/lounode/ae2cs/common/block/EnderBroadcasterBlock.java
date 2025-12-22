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
 * 末影广播装置
 */
public class EnderBroadcasterBlock extends AEBaseEntityBlock<EnderBroadcasterBlockEntity>
{
    public EnderBroadcasterBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player
            player, BlockHitResult hitResult)
    {
//        super.useWithoutItem(state, level, pos, player, hitResult);
//        if (!level.isClientSide() && !player.isShiftKeyDown())
//        {
//            if (level.getBlockEntity(pos) instanceof EnderBroadcasterBlockEntity be)
//                MenuOpener.open(AECSMenus.CRYSTAL_GROWTH_CHAMBER_MENU.get(), player, MenuLocators.forBlockEntity(be));
//        }
//        return InteractionResult.SUCCESS_NO_ITEM_USED;

        super.useWithoutItem(state, level, pos, player, hitResult);
        if (!level.isClientSide())
        {
            if(!(level.getBlockEntity(pos) instanceof EnderBroadcasterBlockEntity be)) return InteractionResult.PASS;
            BroadcastFrequencyBand band = FrequencyBandManager.tryCreateBand("test", "", true, true);
            if(band == null) return InteractionResult.PASS;
            be.connectToBand(band.getName(), player.isShiftKeyDown());
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
