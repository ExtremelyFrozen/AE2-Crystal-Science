package io.github.lounode.ae2cs.api.linker.broadcast.networking;

import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;

import appeng.menu.guisync.PacketWritable;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;

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

    public FrequencyBandDetailInfo(FriendlyByteBuf buf) {
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

    private static List<String> readStringList(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buf.readUtf());
        }
        return list;
    }

    private static List<GlobalPos> readGlobalPosList(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<GlobalPos> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buf.readGlobalPos());
        }
        return list;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
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
            buf.writeGlobalPos(sender);
        }

        buf.writeVarInt(receiverList.size());
        for (GlobalPos receiver : receiverList) {
            buf.writeGlobalPos(receiver);
        }
    }
}
