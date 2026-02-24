package io.github.lounode.ae2cs.datagen;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseBlock;
import appeng.block.crafting.PatternProviderBlock;
import com.google.gson.JsonPrimitive;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.util.RegistryBlock;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class AECSBlockStateProvider extends BlockStateProvider
{
    private static final VariantProperty<VariantProperties.Rotation> Z_ROT = new VariantProperty<>("ae2:z",
            r -> new JsonPrimitive(r.ordinal() * 90));

    public AECSBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper)
    {
        super(output, AECSConstants.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        for (RegistryBlock<? extends Block> block : AECSBlocks.getCrystalBlocks())
        {
            blockWithItem(block);
        }
        blockWithItem(AECSBlocks.SILICON_BLOCK);
        blockWithItem(AECSBlocks.CERTUS_QUARTZ_ORE);
        blockWithItem(AECSBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE);
        blockWithItem(AECSBlocks.CHARGED_CERTUS_QUARTZ_ORE);
        blockWithItem(AECSBlocks.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE);
        genPatternProviderLike(AECSBlocks.INTEGRATED_INTERFACE_BLOCK.get(), "block/integrated_interface");
        genPatternProviderLike(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK.get(), "block/extended_integrated_interface");
        genPatternProviderLike(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK.get(), "block/meteorite_pattern_provider");
        genPatternProviderLike(AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK.get(), "block/simple_pattern_provider");
        genPatternProviderLike(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK.get(), "block/resonating_pattern_provider");
        genPatternProviderLike(AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK.get(), "block/extended_resonating_pattern_provider");
        genSixFaceLike(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK.get());
        genSixFaceLike(AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK.get());
        genSixFaceLike(AECSBlocks.QUARTZ_GRINDSTONE_BLOCK.get());
        genSixFaceLike(AECSBlocks.CIRCUIT_ETCHER_BLOCK.get());
        genSixFaceLike(AECSBlocks.CRYSTAL_PULVERIZER_BLOCK.get());
        genSixFaceLike(AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK.get());
        genSixFaceLike(AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK.get());
        genSixFaceLike(AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK.get());
        genEnderBroadcaster();
        genEnderEmitter();
        simpleBlockWithItem(AECSBlocks.ENDER_INTERFACE_BLOCK.get(), cubeAllWithTexture(AECSBlocks.ENDER_INTERFACE_BLOCK.get(), AE2CrystalScience.makeId("block/ender_interface/base")));
        simpleBlockWithItem(AECSBlocks.EX_ENDER_INTERFACE_BLOCK.get(), cubeAllWithTexture(AECSBlocks.EX_ENDER_INTERFACE_BLOCK.get(), AE2CrystalScience.makeId("block/ender_interface/extended")));
    }

    private void blockWithItem(RegistryBlock<? extends Block> deferredBlock)
    {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    /**
     * genSixFaceLike的快速应用，详细信息见{@link #genSixFaceLike(Block, IOrientationStrategy, boolean)}
     */
    public void genSixFaceLike(Block block)
    {
        IOrientationStrategy orientationStrategy = null;
        if (block instanceof AEBaseBlock aeBaseBlock)
        {
            orientationStrategy = aeBaseBlock.getOrientationStrategy();
        }
        genSixFaceLike(block, orientationStrategy, block.getStateDefinition().getProperties().contains(AECSBlockProperties.ACTIVE));
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
    public void genSixFaceLike(Block block, @Nullable IOrientationStrategy strategy, boolean switchable)
    {
        String blockPath = path(block).getPath();

        @Nullable PropertyDispatch directionDispatch = null;
        if (strategy == OrientationStrategies.full()) directionDispatch = createFacingSpinDispatch();
        else if (strategy == OrientationStrategies.horizontalFacing())
            directionDispatch = createHorizontalFacingDispatch(0);

        final var offOrBaseModel = switchable
                ? genSixFaceModel(block, "/off", "block/" + blockPath + "/off")
                : genSixFaceModel(block, "", "block/" + blockPath);

        final var onModel = switchable
                ? genSixFaceModel(block, "/on", "block/" + blockPath + "/on")
                : null;

        var gen = multiVariantGenerator(block, Variant.variant().with(VariantProperties.MODEL, offOrBaseModel.getLocation()));

        if (directionDispatch != null)
        {
            gen = gen.with(directionDispatch);
        }

        if (switchable)
        {
            gen = gen.with(PropertyDispatch.property(AECSBlockProperties.ACTIVE)
                    .select(false, Variant.variant().with(VariantProperties.MODEL, offOrBaseModel.getLocation()))
                    .select(true, Variant.variant().with(VariantProperties.MODEL, onModel.getLocation())));
        }

        itemModels().withExistingParent(blockPath, offOrBaseModel.getLocation());
    }

    /**
     * 按照一定规则生成六面模型（不包含方块状态）
     * - 最终会产出一个模型路径为 方块注册路径 + modelPathSuffix 的模型
     * - 六个面的纹理都需要在texturePath中，例如block/crystal_vibration_chamber/(bottom,top,front,back,left,right)
     */
    public ModelBuilder<BlockModelBuilder> genSixFaceModel(Block block, String modNameSpace, String modelPathSuffix, String texturePath)
    {
        ResourceLocation bottom = ResourceLocation.tryBuild(modNameSpace, texturePath + "/bottom");
        ResourceLocation top = ResourceLocation.tryBuild(modNameSpace, texturePath + "/top");
        ResourceLocation front = ResourceLocation.tryBuild(modNameSpace, texturePath + "/front");
        ResourceLocation back = ResourceLocation.tryBuild(modNameSpace, texturePath + "/back");
        ResourceLocation left = ResourceLocation.tryBuild(modNameSpace, texturePath + "/left");
        ResourceLocation right = ResourceLocation.tryBuild(modNameSpace, texturePath + "/right");
        return models().cube(
                        "block/" + path(block).getPath() + modelPathSuffix,
                        bottom, top,
                        front, back,
                        left, right)
                .texture("particle", front);
    }

    /**
     * 按照一定规则生成六面模型（不包含方块状态）
     * - 最终会产出一个模型路径为 方块注册路径 + _modelPathSuffix 的模型
     * - 六个面的纹理都需要在texturePath中，例如block/crystal_vibration_chamber/(bottom,top,front,back,left,right,particle)
     */
    public ModelBuilder<BlockModelBuilder> genSixFaceModel(Block block, String modelPathSuffix, String texturePath)
    {
        return genSixFaceModel(block, AECSConstants.MODID, modelPathSuffix, texturePath);
    }


    /**
     * @param block    类似样板供应器的方块
     * @param basePath 如：block/integrated_interface，随后你需要保证 block/integrated_interface/ 中包含其贴图
     *                 （base、alternate、alternate_front、alternate_arrow）
     */
    private void genPatternProviderLike(Block block, String basePath)
    {
        // 普通 cubeAll 模型（所有面同贴图）
        ModelFile normalModel = cubeAllWithTexture(block, AE2CrystalScience.makeId(basePath + "/base"));
        simpleBlockItem(block, normalModel);

        // 自动生成 oriented 模型
        ModelFile orientedModel = models()
                .withExistingParent(basePath + "_oriented", mcLoc("block/cube"))
                .texture("particle", AE2CrystalScience.makeId(basePath + "/base"))
                .texture("down", AE2CrystalScience.makeId(basePath + "/alternate"))
                .texture("up", AE2CrystalScience.makeId(basePath + "/alternate_front"))
                .texture("north", AE2CrystalScience.makeId(basePath + "/alternate_arrow"))
                .texture("south", AE2CrystalScience.makeId(basePath + "/alternate_arrow"))
                .texture("east", AE2CrystalScience.makeId(basePath + "/alternate_arrow"))
                .texture("west", AE2CrystalScience.makeId(basePath + "/alternate_arrow"));

        // blockstate：根据 PUSH_DIRECTION 选择 normal / oriented，并对 oriented 做旋转
        multiVariantGenerator(block, Variant.variant())
                .with(PropertyDispatch.property(PatternProviderBlock.PUSH_DIRECTION).generate(pushDirection -> {
                    Direction forward = pushDirection.getDirection();
                    if (forward == null)
                    {
                        return Variant.variant().with(VariantProperties.MODEL, normalModel.getLocation());
                    }
                    else
                    {
                        BlockOrientation orientation = BlockOrientation.get(forward);
                        return applyRotation(
                                Variant.variant().with(VariantProperties.MODEL, orientedModel.getLocation()),
                                // +90 因为默认模型是朝上的，而方块方向假设是朝北的
                                orientation.getAngleX() + 90,
                                orientation.getAngleY(),
                                0);
                    }
                }));
    }

    private ModelFile cubeAllWithTexture(Block block, ResourceLocation texture)
    {
        return this.models().cubeAll(path(block).getPath(), texture);
    }

    private ResourceLocation path(Block block)
    {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    protected final MultiVariantGenerator multiVariantGenerator(Block block, Variant... variants)
    {
        if (variants.length == 0)
        {
            variants = new Variant[]{Variant.variant()};
        }
        var builder = MultiVariantGenerator.multiVariant(block, variants);
        registeredBlocks.put(block, () -> builder.get().getAsJsonObject());
        return builder;
    }

    /**
     * 创建一个Facing可以六面旋转的方块
     */
    protected static PropertyDispatch createFacingSpinDispatch(int baseRotX, int baseRotY)
    {
        return PropertyDispatch.properties(BlockStateProperties.FACING, IOrientationStrategy.SPIN)
                .generate((facing, spin) -> {
                    var orientation = BlockOrientation.get(facing, spin);
                    return applyRotation(
                            Variant.variant(),
                            orientation.getAngleX() + baseRotX,
                            orientation.getAngleY() + baseRotY,
                            orientation.getAngleZ());
                });
    }

    /**
     * 创建一个face仅四面旋转的方块
     */
    protected static PropertyDispatch createHorizontalFacingDispatch(int baseRotY)
    {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                .select(Direction.NORTH, applyRotation(Variant.variant(), 0, baseRotY, 0))
                .select(Direction.SOUTH, applyRotation(Variant.variant(), 0, baseRotY + 180, 0))
                .select(Direction.WEST, applyRotation(Variant.variant(), 0, baseRotY + 270, 0))
                .select(Direction.EAST, applyRotation(Variant.variant(), 0, baseRotY + 90, 0));
    }

    protected static Variant applyRotation(Variant variant, int angleX, int angleY, int angleZ)
    {
        angleX = normalizeAngle(angleX);
        angleY = normalizeAngle(angleY);
        angleZ = normalizeAngle(angleZ);

        if (angleX != 0)
        {
            variant = variant.with(VariantProperties.X_ROT, rotationByAngle(angleX));
        }
        if (angleY != 0)
        {
            variant = variant.with(VariantProperties.Y_ROT, rotationByAngle(angleY));
        }
        if (angleZ != 0)
        {
            variant = variant.with(Z_ROT, rotationByAngle(angleZ));
        }
        return variant;
    }

    private static int normalizeAngle(int angle)
    {
        return angle - (angle / 360) * 360;
    }

    private static VariantProperties.Rotation rotationByAngle(int angle)
    {
        return switch (angle)
        {
            case 0 -> VariantProperties.Rotation.R0;
            case 90 -> VariantProperties.Rotation.R90;
            case 180 -> VariantProperties.Rotation.R180;
            case 270 -> VariantProperties.Rotation.R270;
            default -> throw new IllegalArgumentException("Invalid angle: " + angle);
        };
    }

    protected static PropertyDispatch createFacingSpinDispatch()
    {
        return createFacingSpinDispatch(0, 0);
    }


    // 以下是特殊方块模型生成------------------------------------------------------------------------------------------------

    /**
     * 生成末影广播装置
     */
    private void genEnderBroadcaster()
    {
        Block block = AECSBlocks.ENDER_BROADCASTER_BLOCK.get();
        // 核心由BER渲染
        var formModel = models().getExistingFile(modLoc("block/me_ender_broadcaster/form"));

        getVariantBuilder(block).forAllStates(state -> {
            return ConfiguredModel.builder()
                    .modelFile(formModel)
                    .build();
        });

        simpleBlockItem(block, formModel);
    }

    /**
     * 生成末影发信器
     */
    private void genEnderEmitter()
    {
        Block block = AECSBlocks.ENDER_EMITTER_BLOCK.get();
        var offModel = models().getExistingFile(modLoc("block/me_ender_emitter/off_form"));
        var onModel = models().getExistingFile(modLoc("block/me_ender_emitter/on_form"));
        var itemModel = models().getExistingFile(modLoc("block/me_ender_emitter/item"));
        var upperModel = models().getExistingFile(modLoc("block/me_ender_emitter/upper"));

        getVariantBuilder(block).forAllStates(state -> {
            boolean active = state.getValue(AECSBlockProperties.ACTIVE);
            boolean upper = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;

            ModelFile model;
            if (!active)
            {
                model = offModel;
            }
            else
            {
                model = onModel;
            }

            if (upper)
                model = upperModel;


            return ConfiguredModel.builder()
                    .modelFile(model)
                    .build();
        });

        simpleBlockItem(block, itemModel);
    }
}
