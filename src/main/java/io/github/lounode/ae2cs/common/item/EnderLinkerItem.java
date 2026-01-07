package io.github.lounode.ae2cs.common.item;

import appeng.api.AECapabilities;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EnderLinkerItem extends Item
{
    public EnderLinkerItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack stack, UseOnContext context)
    {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (context.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide())
        {
            if (player.isShiftKeyDown()) return InteractionResult.SUCCESS_NO_ITEM_USED;
            else return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() && level.getBlockEntity(context.getClickedPos()) instanceof EnderEmitterBlockEntity)
        {
            stack.set(AECSDataComponents.ENDER_EMITTER_POS, GlobalPos.of(level.dimension(), context.getClickedPos()));
            player.displayClientMessage(Component.translatable("ae2cs.msg.item.ender_linker.bing_to_emitter"), true);
        }
        else if (!player.isShiftKeyDown() &&
                level.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST, context.getClickedPos()) != null)
        {
            GlobalPos linkerPos = stack.get(AECSDataComponents.ENDER_EMITTER_POS);
            if (linkerPos == null || !linkerPos.dimension().equals(level.dimension()))
                return InteractionResult.SUCCESS_NO_ITEM_USED;

            if (level.getBlockEntity(linkerPos.pos()) instanceof EnderEmitterBlockEntity emitter)
            {
                if (emitter.getLinkedRenderPositionsSnapshot().contains(context.getClickedPos())
                        || emitter.getPendingRenderPositionsSnapshot().contains(context.getClickedPos()))
                {
                    EnderEmitterBlockEntity.removePosFromEmitter(emitter, context.getClickedPos());
                }
                else if (!emitter.getBlockPos().equals(context.getClickedPos()))
                {
                    // 如果能成功添加的话，我们额外做一次移除，以实现类似换绑的功能
                    if (EnderEmitterBlockEntity.addPosToEmitter(emitter, context.getClickedPos(), true, false))
                    {
                        EnderEmitterBlockEntity.removePosFromRecentEmitter(level, context.getClickedPos());
                        EnderEmitterBlockEntity.addPosToEmitter(emitter, context.getClickedPos(), true, false);
                        player.displayClientMessage(Component.translatable("ae2cs.msg.item.ender_linker.success_to_link"), true);
                    }
                    else
                    {
                        player.displayClientMessage(Component.translatable("ae2cs.msg.item.ender_linker.failed_to_link"), true);
                    }
                }
            }
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        return InteractionResult.PASS;
    }
}
