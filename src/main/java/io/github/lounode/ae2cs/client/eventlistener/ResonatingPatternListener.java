package io.github.lounode.ae2cs.client.eventlistener;

import appeng.util.InteractionUtil;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.init.AECSPackets;
import io.github.lounode.ae2cs.common.item.ResonatingPatternItem;
import io.github.lounode.ae2cs.network.c2s.ScrollResonatingPatternSelectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AECSConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ResonatingPatternListener
{
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 只在 Shift 模式下接管滚轮
        if (!InteractionUtil.isInAlternateUseMode(player)) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ResonatingPatternItem)) return;

        var encoded = AECSDataComponents.getEncodedResonatingPattern(stack);
        if (encoded == null) return;

        double dy = event.getScrollDelta();
        if (dy == 0) return;

        // 阻止滚轮切换快捷栏
        event.setCanceled(true);

        boolean next = dy < 0;
        AECSPackets.INSTANCE.sendToServer(new ScrollResonatingPatternSelectPacket(next));
    }
}
