package io.github.lounode.ae2cs.api.linker.broadcast.networking;

/**
 * 用于从客户端向服务端发送连接确认信息
 */
public record FrequencyBandLinkInfo(String password, boolean asSender) {}
