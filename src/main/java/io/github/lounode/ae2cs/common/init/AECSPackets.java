package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.network.c2s.ScrollResonatingPatternSelectPacket;
import io.github.lounode.ae2cs.network.c2s.SideConfigMenuOpenPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AECSConstants.MODID)
public class AECSPackets
{
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event)
    {
        //设置当前网络版本
        final PayloadRegistrar registrar = event.registrar("1");


        registrar.playBidirectional(
                ScrollResonatingPatternSelectPacket.TYPE,
                ScrollResonatingPatternSelectPacket.STREAM_CODEC,
                ScrollResonatingPatternSelectPacket::handle,
                ScrollResonatingPatternSelectPacket::handle
        );

        registrar.playBidirectional(
                SideConfigMenuOpenPacket.TYPE,
                SideConfigMenuOpenPacket.STREAM_CODEC,
                SideConfigMenuOpenPacket::handle,
                SideConfigMenuOpenPacket::handle
        );
    }
}
