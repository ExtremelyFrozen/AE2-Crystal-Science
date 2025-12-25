package io.github.lounode.ae2cs.api.linker.broadcast.networking;

import java.util.UUID;

/**
 * 由客户端发送到服务端，用于包含用于创建一个频段的全部信息
 */
public record FrequencyBandCreateInfo(String name, String password, UUID ownerId, boolean isPublic,
                                      boolean allowedMemoryCardCopy)
{
}
