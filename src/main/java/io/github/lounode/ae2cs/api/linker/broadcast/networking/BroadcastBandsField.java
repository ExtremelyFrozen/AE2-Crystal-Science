package io.github.lounode.ae2cs.api.linker.broadcast.networking;

import appeng.menu.guisync.PacketWritable;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于将服务器上全部频段的简单信息发送到客户端
 */
public record BroadcastBandsField(List<Entry> bands) implements PacketWritable {

    // 反序列化构造器：CustomField 会通过反射调用它
    public BroadcastBandsField(FriendlyByteBuf buf) {
        this(read(buf));
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeVarInt(bands.size());
        for (var entry : bands) {
            buf.writeUtf(entry.name());
            buf.writeByte(entry.flags());
        }
    }

    private static List<Entry> read(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        var list = new ArrayList<Entry>(n);
        for (int i = 0; i < n; i++) {
            String name = buf.readUtf(64);
            byte flags = buf.readByte();
            list.add(new Entry(name, flags));
        }
        return List.copyOf(list);
    }

    public record Entry(String name, byte flags) {

        public boolean isPublic() {
            return (flags & 0x01) != 0;
        }

        public boolean isEncrypted() {
            return (flags & 0x02) != 0;
        }

        public static byte pack(boolean isPublic, boolean isEncrypted) {
            return (byte) ((isPublic ? 0x01 : 0) | (isEncrypted ? 0x02 : 0));
        }
    }
}
