package io.github.lounode.ae2cs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.item.IResonatingTargetModeItem;
import io.github.lounode.ae2cs.common.init.client.AECSRenderTypes;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingProviderDefaults;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;

import java.util.List;
import java.util.Optional;


/**
 * 在手持谐振样板时渲染目标面
 * - 选中：绿色半透明
 * - 未选中：蓝色半透明
 * - 渲染所有存在目标的 sparse input
 */
@Mod.EventBusSubscriber(modid = AECSConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ResonatingPatternTargetHighlighter
{
    private static ResonatingPatternProviderHost trackedProvider;

    // 选中：绿色 + 半透明
    private static final int SEL_R = 0;
    private static final int SEL_G = 255;
    private static final int SEL_B = 0;

    // 未选中：蓝色 + 半透明
    private static final int UNS_R = 0;
    private static final int UNS_G = 80;
    private static final int UNS_B = 255;

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

        if (trackedProvider != null && trackedProvider.getResonatingLogic().isRenderMarkedFacesInClient())
        {
            renderTargets(event, player, new TargetRenderData(trackedProvider.getDefaultInputTargets(), trackedProvider.getDefaultSelectedInput()));
        }

        ItemStack stack = getHeldTargetModeItem(player);
        if (stack.isEmpty())
            return;

        TargetRenderData renderData = getRenderData(stack);
        renderTargets(event, player, renderData);
    }

    public static void setTrackedProvider(ResonatingPatternProviderHost host)
    {
        trackedProvider = host;
    }

    private static void renderTargets(RenderLevelStageEvent event, LocalPlayer player, TargetRenderData renderData)
    {
        if (renderData == null || renderData.targets().isEmpty())
            return;

        Minecraft mc = Minecraft.getInstance();
        Level level = player.level();
        var cam = event.getCamera();
        var camPos = cam.getPosition();

        PoseStack poseStack = event.getPoseStack();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(AECSRenderTypes.RESONATING_MARK_FACE);

        for (int i = 0; i < renderData.targets().size(); i++)
        {
            var opt = renderData.targets().get(i);
            if (opt.isEmpty())
                continue;

            EncodedResonatingPattern.Target t = opt.get();
            if (!level.dimension().equals(t.pos().dimension()))
                continue;

            BlockPos pos = t.pos().pos();
            if (!level.hasChunkAt(pos))
                continue;

            boolean selected = (i == renderData.selected());

            poseStack.pushPose();
            poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
            drawFaceQuad(poseStack, vc, t.face(), EPS,
                    selected ? SEL_R : UNS_R,
                    selected ? SEL_G : UNS_G,
                    selected ? SEL_B : UNS_B,
                    A);
            poseStack.popPose();
        }

        bufferSource.endBatch(AECSRenderTypes.RESONATING_MARK_FACE);
    }

    private static ItemStack getHeldTargetModeItem(LocalPlayer player)
    {
        ItemStack main = player.getMainHandItem();
        if (supportsRender(main))
            return main;

        ItemStack off = player.getOffhandItem();
        if (supportsRender(off))
            return off;

        return ItemStack.EMPTY;
    }

    private static boolean supportsRender(ItemStack stack)
    {
        return AECSDataComponents.getEncodedResonatingPattern(stack) != null
                || stack.getItem() instanceof IResonatingTargetModeItem;
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

            int sel = AECSDataComponents.getResonatingPatternSelectedInput(stack, 0);
            sel = ResonatingPatternDetails.clampSelected(sel, size);

            List<Optional<EncodedResonatingPattern.Target>> targets = new java.util.ArrayList<>(size);
            for (int i = 0; i < size; i++)
            {
                targets.add(encoded.targetOfSparseInput(i));
            }
            return new TargetRenderData(targets, sel);
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

    private record TargetRenderData(List<Optional<EncodedResonatingPattern.Target>> targets, int selected)
    {
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
        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x2, y2, z2).color(r, g, b, a).endVertex();
        vc.vertex(mat, x3, y3, z3).color(r, g, b, a).endVertex();
    }
}
