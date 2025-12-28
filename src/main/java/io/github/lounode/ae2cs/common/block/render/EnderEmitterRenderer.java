package io.github.lounode.ae2cs.common.block.render;

import appeng.api.orientation.IOrientationStrategy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.client.AECSAdditionalModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class EnderEmitterRenderer implements BlockEntityRenderer<EnderEmitterBlockEntity>
{


    public EnderEmitterRenderer(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public void render(@NotNull EnderEmitterBlockEntity be, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int packedLight, int packedOverlay)
    {

        var level = be.getLevel();
        if (level == null) return;

        var state = be.getBlockState();

        var modelRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();

        boolean active = be.isActive();

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(
                active ? AECSAdditionalModels.EMITTER_TOP_ON_MODEL : AECSAdditionalModels.EMITTER_TOP_OFF_MODEL
        );

        poseStack.pushPose();
        try
        {
            applyAe2Orientation(state, poseStack);

            if (active)
            {
                float angleDeg = ((level.getGameTime() + partialTick) * 6.0f) % 360.0f;
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angleDeg));
                poseStack.translate(-0.5, -0.5, -0.5);
            }

            RenderType rt = RenderType.cutout();
            modelRenderer.renderModel(
                    poseStack.last(),
                    buffer.getBuffer(rt),
                    state,
                    model,
                    1.0f, 1.0f, 1.0f,
                    packedLight,
                    packedOverlay,
                    ModelData.EMPTY,
                    rt
            );
        }
        finally
        {
            poseStack.popPose();
        }

    }

    private static void applyAe2Orientation(BlockState state, PoseStack poseStack)
    {
        IOrientationStrategy strategy = IOrientationStrategy.get(state);
        Direction facing = strategy.getFacing(state);
        int spin = Mth.positiveModulo(strategy.getSpin(state), 4);

        // 以中心为旋转基点
        poseStack.translate(0.5, 0.5, 0.5);

        // 把“默认前方 NORTH”对齐到 facing
        rotateNorthToFacing(facing, poseStack);

        // 围绕 facing 轴做 spin（0/90/180/270）
        if (spin != 0)
        {
            float deg = spin * 90.0f;
            switch (facing.getAxis())
            {
                case X -> poseStack.mulPose((facing == Direction.EAST ? Axis.XP : Axis.XN).rotationDegrees(deg));
                case Y -> poseStack.mulPose((facing == Direction.UP ? Axis.YP : Axis.YN).rotationDegrees(deg));
                case Z -> poseStack.mulPose((facing == Direction.SOUTH ? Axis.ZP : Axis.ZN).rotationDegrees(deg));
            }
        }

        poseStack.translate(-0.5, -0.5, -0.5);
    }

    private static void rotateNorthToFacing(Direction facing, PoseStack poseStack)
    {
        switch (facing)
        {
            case NORTH ->
            {
            }
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));

            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        }
    }
}