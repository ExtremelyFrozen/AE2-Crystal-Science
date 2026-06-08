package io.github.lounode.ae2cs.client.eventlistener;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.item.MirrorLinkerItem;
import io.github.lounode.ae2cs.network.c2s.MirrorLinkerBatchApplyPacket;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class MirrorLinkerListener
{
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (!Screen.hasControlDown())
        {
            return;
        }

        if (!(event.getItemStack().getItem() instanceof MirrorLinkerItem))
        {
            return;
        }

        if (event.getEntity().isShiftKeyDown())
        {
            return;
        }

        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        PacketDistributor.sendToServer(new MirrorLinkerBatchApplyPacket(event.getPos(),
                event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND,
                event.getHitVec().getLocation().x,
                event.getHitVec().getLocation().y,
                event.getHitVec().getLocation().z));
    }
}
