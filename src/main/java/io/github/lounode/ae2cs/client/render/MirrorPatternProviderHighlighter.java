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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public final class MirrorPatternProviderHighlighter {

    private static final int RED = 0;
    private static final int GREEN = 180;
    private static final int BLUE = 255;
    private static final int ALPHA = 90;
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

        MirroredPatternProviderTarget target = getHeldTarget(player);
        if (target == null || !level.dimension().equals(target.pos().dimension())) {
            return;
        }

        List<HighlightTarget> highlights = getHighlights(level, player, target, minecraft.options.renderDistance().get());
        if (highlights.isEmpty()) {
            return;
        }

        var cameraPosition = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_FACE);

        for (HighlightTarget highlight : highlights) {
            poseStack.pushPose();
            BlockPos pos = highlight.pos();
            poseStack.translate(pos.getX() - cameraPosition.x, pos.getY() - cameraPosition.y, pos.getZ() - cameraPosition.z);
            if (highlight.side() == null) {
                for (Direction direction : Direction.values()) {
                    drawFaceQuad(poseStack, consumer, direction);
                }
            } else {
                drawFaceQuad(poseStack, consumer, highlight.side());
            }
            poseStack.popPose();
        }

        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_FACE);
    }

    private static @Nullable MirroredPatternProviderTarget getHeldTarget(LocalPlayer player) {
        MirroredPatternProviderTarget mainHandTarget = getTarget(player.getMainHandItem());
        return mainHandTarget != null ? mainHandTarget : getTarget(player.getOffhandItem());
    }

    private static @Nullable MirroredPatternProviderTarget getTarget(ItemStack stack) {
        if (!(stack.getItem() instanceof MirrorLinkerItem)) {
            return null;
        }
        return SimplePatternProviderMirrorHelper.getTarget(stack);
    }

    private static List<HighlightTarget> getHighlights(ClientLevel level, LocalPlayer player,
                                                       MirroredPatternProviderTarget target, int renderDistance) {
        ChunkPos center = new ChunkPos(player.blockPosition());
        HighlightCache previous = cache;
        long gameTime = level.getGameTime();
        if (previous != null && previous.matches(level, target, center, renderDistance, gameTime)) {
            return previous.highlights();
        }

        List<HighlightTarget> highlights = new ArrayList<>();
        for (int chunkX = center.x - renderDistance; chunkX <= center.x + renderDistance; chunkX++) {
            for (int chunkZ = center.z - renderDistance; chunkZ <= center.z + renderDistance; chunkZ++) {
                var chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    collectHighlights(blockEntity, target, highlights);
                }
            }
        }

        cache = new HighlightCache(level, target, center, renderDistance, gameTime, List.copyOf(highlights));
        return highlights;
    }

    private static void collectHighlights(BlockEntity blockEntity, MirroredPatternProviderTarget target,
                                          List<HighlightTarget> highlights) {
        BlockPos pos = blockEntity.getBlockPos();
        if (blockEntity instanceof MirrorPatternProviderHost mirrorHost && target.equals(mirrorHost.getMirroringLogic().getMirrorTarget())) {
            highlights.add(new HighlightTarget(pos, null));
        }

        if (!(blockEntity instanceof IPartHost partHost)) {
            return;
        }

        for (Direction side : Direction.values()) {
            if (partHost.getPart(side) instanceof MirrorPatternProviderHost mirrorHost && target.equals(mirrorHost.getMirroringLogic().getMirrorTarget())) {
                highlights.add(new HighlightTarget(pos, side));
            }
        }
    }

    private static void drawFaceQuad(PoseStack poseStack, VertexConsumer consumer, Direction face) {
        float x0 = 0F;
        float x1 = 1F;
        float y0 = 0F;
        float y1 = 1F;
        float z0 = 0F;
        float z1 = 1F;
        Matrix4f matrix = poseStack.last().pose();

        switch (face) {
            case NORTH -> addQuad(consumer, matrix, x0, y0, z0 - EPSILON, x1, y0, z0 - EPSILON, x1, y1, z0 - EPSILON, x0, y1, z0 - EPSILON);
            case SOUTH -> addQuad(consumer, matrix, x1, y0, z1 + EPSILON, x0, y0, z1 + EPSILON, x0, y1, z1 + EPSILON, x1, y1, z1 + EPSILON);
            case WEST -> addQuad(consumer, matrix, x0 - EPSILON, y0, z1, x0 - EPSILON, y0, z0, x0 - EPSILON, y1, z0, x0 - EPSILON, y1, z1);
            case EAST -> addQuad(consumer, matrix, x1 + EPSILON, y0, z0, x1 + EPSILON, y0, z1, x1 + EPSILON, y1, z1, x1 + EPSILON, y1, z0);
            case DOWN -> addQuad(consumer, matrix, x0, y0 - EPSILON, z0, x1, y0 - EPSILON, z0, x1, y0 - EPSILON, z1, x0, y0 - EPSILON, z1);
            case UP -> addQuad(consumer, matrix, x0, y1 + EPSILON, z1, x1, y1 + EPSILON, z1, x1, y1 + EPSILON, z0, x0, y1 + EPSILON, z0);
        }
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix,
                                float x0, float y0, float z0, float x1, float y1, float z1,
                                float x2, float y2, float z2, float x3, float y3, float z3) {
        int color = FastColor.ARGB32.color(ALPHA, RED, GREEN, BLUE);
        consumer.addVertex(matrix, x0, y0, z0).setColor(color);
        consumer.addVertex(matrix, x1, y1, z1).setColor(color);
        consumer.addVertex(matrix, x2, y2, z2).setColor(color);
        consumer.addVertex(matrix, x3, y3, z3).setColor(color);
    }

    private record HighlightTarget(BlockPos pos, @Nullable Direction side) {}

    private record HighlightCache(ClientLevel level, MirroredPatternProviderTarget target, ChunkPos center,
                                  int renderDistance, long gameTime, List<HighlightTarget> highlights) {

        private boolean matches(ClientLevel level, MirroredPatternProviderTarget target, ChunkPos center,
                                int renderDistance, long gameTime) {
            return this.level == level && this.target.equals(target) && this.center.equals(center) && this.renderDistance == renderDistance && this.gameTime == gameTime;
        }
    }
}
