package io.github.lounode.ae2cs.datagen;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseBlock;
import appeng.block.crafting.PatternProviderBlock;
import com.mojang.math.Quadrant;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import io.github.lounode.ae2cs.common.item.PureCrystalItem;
import io.github.lounode.ae2cs.datagen.properties.GrowProcess;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class AECSModelProvider extends ModelProvider {

    public AECSModelProvider(PackOutput output) {
        super(output, AECSConstants.MODID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        //block
        for (DeferredBlock<? extends Block> block : AECSBlocks.getCrystalBlocks()) {
            blockWithItem(blockModels, block);
        }
        blockWithItem(blockModels, AECSBlocks.SILICON_BLOCK);
        blockWithItem(blockModels, AECSBlocks.CERTUS_QUARTZ_ORE);
        blockWithItem(blockModels, AECSBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE);
        blockWithItem(blockModels, AECSBlocks.CHARGED_CERTUS_QUARTZ_ORE);
        blockWithItem(blockModels, AECSBlocks.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE);
        genPatternProviderLike(blockModels, AECSBlocks.INTEGRATED_INTERFACE_BLOCK);
        genPatternProviderLike(blockModels, AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK);
        genPatternProviderLike(blockModels, AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK);
        genPatternProviderLike(blockModels, AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK);
        genPatternProviderLike(blockModels, AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK);
        genPatternProviderLike(blockModels, AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK);
        genSixFaceLike(blockModels, AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.QUARTZ_GRINDSTONE_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.CIRCUIT_ETCHER_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.CRYSTAL_PULVERIZER_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK.get());
//        genEnderBroadcaster();
//        genEnderEmitter();

        blockWithItem(blockModels, AECSBlocks.ENDER_INTERFACE_BLOCK, ModelLocationUtils.getModelLocation(AECSBlocks.ENDER_INTERFACE_BLOCK.get(), "/base"));
        blockWithItem(blockModels, AECSBlocks.EX_ENDER_INTERFACE_BLOCK, ModelLocationUtils.getModelLocation(AECSBlocks.ENDER_INTERFACE_BLOCK.get(), "/extended"));

        //item
        for (DeferredItem<CrystalSeedItem> item : AECSItems.getCrystalSeeds()) {
            crystalSeedItem(itemModels, item.get());
        }
        for (DeferredItem<PureCrystalItem> item : AECSItems.getPureCrystal()) {
            itemModels.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM);
        }
        for (DeferredItem<? extends Item> item : AECSItems.getTools()) {
            itemModels.generateFlatItem(item.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        }
        for (DeferredItem<? extends Item> item : AECSItems.getOthers()) {
            itemModels.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM);
        }
    }


    private String getItemName(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    /**
     * 把非本模组命名空间的纹理标记为“已生成”，从而绕过存在性校验
     */
//    private void allowExternalTexture(String path)
//    {
//        Identifier rl = Identifier.parse(path);
//        if (!rl.getNamespace().equals(AECSConstants.MODID))
//        {
//            this.existingFileHelper.trackGenerated(rl, ModelProvider.TEXTURE);
//        }
//    }
    public void crystalSeedItem(ItemModelGenerators itemModels, Item item) {
        var model0 = ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(item, "_0"));
        var model1 = ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(item, "_1"));
        var model2 = ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(item, "_2"));

        itemModels.itemModelOutput.accept(
                item,
                ItemModelUtils.rangeSelect(
                        new GrowProcess(),
                        model0,
                        ItemModelUtils.override(model1, 0.333f),
                        ItemModelUtils.override(model2, 0.666f)
                )
        );
    }

    protected void blockWithItem(BlockModelGenerators blockModels, DeferredBlock<?> deferredBlock) {
        var block = deferredBlock.get();
        blockModels.createTrivialCube(block);
        blockModels.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block));
    }

    protected void blockWithItem(BlockModelGenerators blockModels, DeferredBlock<?> deferredBlock, Identifier model) {
        var block = deferredBlock.get();
        blockModels.createTrivialCube(block);
        blockModels.registerSimpleItemModel(block, model);
    }

    /**
     * @param block    类似样板供应器的方块
     * @param basePath 如：block/integrated_interface，随后你需要保证 block/integrated_interface/ 中包含其贴图
     *                 （base、alternate、alternate_front、alternate_arrow）
     */
    private void genPatternProviderLike(BlockModelGenerators blockModels, DeferredBlock<?> deferredBlock) {
        // 普通 cubeAll 模型（所有面同贴图）
        var block = deferredBlock.get();
        var normalModel = TexturedModel.CUBE.create(block, blockModels.modelOutput).withSuffix("/base");
//        blockModels.createTrivialCube(block);
//        blockModels.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block, "/base"));

        // 自动生成 oriented 模型
        var textures = TextureMapping.particle(new Material(ModelLocationUtils.getModelLocation(block, "/base")))
                .put(TextureSlot.DOWN, new Material(ModelLocationUtils.getModelLocation(block, "/alternate")))
                .put(TextureSlot.UP, new Material(ModelLocationUtils.getModelLocation(block, "/alternate_front")))
                .put(TextureSlot.NORTH, new Material(ModelLocationUtils.getModelLocation(block, "/alternate_arrow")))
                .put(TextureSlot.SOUTH, new Material(ModelLocationUtils.getModelLocation(block, "/alternate_arrow")))
                .put(TextureSlot.EAST, new Material(ModelLocationUtils.getModelLocation(block, "/alternate_arrow")))
                .put(TextureSlot.WEST, new Material(ModelLocationUtils.getModelLocation(block, "/alternate_arrow")));
        var orientedModel = ModelTemplates.CUBE.create(block, textures, blockModels.modelOutput);

        // blockstate：根据 PUSH_DIRECTION 选择 normal / oriented，并对 oriented 做旋转
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(PatternProviderBlock.PUSH_DIRECTION)
                                .generate(pushDirection -> {
                                    var forward = pushDirection.getDirection();
                                    if (forward == null) {
                                        return BlockModelGenerators.plainVariant(normalModel);
                                    } else {
                                        var orientation = BlockOrientation.get(forward);
                                        return BlockModelGenerators.plainVariant(orientedModel)
                                                .with(applyRotation(
                                                        orientation.getAngleX() + 90,
                                                        orientation.getAngleY()
                                                ));
                                    }
                                })));
    }

    protected static VariantMutator applyRotation(int angleX, int angleY) {
        angleX = normalizeAngle(angleX);
        angleY = normalizeAngle(angleY);

        VariantMutator mutator = variant -> variant;
        if (angleX != 0) {
            mutator = mutator.then(VariantMutator.X_ROT.withValue(rotationByAngle(angleX)));
        }
        if (angleY != 0) {
            mutator = mutator.then(VariantMutator.Y_ROT.withValue(rotationByAngle(angleY)));
        }
        return mutator;
    }

    private static int normalizeAngle(int angle) {
        return angle - (angle / 360) * 360;
    }

    private static Quadrant rotationByAngle(int angle) {
        return switch (angle) {
            case 0 -> Quadrant.R0;
            case 90 -> Quadrant.R90;
            case 180 -> Quadrant.R180;
            case 270 -> Quadrant.R270;
            default -> throw new IllegalArgumentException("Invalid angle: " + angle);
        };
    }

    public void genSixFaceLike(BlockModelGenerators blockModels, Block block) {
        IOrientationStrategy orientationStrategy = null;
        if (block instanceof AEBaseBlock aeBaseBlock) {
            orientationStrategy = aeBaseBlock.getOrientationStrategy();
        }
        genSixFaceLike(blockModels, block, orientationStrategy, block.getStateDefinition().getProperties().contains(AECSBlockProperties.ACTIVE));
    }

    /**
     * 生成六个面各有不同贴图的机器，包括其模型、方块状态、方块物品模型
     * <p>
     * 你需要保证“path/方块路径”下包含{@link #genSixFaceModel(Block, String, String, String)}中指定的6个纹理图片(bottom,top,front,back,left,right)；
     * 此外，如果switchable为true，你应该在“path/方块路径/on”与“path/方块路径/off”分别提供对应的两套纹理，以对应机器打开与关闭状态下的模型纹理
     *
     * @param block      方块
     * @param strategy   方块的旋转策略，决定了用哪一种旋转方法生成方块状态json，若不是可用的旋转策略，则不进行旋转应用
     * @param switchable 该方块是否可开关，如果是，你应该提供on和off两套纹理
     */
    public void genSixFaceLike(BlockModelGenerators blockModels, Block block, @Nullable IOrientationStrategy strategy, boolean switchable) {
        PropertyDispatch<VariantMutator> directionDispatch = null;
        if (strategy == OrientationStrategies.full()) {
            directionDispatch = createFacingSpinDispatch(0, 0);
        } else if (strategy == OrientationStrategies.horizontalFacing()) {
            directionDispatch = createHorizontalFacingDispatch(0);
        }

        MultiVariantGenerator gen;

        if (switchable) {
            var onModel = genSixFaceModel(blockModels, block, "/on");
            var offModel = genSixFaceModel(blockModels, block, "/off");
            gen = MultiVariantGenerator.dispatch(block)
                    .with(PropertyDispatch.initial(AECSBlockProperties.ACTIVE)
                            .select(false, BlockModelGenerators.plainVariant(offModel))
                            .select(true, BlockModelGenerators.plainVariant(onModel)));
        } else {
            var model = genSixFaceModel(blockModels, block, "");
            gen = MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model));
        }

        if (directionDispatch != null) {
            gen = gen.with(directionDispatch);
        }

        blockModels.blockStateOutput.accept(gen);
    }

    /**
     * 创建一个Facing可以六面旋转的方块
     */
    protected static PropertyDispatch<VariantMutator> createFacingSpinDispatch(int baseRotX, int baseRotY) {
        return PropertyDispatch.modify(BlockStateProperties.FACING, IOrientationStrategy.SPIN)
                .generate((facing, spin) -> {
                    var orientation = BlockOrientation.get(facing, spin);
                    return applyRotation(
                            orientation.getAngleX() + baseRotX,
                            orientation.getAngleY() + baseRotY
                    );
                });
    }

    /**
     * 创建一个face仅四面旋转的方块
     */
    protected static PropertyDispatch<VariantMutator> createHorizontalFacingDispatch(int baseRotY) {
        return PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                .select(Direction.NORTH, applyRotation(0, baseRotY))
                .select(Direction.SOUTH, applyRotation(0, baseRotY + 180))
                .select(Direction.WEST, applyRotation(0, baseRotY + 270))
                .select(Direction.EAST, applyRotation(0, baseRotY + 90));
    }

    /**
     * 按照一定规则生成六面模型（不包含方块状态）
     * - 最终会产出一个模型路径为 方块注册路径 + modelPathSuffix 的模型
     * - 六个面的纹理都需要在texturePath中，例如block/crystal_vibration_chamber/(bottom,top,front,back,left,right)
     */
    public Identifier genSixFaceModel(BlockModelGenerators blockModels, Block block, String modelPathSuffix) {
        var model = ModelLocationUtils.getModelLocation(block, modelPathSuffix);
        var bottom = model.withSuffix("/bottom");
        var top = model.withSuffix("/top");
        var front = model.withSuffix("/front");
        var back = model.withSuffix("/back");
        var left = model.withSuffix("/left");
        var right = model.withSuffix("/right");

        return ModelTemplates.CUBE.create(
                block,
                new TextureMapping()
                        .put(TextureSlot.BOTTOM, new Material(bottom))
                        .put(TextureSlot.TOP, new Material(top))
                        .put(TextureSlot.NORTH, new Material(front))
                        .put(TextureSlot.SOUTH, new Material(back))
                        .put(TextureSlot.WEST, new Material(right))
                        .put(TextureSlot.EAST, new Material(left)),
                blockModels.modelOutput);
    }

    // 以下是特殊方块模型生成------------------------------------------------------------------------------------------------

    /**
     * 生成末影广播装置
     */
//    private void genEnderBroadcaster() {
//        Block block = AECSBlocks.ENDER_BROADCASTER_BLOCK.get();
//        // 核心由BER渲染
//        var formModel = models().getExistingFile(modLoc("block/me_ender_broadcaster/form"));
//
//        getVariantBuilder(block).forAllStates(state -> {
//            return ConfiguredModel.builder()
//                    .modelFile(formModel)
//                    .build();
//        });
//
//        simpleBlockItem(block, formModel);
//    }

    /**
     * 生成末影发信器
     */
//    private void genEnderEmitter()
//    {
//        Block block = AECSBlocks.ENDER_EMITTER_BLOCK.get();
//        var offModel = models().getExistingFile(modLoc("block/me_ender_emitter/off_form"));
//        var onModel = models().getExistingFile(modLoc("block/me_ender_emitter/on_form"));
//        var itemModel = models().getExistingFile(modLoc("block/me_ender_emitter/item"));
//        var upperModel = models().getExistingFile(modLoc("block/me_ender_emitter/upper"));
//
//        getVariantBuilder(block).forAllStates(state -> {
//            boolean active = state.getValue(AECSBlockProperties.ACTIVE);
//            boolean upper = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
//
//            ModelFile model;
//            if (!active)
//            {
//                model = offModel;
//            }
//            else
//            {
//                model = onModel;
//            }
//
//            if (upper)
//                model = upperModel;
//
//
//            return ConfiguredModel.builder()
//                    .modelFile(model)
//                    .build();
//        });
//
//        simpleBlockItem(block, itemModel);
//    }
}
