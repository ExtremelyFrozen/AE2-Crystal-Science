package io.github.lounode.ae2cs.common.block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.lounode.ae2cs.common.block.entity.EnderInterfaceBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class EnderInterfaceRender implements BlockEntityRenderer<EnderInterfaceBlockEntity, EnderInterfaceRender.State>
{
    private static final int COLOR = ARGB.colorFromFloat(0.8f, 0.2f, 0.9f, 0.9f);

    public EnderInterfaceRender(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(
            EnderInterfaceBlockEntity be,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    )
    {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPosition, breakProgress);

        var logic = be.getEnderInterfaceLogic();
        state.renderRange = logic.isRenderRangeInClient();
        state.range = logic.getRange();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState)
    {
        if (!state.renderRange || state.range <= 0)
        {
            return;
        }

        int r = state.range;
        float min = -r;
        float max = r + 1.0f;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.linesTranslucent(),
                (pose, consumer) -> renderBox(pose, consumer, min, min, min, max, max, max));
    }

    @Override
    public boolean shouldRenderOffScreen()
    {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull EnderInterfaceBlockEntity blockEntity)
    {
        return blockEntity.getCustomBoundingBox(blockEntity.getBlockPos());
    }

    private static void renderBox(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ
    )
    {
        line(pose, consumer, minX, minY, minZ, maxX, minY, minZ);
        line(pose, consumer, maxX, minY, minZ, maxX, minY, maxZ);
        line(pose, consumer, maxX, minY, maxZ, minX, minY, maxZ);
        line(pose, consumer, minX, minY, maxZ, minX, minY, minZ);

        line(pose, consumer, minX, maxY, minZ, maxX, maxY, minZ);
        line(pose, consumer, maxX, maxY, minZ, maxX, maxY, maxZ);
        line(pose, consumer, maxX, maxY, maxZ, minX, maxY, maxZ);
        line(pose, consumer, minX, maxY, maxZ, minX, maxY, minZ);

        line(pose, consumer, minX, minY, minZ, minX, maxY, minZ);
        line(pose, consumer, maxX, minY, minZ, maxX, maxY, minZ);
        line(pose, consumer, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(pose, consumer, minX, minY, maxZ, minX, maxY, maxZ);
    }

    private static void line(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2
    )
    {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0.0f)
        {
            dx /= length;
            dy /= length;
            dz /= length;
        }

        consumer.addVertex(pose, x1, y1, z1).setColor(COLOR).setNormal(pose, dx, dy, dz).setLineWidth(1.0f);
        consumer.addVertex(pose, x2, y2, z2).setColor(COLOR).setNormal(pose, dx, dy, dz).setLineWidth(1.0f);
    }

    public static final class State extends BlockEntityRenderState
    {
        private boolean renderRange;
        private int range;
    }
}
