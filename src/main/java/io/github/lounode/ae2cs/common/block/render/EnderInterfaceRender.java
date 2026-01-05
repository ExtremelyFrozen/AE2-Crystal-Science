package io.github.lounode.ae2cs.common.block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lounode.ae2cs.common.block.entity.EnderInterfaceBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public class EnderInterfaceRender implements BlockEntityRenderer<EnderInterfaceBlockEntity>
{


    public EnderInterfaceRender(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull EnderInterfaceBlockEntity blockEntity)
    {
        return BlockEntityRenderer.super.shouldRenderOffScreen(blockEntity) || blockEntity.getEnderInterfaceLogic().isRenderRangeInClient();
    }

    @Override
    public void render(@NotNull EnderInterfaceBlockEntity be, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int packedLight, int packedOverlay)
    {
        var logic = be.getEnderInterfaceLogic();
        if (!logic.isRenderRangeInClient())
        {
            return;
        }

        int r = logic.getRange();
        if (r <= 0)
        {
            return;
        }

        var aabb = new net.minecraft.world.phys.AABB(
                -r, -r, -r,
                r + 1.0, r + 1.0, r + 1.0
        );

        var consumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.lines());

        float red = 0.2f, green = 0.9f, blue = 0.9f, alpha = 0.8f;

        net.minecraft.client.renderer.LevelRenderer.renderLineBox(
                poseStack,
                consumer,
                aabb,
                red, green, blue, alpha
        );
    }
}