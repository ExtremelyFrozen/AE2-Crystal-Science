package io.github.lounode.ae2cs.common.item;

import appeng.api.AECapabilities;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
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
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context)
    {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide())
        {
            if (player.isShiftKeyDown()) return InteractionResult.SUCCESS_NO_ITEM_USED;
            else return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        if (player.isShiftKeyDown() && level.getBlockEntity(context.getClickedPos()) instanceof EnderEmitterBlockEntity)
        {
            stack.set(AECSDataComponents.ENDER_EMITTER_POS, GlobalPos.of(level.dimension(), context.getClickedPos()));
            player.displayClientMessage(Component.translatable("ae2cs.msg.item.ender_linker.bing_to_emitter"), true);
        }
        else if (player.isShiftKeyDown() &&
                level.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST, context.getClickedPos()) != null)
        {
            GlobalPos linkerPos = stack.get(AECSDataComponents.ENDER_EMITTER_POS);
            if (linkerPos == null || !linkerPos.dimension().equals(level.dimension()))
                return InteractionResult.SUCCESS_NO_ITEM_USED;

            if (level.getBlockEntity(linkerPos.pos()) instanceof EnderEmitterBlockEntity emitter)
            {
                if (EnderEmitterBlockEntity.addPosToEmitter(emitter, context.getClickedPos(), true, false))
                {
                    player.displayClientMessage(Component.translatable("ae2cs.msg.item.ender_linker.success_to_link"), true);
                }
                else
                {
                    player.displayClientMessage(Component.translatable("ae2cs.msg.item.ender_linker.failed_to_link"), true);
                }
            }
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        return super.useOn(context);
    }
}
