package io.github.lounode.ae2cs.api.linker.broadcast;

/** 广播装置的接收端应当实现此接口 */
public interface BroadcastReceiverHost
{
    /** 此接收端期望被分配多少频道 */
    int getExpectedChannels();
}
