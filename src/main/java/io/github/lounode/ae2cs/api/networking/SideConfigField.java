package io.github.lounode.ae2cs.api.networking;

import appeng.menu.guisync.PacketWritable;
import io.github.lounode.ae2cs.common.machine.component.SidePolicy;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.EnumMap;
import java.util.Map;

public record SideConfigField(EnumMap<Direction, SidePolicy> sidePolicies, boolean autoImport,
                              boolean autoExport) implements PacketWritable
{
    public SideConfigField(RegistryFriendlyByteBuf buf)
    {
        this(readMap(buf), buf.readBoolean(), buf.readBoolean());
    }

    private static EnumMap<Direction, SidePolicy> readMap(RegistryFriendlyByteBuf buf)
    {
        int size = buf.readVarInt();
        int max = Direction.values().length;
        if (size < 0 || size > max)
        {
            throw new IllegalArgumentException("Invalid sidePolicies size: " + size + " (max " + max + ")");
        }

        EnumMap<Direction, SidePolicy> map = new EnumMap<>(Direction.class);
        for (int i = 0; i < size; i++)
        {
            Direction dir = buf.readEnum(Direction.class);
            SidePolicy policy = buf.readEnum(SidePolicy.class);
            map.put(dir, policy);
        }
        return map;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf)
    {
        buf.writeVarInt(sidePolicies.size());
        for (Map.Entry<Direction, SidePolicy> e : sidePolicies.entrySet())
        {
            buf.writeEnum(e.getKey());
            buf.writeEnum(e.getValue());
        }
        buf.writeBoolean(autoImport);
        buf.writeBoolean(autoExport);
    }
}

