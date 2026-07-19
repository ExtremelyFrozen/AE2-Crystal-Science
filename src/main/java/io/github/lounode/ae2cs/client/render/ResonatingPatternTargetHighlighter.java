package io.github.lounode.ae2cs.client.render;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.init.client.AECSRenderTypes;
import io.github.lounode.ae2cs.common.item.IResonatingTargetModeItem;
import io.github.lounode.ae2cs.common.item.ResonatingLinkerItem;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingProviderDefaults;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderReference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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
    private static final int ALPHA = 90;
    private static final int LINE_ALPHA = 180;
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
            renderBoundProvider(event, player);
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

        if (stack.getItem() instanceof IResonatingTargetModeItem && !(stack.getItem() instanceof ResonatingLinkerItem)) {
            return new TargetRenderData(
                    ResonatingProviderDefaults.readTargets(stack),
                    ResonatingProviderDefaults.getSelectedInput(stack));
        }

        return null;
    }

    private static boolean isHoldingResonatingLinker(LocalPlayer player) {
        return player.getMainHandItem().getItem() instanceof ResonatingLinkerItem || player.getOffhandItem().getItem() instanceof ResonatingLinkerItem;
    }

    private static void renderBoundProvider(RenderLevelStageEvent event, LocalPlayer player) {
        ItemStack linker = player.getMainHandItem().getItem() instanceof ResonatingLinkerItem ? player.getMainHandItem() : player.getOffhandItem();
        ResonatingPatternProviderReference reference = linker.get(AECSDataComponents.RESONATING_LINKER_PROVIDER.get());
        ResonatingProviderDefaults.Defaults renderSnapshot = linker.get(AECSDataComponents.RESONATING_LINKER_RENDER_DATA.get());
        if (reference == null || renderSnapshot == null || !player.level().dimension().equals(reference.pos().dimension())) {
            return;
        }

        TargetRenderData renderData = new TargetRenderData(renderSnapshot.targets(), renderSnapshot.selectedInput());
        renderTargets(event, player, renderData, UNSELECTED_RED, UNSELECTED_GREEN, UNSELECTED_BLUE);
        renderProviderSourceAndLines(event, player, reference.pos().pos(), renderData);
    }

    private static void renderProviderSourceAndLines(RenderLevelStageEvent event, LocalPlayer player,
                                                     BlockPos sourcePos, TargetRenderData renderData) {
        var cameraPosition = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer sourceConsumer = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_FACE);
        poseStack.pushPose();
        poseStack.translate(sourcePos.getX() - cameraPosition.x, sourcePos.getY() - cameraPosition.y, sourcePos.getZ() - cameraPosition.z);
        for (Direction direction : Direction.values()) {
            drawFaceQuad(poseStack, sourceConsumer, direction, UNSELECTED_RED, UNSELECTED_GREEN, UNSELECTED_BLUE);
        }
        poseStack.popPose();
        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_FACE);

        VertexConsumer lineConsumer = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_LINE);
        Vector3f source = anchorOf(sourcePos, null);
        for (int index = 0; index < renderData.targets().size(); index++) {
            Optional<EncodedResonatingPattern.Target> optionalTarget = renderData.targets().get(index);
            if (optionalTarget.isEmpty()) {
                continue;
            }
            EncodedResonatingPattern.Target target = optionalTarget.get();
            if (!player.level().dimension().equals(target.pos().dimension()) || !player.level().hasChunkAt(target.pos().pos())) {
                continue;
            }
            boolean selected = index == renderData.selected();
            drawLine(poseStack, lineConsumer, cameraPosition, source, anchorOf(target.pos().pos(), target.face()),
                    selected ? SELECTED_RED : UNSELECTED_RED,
                    selected ? SELECTED_GREEN : UNSELECTED_GREEN,
                    selected ? SELECTED_BLUE : UNSELECTED_BLUE);
        }
        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_LINE);
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

    private static Vector3f anchorOf(BlockPos pos, Direction face) {
        float x = pos.getX() + 0.5F;
        float y = pos.getY() + 0.5F;
        float z = pos.getZ() + 0.5F;
        if (face != null) {
            x += face.getStepX() * 0.501F;
            y += face.getStepY() * 0.501F;
            z += face.getStepZ() * 0.501F;
        }
        return new Vector3f(x, y, z);
    }

    private static void drawLine(PoseStack poseStack, VertexConsumer consumer, Vec3 cameraPosition,
                                 Vector3f start, Vector3f end, int red, int green, int blue) {
        var pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Vector3f normal = new Vector3f(end).sub(start);
        if (normal.lengthSquared() == 0F) {
            normal.set(0F, 1F, 0F);
        } else {
            normal.normalize();
        }
        consumer.addVertex(matrix, start.x - (float) cameraPosition.x, start.y - (float) cameraPosition.y, start.z - (float) cameraPosition.z)
                .setColor(red, green, blue, LINE_ALPHA).setNormal(pose, normal.x, normal.y, normal.z);
        consumer.addVertex(matrix, end.x - (float) cameraPosition.x, end.y - (float) cameraPosition.y, end.z - (float) cameraPosition.z)
                .setColor(red, green, blue, LINE_ALPHA).setNormal(pose, normal.x, normal.y, normal.z);
    }

    private record TargetRenderData(List<Optional<EncodedResonatingPattern.Target>> targets, int selected) {}
}
