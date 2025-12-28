package io.github.lounode.ae2cs.common.block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.client.AECSAdditionalModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import org.jetbrains.annotations.NotNull;

public class EnderBroadcasterRender implements BlockEntityRenderer<EnderBroadcasterBlockEntity>
{

    public EnderBroadcasterRender(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public void render(@NotNull EnderBroadcasterBlockEntity be, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        var level = be.getLevel();
        if (level == null) return;

        // 角速度：每 tick 转 6 度
        float angle = ((level.getGameTime() + partialTick) * 6.0f) % 360.0f;

        // 拿到核心的 baked model
        BakedModel coreModel;
        if (be.isActiveForClient())
        {
            if (be.isAsSenderForClient())
                coreModel = Minecraft.getInstance().getModelManager().getModel(AECSAdditionalModels.BROADCASTER_SENDER_CORE_MODEL);
            else
                coreModel = Minecraft.getInstance().getModelManager().getModel(AECSAdditionalModels.BROADCASTER_RECEIVER_CORE_MODEL);
        }
        else
        {
            coreModel = Minecraft.getInstance().getModelManager().getModel(AECSAdditionalModels.BROADCASTER_OFF_CORE);
        }

        RenderType rt = RenderType.cutout();
        VertexConsumer vc = buffer.getBuffer(rt);

        // 围绕方块中心 (0.5, 0.5, 0.5) 绕 Y 轴旋转
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.5, -0.5);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                vc,
                be.getBlockState(),
                coreModel,
                1.0f, 1.0f, 1.0f,
                packedLight,
                packedOverlay,
                net.neoforged.neoforge.client.model.data.ModelData.EMPTY,
                rt
        );
        poseStack.popPose();
    }
}
