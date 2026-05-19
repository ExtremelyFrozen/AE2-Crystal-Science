package io.github.lounode.ae2cs.api.linker.broadcast.networking;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/**
 * 由客户端发送到服务端，用于包含用于创建一个频段的全部信息
 */
public record FrequencyBandCreateInfo(String name, String password, UUID ownerId, boolean isPublic,
                                       boolean allowedMemoryCardCopy)
{
    public static final StreamCodec<FriendlyByteBuf, FrequencyBandCreateInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            FrequencyBandCreateInfo::name,
            ByteBufCodecs.STRING_UTF8,
            FrequencyBandCreateInfo::password,
            UUIDUtil.STREAM_CODEC,
            FrequencyBandCreateInfo::ownerId,
            ByteBufCodecs.BOOL,
            FrequencyBandCreateInfo::isPublic,
            ByteBufCodecs.BOOL,
            FrequencyBandCreateInfo::allowedMemoryCardCopy,
            FrequencyBandCreateInfo::new
    );
}
