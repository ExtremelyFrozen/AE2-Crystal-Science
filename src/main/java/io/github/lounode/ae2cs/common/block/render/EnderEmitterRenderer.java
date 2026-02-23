package io.github.lounode.ae2cs.common.block.render;

import appeng.api.orientation.IOrientationStrategy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.client.AECSAdditionalModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

public class EnderEmitterRenderer implements BlockEntityRenderer<EnderEmitterBlockEntity>
{
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.tryBuild("minecraft", "textures/misc/white.png");

    // 相机离 emitter 多近时显示
    private static final double SHOW_RANGE = 96.0;
    private static final double SHOW_RANGE_SQR = SHOW_RANGE * SHOW_RANGE;

    private static final float PENDING_WIDTH = 0.045f;
    private static final float LINKED_WIDTH = 0.060f;

    private static final int MAX_BEAMS_PER_SET = 512;

    // === 范围渲染参数 ===
    private static final double RANGE_BOX_EPS = 0.002; // 防 z-fighting
    private static final float RANGE_LINE_ALPHA = 0.55f;

    // 自动连接范围（偏青绿）
    private static final float AUTO_R = 0.20f, AUTO_G = 0.95f, AUTO_B = 0.75f;
    // 最大连接范围（偏黄橙）
    private static final float MAX_R = 1.00f, MAX_G = 0.85f, MAX_B = 0.15f;

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

        // link status：必须放在 orientation 之外，否则线框/光束会被旋转
        if (be.isShowLinkStatus())
        {
            renderLinkStatus(be, partialTick, poseStack, buffer);
        }

        // ② 原本的顶部模型渲染（保持原逻辑）
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

    // ====== Offscreen & Culling Control ======

    @Override
    public boolean shouldRenderOffScreen(@NotNull EnderEmitterBlockEntity be)
    {
        // link status 打开时，允许离屏渲染（否则 BER 根本不会被调用）
        return be.isShowLinkStatus();
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(EnderEmitterBlockEntity be)
    {
        return be.getCustomBoundingBox(be.getBlockPos());
    }

    @Override
    public int getViewDistance()
    {
        return 96;
    }

    /**
     * 渲染线框与光束
     */
    private static void renderLinkStatus(EnderEmitterBlockEntity be, float partialTick,
                                         PoseStack poseStack, MultiBufferSource buffer)
    {
        Level level = be.getLevel();
        if (level == null) return;

        // 相机世界坐标
        var cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPosW = cam.getPosition();

        // 距离裁剪（世界坐标）
        BlockPos bePos = be.getBlockPos();
        Vec3 beCenterW = Vec3.atCenterOf(bePos);
        if (camPosW.distanceToSqr(beCenterW) > SHOW_RANGE_SQR) return;

        int packedLight = LightTexture.FULL_BRIGHT;
        int packedOverlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();
        try
        {
            // 画范围线框（局部坐标）
            renderChebyshevRangesLocal(poseStack, buffer, be);

            // 画光束（局部坐标）
            VertexConsumer beamVc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
            PoseStack.Pose pose = poseStack.last();

            // 起点：本方块中心（BER 局部坐标系）
            Vector3f startL = new Vector3f(0.5f, 0.5f, 0.5f);

            // 相机局部坐标 = camWorld - bePos
            Vector3f camL = new Vector3f(
                    (float) (camPosW.x - bePos.getX()),
                    (float) (camPosW.y - bePos.getY()),
                    (float) (camPosW.z - bePos.getZ())
            );

            // pending（淡红）
            List<BlockPos> pending = be.getPendingRenderPositionsSnapshot();
            int c1 = 0;
            for (BlockPos target : pending)
            {
                if (++c1 > MAX_BEAMS_PER_SET) break;
                if (!level.isLoaded(target)) continue;

                int dx = target.getX() - bePos.getX();
                int dy = target.getY() - bePos.getY();
                int dz = target.getZ() - bePos.getZ();
                Vector3f endL = new Vector3f(dx + 0.5f, dy + 0.5f, dz + 0.5f);

                drawBeamCrossLocal(pose, beamVc, startL, endL, camL, PENDING_WIDTH,
                        1.00f, 0.35f, 0.35f, 0.35f,
                        packedLight, packedOverlay);
            }

            // linked（深紫）
            List<BlockPos> linked = be.getLinkedRenderPositionsSnapshot();
            int c2 = 0;
            for (BlockPos target : linked)
            {
                if (++c2 > MAX_BEAMS_PER_SET) break;
                if (!level.isLoaded(target)) continue;

                int dx = target.getX() - bePos.getX();
                int dy = target.getY() - bePos.getY();
                int dz = target.getZ() - bePos.getZ();
                Vector3f endL = new Vector3f(dx + 0.5f, dy + 0.5f, dz + 0.5f);

                drawBeamCrossLocal(pose, beamVc, startL, endL, camL, LINKED_WIDTH,
                        0.55f, 0.05f, 1.00f, 0.80f,
                        packedLight, packedOverlay);
            }
        }
        finally
        {
            poseStack.popPose();
        }
    }

    /**
     * 渲染切比雪夫半径范围：两个线框立方体（局部坐标系：本方块为 [0..1]）
     */
    private static void renderChebyshevRangesLocal(PoseStack poseStack,
                                                   MultiBufferSource buffer,
                                                   EnderEmitterBlockEntity be)
    {
        int autoR = Mth.clamp(be.getLinkDistance(), 0, 512);
        int maxR = Mth.clamp(be.getMaxLinkDistanceForClient(), 0, 512);
        if (maxR < autoR) maxR = autoR;

        VertexConsumer lineVc = buffer.getBuffer(RenderType.lines());

        AABB base = new AABB(0, 0, 0, 1, 1, 1);

        // 外层（最大范围）
        if (maxR > 0)
        {
            AABB maxBox = base.inflate(maxR).inflate(RANGE_BOX_EPS);
            LevelRenderer.renderLineBox(poseStack, lineVc, maxBox, MAX_R, MAX_G, MAX_B, RANGE_LINE_ALPHA);
        }

        // 内层（自动范围）
        if (autoR > 0)
        {
            AABB autoBox = base.inflate(autoR).inflate(RANGE_BOX_EPS);
            LevelRenderer.renderLineBox(poseStack, lineVc, autoBox, AUTO_R, AUTO_G, AUTO_B, RANGE_LINE_ALPHA);
        }
    }

    /**
     * 光束渲染
     */
    private static void drawBeamCrossLocal(PoseStack.Pose pose, VertexConsumer vc,
                                           Vector3f startL, Vector3f endL, Vector3f camL,
                                           float width,
                                           float r, float g, float b, float a,
                                           int packedLight, int packedOverlay)
    {
        Vector3f dir = new Vector3f(endL).sub(startL);
        if (dir.lengthSquared() < 1.0e-6f) return;
        dir.normalize();

        Vector3f mid = new Vector3f(startL).add(endL).mul(0.5f);
        Vector3f toCam = new Vector3f(camL).sub(mid);
        if (toCam.lengthSquared() < 1.0e-6f) toCam.set(0, 1, 0);
        else toCam.normalize();

        Vector3f side1 = new Vector3f(dir).cross(toCam);
        if (side1.lengthSquared() < 1.0e-6f)
        {
            Vector3f fallback = Math.abs(dir.y()) < 0.99f ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
            side1 = new Vector3f(dir).cross(fallback);
        }
        side1.normalize().mul(width * 0.5f);

        Vector3f side2 = new Vector3f(dir).cross(side1);
        if (side2.lengthSquared() > 1.0e-6f) side2.normalize().mul(width * 0.5f);
        else side2.set(0, 0, 0);

        drawBeamQuadLocal(pose, vc, startL, endL, side1, r, g, b, a, packedLight, packedOverlay);
        if (side2.lengthSquared() > 1.0e-8f)
            drawBeamQuadLocal(pose, vc, startL, endL, side2, r, g, b, a, packedLight, packedOverlay);
    }

    private static void drawBeamQuadLocal(PoseStack.Pose pose, VertexConsumer vc,
                                          Vector3f startL, Vector3f endL, Vector3f side,
                                          float r, float g, float b, float a,
                                          int packedLight, int packedOverlay)
    {
        Vector3f s1 = new Vector3f(startL).add(side);
        Vector3f s2 = new Vector3f(startL).sub(side);
        Vector3f e1 = new Vector3f(endL).add(side);
        Vector3f e2 = new Vector3f(endL).sub(side);

        float aStart = a * 0.85f;
        float aEnd = a * 0.20f;

        float nx = 0f, ny = 1f, nz = 0f;

        putVertex(pose, vc, s1, 0f, 0f, r, g, b, aStart, packedLight, packedOverlay, nx, ny, nz);
        putVertex(pose, vc, s2, 1f, 0f, r, g, b, aStart, packedLight, packedOverlay, nx, ny, nz);
        putVertex(pose, vc, e2, 1f, 1f, r, g, b, aEnd, packedLight, packedOverlay, nx, ny, nz);

        putVertex(pose, vc, s1, 0f, 0f, r, g, b, aStart, packedLight, packedOverlay, nx, ny, nz);
        putVertex(pose, vc, e2, 1f, 1f, r, g, b, aEnd, packedLight, packedOverlay, nx, ny, nz);
        putVertex(pose, vc, e1, 0f, 1f, r, g, b, aEnd, packedLight, packedOverlay, nx, ny, nz);
    }

    private static void putVertex(PoseStack.Pose pose, VertexConsumer vc,
                                  Vector3f p, float u, float v,
                                  float r, float g, float b, float a,
                                  int packedLight, int packedOverlay,
                                  float nx, float ny, float nz)
    {
        vc.vertex(pose.pose(), p.x(), p.y(), p.z())
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(pose.normal(), nx, ny, nz)
                .endVertex();
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