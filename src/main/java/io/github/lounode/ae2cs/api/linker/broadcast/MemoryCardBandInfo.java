package io.github.lounode.ae2cs.api.linker.broadcast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 给内存卡复制band信息用
 */
public record MemoryCardBandInfo(String bandName, boolean asSender)
{
    public static final Codec<MemoryCardBandInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("band_name").forGetter(MemoryCardBandInfo::bandName),
            Codec.BOOL.fieldOf("as_sender").forGetter(MemoryCardBandInfo::asSender)
    ).apply(instance, MemoryCardBandInfo::new));

    public static final StreamCodec<FriendlyByteBuf, MemoryCardBandInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            MemoryCardBandInfo::bandName,
            ByteBufCodecs.BOOL,
            MemoryCardBandInfo::asSender,
            MemoryCardBandInfo::new
    );
}
