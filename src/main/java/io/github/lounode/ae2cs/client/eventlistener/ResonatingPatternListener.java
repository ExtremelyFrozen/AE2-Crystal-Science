package io.github.lounode.ae2cs.client.eventlistener;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.item.ResonatingPatternItem;
import io.github.lounode.ae2cs.network.c2s.ScrollResonatingPatternSelectPacket;

import appeng.util.InteractionUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class ResonatingPatternListener {

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 只在 Shift 模式下接管滚轮
        if (!InteractionUtil.isInAlternateUseMode(player)) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ResonatingPatternItem)) return;

        var encoded = stack.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get());
        if (encoded == null) return;

        double dy = event.getScrollDeltaY();
        if (dy == 0) return;

        // 阻止滚轮切换快捷栏
        event.setCanceled(true);

        boolean next = dy < 0;
        PacketDistributor.sendToServer(new ScrollResonatingPatternSelectPacket(next));
    }
}
