package io.github.lounode.ae2cs.api.linker.broadcast;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

/**
 * 给内存卡复制band信息用
 */
public record MemoryCardBandInfo(String bandName, boolean asSender)
{
    public static CompoundTag writeToNBT(MemoryCardBandInfo info)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("band_name", info.bandName);
        tag.putBoolean("as_sender", info.asSender);
        return tag;
    }

    public static @Nullable MemoryCardBandInfo readFromNBT(CompoundTag tag)
    {
        if (!tag.contains("band_name") || !tag.contains("as_sender"))
            return null;
        else
            return new MemoryCardBandInfo(tag.getString("band_name"), tag.getBoolean("as_sender"));
    }

    public static void writeToBuf(FriendlyByteBuf buf, MemoryCardBandInfo info)
    {
        buf.writeUtf(info.bandName);
        buf.writeBoolean(info.asSender);
    }

    public static MemoryCardBandInfo readFromBuf(FriendlyByteBuf buf)
    {
        String bandName = buf.readUtf();
        boolean asSender = buf.readBoolean();
        return new MemoryCardBandInfo(bandName, asSender);
    }
}
