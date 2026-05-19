package io.github.lounode.ae2cs.api.linker.broadcast.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 用于从客户端向服务端发送连接确认信息
 */
public record FrequencyBandLinkInfo(String password, boolean asSender)
{
    public static final StreamCodec<FriendlyByteBuf, FrequencyBandLinkInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            FrequencyBandLinkInfo::password,
            ByteBufCodecs.BOOL,
            FrequencyBandLinkInfo::asSender,
            FrequencyBandLinkInfo::new
    );
}
