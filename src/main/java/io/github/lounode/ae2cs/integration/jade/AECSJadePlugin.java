package io.github.lounode.ae2cs.integration.jade;

import io.github.lounode.ae2cs.common.block.EnderBroadcasterBlock;
import io.github.lounode.ae2cs.common.block.EnderEmitterBlock;
import io.github.lounode.ae2cs.common.block.MirrorPatternProviderBlock;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.block.entity.MirrorPatternProviderBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class AECSJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(EnderChannelDataProvider.INSTANCE, EnderEmitterBlockEntity.class);
        registration.registerBlockDataProvider(EnderChannelDataProvider.INSTANCE, EnderBroadcasterBlockEntity.class);
        registration.registerBlockDataProvider(EnderBroadcasterDataProvider.INSTANCE, EnderBroadcasterBlockEntity.class);
        registration.registerBlockDataProvider(MirrorPatternProviderDataProvider.INSTANCE, MirrorPatternProviderBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(EnderChannelDataProvider.INSTANCE, EnderEmitterBlock.class);
        registration.registerBlockComponent(EnderChannelDataProvider.INSTANCE, EnderBroadcasterBlock.class);
        registration.registerBlockComponent(EnderEmitterDataProvider.INSTANCE, EnderEmitterBlock.class);
        registration.registerBlockComponent(EnderBroadcasterDataProvider.INSTANCE, EnderBroadcasterBlock.class);
        registration.registerBlockComponent(MirrorPatternProviderDataProvider.INSTANCE, MirrorPatternProviderBlock.class);
    }
}
