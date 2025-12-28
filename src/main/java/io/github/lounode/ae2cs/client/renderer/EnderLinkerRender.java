package io.github.lounode.ae2cs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.item.EnderLinkerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

import java.util.List;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class EnderLinkerRender
{
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");

    // 玩家离 emitter 多近时显示（世界距离）
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

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        PoseStack poseStack = event.getPoseStack();

        // 仅当玩家手持末影连接器时渲染
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        GlobalPos targetPos = null;
        if (main.getItem() instanceof EnderLinkerItem)
        {
            targetPos = main.get(AECSDataComponents.ENDER_EMITTER_POS);
        }
        else if (off.getItem() instanceof EnderLinkerItem)
        {
            targetPos = off.get(AECSDataComponents.ENDER_EMITTER_POS);
        }
        if (targetPos == null) return;

        Level level = player.level();
        if (level.dimension() != targetPos.dimension()) return;

        if (!(level.getBlockEntity(targetPos.pos()) instanceof EnderEmitterBlockEntity emitter)) return;

        // 距离裁剪
        Vec3 playerEye = player.getEyePosition();
        Vec3 emitterCenter = Vec3.atCenterOf(targetPos.pos());
        if (playerEye.distanceToSqr(emitterCenter) > SHOW_RANGE_SQR) return;

        // 渲染准备
        var camera = event.getCamera();
        Vec3 camPos = camera.getPosition();

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        // 把世界坐标平移到相机原点
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        int packedLight = LightTexture.FULL_BRIGHT;
        int packedOverlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        // 先画范围线框
        renderChebyshevRanges(poseStack, buffer, emitter, targetPos.pos());

        // 再获取 beam 的 consumer 并绘制（避免 Not building）
        VertexConsumer beamVc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));

        PoseStack.Pose pose = poseStack.last();

        Vector3f startW = new Vector3f((float) emitterCenter.x, (float) emitterCenter.y, (float) emitterCenter.z);
        Vector3f camW = new Vector3f((float) camPos.x, (float) camPos.y, (float) camPos.z);

        // pending（淡红）
        List<?> pending = emitter.getPendingRenderPositionsSnapshot();
        int c1 = 0;
        for (Object o : pending)
        {
            if (++c1 > MAX_BEAMS_PER_SET) break;
            BlockPos target = (BlockPos) o;
            if (!level.isLoaded(target)) continue;

            Vec3 targetCenter = Vec3.atCenterOf(target);
            Vector3f endW = new Vector3f((float) targetCenter.x, (float) targetCenter.y, (float) targetCenter.z);

            drawBeamCrossWorld(pose, beamVc, startW, endW, camW, PENDING_WIDTH,
                    1.00f, 0.35f, 0.35f, 0.35f,
                    packedLight, packedOverlay);
        }

        // linked（深紫）
        List<?> linked = emitter.getLinkedRenderPositionsSnapshot();
        int c2 = 0;
        for (Object o : linked)
        {
            if (++c2 > MAX_BEAMS_PER_SET) break;
            BlockPos target = (BlockPos) o;
            if (!level.isLoaded(target)) continue;

            Vec3 targetCenter = Vec3.atCenterOf(target);
            Vector3f endW = new Vector3f((float) targetCenter.x, (float) targetCenter.y, (float) targetCenter.z);

            drawBeamCrossWorld(pose, beamVc, startW, endW, camW, LINKED_WIDTH,
                    0.55f, 0.05f, 1.00f, 0.80f,
                    packedLight, packedOverlay);
        }

        poseStack.popPose();

        buffer.endBatch(RenderType.lines());
        buffer.endBatch(RenderType.entityTranslucent(WHITE_TEX));
    }

    /**
     * 渲染切比雪夫半径范围：两个线框立方体
     */
    private static void renderChebyshevRanges(PoseStack poseStack,
                                              MultiBufferSource.BufferSource buffer,
                                              EnderEmitterBlockEntity emitter,
                                              BlockPos emitterPos)
    {
        int autoR = Mth.clamp(emitter.getLinkDistance(), 0, 512);
        int maxR = Mth.clamp(emitter.getMaxLinkDistanceForClient(), 0, 512);
        if (maxR < autoR) maxR = autoR;

        VertexConsumer lineVc = buffer.getBuffer(RenderType.lines());

        // 外层（最大范围）
        if (maxR > 0)
        {
            AABB maxBox = new AABB(emitterPos).inflate(maxR).inflate(RANGE_BOX_EPS);
            LevelRenderer.renderLineBox(poseStack, lineVc, maxBox, MAX_R, MAX_G, MAX_B, RANGE_LINE_ALPHA);
        }

        // 内层（自动范围）
        if (autoR > 0)
        {
            AABB autoBox = new AABB(emitterPos).inflate(autoR).inflate(RANGE_BOX_EPS);
            LevelRenderer.renderLineBox(poseStack, lineVc, autoBox, AUTO_R, AUTO_G, AUTO_B, RANGE_LINE_ALPHA);
        }
    }

    // 光束绘制
    private static void drawBeamCrossWorld(PoseStack.Pose pose, VertexConsumer vc,
                                           Vector3f startW, Vector3f endW, Vector3f camW,
                                           float width,
                                           float r, float g, float b, float a,
                                           int packedLight, int packedOverlay)
    {
        Vector3f dir = new Vector3f(endW).sub(startW);
        if (dir.lengthSquared() < 1.0e-6f) return;
        dir.normalize();

        Vector3f mid = new Vector3f(startW).add(endW).mul(0.5f);
        Vector3f toCam = new Vector3f(camW).sub(mid);
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

        drawBeamQuadWorld(pose, vc, startW, endW, side1, r, g, b, a, packedLight, packedOverlay);
        if (side2.lengthSquared() > 1.0e-8f)
            drawBeamQuadWorld(pose, vc, startW, endW, side2, r, g, b, a, packedLight, packedOverlay);
    }

    private static void drawBeamQuadWorld(PoseStack.Pose pose, VertexConsumer vc,
                                          Vector3f startW, Vector3f endW, Vector3f side,
                                          float r, float g, float b, float a,
                                          int packedLight, int packedOverlay)
    {
        Vector3f s1 = new Vector3f(startW).add(side);
        Vector3f s2 = new Vector3f(startW).sub(side);
        Vector3f e1 = new Vector3f(endW).add(side);
        Vector3f e2 = new Vector3f(endW).sub(side);

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
}