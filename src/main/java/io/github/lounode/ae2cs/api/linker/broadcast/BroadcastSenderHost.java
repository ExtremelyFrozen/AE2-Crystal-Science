package io.github.lounode.ae2cs.api.linker.broadcast;

/**
 * 实现此接口的对象可以作为广播器的发送端
 */
public interface BroadcastSenderHost
{
    /**
     * 该发送端可以向频段提供的频道数
     */
    int getCouldSendChannels();
}
