package io.github.lounode.ae2cs.client.eventlistener;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.item.ResonatingLinkerItem;
import io.github.lounode.ae2cs.network.c2s.ResonatingLinkerBatchApplyPacket;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class ResonatingLinkerListener {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!Screen.hasControlDown() || event.getEntity().isShiftKeyDown()) {
            return;
        }

        if (!(event.getItemStack().getItem() instanceof ResonatingLinkerItem)) {
            return;
        }

        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        PacketDistributor.sendToServer(new ResonatingLinkerBatchApplyPacket(event.getPos(),
                event.getHand() == InteractionHand.MAIN_HAND,
                event.getHitVec().getLocation().x,
                event.getHitVec().getLocation().y,
                event.getHitVec().getLocation().z));
    }
}
