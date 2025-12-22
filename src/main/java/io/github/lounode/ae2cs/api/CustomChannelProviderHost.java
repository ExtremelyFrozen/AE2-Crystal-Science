package io.github.lounode.ae2cs.api;

public interface CustomChannelProviderHost
{
    /**
     * 该host能承担的最大频道数
     */
    int getMaxChannels();

    /**
     * 用来设置的值还未经过Config乘数，实现需要处理
     */
    void setMaxChannelsWithOutConfig(int maxChannels);

    /**
     * 用来设置的值已经被Config乘数处理过，直接应用即可
     */
    void setMaxChannelsWithConfig(int maxChannels);
}
