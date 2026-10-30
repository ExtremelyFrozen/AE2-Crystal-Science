package io.github.lounode.ae2cs.client.render;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.client.AECSRenderTypes;
import io.github.lounode.ae2cs.common.item.MirrorLinkerItem;
import io.github.lounode.ae2cs.common.item.SimplePatternProviderMirrorHelper;
import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MirroredPatternProviderTarget;

import appeng.api.parts.IPartHost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public final class MirrorPatternProviderHighlighter {

    private static final int SOURCE_COLOR = FastColor.ARGB32.color(90, 0, 180, 255);
    private static final int SELECTED_MIRROR_COLOR = FastColor.ARGB32.color(110, 255, 215, 0);
    private static final int UNSELECTED_MIRROR_COLOR = FastColor.ARGB32.color(90, 0, 65, 180);
    private static final int SELECTED_LINE_COLOR = FastColor.ARGB32.color(255, 100, 220, 255);
    private static final int UNSELECTED_LINE_COLOR = FastColor.ARGB32.color(200, 0, 65, 180);
    private static final float EPSILON = 0.002F;

    private static @Nullable HighlightCache cache;

    private MirrorPatternProviderHighlighter() {}

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return;
        }

        ItemStack linker = getHeldLinker(player);
        if (linker.isEmpty()) {
            cache = null;
            return;
        }

        MirroredPatternProviderTarget target = SimplePatternProviderMirrorHelper.getTarget(linker);
        HighlightCache highlights = getHighlights(level, player, target, minecraft.options.renderDistance().get());
        if (highlights.highlights().isEmpty()) {
            return;
        }

        var cameraPosition = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_FACE);

        for (HighlightTarget highlight : highlights.highlights()) {
            int color = highlight.source() == null ? SOURCE_COLOR : highlight.source().equals(target) ? SELECTED_MIRROR_COLOR : UNSELECTED_MIRROR_COLOR;
            poseStack.pushPose();
            BlockPos pos = highlight.pos();
            poseStack.translate(pos.getX() - cameraPosition.x, pos.getY() - cameraPosition.y, pos.getZ() - cameraPosition.z);
            if (highlight.side() == null) {
                for (Direction direction : Direction.values()) {
                    drawFaceQuad(poseStack, consumer, direction, color);
                }
            } else {
                drawFaceQuad(poseStack, consumer, highlight.side(), color);
            }
            poseStack.popPose();
        }

        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_FACE);

        VertexConsumer lineConsumer = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_LINE);
        for (HighlightTarget highlight : highlights.highlights()) {
            MirroredPatternProviderTarget source = highlight.source();
            if (source == null || !highlights.visibleSources().contains(source)) {
                continue;
            }
            drawLine(poseStack, lineConsumer, cameraPosition, anchorOf(source.pos().pos(), source.side()),
                    anchorOf(highlight.pos(), highlight.side()), source.equals(target) ? SELECTED_LINE_COLOR : UNSELECTED_LINE_COLOR);
        }
        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_LINE);
    }

    private static ItemStack getHeldLinker(LocalPlayer player) {
        if (player.getMainHandItem().getItem() instanceof MirrorLinkerItem) {
            return player.getMainHandItem();
        }
        return player.getOffhandItem().getItem() instanceof MirrorLinkerItem ? player.getOffhandItem() : ItemStack.EMPTY;
    }

    private static HighlightCache getHighlights(ClientLevel level, LocalPlayer player,
                                                @Nullable MirroredPatternProviderTarget target, int renderDistance) {
        ChunkPos center = new ChunkPos(player.blockPosition());
        HighlightCache previous = cache;
        long gameTime = level.getGameTime();
        if (previous != null && previous.matches(level, target, center, renderDistance, gameTime)) {
            return previous;
        }

        List<HighlightTarget> highlights = new ArrayList<>();
        Set<MirroredPatternProviderTarget> sources = new LinkedHashSet<>();
        if (target != null && level.dimension().equals(target.pos().dimension())) {
            sources.add(target);
        }
        for (int chunkX = center.x - renderDistance; chunkX <= center.x + renderDistance; chunkX++) {
            for (int chunkZ = center.z - renderDistance; chunkZ <= center.z + renderDistance; chunkZ++) {
                var chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    collectHighlights(level, blockEntity, highlights, sources);
                }
            }
        }

        sources.removeIf(source -> !source.isPresent(level));
        for (MirroredPatternProviderTarget source : sources) {
            highlights.add(new HighlightTarget(source.pos().pos(), source.side(), null));
        }
        cache = new HighlightCache(level, target, center, renderDistance, gameTime, List.copyOf(highlights), Set.copyOf(sources));
        return cache;
    }

    private static void collectHighlights(ClientLevel level, BlockEntity blockEntity, List<HighlightTarget> highlights,
                                          Set<MirroredPatternProviderTarget> sources) {
        BlockPos pos = blockEntity.getBlockPos();
        if (blockEntity instanceof MirrorPatternProviderHost mirrorHost) {
            collectMirror(level, mirrorHost, pos, null, highlights, sources);
        }

        if (!(blockEntity instanceof IPartHost partHost)) {
            return;
        }

        for (Direction side : Direction.values()) {
            if (partHost.getPart(side) instanceof MirrorPatternProviderHost mirrorHost) {
                collectMirror(level, mirrorHost, pos, side, highlights, sources);
            }
        }
    }

    private static void collectMirror(ClientLevel level, MirrorPatternProviderHost host, BlockPos pos, @Nullable Direction side,
                                      List<HighlightTarget> highlights, Set<MirroredPatternProviderTarget> sources) {
        MirroredPatternProviderTarget source = host.getMirroringLogic().getMirrorTarget();
        if (source != null && level.dimension().equals(source.pos().dimension())) {
            highlights.add(new HighlightTarget(pos, side, source));
            sources.add(source);
        }
    }

    private static Vec3 anchorOf(BlockPos pos, @Nullable Direction side) {
        Vec3 center = Vec3.atCenterOf(pos);
        return side == null ? center : center.add(side.getStepX() * 0.501D, side.getStepY() * 0.501D, side.getStepZ() * 0.501D);
    }

    private static void drawLine(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, int color) {
        Vec3 delta = end.subtract(start);
        if (delta.lengthSqr() == 0D) {
            return;
        }
        Vec3 normal = delta.normalize();
        var pose = poseStack.last();
        consumer.addVertex(pose.pose(), (float) (start.x - camera.x), (float) (start.y - camera.y), (float) (start.z - camera.z))
                .setColor(color).setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        consumer.addVertex(pose.pose(), (float) (end.x - camera.x), (float) (end.y - camera.y), (float) (end.z - camera.z))
                .setColor(color).setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    private static void drawFaceQuad(PoseStack poseStack, VertexConsumer consumer, Direction face, int color) {
        float x0 = 0F;
        float x1 = 1F;
        float y0 = 0F;
        float y1 = 1F;
        float z0 = 0F;
        float z1 = 1F;
        Matrix4f matrix = poseStack.last().pose();

        switch (face) {
            case NORTH -> addQuad(consumer, matrix, x0, y0, z0 - EPSILON, x1, y0, z0 - EPSILON, x1, y1, z0 - EPSILON, x0, y1, z0 - EPSILON, color);
            case SOUTH -> addQuad(consumer, matrix, x1, y0, z1 + EPSILON, x0, y0, z1 + EPSILON, x0, y1, z1 + EPSILON, x1, y1, z1 + EPSILON, color);
            case WEST -> addQuad(consumer, matrix, x0 - EPSILON, y0, z1, x0 - EPSILON, y0, z0, x0 - EPSILON, y1, z0, x0 - EPSILON, y1, z1, color);
            case EAST -> addQuad(consumer, matrix, x1 + EPSILON, y0, z0, x1 + EPSILON, y0, z1, x1 + EPSILON, y1, z1, x1 + EPSILON, y1, z0, color);
            case DOWN -> addQuad(consumer, matrix, x0, y0 - EPSILON, z0, x1, y0 - EPSILON, z0, x1, y0 - EPSILON, z1, x0, y0 - EPSILON, z1, color);
            case UP -> addQuad(consumer, matrix, x0, y1 + EPSILON, z1, x1, y1 + EPSILON, z1, x1, y1 + EPSILON, z0, x0, y1 + EPSILON, z0, color);
        }
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix,
                                float x0, float y0, float z0, float x1, float y1, float z1,
                                float x2, float y2, float z2, float x3, float y3, float z3, int color) {
        consumer.addVertex(matrix, x0, y0, z0).setColor(color);
        consumer.addVertex(matrix, x1, y1, z1).setColor(color);
        consumer.addVertex(matrix, x2, y2, z2).setColor(color);
        consumer.addVertex(matrix, x3, y3, z3).setColor(color);
    }

    private record HighlightTarget(BlockPos pos, @Nullable Direction side, @Nullable MirroredPatternProviderTarget source) {}

    private record HighlightCache(ClientLevel level, @Nullable MirroredPatternProviderTarget target, ChunkPos center,
                                  int renderDistance, long gameTime, List<HighlightTarget> highlights,
                                  Set<MirroredPatternProviderTarget> visibleSources) {

        private boolean matches(ClientLevel level, @Nullable MirroredPatternProviderTarget target, ChunkPos center,
                                int renderDistance, long gameTime) {
            return this.level == level && Objects.equals(this.target, target) && this.center.equals(center) && this.renderDistance == renderDistance && this.gameTime == gameTime;
        }
    }
}
