package io.github.lounode.ae2cs.integration.jade;

import io.github.lounode.ae2cs.common.block.EnderBroadcasterBlock;
import io.github.lounode.ae2cs.common.block.EnderEmitterBlock;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
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
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(EnderChannelDataProvider.INSTANCE, EnderEmitterBlock.class);
        registration.registerBlockComponent(EnderChannelDataProvider.INSTANCE, EnderBroadcasterBlock.class);
    }
}
