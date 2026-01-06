package io.github.lounode.ae2cs.common.block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lounode.ae2cs.common.block.entity.EnderInterfaceBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
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

        var aabb = new AABB(
                -r, -r, -r,
                r + 1.0, r + 1.0, r + 1.0
        );

        var consumer = buffer.getBuffer(RenderType.lines());

        float red = 0.2f, green = 0.9f, blue = 0.9f, alpha = 0.8f;

        LevelRenderer.renderLineBox(
                poseStack,
                consumer,
                aabb,
                red, green, blue, alpha
        );
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull EnderInterfaceBlockEntity blockEntity)
    {
        BlockPos centerPos = blockEntity.getBlockEntity().getBlockPos();
        int range = blockEntity.getEnderInterfaceLogic().getRange();
        return new AABB(
                centerPos.getX() - range,
                centerPos.getY() - range,
                centerPos.getZ() - range,
                centerPos.getX() + range,
                centerPos.getY() + range,
                centerPos.getZ() + range
        );
    }
}