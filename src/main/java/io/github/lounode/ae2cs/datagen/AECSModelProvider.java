package io.github.lounode.ae2cs.datagen;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.core.AppEng;
import com.mojang.math.Quadrant;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import io.github.lounode.ae2cs.common.item.PureCrystalItem;
import io.github.lounode.ae2cs.datagen.properties.GrowProcess;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
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

import java.util.Optional;

public class AECSModelProvider extends ModelProvider {
    private static final ModelTemplate PART_INTERFACE = new ModelTemplate(
            Optional.of(AppEng.makeId("part/interface_base")),
            Optional.empty(),
            TextureSlot.FRONT
    );
    private static final ModelTemplate PART_PATTERN_PROVIDER = new ModelTemplate(
            Optional.of(AppEng.makeId("part/pattern_provider_base")),
            Optional.empty(),
            TextureSlot.FRONT
    );
    private static final ModelTemplate ITEM_INTERFACE = new ModelTemplate(
            Optional.of(AppEng.makeId("item/cable_interface")),
            Optional.empty(),
            TextureSlot.FRONT
    );
    private static final ModelTemplate ITEM_PATTERN_PROVIDER = new ModelTemplate(
            Optional.of(AppEng.makeId("item/cable_pattern_provider")),
            Optional.empty(),
            TextureSlot.FRONT
    );

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
        genPatternProviderLike(blockModels, AECSBlocks.INTEGRATED_INTERFACE_BLOCK, "block/integrated_interface");
        genPatternProviderLike(blockModels, AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK, "block/extended_integrated_interface");
        genPatternProviderLike(blockModels, AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK, "block/meteorite_pattern_provider");
        genPatternProviderLike(blockModels, AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK, "block/simple_pattern_provider");
        genPatternProviderLike(blockModels, AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK, "block/resonating_pattern_provider");
        genPatternProviderLike(blockModels, AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK, "block/extended_resonating_pattern_provider");
        genSixFaceLike(blockModels, AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.QUARTZ_GRINDSTONE_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.CIRCUIT_ETCHER_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.CRYSTAL_PULVERIZER_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK.get());
        genSixFaceLike(blockModels, AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK.get());
        genEnderBroadcaster(blockModels);
        genEnderEmitter(blockModels);

        blockWithItem(blockModels, AECSBlocks.ENDER_INTERFACE_BLOCK, ModelTemplates.CUBE_ALL.create(AECSBlocks.ENDER_INTERFACE_BLOCK.get(), TextureMapping.cube(new Material(AE2CrystalScience.makeId("block/ender_interface/base"))), blockModels.modelOutput));
        blockWithItem(blockModels, AECSBlocks.EX_ENDER_INTERFACE_BLOCK, ModelTemplates.CUBE_ALL.create(AECSBlocks.EX_ENDER_INTERFACE_BLOCK.get(), TextureMapping.cube(new Material(AE2CrystalScience.makeId("block/ender_interface/extended"))), blockModels.modelOutput));

        //item
        for (DeferredItem<CrystalSeedItem> item : AECSItems.getCrystalSeeds()) {
            crystalSeedItem(blockModels, itemModels, item.get());
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

        genPartModels(blockModels, itemModels);
    }


    private String getItemName(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    public void crystalSeedItem(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Item item) {
        for (int stage = 0; stage < 3; stage++) {
            var suffix = "_" + stage;
            ModelTemplates.FLAT_ITEM.create(
                    ModelLocationUtils.getModelLocation(item, suffix),
                    TextureMapping.layer0(TextureMapping.getItemTexture(item, suffix)),
                    blockModels.modelOutput
            );
        }

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

    private void genPartModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        partModel(blockModels, "part/ender_interface/base", PART_INTERFACE,
                "block/ender_interface/base", null, null);
        partModel(blockModels, "part/ender_interface/extended", PART_INTERFACE,
                "block/ender_interface/extended", null, null);
        partItemModel(blockModels, itemModels, AECSParts.ENDER_INTERFACE_PART.get(), ITEM_INTERFACE,
                "block/ender_interface/base", null);
        partItemModel(blockModels, itemModels, AECSParts.EX_ENDER_INTERFACE_PART.get(), ITEM_INTERFACE,
                "block/ender_interface/extended", null);

        partModel(blockModels, "part/integrate_interface/base", PART_INTERFACE,
                "block/integrated_interface/base", "block/integrated_interface/back", "block/integrated_interface/back");
        partModel(blockModels, "part/integrate_interface/extended", PART_INTERFACE,
                "block/extended_integrated_interface/base", "block/extended_integrated_interface/back", "block/extended_integrated_interface/back");
        partItemModel(blockModels, itemModels, AECSParts.INTEGRATE_INTERFACE_PART.get(), ITEM_INTERFACE,
                "block/integrated_interface/base", "block/integrated_interface/back");
        partItemModel(blockModels, itemModels, AECSParts.EX_INTEGRATE_INTERFACE_PART.get(), ITEM_INTERFACE,
                "block/extended_integrated_interface/base", "block/extended_integrated_interface/back");

        partModel(blockModels, "part/simple_pattern_provider/base", PART_PATTERN_PROVIDER,
                "block/simple_pattern_provider/base", "block/simple_pattern_provider/back", "block/simple_pattern_provider/back");
        partItemModel(blockModels, itemModels, AECSParts.SIMPLE_PATTERN_PROVIDER_PART.get(), ITEM_PATTERN_PROVIDER,
                "block/simple_pattern_provider/base", "block/simple_pattern_provider/back");

        partModel(blockModels, "part/meteorite_pattern_provider/base", PART_PATTERN_PROVIDER,
                "block/meteorite_pattern_provider/base", "block/meteorite_pattern_provider/back", "block/meteorite_pattern_provider/back");
        partItemModel(blockModels, itemModels, AECSParts.METEORITE_PATTERN_PROVIDER_PART.get(), ITEM_PATTERN_PROVIDER,
                "block/meteorite_pattern_provider/base", "block/meteorite_pattern_provider/back");

        partModel(blockModels, "part/resonating_pattern_provider/base", PART_PATTERN_PROVIDER,
                "block/resonating_pattern_provider/base", "block/resonating_pattern_provider/back", "block/resonating_pattern_provider/back");
        partModel(blockModels, "part/resonating_pattern_provider/extended", PART_PATTERN_PROVIDER,
                "block/extended_resonating_pattern_provider/base", "block/extended_resonating_pattern_provider/back", "block/extended_resonating_pattern_provider/back");
        partItemModel(blockModels, itemModels, AECSParts.RESONATING_PATTERN_PROVIDER_PART.get(), ITEM_PATTERN_PROVIDER,
                "block/resonating_pattern_provider/base", "block/resonating_pattern_provider/back");
        partItemModel(blockModels, itemModels, AECSParts.EX_RESONATING_PATTERN_PROVIDER_PART.get(), ITEM_PATTERN_PROVIDER,
                "block/extended_resonating_pattern_provider/base", "block/extended_resonating_pattern_provider/back");

        partModel(blockModels, "part/quartz_oscillator_clock/base_off", PART_INTERFACE,
                "block/quartz_oscillator_clock/off/part/front", "block/quartz_oscillator_clock/off/part/back", null);
        partModel(blockModels, "part/quartz_oscillator_clock/base_on", PART_INTERFACE,
                "block/quartz_oscillator_clock/on/part/front", "block/quartz_oscillator_clock/on/part/back", null);
        partItemModel(blockModels, itemModels, AECSParts.QUARTZ_OSCILLATOR_CLOCK_PART.get(), ITEM_INTERFACE,
                "block/quartz_oscillator_clock/off/part/front", null);
    }

    private void partModel(BlockModelGenerators blockModels, String modelPath, ModelTemplate template,
                           String front, @Nullable String back, @Nullable String particle) {
        template.create(AE2CrystalScience.makeId(modelPath), partTextures(front, back, particle), blockModels.modelOutput);
    }

    private void partItemModel(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Item item,
                               ModelTemplate template, String front, @Nullable String back) {
        var model = template.create(
                ModelLocationUtils.getModelLocation(item),
                partTextures(front, back, null),
                blockModels.modelOutput
        );
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private TextureMapping partTextures(String front, @Nullable String back, @Nullable String particle) {
        var textures = new TextureMapping()
                .put(TextureSlot.FRONT, new Material(AE2CrystalScience.makeId(front)));

        if (back != null) {
            textures.putForced(TextureSlot.BACK, new Material(AE2CrystalScience.makeId(back)));
        }
        if (particle != null) {
            textures.putForced(TextureSlot.PARTICLE, new Material(AE2CrystalScience.makeId(particle)));
        }

        return textures;
    }

    protected void blockWithItem(BlockModelGenerators blockModels, DeferredBlock<?> deferredBlock) {
        var block = deferredBlock.get();
        blockModels.createTrivialCube(block);
        blockModels.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block));
    }

    protected void blockWithItem(BlockModelGenerators blockModels, DeferredBlock<?> deferredBlock, Identifier model) {
        var block = deferredBlock.get();
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
        );
        blockModels.registerSimpleItemModel(block, model);
    }

    /**
     * @param deferredBlock 类似样板供应器的方块
     * @param basePath      如：block/integrated_interface，随后你需要保证 block/integrated_interface/ 中包含其贴图
     *                      （base、alternate、alternate_front、alternate_arrow）
     */
    private void genPatternProviderLike(BlockModelGenerators blockModels, DeferredBlock<?> deferredBlock, String basePath) {
        var block = deferredBlock.get();

        // 物品模型生成
        Identifier textureBase = AE2CrystalScience.makeId(basePath + "/base");
        var normalModel = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(new Material(textureBase)), blockModels.modelOutput);
        blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(normalModel));

        // 自动生成 oriented 模型
        var textures = TextureMapping.particle(new Material(ModelLocationUtils.getModelLocation(block, "/base")))
                .put(TextureSlot.DOWN, new Material(ModelLocationUtils.getModelLocation(block, "/alternate")))
                .put(TextureSlot.UP, new Material(ModelLocationUtils.getModelLocation(block, "/alternate_front")))
                .put(TextureSlot.NORTH, new Material(ModelLocationUtils.getModelLocation(block, "/alternate_arrow")))
                .put(TextureSlot.SOUTH, new Material(ModelLocationUtils.getModelLocation(block, "/alternate_arrow")))
                .put(TextureSlot.EAST, new Material(ModelLocationUtils.getModelLocation(block, "/alternate_arrow")))
                .put(TextureSlot.WEST, new Material(ModelLocationUtils.getModelLocation(block, "/alternate_arrow")));
        var orientedModel = ModelTemplates.CUBE.createWithSuffix(block, "_oriented", textures, blockModels.modelOutput);

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
     * 你需要保证“path/方块路径”下包含{@link #genSixFaceModel(BlockModelGenerators, Block, String, String, String)}中指定的6个纹理图片(bottom,top,front,back,left,right)；
     * 此外，如果switchable为true，你应该在“path/方块路径/on”与“path/方块路径/off”分别提供对应的两套纹理，以对应机器打开与关闭状态下的模型纹理
     *
     * @param block      方块
     * @param strategy   方块的旋转策略，决定了用哪一种旋转方法生成方块状态json，若不是可用的旋转策略，则不进行旋转应用
     * @param switchable 该方块是否可开关，如果是，你应该提供on和off两套纹理
     */
    public void genSixFaceLike(BlockModelGenerators blockModels, Block block, @Nullable IOrientationStrategy strategy, boolean switchable) {
        String blockPath = BuiltInRegistries.BLOCK.getKey(block).getPath();

        PropertyDispatch<VariantMutator> directionDispatch = null;
        if (strategy == OrientationStrategies.full()) {
            directionDispatch = createFacingSpinDispatch(0, 0);
        } else if (strategy == OrientationStrategies.horizontalFacing()) {
            directionDispatch = createHorizontalFacingDispatch(0);
        }

        Identifier offOrBaseModel = switchable
                ? genSixFaceModel(blockModels, block, "/off", "block/" + blockPath + "/off")
                : genSixFaceModel(blockModels, block, "", "block/" + blockPath);

        Identifier onModel = switchable
                ? genSixFaceModel(blockModels, block, "/on", "block/" + blockPath + "/on")
                : null;

        var gen = MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(offOrBaseModel));

        if (directionDispatch != null) {
            gen = gen.with(directionDispatch);
        }

        if (switchable) {
            gen = gen.with(PropertyDispatch.modify(AECSBlockProperties.ACTIVE)
                    .select(false, BlockModelGenerators.NOP)
                    .select(true, VariantMutator.MODEL.withValue(onModel)));
        }

        blockModels.blockStateOutput.accept(gen);
        blockModels.registerSimpleItemModel(block, offOrBaseModel);
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
    public Identifier genSixFaceModel(BlockModelGenerators blockModels, Block block, String modNameSpace, String modelPathSuffix, String texturePath) {
        var model = ModelLocationUtils.getModelLocation(block, modelPathSuffix);
        var bottom = Identifier.fromNamespaceAndPath(modNameSpace, texturePath + "/bottom");
        var top = Identifier.fromNamespaceAndPath(modNameSpace, texturePath + "/top");
        var front = Identifier.fromNamespaceAndPath(modNameSpace, texturePath + "/front");
        var back = Identifier.fromNamespaceAndPath(modNameSpace, texturePath + "/back");
        var left = Identifier.fromNamespaceAndPath(modNameSpace, texturePath + "/left");
        var right = Identifier.fromNamespaceAndPath(modNameSpace, texturePath + "/right");

        return ModelTemplates.CUBE.create(
                model,
                new TextureMapping()
                        .put(TextureSlot.PARTICLE, new Material(front))
                        .put(TextureSlot.DOWN, new Material(bottom))
                        .put(TextureSlot.UP, new Material(top))
                        .put(TextureSlot.NORTH, new Material(front))
                        .put(TextureSlot.SOUTH, new Material(back))
                        .put(TextureSlot.WEST, new Material(right))
                        .put(TextureSlot.EAST, new Material(left)),
                blockModels.modelOutput);
    }

    /**
     * 按照一定规则生成六面模型（不包含方块状态）
     * - 最终会产出一个模型路径为 方块注册路径 + _modelPathSuffix 的模型
     * - 六个面的纹理都需要在texturePath中，例如block/crystal_vibration_chamber/(bottom,top,front,back,left,right,particle)
     */
    public Identifier genSixFaceModel(BlockModelGenerators blockModels, Block block, String modelPathSuffix, String texturePath) {
        return genSixFaceModel(blockModels, block, AECSConstants.MODID, modelPathSuffix, texturePath);
    }

    // 以下是特殊方块模型生成------------------------------------------------------------------------------------------------

    /**
     * 生成末影广播装置
     */
    private void genEnderBroadcaster(BlockModelGenerators blockModels) {
        Block block = AECSBlocks.ENDER_BROADCASTER_BLOCK.get();
        // 核心由BER渲染
        Identifier formModel = AE2CrystalScience.makeId("block/me_ender_broadcaster/form");

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(formModel))
        );

        blockModels.registerSimpleItemModel(block, formModel);
    }

    /**
     * 生成末影发信器
     */
    private void genEnderEmitter(BlockModelGenerators blockModels) {
        Block block = AECSBlocks.ENDER_EMITTER_BLOCK.get();
        Identifier offModel = AE2CrystalScience.makeId("block/me_ender_emitter/off_form");
        Identifier onModel = AE2CrystalScience.makeId("block/me_ender_emitter/on_form");
        Identifier itemModel = AE2CrystalScience.makeId("block/me_ender_emitter/item");
        Identifier upperModel = AE2CrystalScience.makeId("block/me_ender_emitter/upper");

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(
                                AECSBlockProperties.ACTIVE,
                                BlockStateProperties.HORIZONTAL_FACING,
                                BlockStateProperties.DOUBLE_BLOCK_HALF
                        ).generate((active, facing, half) -> {
                            if (half == DoubleBlockHalf.UPPER) {
                                return BlockModelGenerators.plainVariant(upperModel);
                            }

                            return BlockModelGenerators.plainVariant(active ? onModel : offModel);
                        }))
        );

        blockModels.registerSimpleItemModel(block, itemModel);
    }
}
