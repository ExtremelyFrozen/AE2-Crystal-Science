package io.github.lounode.ae2cs.client.render;

import appeng.api.parts.IPartHost;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.init.client.AECSRenderTypes;
import io.github.lounode.ae2cs.common.item.IResonatingTargetModeItem;
import io.github.lounode.ae2cs.common.item.MirrorLinkerItem;
import io.github.lounode.ae2cs.common.item.MirrorPatternProviderBlockItem;
import io.github.lounode.ae2cs.common.item.MirrorPatternProviderPartItem;
import io.github.lounode.ae2cs.common.item.ResonatingLinkerItem;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingProviderDefaults;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MirroredPatternProviderTarget;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;

@Mod.EventBusSubscriber(modid = AECSConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ResonatingPatternTargetHighlighter
{
    private static final int SEL_R = 0;
    private static final int SEL_G = 255;
    private static final int SEL_B = 0;
    private static final int UNS_R = 0;
    private static final int UNS_G = 80;
    private static final int UNS_B = 255;
    private static final int MIRROR_R = 255;
    private static final int MIRROR_G = 170;
    private static final int MIRROR_B = 40;
    private static final int MIRROR_BLOCK_TARGET_R = 0;
    private static final int MIRROR_BLOCK_TARGET_G = 216;
    private static final int MIRROR_BLOCK_TARGET_B = 151;
    private static final int FACE_A = 90;
    private static final int CUBE_A = 128;
    private static final int LINE_A = 180;
    private static final float EPS = 0.002f;
    private static final int SEARCH_RADIUS = 24;

    private ResonatingPatternTargetHighlighter()
    {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event)
    {
        if (event.getStage() != AFTER_TRANSLUCENT_BLOCKS)
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
        {
            return;
        }

        if (isHoldingResonatingLinker(player))
        {
            renderNearbyResonatingProviders(event, player);
        }

        ItemStack heldTargetModeItem = getHeldTargetModeItem(player);
        if (!heldTargetModeItem.isEmpty())
        {
            renderHeldItemTargets(event, player, heldTargetModeItem);
        }

        if (isHoldingMirrorVisualizer(player))
        {
            renderNearbyMirrorProviders(event, player);
        }
    }

    private static boolean isHoldingResonatingLinker(LocalPlayer player)
    {
        return player.getMainHandItem().getItem() instanceof ResonatingLinkerItem
                || player.getOffhandItem().getItem() instanceof ResonatingLinkerItem;
    }

    private static ItemStack getHeldTargetModeItem(LocalPlayer player)
    {
        ItemStack main = player.getMainHandItem();
        if (supportsResonatingTargetRender(main))
        {
            return main;
        }

        ItemStack off = player.getOffhandItem();
        if (supportsResonatingTargetRender(off))
        {
            return off;
        }

        return ItemStack.EMPTY;
    }

    private static boolean supportsResonatingTargetRender(ItemStack stack)
    {
        return AECSDataComponents.getEncodedResonatingPattern(stack) != null
                || stack.getItem() instanceof IResonatingTargetModeItem;
    }

    private static void renderHeldItemTargets(RenderLevelStageEvent event, LocalPlayer player, ItemStack stack)
    {
        TargetRenderData renderData = getRenderData(stack);
        if (renderData == null || renderData.targets().isEmpty())
        {
            return;
        }

        renderTargets(event, player, renderData);
    }

    private static boolean isHoldingMirrorVisualizer(LocalPlayer player)
    {
        return supportsMirrorRender(player.getMainHandItem()) || supportsMirrorRender(player.getOffhandItem());
    }

    private static boolean supportsMirrorRender(ItemStack stack)
    {
        return stack.getItem() instanceof MirrorLinkerItem
                || stack.getItem() instanceof MirrorPatternProviderBlockItem
                || stack.getItem() instanceof MirrorPatternProviderPartItem
                || stack.is(AECSBlocks.MIRROR_PATTERN_PROVIDER_BLOCK.get().asItem())
                || stack.is(AECSParts.MIRROR_PATTERN_PROVIDER_PART.get());
    }

    private static void renderNearbyResonatingProviders(RenderLevelStageEvent event, LocalPlayer player)
    {
        for (ResonatingPatternProviderHost provider : collectNearbyResonatingProviders(player.level(), player.blockPosition(), SEARCH_RADIUS))
        {
            renderResonatingTargets(event, player, provider);
        }
    }

    private static void renderNearbyMirrorProviders(RenderLevelStageEvent event, LocalPlayer player)
    {
        Minecraft mc = Minecraft.getInstance();
        Level level = player.level();
        var camPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer faceVc = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_FACE);

        record MirrorLine(Vector3f start, Vector3f end) {}
        List<MirrorLine> lines = new ArrayList<>();
        List<BlockPos> cubes = new ArrayList<>();
        List<BlockPos> blockTargetCubes = new ArrayList<>();

        for (MirrorPatternProviderHost provider : collectNearbyMirrorProviders(level, player.blockPosition(), SEARCH_RADIUS))
        {
            MirroredPatternProviderTarget target = provider.getMirroringLogic().getMirrorTarget();
            if (target == null || !level.dimension().equals(target.pos().dimension()))
            {
                continue;
            }

            BlockPos targetPos = target.pos().pos();
            if (!level.hasChunkAt(targetPos))
            {
                continue;
            }

            Direction targetFace = target.side();
            if (targetFace == null)
            {
                blockTargetCubes.add(targetPos);
            }
            else
            {
                poseStack.pushPose();
                poseStack.translate(targetPos.getX() - camPos.x, targetPos.getY() - camPos.y, targetPos.getZ() - camPos.z);
                drawFaceQuad(poseStack, faceVc, targetFace, EPS, MIRROR_R, MIRROR_G, MIRROR_B, FACE_A);
                poseStack.popPose();
            }

            BlockPos sourcePos = provider.getBlockEntity().getBlockPos();
            cubes.add(sourcePos);

            lines.add(new MirrorLine(
                    anchorOf(sourcePos, null),
                    anchorOf(targetPos, targetFace)
            ));
        }

        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_FACE);

        VertexConsumer cubeVc = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_SEE_THROUGH);
        for (BlockPos cubePos : cubes)
        {
            poseStack.pushPose();
            poseStack.translate(cubePos.getX() - camPos.x, cubePos.getY() - camPos.y, cubePos.getZ() - camPos.z);
            drawInnerCube(poseStack, cubeVc, MIRROR_R, MIRROR_G, MIRROR_B, CUBE_A);
            poseStack.popPose();
        }
        for (BlockPos cubePos : blockTargetCubes)
        {
            poseStack.pushPose();
            poseStack.translate(cubePos.getX() - camPos.x, cubePos.getY() - camPos.y, cubePos.getZ() - camPos.z);
            drawInnerCube(poseStack, cubeVc, MIRROR_BLOCK_TARGET_R, MIRROR_BLOCK_TARGET_G, MIRROR_BLOCK_TARGET_B, CUBE_A);
            poseStack.popPose();
        }
        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_SEE_THROUGH);

        VertexConsumer lineVc = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_LINE);
        for (MirrorLine line : lines)
        {
            drawLine(poseStack, lineVc, camPos, line.start(), line.end(), MIRROR_R, MIRROR_G, MIRROR_B, LINE_A);
        }
        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_LINE);
    }

    private static void renderResonatingTargets(RenderLevelStageEvent event, LocalPlayer player, ResonatingPatternProviderHost provider)
    {
        Minecraft mc = Minecraft.getInstance();
        Level level = player.level();
        var camPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer faceVc = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_FACE);

        record ResonatingLine(Vector3f start, Vector3f end, int r, int g, int b) {}
        List<ResonatingLine> lines = new ArrayList<>();

        List<Optional<EncodedResonatingPattern.Target>> targets = provider.getDefaultInputTargets();
        BlockPos sourcePos = provider.getBlockEntity().getBlockPos();
        Vector3f sourceAnchor = anchorOf(sourcePos, null);
        boolean hasRenderableTarget = false;

        for (int i = 0; i < targets.size(); i++)
        {
            Optional<EncodedResonatingPattern.Target> opt = targets.get(i);
            if (opt.isEmpty())
            {
                continue;
            }

            EncodedResonatingPattern.Target target = opt.get();
            if (!level.dimension().equals(target.pos().dimension()))
            {
                continue;
            }

            BlockPos targetPos = target.pos().pos();
            if (!level.hasChunkAt(targetPos))
            {
                continue;
            }

            int r = UNS_R;
            int g = UNS_G;
            int b = UNS_B;
            hasRenderableTarget = true;

            poseStack.pushPose();
            poseStack.translate(targetPos.getX() - camPos.x, targetPos.getY() - camPos.y, targetPos.getZ() - camPos.z);
            drawFaceQuad(poseStack, faceVc, target.face(), EPS, r, g, b, FACE_A);
            poseStack.popPose();

            lines.add(new ResonatingLine(sourceAnchor, anchorOf(targetPos, target.face()), r, g, b));
        }

        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_FACE);

        if (hasRenderableTarget)
        {
            VertexConsumer cubeVc = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_SEE_THROUGH);
            poseStack.pushPose();
            poseStack.translate(sourcePos.getX() - camPos.x, sourcePos.getY() - camPos.y, sourcePos.getZ() - camPos.z);
            drawInnerCube(poseStack, cubeVc, UNS_R, UNS_G, UNS_B, CUBE_A);
            poseStack.popPose();
            bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_SEE_THROUGH);
        }

        VertexConsumer lineVc = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_LINE);
        for (ResonatingLine line : lines)
        {
            drawLine(poseStack, lineVc, camPos, line.start(), line.end(), line.r(), line.g(), line.b(), LINE_A);
        }
        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_LINE);
    }

    private static void renderTargets(RenderLevelStageEvent event, LocalPlayer player, TargetRenderData renderData)
    {
        Minecraft mc = Minecraft.getInstance();
        Level level = player.level();
        var camPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer faceVc = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_FACE);

        for (int i = 0; i < renderData.targets().size(); i++)
        {
            var opt = renderData.targets().get(i);
            if (opt.isEmpty())
            {
                continue;
            }

            EncodedResonatingPattern.Target target = opt.get();
            if (!level.dimension().equals(target.pos().dimension()))
            {
                continue;
            }

            BlockPos pos = target.pos().pos();
            if (!level.hasChunkAt(pos))
            {
                continue;
            }

            boolean selected = i == renderData.selected();
            poseStack.pushPose();
            poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
            drawFaceQuad(poseStack, faceVc, target.face(), EPS,
                    selected ? SEL_R : UNS_R,
                    selected ? SEL_G : UNS_G,
                    selected ? SEL_B : UNS_B,
                    FACE_A);
            poseStack.popPose();
        }

        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_FACE);
    }

    private static TargetRenderData getRenderData(ItemStack stack)
    {
        EncodedResonatingPattern encoded = AECSDataComponents.getEncodedResonatingPattern(stack);
        if (encoded != null)
        {
            int size = encoded.sparseInputs().size();
            if (size <= 0)
            {
                return null;
            }

            int selected = AECSDataComponents.getResonatingPatternSelectedInput(stack, 0);
            selected = ResonatingPatternDetails.clampSelected(selected, size);

            List<Optional<EncodedResonatingPattern.Target>> targets = new ArrayList<>(size);
            for (int i = 0; i < size; i++)
            {
                targets.add(encoded.targetOfSparseInput(i));
            }
            return new TargetRenderData(targets, selected);
        }

        if (stack.getItem() instanceof IResonatingTargetModeItem)
        {
            return new TargetRenderData(
                    ResonatingProviderDefaults.readTargets(stack),
                    ResonatingProviderDefaults.getSelectedInput(stack)
            );
        }

        return null;
    }

    private static List<ResonatingPatternProviderHost> collectNearbyResonatingProviders(Level level, BlockPos center, int radius)
    {
        List<ResonatingPatternProviderHost> result = new ArrayList<>();
        forEachNearbyProviderBlockEntity(level, center, radius, be -> {
            if (be instanceof ResonatingPatternProviderHost host)
            {
                result.add(host);
            }
            if (be instanceof IPartHost partHost)
            {
                for (Direction side : Direction.values())
                {
                    var part = partHost.getPart(side);
                    if (part instanceof ResonatingPatternProviderHost host)
                    {
                        result.add(host);
                    }
                }
            }
        });
        return result;
    }

    private static List<MirrorPatternProviderHost> collectNearbyMirrorProviders(Level level, BlockPos center, int radius)
    {
        Set<MirrorPatternProviderHost> result = new LinkedHashSet<>();
        forEachNearbyProviderBlockEntity(level, center, radius, be -> {
            if (be instanceof MirrorPatternProviderHost host)
            {
                result.add(host);
            }
            if (be instanceof IPartHost partHost)
            {
                for (Direction side : Direction.values())
                {
                    var part = partHost.getPart(side);
                    if (part instanceof MirrorPatternProviderHost host)
                    {
                        result.add(host);
                    }
                }
            }
        });
        return new ArrayList<>(result);
    }

    private static void forEachNearbyProviderBlockEntity(Level level, BlockPos center, int radius, Consumer<BlockEntity> consumer)
    {
        int minChunkX = (center.getX() - radius) >> 4;
        int maxChunkX = (center.getX() + radius) >> 4;
        int minChunkZ = (center.getZ() - radius) >> 4;
        int maxChunkZ = (center.getZ() + radius) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
        {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
            {
                var chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
                if (chunk == null)
                {
                    continue;
                }

                for (BlockEntity be : chunk.getBlockEntities().values())
                {
                    if (be != null && be.getBlockPos().closerThan(center, radius))
                    {
                        consumer.accept(be);
                    }
                }
            }
        }
    }

    private static Vector3f anchorOf(BlockPos pos, Direction face)
    {
        float x = pos.getX() + 0.5f;
        float y = pos.getY() + 0.5f;
        float z = pos.getZ() + 0.5f;
        if (face != null)
        {
            x += face.getStepX() * 0.501f;
            y += face.getStepY() * 0.501f;
            z += face.getStepZ() * 0.501f;
        }
        return new Vector3f(x, y, z);
    }

    private static void drawLine(PoseStack poseStack, VertexConsumer vc, net.minecraft.world.phys.Vec3 camPos,
                                 Vector3f start, Vector3f end, int r, int g, int b, int a)
    {
        var pose = poseStack.last();
        Matrix4f mat = pose.pose();
        Matrix3f normalMat = pose.normal();
        float sx = start.x - (float) camPos.x;
        float sy = start.y - (float) camPos.y;
        float sz = start.z - (float) camPos.z;
        float ex = end.x - (float) camPos.x;
        float ey = end.y - (float) camPos.y;
        float ez = end.z - (float) camPos.z;

        Vector3f normal = new Vector3f(ex - sx, ey - sy, ez - sz);
        if (normal.lengthSquared() > 0.0f)
        {
            normal.normalize();
        }
        else
        {
            normal.set(0, 1, 0);
        }

        vc.vertex(mat, sx, sy, sz).color(r, g, b, a).normal(normalMat, normal.x, normal.y, normal.z).endVertex();
        vc.vertex(mat, ex, ey, ez).color(r, g, b, a).normal(normalMat, normal.x, normal.y, normal.z).endVertex();
    }

    private static void drawFaceQuad(PoseStack poseStack, VertexConsumer vc, Direction face, float eps,
                                      int r, int g, int b, int a)
    {
        float x0 = 0f, x1 = 1f;
        float y0 = 0f, y1 = 1f;
        float z0 = 0f, z1 = 1f;

        var pose = poseStack.last();
        Matrix4f mat = pose.pose();
        Matrix3f normalMat = pose.normal();
        Vector3f normal = new Vector3f(face.getStepX(), face.getStepY(), face.getStepZ());

        switch (face)
        {
            case NORTH -> quadPosColor(vc, mat,
                    x0, y0, z0 - eps, x1, y0, z0 - eps, x1, y1, z0 - eps, x0, y1, z0 - eps,
                    r, g, b, a, normalMat, normal);
            case SOUTH -> quadPosColor(vc, mat,
                    x1, y0, z1 + eps, x0, y0, z1 + eps, x0, y1, z1 + eps, x1, y1, z1 + eps,
                    r, g, b, a, normalMat, normal);
            case WEST -> quadPosColor(vc, mat,
                    x0 - eps, y0, z1, x0 - eps, y0, z0, x0 - eps, y1, z0, x0 - eps, y1, z1,
                    r, g, b, a, normalMat, normal);
            case EAST -> quadPosColor(vc, mat,
                    x1 + eps, y0, z0, x1 + eps, y0, z1, x1 + eps, y1, z1, x1 + eps, y1, z0,
                    r, g, b, a, normalMat, normal);
            case DOWN -> quadPosColor(vc, mat,
                    x0, y0 - eps, z0, x1, y0 - eps, z0, x1, y0 - eps, z1, x0, y0 - eps, z1,
                    r, g, b, a, normalMat, normal);
            case UP -> quadPosColor(vc, mat,
                    x0, y1 + eps, z1, x1, y1 + eps, z1, x1, y1 + eps, z0, x0, y1 + eps, z0,
                    r, g, b, a, normalMat, normal);
        }
    }

    private static void drawInnerCube(PoseStack poseStack, VertexConsumer vc, int r, int g, int b, int a)
    {
        float lo = 0.25f;
        float hi = 0.75f;

        var pose = poseStack.last();
        Matrix4f mat = pose.pose();
        Matrix3f normalMat = pose.normal();

        quadPosColor(vc, mat,
                lo, lo, lo, hi, lo, lo, hi, lo, hi, lo, lo, hi,
                r, g, b, a, normalMat, new Vector3f(0, -1, 0));
        quadPosColor(vc, mat,
                lo, hi, hi, hi, hi, hi, hi, hi, lo, lo, hi, lo,
                r, g, b, a, normalMat, new Vector3f(0, 1, 0));
        quadPosColor(vc, mat,
                lo, lo, lo, lo, hi, lo, hi, hi, lo, hi, lo, lo,
                r, g, b, a, normalMat, new Vector3f(0, 0, -1));
        quadPosColor(vc, mat,
                hi, lo, hi, hi, hi, hi, lo, hi, hi, lo, lo, hi,
                r, g, b, a, normalMat, new Vector3f(0, 0, 1));
        quadPosColor(vc, mat,
                lo, lo, hi, lo, hi, hi, lo, hi, lo, lo, lo, lo,
                r, g, b, a, normalMat, new Vector3f(-1, 0, 0));
        quadPosColor(vc, mat,
                hi, lo, lo, hi, hi, lo, hi, hi, hi, hi, lo, hi,
                r, g, b, a, normalMat, new Vector3f(1, 0, 0));
    }

    private static void quadPosColor(VertexConsumer vc, Matrix4f mat,
                                     float x0, float y0, float z0,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float x3, float y3, float z3,
                                     int r, int g, int b, int a,
                                     Matrix3f normalMat, Vector3f normal)
    {
        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).normal(normalMat, normal.x, normal.y, normal.z).endVertex();
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(normalMat, normal.x, normal.y, normal.z).endVertex();
        vc.vertex(mat, x2, y2, z2).color(r, g, b, a).normal(normalMat, normal.x, normal.y, normal.z).endVertex();
        vc.vertex(mat, x3, y3, z3).color(r, g, b, a).normal(normalMat, normal.x, normal.y, normal.z).endVertex();
    }

    private record TargetRenderData(List<Optional<EncodedResonatingPattern.Target>> targets, int selected)
    {
    }
}
