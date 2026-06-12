package io.github.lounode.ae2cs.api.linker.broadcast.networking;

import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;

import appeng.menu.guisync.PacketWritable;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * 记录了单个频段详细信息的记录类，由服务端发送给客户端作信息展示
 */
public record FrequencyBandDetailInfo(
                                      String name,
                                      boolean isEncrypted,
                                      boolean isPublic,
                                      boolean allowedMemoryCardCopy,
                                      BroadcastFrequencyBand.BandError errorState,
                                      List<String> whiteList,
                                      List<GlobalPos> senderList,
                                      List<GlobalPos> receiverList)
        implements PacketWritable {

    public FrequencyBandDetailInfo(RegistryFriendlyByteBuf buf) {
        this(
                buf.readUtf(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readEnum(BroadcastFrequencyBand.BandError.class),
                readStringList(buf),
                readGlobalPosList(buf),
                readGlobalPosList(buf));
    }

    private static List<String> readStringList(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buf.readUtf());
        }
        return list;
    }

    private static List<GlobalPos> readGlobalPosList(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<GlobalPos> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(GlobalPos.STREAM_CODEC.decode(buf));
        }
        return list;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeBoolean(isEncrypted);
        buf.writeBoolean(isPublic);
        buf.writeBoolean(allowedMemoryCardCopy);
        buf.writeEnum(errorState);

        buf.writeVarInt(whiteList.size());
        for (String whiteListEntry : whiteList) {
            buf.writeUtf(whiteListEntry);
        }

        buf.writeVarInt(senderList.size());
        for (GlobalPos sender : senderList) {
            GlobalPos.STREAM_CODEC.encode(buf, sender);
        }

        buf.writeVarInt(receiverList.size());
        for (GlobalPos receiver : receiverList) {
            GlobalPos.STREAM_CODEC.encode(buf, receiver);
        }
    }
}
