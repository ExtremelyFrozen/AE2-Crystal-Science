package io.github.lounode.ae2cs.common.block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.client.AECSAdditionalModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnderBroadcasterRender implements BlockEntityRenderer<EnderBroadcasterBlockEntity>
{
    // 缓存参数，避免每帧重复生成
    private static final Map<String, AnimParams> PARAM_CACHE = new ConcurrentHashMap<>();

    public EnderBroadcasterRender(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public void render(@NotNull EnderBroadcasterBlockEntity be, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        var level = be.getLevel();
        if (level == null) return;

        // 拿到核心的 baked model
        BakedModel coreModel;
        if (be.isActiveForClient())
        {
            if (be.isAsSenderForClient())
                coreModel = Minecraft.getInstance().getModelManager().getModel(AECSAdditionalModels.BROADCASTER_SENDER_CORE_MODEL);
            else
                coreModel = Minecraft.getInstance().getModelManager().getModel(AECSAdditionalModels.BROADCASTER_RECEIVER_CORE_MODEL);
        }
        else
        {
            coreModel = Minecraft.getInstance().getModelManager().getModel(AECSAdditionalModels.BROADCASTER_OFF_CORE);
        }

        RenderType rt = RenderType.cutout();
        VertexConsumer vc = buffer.getBuffer(rt);

        // 根据band name产生动画
        String name = be.getBandName();
        if (name == null) name = "";
        AnimParams p = PARAM_CACHE.computeIfAbsent(name, AnimParams::fromName);

        float time = (level.getGameTime() + partialTick);

        // 周期内的归一化时间
        float t = (time % p.cycleTicks) / (float) p.cycleTicks;

        // 三段：A(缩小+旋转到 qA) -> B(放大回 1 + 旋转到 qAB) -> C(回到 identity)
        float aEnd = p.phaseA;
        float bEnd = p.phaseA + p.phaseB;

        float scale;
        Quaternionf q; // 当前姿态（旋转）

        if (t < aEnd)
        {
            float u = t / aEnd;
            float e = easeInOut(u);
            scale = lerp(1.0f, p.minScale, e); // 只缩小
            q = slerp(new Quaternionf(), p.qA, e);
        }
        else if (t < bEnd)
        {
            float u = (t - aEnd) / p.phaseB;
            float e = easeInOut(u);
            scale = lerp(p.minScale, 1.0f, e); // 放大回原大小
            q = slerp(new Quaternionf(p.qA), p.qAB, e);
        }
        else
        {
            float u = (t - bEnd) / p.phaseC;
            float e = easeInOut(u);
            scale = 1.0f; // 保持原大小
            q = slerp(new Quaternionf(p.qAB), new Quaternionf(), e); // 回到原始姿态
        }

        // 围绕方块中心应用：缩放 + 旋转
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // 缩放
        poseStack.scale(scale, scale, scale);

        // 旋转
        poseStack.mulPose(q);

        poseStack.translate(-0.5, -0.5, -0.5);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                vc,
                be.getBlockState(),
                coreModel,
                1.0f, 1.0f, 1.0f,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                rt
        );
        poseStack.popPose();
    }

    // ------------------辅助工具---------------------

    private record AnimParams(
            int cycleTicks,
            float phaseA, float phaseB, float phaseC,
            float minScale,
            Quaternionf qA,
            Quaternionf qAB
    )
    {
        static AnimParams fromName(String name)
        {
            long seed = fnv1a64(name);

            SplitMix64 rng = new SplitMix64(seed);

            // 周期：80~200 ticks
            int cycle = 80 + (rng.nextIntBounded(121));

            // 三段占比：A 0.25~0.45, B 0.25~0.45, C 剩余且>=0.15
            float a = 0.25f + rng.nextFloat() * 0.20f;
            float b = 0.25f + rng.nextFloat() * 0.20f;
            float c = 1.0f - a - b;
            if (c < 0.15f)
            {
                // 强制给 C 留出足够回正时间
                float deficit = 0.15f - c;
                a -= deficit * 0.5f;
                b -= deficit * 0.5f;
                c = 0.15f;
            }

            // 最小缩放：0.55~0.90
            float minScale = 0.55f + rng.nextFloat() * 0.35f;

            // 两段旋转轴：从若干方向里选，尽量不重复
            Vector3f axisA = pickAxis(rng.nextIntBounded(12));
            Vector3f axisB = pickAxis(rng.nextIntBounded(12));
            if (axisA.dot(axisB) > 0.85f)
                axisB = pickAxis((rng.nextIntBounded(12) + 5) % 12);

            // 角度：A 90~270, B 120~360
            float angA = 90f + rng.nextFloat() * 180f;
            float angB = 120f + rng.nextFloat() * 240f;
            if (rng.nextBoolean()) angA = -angA;
            if (rng.nextBoolean()) angB = -angB;

            Quaternionf qA = new Quaternionf().rotateAxis((float) Math.toRadians(angA), axisA.x, axisA.y, axisA.z);
            Quaternionf qB = new Quaternionf().rotateAxis((float) Math.toRadians(angB), axisB.x, axisB.y, axisB.z);
            Quaternionf qAB = new Quaternionf(qA).mul(qB);

            return new AnimParams(cycle, a, b, c, minScale, qA, qAB);
        }
    }

    private static float lerp(float from, float to, float time)
    {
        return from + (to - from) * time;
    }

    // 平滑插值
    private static float easeInOut(float t)
    {
        return t * t * (3f - 2f * t);
    }

    private static Quaternionf slerp(Quaternionf from, Quaternionf to, float t)
    {
        Quaternionf out = new Quaternionf(from);
        out.slerp(to, t);
        return out;
    }

    // 12个固定轴，让不同name变化更明显
    private static Vector3f pickAxis(int idx)
    {
        idx = Math.floorMod(idx, 12);
        return switch (idx)
        {
            case 0 -> new Vector3f(1, 0, 0);
            case 1 -> new Vector3f(0, 1, 0);
            case 2 -> new Vector3f(0, 0, 1);
            case 3 -> new Vector3f(1, 1, 0).normalize();
            case 4 -> new Vector3f(1, 0, 1).normalize();
            case 5 -> new Vector3f(0, 1, 1).normalize();
            case 6 -> new Vector3f(1, -1, 0).normalize();
            case 7 -> new Vector3f(1, 0, -1).normalize();
            case 8 -> new Vector3f(0, 1, -1).normalize();
            case 9 -> new Vector3f(1, 1, 1).normalize();
            case 10 -> new Vector3f(1, 1, -1).normalize();
            default -> new Vector3f(1, -1, 1).normalize();
        };
    }

    private static long fnv1a64(String s)
    {
        long h = 0xcbf29ce484222325L;
        for (int i = 0; i < s.length(); i++)
        {
            h ^= s.charAt(i);
            h *= 0x100000001b3L;
        }
        return h;
    }

    private static final class SplitMix64
    {
        private long x;

        SplitMix64(long seed)
        {
            this.x = seed;
        }

        long nextLong()
        {
            long z = (x += 0x9E3779B97F4A7C15L);
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            return z ^ (z >>> 31);
        }

        float nextFloat()
        {
            return (nextLong() >>> 40) / (float) (1 << 24);
        }

        boolean nextBoolean()
        {
            return (nextLong() & 1L) != 0;
        }

        int nextIntBounded(int bound)
        {
            long r = nextLong();
            long m = (r >>> 1) % bound;
            return (int) m;
        }
    }
}
