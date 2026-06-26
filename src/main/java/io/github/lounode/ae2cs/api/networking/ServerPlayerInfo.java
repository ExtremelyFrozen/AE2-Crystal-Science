package io.github.lounode.ae2cs.api.networking;

import appeng.menu.guisync.PacketWritable;

import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record ServerPlayerInfo(Map<UUID, String> playerInfo) implements PacketWritable {

    public ServerPlayerInfo(FriendlyByteBuf buf) {
        this(readMap(buf));
    }

    private static Map<UUID, String> readMap(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<UUID, String> map = new LinkedHashMap<>(Math.max(0, size));

        for (int i = 0; i < size; i++) {
            UUID id = buf.readUUID();
            String name = buf.readUtf();
            map.put(id, name);
        }

        return map;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        Map<UUID, String> map = this.playerInfo == null ? Map.of() : this.playerInfo;

        buf.writeVarInt(map.size());
        for (var e : map.entrySet()) {
            buf.writeUUID(e.getKey());
            buf.writeUtf(e.getValue() == null ? "" : e.getValue());
        }
    }
}
