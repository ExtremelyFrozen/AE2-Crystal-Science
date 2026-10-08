package io.github.lounode.ae2cs.common.block.render;

import io.github.lounode.ae2cs.common.block.entity.CrystalGrowthChamberBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSBlocks;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;

public class CrystalGrowthAcceleratorRenderer implements BlockEntityRenderer<CrystalGrowthChamberBlockEntity> {

    public CrystalGrowthAcceleratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull CrystalGrowthChamberBlockEntity be, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (be.getLevel() == null || !be.getBlockState().is(AECSBlocks.CRYSTAL_GROWTH_ACCELERATOR_BLOCK.get())) {
            return;
        }

        Block block = be.getAcceleratorDisplayBlock();
        if (block == null) {
            return;
        }

        // The block entity itself is enclosed by the machine. Sampling its packed light makes
        // the inserted mother rock render at light level zero (pure black). Render the exposed
        // top using the actual light at the block above it instead.
        int topLight = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());

        // Reuse the actual mother rock's baked block model instead of deriving a texture name.
        // This preserves every AE2/local mother-rock top texture and avoids missing-atlas sprites.
        poseStack.pushPose();
        // up_full is now the normal active block top. Its transparent center is 10x10 pixels
        // (from pixel 3 through 12), so only render the mother rock inside that opening.
        poseStack.translate(3.0f / 16.0f, 1.001f, 3.0f / 16.0f);
        poseStack.scale(10.0f / 16.0f, 0.002f, 10.0f / 16.0f);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                block.defaultBlockState(), poseStack, buffer, topLight, packedOverlay, ModelData.EMPTY, null);
        poseStack.popPose();
    }
}
