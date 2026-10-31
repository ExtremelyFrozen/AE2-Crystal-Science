package io.github.lounode.ae2cs.common.block.render;

import io.github.lounode.ae2cs.common.block.entity.CrystalGrowthChamberBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CrystalGrowthAcceleratorRenderer implements BlockEntityRenderer<CrystalGrowthChamberBlockEntity> {

    private static final float CENTER_CROP_START = 3.0f / 16.0f;
    private static final float CENTER_CROP_SIZE = 10.0f / 16.0f;

    public CrystalGrowthAcceleratorRenderer(BlockEntityRendererProvider.Context context) {}

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

        int topLight = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());
        BlockState crystalState = block.defaultBlockState();
        BakedModel crystalModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(crystalState);
        BakedModel topCroppedModel = new CenterCroppedTopModel(crystalModel);
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());

        poseStack.pushPose();
        poseStack.translate(CENTER_CROP_START, 1.001f, CENTER_CROP_START);
        poseStack.scale(CENTER_CROP_SIZE, 0.002f, CENTER_CROP_SIZE);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), consumer, crystalState, topCroppedModel,
                1.0f, 1.0f, 1.0f, topLight, packedOverlay);
        poseStack.popPose();
    }

    /** Keeps only the crystal's UP face and remaps its UVs to the source texture center 10x10. */
    private static final class CenterCroppedTopModel implements BakedModel {

        private final BakedModel parent;

        private CenterCroppedTopModel(BakedModel parent) {
            this.parent = parent;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
            if (side != Direction.UP) {
                return List.of();
            }

            List<BakedQuad> source = parent.getQuads(state, Direction.UP, random);
            List<BakedQuad> result = new ArrayList<>(source.size());
            for (BakedQuad quad : source) {
                result.add(cropQuad(quad));
            }
            return result;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return parent.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return parent.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return parent.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return parent.isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return parent.getParticleIcon();
        }

        @Override
        public ItemTransforms getTransforms() {
            return parent.getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return parent.getOverrides();
        }

        private static BakedQuad cropQuad(BakedQuad quad) {
            int[] vertices = quad.getVertices().clone();
            int uvOffset = DefaultVertexFormat.BLOCK.getOffset(DefaultVertexFormat.BLOCK.getElements().get(2)) / 4;
            int stride = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
            TextureAtlasSprite sprite = quad.getSprite();

            for (int vertex = 0; vertex < 4; vertex++) {
                int index = vertex * stride + uvOffset;
                float u = Float.intBitsToFloat(vertices[index]);
                float v = Float.intBitsToFloat(vertices[index + 1]);
                float localU = (u - sprite.getU0()) / (sprite.getU1() - sprite.getU0());
                float localV = (v - sprite.getV0()) / (sprite.getV1() - sprite.getV0());
                double croppedU = CENTER_CROP_START + localU * CENTER_CROP_SIZE;
                double croppedV = CENTER_CROP_START + localV * CENTER_CROP_SIZE;
                vertices[index] = Float.floatToRawIntBits(sprite.getU((float) croppedU));
                vertices[index + 1] = Float.floatToRawIntBits(sprite.getV((float) croppedV));
            }

            return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), sprite,
                    quad.isShade(), quad.hasAmbientOcclusion());
        }
    }
}
