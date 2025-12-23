package io.github.lounode.ae2cs.api;

/**
 * 该host可以提供自定义最大频道承载量
 */
public interface CustomChannelProviderHost
{
    /**
     * 获取该host能承担的最大频道数
     */
    int getMaxChannels();

    /**
     * 设置最大频道数
     */
    void setMaxChannels(int maxChannels);

    /**
     * 是否使用自定义最大频道数
     */
    boolean isEnabledCustomChannel();

    /**
     * 设置状态
     */
    void setEnabledCustomChannel(boolean enabled);
}