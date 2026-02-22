package io.github.lounode.ae2cs.api.networking;

import appeng.menu.guisync.PacketWritable;
import io.github.lounode.ae2cs.common.machine.component.SidePolicy;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.EnumMap;

public record SideConfigField(EnumMap<Direction, SidePolicy> sidePolicies,
                              boolean autoImport,
                              boolean autoExport) implements PacketWritable
{
    private static final String NBT_SIDE_POLICIES = "side_policies";
    private static final String NBT_AUTO_IMPORT = "auto_import";
    private static final String NBT_AUTO_EXPORT = "auto_export";

    private static final String NBT_DIR = "dir";
    private static final String NBT_POLICY = "policy";

    public SideConfigField(FriendlyByteBuf buf)
    {
        this(readFromNetwork(buf));
    }

    /**
     * 用于转发委托
     */
    private SideConfigField(SideConfigField other)
    {
        this(other.sidePolicies, other.autoImport, other.autoExport);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf)
    {
        writeToNetwork(buf, this);
    }

    public static CompoundTag writeToNBT(SideConfigField value)
    {
        CompoundTag root = new CompoundTag();

        ListTag list = new ListTag();
        if (value.sidePolicies != null)
        {
            for (var e : value.sidePolicies.entrySet())
            {
                Direction dir = e.getKey();
                SidePolicy policy = e.getValue();
                if (dir == null || policy == null) continue;

                CompoundTag kv = new CompoundTag();
                kv.putString(NBT_DIR, dir.name());
                kv.putString(NBT_POLICY, policy.name());
                list.add(kv);
            }
        }

        root.put(NBT_SIDE_POLICIES, list);
        root.putBoolean(NBT_AUTO_IMPORT, value.autoImport);
        root.putBoolean(NBT_AUTO_EXPORT, value.autoExport);
        return root;
    }

    public static SideConfigField readFromNBT(CompoundTag root)
    {
        EnumMap<Direction, SidePolicy> map = new EnumMap<>(Direction.class);

        if (root != null && root.contains(NBT_SIDE_POLICIES, Tag.TAG_LIST))
        {
            ListTag list = root.getList(NBT_SIDE_POLICIES, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++)
            {
                CompoundTag kv = list.getCompound(i);

                Direction dir = Direction.valueOf(kv.getString(NBT_DIR));
                SidePolicy policy = SidePolicy.valueOf(kv.getString(NBT_POLICY));

                map.put(dir, policy);
            }
        }

        boolean autoImport = root != null && root.getBoolean(NBT_AUTO_IMPORT);
        boolean autoExport = root != null && root.getBoolean(NBT_AUTO_EXPORT);

        return new SideConfigField(map, autoImport, autoExport);
    }

    public static void writeToNetwork(FriendlyByteBuf buf, SideConfigField value)
    {
        EnumMap<Direction, SidePolicy> map = value.sidePolicies;
        buf.writeVarInt(map.size());
        for (var e : map.entrySet())
        {
            Direction dir = e.getKey();
            SidePolicy policy = e.getValue();
            if (dir == null || policy == null) continue;
            buf.writeUtf(dir.name());
            buf.writeUtf(policy.name());
        }

        buf.writeBoolean(value.autoImport);
        buf.writeBoolean(value.autoExport);
    }

    public static SideConfigField readFromNetwork(FriendlyByteBuf buf)
    {
        int size = buf.readVarInt();
        EnumMap<Direction, SidePolicy> map = new EnumMap<>(Direction.class);

        for (int i = 0; i < size; i++)
        {
            Direction dir = Direction.valueOf(buf.readUtf());
            SidePolicy policy = SidePolicy.valueOf(buf.readUtf());

            map.put(dir, policy);
        }

        boolean autoImport = buf.readBoolean();
        boolean autoExport = buf.readBoolean();

        return new SideConfigField(map, autoImport, autoExport);
    }
}