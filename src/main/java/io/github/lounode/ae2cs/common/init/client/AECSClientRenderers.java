package io.github.lounode.ae2cs.common.init.client;

import appeng.client.api.renderer.parts.RegisterPartRendererEvent;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.client.render.part.EnderInterfacePartRenderer;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.me.part.EnderInterfacePart;
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

    @SubscribeEvent
    public static void registerPartRenderers(RegisterPartRendererEvent event)
    {
        event.register(EnderInterfacePart.class, new EnderInterfacePartRenderer());
    }
}
