package io.github.lounode.ae2cs.integration.jade;

import io.github.lounode.ae2cs.common.block.EnderBroadcasterBlock;
import io.github.lounode.ae2cs.common.block.EnderEmitterBlock;
import io.github.lounode.ae2cs.common.block.entity.SimplePatternProviderBlockEntity;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import appeng.block.crafting.PatternProviderBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class AECSJadePlugin implements IWailaPlugin
{
    @Override
    public void register(IWailaCommonRegistration registration)
    {
        registration.registerBlockDataProvider(EnderBroadcasterDataProvider.INSTANCE, EnderBroadcasterBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration)
    {
        registration.registerBlockComponent(EnderEmitterDataProvider.INSTANCE, EnderEmitterBlock.class);
        registration.registerBlockComponent(EnderBroadcasterDataProvider.INSTANCE, EnderBroadcasterBlock.class);
        registration.registerBlockComponent(SimplePatternProviderDataProvider.INSTANCE, PatternProviderBlock.class);
    }
}
