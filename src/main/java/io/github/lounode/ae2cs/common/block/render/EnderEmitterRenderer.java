package io.github.lounode.ae2cs.common.block.render;

import appeng.api.orientation.IOrientationStrategy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.client.AECSAdditionalModels;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class EnderEmitterRenderer implements BlockEntityRenderer<EnderEmitterBlockEntity, EnderEmitterRenderer.State>
{
    private static final Identifier WHITE_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/misc/white.png");

    private static final double SHOW_RANGE = 96.0;
    private static final double SHOW_RANGE_SQR = SHOW_RANGE * SHOW_RANGE;

    private static final float PENDING_WIDTH = 0.045f;
    private static final float LINKED_WIDTH = 0.060f;

    private static final int MAX_BEAMS_PER_SET = 512;

    private static final double RANGE_BOX_EPS = 0.002;
    private static final float RANGE_LINE_ALPHA = 0.55f;

    private static final float AUTO_R = 0.20f, AUTO_G = 0.95f, AUTO_B = 0.75f;
    private static final float MAX_R = 1.00f, MAX_G = 0.85f, MAX_B = 0.15f;
    private static final int AUTO_COLOR = ARGB.colorFromFloat(RANGE_LINE_ALPHA, AUTO_R, AUTO_G, AUTO_B);
    private static final int MAX_COLOR = ARGB.colorFromFloat(RANGE_LINE_ALPHA, MAX_R, MAX_G, MAX_B);
    private static final int FULL_BRIGHT = 15728880;
    private static final int[] EMPTY_TINTS = new int[0];

    public EnderEmitterRenderer(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public @NotNull State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(
            EnderEmitterBlockEntity be,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    )
    {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPosition, breakProgress);
        state.reset();

        var level = be.getLevel();
        if (level == null) return;

        state.blockState = be.getBlockState();
        state.active = be.isActive();
        state.angleDeg = state.active ? ((level.getGameTime() + partialTick) * 6.0f) % 360.0f : 0.0f;

        var modelManager = Minecraft.getInstance().getModelManager();
        state.topModel = modelManager.getStandaloneModel(
                state.active ? AECSAdditionalModels.EMITTER_TOP_ON_MODEL : AECSAdditionalModels.EMITTER_TOP_OFF_MODEL
        );

        if (!be.isShowLinkStatus())
        {
            return;
        }

        BlockPos bePos = be.getBlockPos();
        Vec3 beCenter = Vec3.atCenterOf(bePos);
        if (cameraPosition.distanceToSqr(beCenter) > SHOW_RANGE_SQR)
        {
            return;
        }

        state.renderLinkStatus = true;
        state.linkDistance = Mth.clamp(be.getLinkDistance(), 0, 512);
        state.maxLinkDistance = Mth.clamp(be.getMaxLinkDistanceForClient(), 0, 512);
        state.cameraLocal.set(
                (float) (cameraPosition.x - bePos.getX()),
                (float) (cameraPosition.y - bePos.getY()),
                (float) (cameraPosition.z - bePos.getZ())
        );
        state.pendingTargets = copyLoadedTargets(level, be.getPendingRenderPositionsSnapshot());
        state.linkedTargets = copyLoadedTargets(level, be.getLinkedRenderPositionsSnapshot());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState)
    {
        if (state.renderLinkStatus)
        {
            renderLinkStatus(state, poseStack, submitNodeCollector);
        }

        if (state.topModel == null || state.blockState == null)
        {
            return;
        }

        poseStack.pushPose();
        try
        {
            applyAe2Orientation(state.blockState, poseStack);

            if (state.active)
            {
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(state.angleDeg));
                poseStack.translate(-0.5, -0.5, -0.5);
            }

            submitNodeCollector.submitBlockModel(
                    poseStack,
                    Sheets.cutoutBlockSheet(),
                    List.of(state.topModel),
                    EMPTY_TINTS,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0
            );
        }
        finally
        {
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen()
    {
        return true;
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

    private static List<BlockPos> copyLoadedTargets(Level level, List<BlockPos> targets)
    {
        var result = new ArrayList<BlockPos>(Math.min(targets.size(), MAX_BEAMS_PER_SET));
        for (BlockPos target : targets)
        {
            if (result.size() >= MAX_BEAMS_PER_SET) break;
            if (level.isLoaded(target)) result.add(target);
        }
        return result;
    }

    private static void renderLinkStatus(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector)
    {
        renderChebyshevRangesLocal(poseStack, submitNodeCollector, state);

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(WHITE_TEX), (pose, beamVc) ->
        {
            Vector3f startL = new Vector3f(0.5f, 0.5f, 0.5f);
            BlockPos bePos = state.blockPos;

            for (BlockPos target : state.pendingTargets)
            {
                Vector3f endL = new Vector3f(
                        target.getX() - bePos.getX() + 0.5f,
                        target.getY() - bePos.getY() + 0.5f,
                        target.getZ() - bePos.getZ() + 0.5f
                );

                drawBeamCrossLocal(pose, beamVc, startL, endL, state.cameraLocal, PENDING_WIDTH,
                        1.00f, 0.35f, 0.35f, 0.35f,
                        FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }

            for (BlockPos target : state.linkedTargets)
            {
                Vector3f endL = new Vector3f(
                        target.getX() - bePos.getX() + 0.5f,
                        target.getY() - bePos.getY() + 0.5f,
                        target.getZ() - bePos.getZ() + 0.5f
                );

                drawBeamCrossLocal(pose, beamVc, startL, endL, state.cameraLocal, LINKED_WIDTH,
                        0.55f, 0.05f, 1.00f, 0.80f,
                        FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }
        });
    }

    private static void renderChebyshevRangesLocal(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, State state)
    {
        int autoR = state.linkDistance;
        int maxR = Math.max(state.maxLinkDistance, autoR);

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.linesTranslucent(), (pose, lineVc) ->
        {
            if (maxR > 0)
            {
                renderLineBox(pose, lineVc,
                        -maxR - RANGE_BOX_EPS,
                        -maxR - RANGE_BOX_EPS,
                        -maxR - RANGE_BOX_EPS,
                        maxR + 1.0 + RANGE_BOX_EPS,
                        maxR + 1.0 + RANGE_BOX_EPS,
                        maxR + 1.0 + RANGE_BOX_EPS,
                        MAX_COLOR);
            }

            if (autoR > 0)
            {
                renderLineBox(pose, lineVc,
                        -autoR - RANGE_BOX_EPS,
                        -autoR - RANGE_BOX_EPS,
                        -autoR - RANGE_BOX_EPS,
                        autoR + 1.0 + RANGE_BOX_EPS,
                        autoR + 1.0 + RANGE_BOX_EPS,
                        autoR + 1.0 + RANGE_BOX_EPS,
                        AUTO_COLOR);
            }
        });
    }

    private static void renderLineBox(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            int color
    )
    {
        line(pose, consumer, minX, minY, minZ, maxX, minY, minZ, color);
        line(pose, consumer, maxX, minY, minZ, maxX, minY, maxZ, color);
        line(pose, consumer, maxX, minY, maxZ, minX, minY, maxZ, color);
        line(pose, consumer, minX, minY, maxZ, minX, minY, minZ, color);

        line(pose, consumer, minX, maxY, minZ, maxX, maxY, minZ, color);
        line(pose, consumer, maxX, maxY, minZ, maxX, maxY, maxZ, color);
        line(pose, consumer, maxX, maxY, maxZ, minX, maxY, maxZ, color);
        line(pose, consumer, minX, maxY, maxZ, minX, maxY, minZ, color);

        line(pose, consumer, minX, minY, minZ, minX, maxY, minZ, color);
        line(pose, consumer, maxX, minY, minZ, maxX, maxY, minZ, color);
        line(pose, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, color);
        line(pose, consumer, minX, minY, maxZ, minX, maxY, maxZ, color);
    }

    private static void line(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            int color
    )
    {
        float dx = (float) (x2 - x1);
        float dy = (float) (y2 - y1);
        float dz = (float) (z2 - z1);
        float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0.0f)
        {
            dx /= length;
            dy /= length;
            dz /= length;
        }

        consumer.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(1.0f);
        consumer.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(1.0f);
    }

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
        vc.addVertex(pose, p.x(), p.y(), p.z())
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
    }

    private static void applyAe2Orientation(BlockState state, PoseStack poseStack)
    {
        IOrientationStrategy strategy = IOrientationStrategy.get(state);
        Direction facing = strategy.getFacing(state);
        int spin = Mth.positiveModulo(strategy.getSpin(state), 4);

        poseStack.translate(0.5, 0.5, 0.5);
        rotateNorthToFacing(facing, poseStack);

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

    public static final class State extends BlockEntityRenderState
    {
        private BlockState blockState;
        private BlockStateModelPart topModel;
        private boolean active;
        private float angleDeg;
        private boolean renderLinkStatus;
        private int linkDistance;
        private int maxLinkDistance;
        private final Vector3f cameraLocal = new Vector3f();
        private List<BlockPos> pendingTargets = List.of();
        private List<BlockPos> linkedTargets = List.of();

        private void reset()
        {
            blockState = null;
            topModel = null;
            active = false;
            angleDeg = 0.0f;
            renderLinkStatus = false;
            linkDistance = 0;
            maxLinkDistance = 0;
            cameraLocal.set(0.0f, 0.0f, 0.0f);
            pendingTargets = List.of();
            linkedTargets = List.of();
        }
    }
}
