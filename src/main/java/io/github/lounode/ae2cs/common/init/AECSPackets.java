package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.network.c2s.MirrorLinkerBatchApplyPacket;
import io.github.lounode.ae2cs.network.c2s.ScrollResonatingPatternSelectPacket;
import io.github.lounode.ae2cs.network.c2s.SideConfigMenuOpenPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class AECSPackets
{

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryBuild(AECSConstants.MODID, "simple_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 1;

    public static void register()
    {
        INSTANCE.registerMessage(
                packetId++,
                ScrollResonatingPatternSelectPacket.class,
                ScrollResonatingPatternSelectPacket::encode,
                ScrollResonatingPatternSelectPacket::decode,
                ScrollResonatingPatternSelectPacket::handle
        );

        INSTANCE.registerMessage(
                packetId++,
                SideConfigMenuOpenPacket.class,
                SideConfigMenuOpenPacket::encode,
                SideConfigMenuOpenPacket::decode,
                SideConfigMenuOpenPacket::handle
        );

        INSTANCE.registerMessage(
                packetId++,
                MirrorLinkerBatchApplyPacket.class,
                MirrorLinkerBatchApplyPacket::encode,
                MirrorLinkerBatchApplyPacket::decode,
                MirrorLinkerBatchApplyPacket::handle
        );
    }
}
