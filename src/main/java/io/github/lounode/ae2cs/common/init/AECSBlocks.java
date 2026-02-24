package io.github.lounode.ae2cs.common.init;

import appeng.block.AEBaseBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.InterfaceBlock;
import io.github.lounode.ae2cs.api.ids.AECSBlockIds;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.util.RegistryBlock;
import io.github.lounode.ae2cs.common.block.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class AECSBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AECSConstants.MODID);

    /**
     * 所有已注册方块
     */
    private static final List<RegistryBlock<? extends Block>> ALL = new ArrayList<>();

    /**
     * 水晶块
     */
    private static final List<RegistryBlock<? extends Block>> CRYSTAL_BLOCKS = new ArrayList<>();

    /**
     * 杂项方块
     */
    private static final List<RegistryBlock<? extends Block>> OTHERS = new ArrayList<>();

    /**
     * 非自身掉落式方块 用于datagen避让
     */
    private static final List<RegistryBlock<? extends Block>> NOT_SELF_DROP = new ArrayList<>();

    // -------------------高纯水晶块-----------------
    public static final RegistryBlock<Block> PURE_ENDER_QUARTZ_BLOCK = registerCrystalBlock(AECSBlockIds.ENDER_QUARTZ_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final RegistryBlock<Block> PURE_RESONATING_CRYSTAL_BLOCK = registerCrystalBlock(AECSBlockIds.RESONATING_CRYSTAL_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final RegistryBlock<Block> PURE_METEOR_CRYSTAL_BLOCK = registerCrystalBlock(AECSBlockIds.METEOR_CRYSTAL_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final RegistryBlock<Block> PURE_REDSTONE_CRYSTAL_BLOCK = registerCrystalBlock(AECSBlockIds.REDSTONE_CRYSTAL_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final RegistryBlock<Block> PURE_QUANTUM_CRYSTAL_BLOCK = registerCrystalBlock(AECSBlockIds.QUANTUM_CRYSTAL_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final RegistryBlock<Block> PURE_ROSE_QUARTZ_BLOCK = registerCrystalBlock(AECSBlockIds.ROSE_QUARTZ_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final RegistryBlock<Block> IRRADIATED_CRYSTAL_BLOCK = registerCrystalBlock(AECSBlockIds.IRRADIATED_CRYSTAL_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));

    /**
     * 硅块
     */
    public static final RegistryBlock<Block> SILICON_BLOCK = registerOtherBlock(AECSBlockIds.SILICON_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));

    /**
     * 赛特斯石英矿石
     */
    public static final RegistryBlock<CertusQuartzOreBlock> CERTUS_QUARTZ_ORE =
            registerOtherBlock(AECSBlockIds.CERTUS_QUARTZ_ORE,
                    () -> new CertusQuartzOreBlock(
                            copy(Blocks.STONE)
                                    .strength(3, 5)
                                    .requiresCorrectToolForDrops()
                    ));

    /**
     * 深层赛特斯石英矿石
     */
    public static final RegistryBlock<CertusQuartzOreBlock> DEEPSLATE_CERTUS_QUARTZ_ORE =
            registerOtherBlock(AECSBlockIds.DEEPSLATE_CERTUS_QUARTZ_ORE,
                    () -> new CertusQuartzOreBlock(
                            copy(Blocks.DEEPSLATE)
                                    .strength(4.5f, 7.5f)
                                    .requiresCorrectToolForDrops()
                    ));

    /**
     * 充能赛特斯石英矿石
     */
    public static final RegistryBlock<ChargedCertusQuartzOreBlock> CHARGED_CERTUS_QUARTZ_ORE =
            registerOtherBlock(AECSBlockIds.CHARGED_CERTUS_QUARTZ_ORE,
                    () -> new ChargedCertusQuartzOreBlock(
                            copy(Blocks.STONE)
                                    .strength(3, 5)
                                    .requiresCorrectToolForDrops()
                                    .lightLevel(value -> 7)
                    ));

    /**
     * 深层充能赛特斯石英矿石
     */
    public static final RegistryBlock<ChargedCertusQuartzOreBlock> DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE =
            registerOtherBlock(AECSBlockIds.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE,
                    () -> new ChargedCertusQuartzOreBlock(copy(Blocks.DEEPSLATE)
                            .strength(4.5f, 7.5f)
                            .requiresCorrectToolForDrops()
                            .lightLevel(value -> 7)
                    ));

    /**
     * 水晶催生仓
     */
    public static final RegistryBlock<CrystalGrowthChamberBlock> CRYSTAL_GROWTH_CHAMBER_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_GROWTH_CHAMBER, () -> new CrystalGrowthChamberBlock(copy(Blocks.IRON_BLOCK)));

    /**
     * 电路蚀刻器
     */
    public static final RegistryBlock<CircuitEtcherBlock> CIRCUIT_ETCHER_BLOCK = registerOtherBlock(AECSBlockIds.CIRCUIT_ETCHER, () -> new CircuitEtcherBlock(copy(Blocks.IRON_BLOCK)));

    /**
     * 石英磨具
     */
    public static final RegistryBlock<QuartzGrindstoneBlock> QUARTZ_GRINDSTONE_BLOCK = registerOtherBlock(AECSBlockIds.QUARTZ_GRINDSTONE, () -> new QuartzGrindstoneBlock(copy(Blocks.STONE)));

    /**
     * 晶能粉碎机
     */
    public static final RegistryBlock<CrystalPulverizerBlock> CRYSTAL_PULVERIZER_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_PULVERIZER, () -> new CrystalPulverizerBlock(copy(Blocks.IRON_BLOCK)));

    /**
     * 晶能谐振器
     */
    public static final RegistryBlock<CrystalVibrationChamberBlock> CRYSTAL_VIBRATION_CHAMBER_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_VIBRATION_CHAMBER, () -> new CrystalVibrationChamberBlock(AEBaseBlock.metalProps().strength(4.5f)));

    /**
     * 水晶聚合器
     */
    public static final RegistryBlock<CrystalAggregatorBlock> CRYSTAL_AGGREGATOR_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_AGGREGATOR, () -> new CrystalAggregatorBlock(AEBaseBlock.metalProps()));

    /**
     * 熵变反应仓
     */
    public static final RegistryBlock<EntropyVariationReactionChamberBlock> ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK = registerOtherBlock(AECSBlockIds.ENTROPY_VARIATION_REACTION_CHAMBER, () -> new EntropyVariationReactionChamberBlock(AEBaseBlock.metalProps()));

    /**
     * 末影广播装置
     */
    public static final RegistryBlock<EnderBroadcasterBlock> ENDER_BROADCASTER_BLOCK = registerOtherBlock(AECSBlockIds.ENDER_BROADCASTER, () -> new EnderBroadcasterBlock(AEBaseBlock.metalProps()));

    /**
     * 末影发信器
     */
    public static final RegistryBlock<EnderEmitterBlock> ENDER_EMITTER_BLOCK = registerOtherBlock(AECSBlockIds.ENDER_EMITTER, () -> new EnderEmitterBlock(AEBaseBlock.metalProps()));

    /**
     * 末影接口
     */
    public static final RegistryBlock<InterfaceBlock> ENDER_INTERFACE_BLOCK = registerOtherBlock(AECSBlockIds.ENDER_INTERFACE, InterfaceBlock::new);

    /**
     * 扩展末影接口
     */
    public static final RegistryBlock<InterfaceBlock> EX_ENDER_INTERFACE_BLOCK = registerOtherBlock(AECSBlockIds.EX_ENDER_INTERFACE, InterfaceBlock::new);

    /**
     * ME集成接口
     */
    public static final RegistryBlock<IntegratedInterfaceBlock> INTEGRATED_INTERFACE_BLOCK = registerOtherBlock(AECSBlockIds.INTEGRATED_INTERFACE, () -> new IntegratedInterfaceBlock(AEBaseBlock.metalProps()));

    /**
     * 扩展ME集成接口
     */
    public static final RegistryBlock<IntegratedInterfaceBlock> EX_INTEGRATED_INTERFACE_BLOCK = registerOtherBlock(AECSBlockIds.EX_INTEGRATED_INTERFACE, () -> new IntegratedInterfaceBlock(AEBaseBlock.metalProps()));

    /**
     * 谐振样板供应器
     */
    public static final RegistryBlock<PatternProviderBlock> RESONATING_PATTERN_PROVIDER_BLOCK = registerOtherBlock(AECSBlockIds.RESONATING_PATTERN_PROVIDER, PatternProviderBlock::new);

    /**
     * 扩展谐振样板供应器
     */
    public static final RegistryBlock<PatternProviderBlock> EX_RESONATING_PATTERN_PROVIDER_BLOCK = registerOtherBlock(AECSBlockIds.EX_RESONATING_PATTERN_PROVIDER, PatternProviderBlock::new);

    /**
     * 初级样板供应器
     */
    public static final RegistryBlock<PatternProviderBlock> SIMPLE_PATTERN_PROVIDER_BLOCK = registerOtherBlock(AECSBlockIds.SIMPLE_PATTERN_PROVIDER, PatternProviderBlock::new);

    /**
     * 陨石样板供应器
     */
    public static final RegistryBlock<PatternProviderBlock> METEORITE_PATTERN_PROVIDER_BLOCK = registerOtherBlock(AECSBlockIds.METEORITE_PATTERN_PROVIDER, PatternProviderBlock::new);

    /**
     * 石英震荡钟
     */
    public static final RegistryBlock<QuartzOscillatorClockBlock> QUARTZ_OSCILLATOR_CLOCK_BLOCK = registerOtherBlock(AECSBlockIds.QUARTZ_OSCILLATOR_CLOCK, () -> new QuartzOscillatorClockBlock(AEBaseBlock.metalProps()));


    public static BlockBehaviour.Properties copy(BlockBehaviour behaviour)
    {
        return BlockBehaviour.Properties.copy(behaviour);
    }

    // getter
    public static List<RegistryBlock<? extends Block>> getALL()
    {
        return Collections.unmodifiableList(ALL);
    }

    public static List<RegistryBlock<? extends Block>> getOthers()
    {
        return Collections.unmodifiableList(OTHERS);
    }

    public static List<RegistryBlock<? extends Block>> getCrystalBlocks()
    {
        return CRYSTAL_BLOCKS;
    }

    public static List<RegistryBlock<? extends Block>> getNotSelfDrop()
    {
        return NOT_SELF_DROP;
    }

    // 工具方法
    private static <T extends Block> RegistryBlock<T> registerNotSelfDropBlock(String name, Supplier<T> block)
    {
        RegistryBlock<T> toReturn = registerBlock(name, block);
        NOT_SELF_DROP.add(toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryBlock<T> registerOtherBlock(String name, Supplier<T> block)
    {
        RegistryBlock<T> toReturn = registerBlock(name, block);
        OTHERS.add(toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryBlock<T> registerCrystalBlock(String name, Supplier<T> block)
    {
        RegistryBlock<T> toReturn = registerBlock(name, block);
        CRYSTAL_BLOCKS.add(toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryBlock<T> registerBlock(String name, Supplier<T> block)
    {
        RegistryBlock<T> toReturn = registerOnlyBlock(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryBlock<T> registerOnlyBlock(String name, Supplier<T> block)
    {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        RegistryBlock<T> registryBlock = new RegistryBlock<>(toReturn);
        ALL.add(registryBlock);
        return registryBlock;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryBlock<T> block)
    {
        AECSItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // 注册监听
    public static void register(IEventBus eventBus)
    {
        BLOCKS.register(eventBus);
    }
}
