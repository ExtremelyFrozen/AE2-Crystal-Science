package io.github.lounode.ae2cs.common.block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public class EnderEmitterRenderer implements BlockEntityRenderer<EnderEmitterBlockEntity>
{


    public EnderEmitterRenderer(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public void render(@NotNull EnderEmitterBlockEntity be, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
    }
}