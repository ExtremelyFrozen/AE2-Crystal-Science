package io.github.lounode.ae2cs.client.eventlistener;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSPackets;
import io.github.lounode.ae2cs.common.item.MirrorLinkerItem;
import io.github.lounode.ae2cs.network.c2s.MirrorLinkerBatchApplyPacket;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AECSConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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

        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        AECSPackets.INSTANCE.sendToServer(new MirrorLinkerBatchApplyPacket(event.getPos(),
                event.getHitVec().getLocation().x,
                event.getHitVec().getLocation().y,
                event.getHitVec().getLocation().z));
    }
}
