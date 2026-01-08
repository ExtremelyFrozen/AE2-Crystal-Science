package io.github.lounode.ae2cs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.init.client.AECSRenderTypes;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import static net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;

/**
 * 在手持谐振样板时渲染目标面
 */
@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class ResonatingPatternTargetHighlighter
{
    // 绿色 + 半透明
    private static final int R = 0;
    private static final int G = 255;
    private static final int B = 0;
    private static final int A = 90;

    // 防Z-fighting
    private static final float EPS = 0.002f;

    private ResonatingPatternTargetHighlighter()
    {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event)
    {
        if (event.getStage() != AFTER_TRANSLUCENT_BLOCKS)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        ItemStack stack = getHeldResonatingPattern(player);
        if (stack.isEmpty())
            return;

        EncodedResonatingPattern encoded = stack.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get());
        if (encoded == null)
            return;

        int sel = stack.getOrDefault(AECSDataComponents.RESONATING_PATTERN_SELECTED_INPUT.get(), 0);
        sel = ResonatingPatternDetails.clampSelected(sel, encoded.sparseInputs().size());

        var opt = encoded.targetOfSparseInput(sel);
        if (opt.isEmpty())
            return;

        EncodedResonatingPattern.Target t = opt.get();

        Level level = player.level();
        if (!level.dimension().equals(t.pos().dimension()))
            return; // 跨维度时不渲染

        BlockPos pos = t.pos().pos();
        if (!level.hasChunkAt(pos))
            return; // 未加载不渲染

        PoseStack poseStack = event.getPoseStack();

        // 移到相机坐标系
        var cam = event.getCamera();
        var camPos = cam.getPosition();

        poseStack.pushPose();
        poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_FACE);

        drawFaceQuad(poseStack, vc, t.face(), EPS, R, G, B, A);

        poseStack.popPose();
        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_FACE);
    }

    private static ItemStack getHeldResonatingPattern(LocalPlayer player)
    {
        ItemStack main = player.getMainHandItem();
        if (main.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get()) != null)
            return main;

        ItemStack off = player.getOffhandItem();
        if (off.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get()) != null)
            return off;

        return ItemStack.EMPTY;
    }

    private static void drawFaceQuad(PoseStack poseStack, VertexConsumer vc, Direction face, float eps,
                                     int r, int g, int b, int a)
    {
        float x0 = 0f, x1 = 1f;
        float y0 = 0f, y1 = 1f;
        float z0 = 0f, z1 = 1f;

        Matrix4f mat = poseStack.last().pose();

        switch (face)
        {
            case NORTH -> quadPosColor(vc, mat,
                    x0, y0, z0 - eps, x1, y0, z0 - eps, x1, y1, z0 - eps, x0, y1, z0 - eps,
                    r, g, b, a);
            case SOUTH -> quadPosColor(vc, mat,
                    x1, y0, z1 + eps, x0, y0, z1 + eps, x0, y1, z1 + eps, x1, y1, z1 + eps,
                    r, g, b, a);
            case WEST -> quadPosColor(vc, mat,
                    x0 - eps, y0, z1, x0 - eps, y0, z0, x0 - eps, y1, z0, x0 - eps, y1, z1,
                    r, g, b, a);
            case EAST -> quadPosColor(vc, mat,
                    x1 + eps, y0, z0, x1 + eps, y0, z1, x1 + eps, y1, z1, x1 + eps, y1, z0,
                    r, g, b, a);
            case DOWN -> quadPosColor(vc, mat,
                    x0, y0 - eps, z0, x1, y0 - eps, z0, x1, y0 - eps, z1, x0, y0 - eps, z1,
                    r, g, b, a);
            case UP -> quadPosColor(vc, mat,
                    x0, y1 + eps, z1, x1, y1 + eps, z1, x1, y1 + eps, z0, x0, y1 + eps, z0,
                    r, g, b, a);
        }
    }


    private static void quadPosColor(VertexConsumer vc, Matrix4f mat,
                                     float x0, float y0, float z0,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float x3, float y3, float z3,
                                     int r, int g, int b, int a)
    {
        int argb = FastColor.ARGB32.color(a, r, g, b);

        vc.addVertex(mat, x0, y0, z0).setColor(argb);
        vc.addVertex(mat, x1, y1, z1).setColor(argb);
        vc.addVertex(mat, x2, y2, z2).setColor(argb);
        vc.addVertex(mat, x3, y3, z3).setColor(argb);
    }

}
