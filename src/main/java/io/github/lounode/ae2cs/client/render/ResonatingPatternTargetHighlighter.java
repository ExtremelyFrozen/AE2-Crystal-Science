package io.github.lounode.ae2cs.client.render;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.init.client.AECSRenderTypes;
import io.github.lounode.ae2cs.common.item.IResonatingTargetModeItem;
import io.github.lounode.ae2cs.common.item.ResonatingLinkerItem;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingProviderDefaults;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;

import appeng.api.parts.IPartHost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public final class ResonatingPatternTargetHighlighter {

    private static final int SELECTED_RED = 0;
    private static final int SELECTED_GREEN = 255;
    private static final int SELECTED_BLUE = 0;
    private static final int UNSELECTED_RED = 0;
    private static final int UNSELECTED_GREEN = 80;
    private static final int UNSELECTED_BLUE = 255;
    private static final int APPLIED_RED = 255;
    private static final int APPLIED_GREEN = 170;
    private static final int APPLIED_BLUE = 40;
    private static final int ALPHA = 90;
    private static final int PROVIDER_SEARCH_RADIUS = 24;
    private static final float EPSILON = 0.002F;

    private ResonatingPatternTargetHighlighter() {}

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        TargetRenderData heldTargets = getHeldTargetRenderData(player);
        if (heldTargets != null) {
            renderTargets(event, player, heldTargets, UNSELECTED_RED, UNSELECTED_GREEN, UNSELECTED_BLUE);
        }

        if (isHoldingResonatingLinker(player)) {
            renderAppliedProviderTargets(event, player);
        }
    }

    private static @Nullable TargetRenderData getHeldTargetRenderData(LocalPlayer player) {
        TargetRenderData mainHand = getTargetRenderData(player.getMainHandItem());
        return mainHand != null ? mainHand : getTargetRenderData(player.getOffhandItem());
    }

    private static @Nullable TargetRenderData getTargetRenderData(ItemStack stack) {
        EncodedResonatingPattern encoded = stack.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get());
        if (encoded != null) {
            int size = encoded.sparseInputs().size();
            if (size == 0) {
                return null;
            }

            List<Optional<EncodedResonatingPattern.Target>> targets = new ArrayList<>(size);
            for (int index = 0; index < size; index++) {
                targets.add(encoded.targetOfSparseInput(index));
            }
            int selected = ResonatingPatternDetails.clampSelected(
                    stack.getOrDefault(AECSDataComponents.RESONATING_PATTERN_SELECTED_INPUT.get(), 0), size);
            return new TargetRenderData(targets, selected);
        }

        if (stack.getItem() instanceof IResonatingTargetModeItem) {
            return new TargetRenderData(
                    ResonatingProviderDefaults.readTargets(stack),
                    ResonatingProviderDefaults.getSelectedInput(stack));
        }

        return null;
    }

    private static boolean isHoldingResonatingLinker(LocalPlayer player) {
        return player.getMainHandItem().getItem() instanceof ResonatingLinkerItem || player.getOffhandItem().getItem() instanceof ResonatingLinkerItem;
    }

    private static void renderAppliedProviderTargets(RenderLevelStageEvent event, LocalPlayer player) {
        for (ResonatingPatternProviderHost provider : collectNearbyProviders(player.level(), player.blockPosition())) {
            renderTargets(event, player, new TargetRenderData(provider.getDefaultInputTargets(), -1), APPLIED_RED, APPLIED_GREEN, APPLIED_BLUE);
        }
    }

    private static List<ResonatingPatternProviderHost> collectNearbyProviders(Level level, BlockPos center) {
        List<ResonatingPatternProviderHost> providers = new ArrayList<>();
        int minChunkX = (center.getX() - PROVIDER_SEARCH_RADIUS) >> 4;
        int maxChunkX = (center.getX() + PROVIDER_SEARCH_RADIUS) >> 4;
        int minChunkZ = (center.getZ() - PROVIDER_SEARCH_RADIUS) >> 4;
        int maxChunkZ = (center.getZ() + PROVIDER_SEARCH_RADIUS) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                var chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
                if (chunk == null) {
                    continue;
                }

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!blockEntity.getBlockPos().closerThan(center, PROVIDER_SEARCH_RADIUS)) {
                        continue;
                    }

                    if (blockEntity instanceof ResonatingPatternProviderHost provider) {
                        providers.add(provider);
                    }
                    if (blockEntity instanceof IPartHost partHost) {
                        for (Direction side : Direction.values()) {
                            if (partHost.getPart(side) instanceof ResonatingPatternProviderHost provider) {
                                providers.add(provider);
                            }
                        }
                    }
                }
            }
        }

        return providers;
    }

    private static void renderTargets(RenderLevelStageEvent event, LocalPlayer player, TargetRenderData renderData,
                                      int unselectedRed, int unselectedGreen, int unselectedBlue) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = player.level();
        var cameraPosition = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_FACE);

        for (int index = 0; index < renderData.targets().size(); index++) {
            Optional<EncodedResonatingPattern.Target> optionalTarget = renderData.targets().get(index);
            if (optionalTarget.isEmpty()) {
                continue;
            }

            EncodedResonatingPattern.Target target = optionalTarget.get();
            if (!level.dimension().equals(target.pos().dimension())) {
                continue;
            }

            BlockPos pos = target.pos().pos();
            if (!level.hasChunkAt(pos)) {
                continue;
            }

            boolean selected = index == renderData.selected();
            poseStack.pushPose();
            poseStack.translate(pos.getX() - cameraPosition.x, pos.getY() - cameraPosition.y, pos.getZ() - cameraPosition.z);
            drawFaceQuad(poseStack, consumer, target.face(), selected ? SELECTED_RED : unselectedRed,
                    selected ? SELECTED_GREEN : unselectedGreen, selected ? SELECTED_BLUE : unselectedBlue);
            poseStack.popPose();
        }

        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_FACE);
    }

    private static void drawFaceQuad(PoseStack poseStack, VertexConsumer consumer, Direction face, int red, int green, int blue) {
        float x0 = 0F;
        float x1 = 1F;
        float y0 = 0F;
        float y1 = 1F;
        float z0 = 0F;
        float z1 = 1F;
        Matrix4f matrix = poseStack.last().pose();

        switch (face) {
            case NORTH -> addQuad(consumer, matrix, x0, y0, z0 - EPSILON, x1, y0, z0 - EPSILON, x1, y1, z0 - EPSILON, x0, y1, z0 - EPSILON, red, green, blue);
            case SOUTH -> addQuad(consumer, matrix, x1, y0, z1 + EPSILON, x0, y0, z1 + EPSILON, x0, y1, z1 + EPSILON, x1, y1, z1 + EPSILON, red, green, blue);
            case WEST -> addQuad(consumer, matrix, x0 - EPSILON, y0, z1, x0 - EPSILON, y0, z0, x0 - EPSILON, y1, z0, x0 - EPSILON, y1, z1, red, green, blue);
            case EAST -> addQuad(consumer, matrix, x1 + EPSILON, y0, z0, x1 + EPSILON, y0, z1, x1 + EPSILON, y1, z1, x1 + EPSILON, y1, z0, red, green, blue);
            case DOWN -> addQuad(consumer, matrix, x0, y0 - EPSILON, z0, x1, y0 - EPSILON, z0, x1, y0 - EPSILON, z1, x0, y0 - EPSILON, z1, red, green, blue);
            case UP -> addQuad(consumer, matrix, x0, y1 + EPSILON, z1, x1, y1 + EPSILON, z1, x1, y1 + EPSILON, z0, x0, y1 + EPSILON, z0, red, green, blue);
        }
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix,
                                float x0, float y0, float z0, float x1, float y1, float z1,
                                float x2, float y2, float z2, float x3, float y3, float z3,
                                int red, int green, int blue) {
        int color = FastColor.ARGB32.color(ALPHA, red, green, blue);
        consumer.addVertex(matrix, x0, y0, z0).setColor(color);
        consumer.addVertex(matrix, x1, y1, z1).setColor(color);
        consumer.addVertex(matrix, x2, y2, z2).setColor(color);
        consumer.addVertex(matrix, x3, y3, z3).setColor(color);
    }

    private record TargetRenderData(List<Optional<EncodedResonatingPattern.Target>> targets, int selected) {}
}
