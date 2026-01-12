package io.github.lounode.ae2cs.common.init.client;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.render.EnderBroadcasterRender;
import io.github.lounode.ae2cs.common.block.render.EnderEmitterRenderer;
import io.github.lounode.ae2cs.common.block.render.EnderInterfaceRender;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class AECSClientRenderers
{

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e)
    {
        e.registerBlockEntityRenderer(AECSBlockEntities.ENDER_EMITTER_BLOCK_ENTITY.get(), EnderEmitterRenderer::new);
        e.registerBlockEntityRenderer(AECSBlockEntities.ENDER_BROADCASTER_BLOCK_ENTITY.get(), EnderBroadcasterRender::new);
        e.registerBlockEntityRenderer(AECSBlockEntities.ENDER_INTERFACE_BLOCK_ENTITY.get(), EnderInterfaceRender::new);
        e.registerBlockEntityRenderer(AECSBlockEntities.EX_ENDER_INTERFACE_BLOCK_ENTITY.get(), EnderInterfaceRender::new);
    }
}