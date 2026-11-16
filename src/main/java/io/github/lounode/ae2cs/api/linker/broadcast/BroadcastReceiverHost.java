package io.github.lounode.ae2cs.api.linker.broadcast;

/**
 * 广播装置的接收端应当实现此接口
 */
public interface BroadcastReceiverHost extends BandLinkHost {

    int UNLIMITED_CHANNELS = -1;
    int DEFAULT_EXPECTED_CHANNELS = 32;

    default boolean hasChannelDemand() {
        return true;
    }

    /**
     * 此接收端期望被分配多少频道
     */
    int getExpectedChannels();
}
