package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSBlockIds;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.*;

import appeng.block.AEBaseBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.InterfaceBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class AECSBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AECSConstants.MODID);

    /**
     * 所有已注册方块
     */
    private static final List<DeferredBlock<? extends Block>> ALL = new ArrayList<>();

    /**
     * 水晶块
     */
    private static final List<DeferredBlock<? extends Block>> CRYSTAL_BLOCKS = new ArrayList<>();

    /**
     * 杂项方块
     */
    private static final List<DeferredBlock<? extends Block>> OTHERS = new ArrayList<>();

    /**
     * 非自身掉落式方块 用于datagen避让
     */
    private static final List<DeferredBlock<? extends Block>> NOT_SELF_DROP = new ArrayList<>();

    // -------------------高纯水晶块-----------------
    public static final DeferredBlock<Block> PURE_ENDER_QUARTZ_BLOCK = registerCrystalBlock(AECSBlockIds.ENDER_QUARTZ_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> PURE_RESONATING_CRYSTAL_BLOCK = registerCrystalBlock(AECSBlockIds.RESONATING_CRYSTAL_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> PURE_METEOR_CRYSTAL_BLOCK = registerCrystalBlock(AECSBlockIds.METEOR_CRYSTAL_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> PURE_REDSTONE_CRYSTAL_BLOCK = registerCrystalBlock(AECSBlockIds.REDSTONE_CRYSTAL_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> PURE_QUANTUM_CRYSTAL_BLOCK = registerCrystalBlock(AECSBlockIds.QUANTUM_CRYSTAL_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> PURE_ROSE_QUARTZ_BLOCK = registerCrystalBlock(AECSBlockIds.ROSE_QUARTZ_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> IRRADIATED_CRYSTAL_BLOCK = registerCrystalBlock(AECSBlockIds.IRRADIATED_CRYSTAL_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));

    /**
     * 硅块
     */
    public static final DeferredBlock<Block> SILICON_BLOCK = registerOtherBlock(AECSBlockIds.SILICON_BLOCK, () -> new Block(copy(Blocks.IRON_BLOCK)));

    /**
     * 赛特斯石英矿石
     */
    public static final DeferredBlock<CertusQuartzOreBlock> CERTUS_QUARTZ_ORE = registerOtherBlock(AECSBlockIds.CERTUS_QUARTZ_ORE,
            () -> new CertusQuartzOreBlock(
                    copy(Blocks.STONE)
                            .strength(3, 5)
                            .requiresCorrectToolForDrops()));

    /**
     * 深层赛特斯石英矿石
     */
    public static final DeferredBlock<CertusQuartzOreBlock> DEEPSLATE_CERTUS_QUARTZ_ORE = registerOtherBlock(AECSBlockIds.DEEPSLATE_CERTUS_QUARTZ_ORE,
            () -> new CertusQuartzOreBlock(
                    copy(Blocks.DEEPSLATE)
                            .strength(4.5f, 7.5f)
                            .requiresCorrectToolForDrops()));

    /**
     * 充能赛特斯石英矿石
     */
    public static final DeferredBlock<ChargedCertusQuartzOreBlock> CHARGED_CERTUS_QUARTZ_ORE = registerOtherBlock(AECSBlockIds.CHARGED_CERTUS_QUARTZ_ORE,
            () -> new ChargedCertusQuartzOreBlock(
                    copy(Blocks.STONE)
                            .strength(3, 5)
                            .requiresCorrectToolForDrops()
                            .lightLevel(value -> 7)));

    /**
     * 深层充能赛特斯石英矿石
     */
    public static final DeferredBlock<ChargedCertusQuartzOreBlock> DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE = registerOtherBlock(AECSBlockIds.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE,
            () -> new ChargedCertusQuartzOreBlock(copy(Blocks.DEEPSLATE)
                    .strength(4.5f, 7.5f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(value -> 7)));

    /**
     * 水晶催生仓
     */
    public static final DeferredBlock<CrystalGrowthChamberBlock> CRYSTAL_GROWTH_CHAMBER_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_GROWTH_CHAMBER, () -> new CrystalGrowthChamberBlock(copy(Blocks.IRON_BLOCK)));

    /**
     * 电路蚀刻器
     */
    public static final DeferredBlock<CircuitEtcherBlock> CIRCUIT_ETCHER_BLOCK = registerOtherBlock(AECSBlockIds.CIRCUIT_ETCHER, () -> new CircuitEtcherBlock(copy(Blocks.IRON_BLOCK)));

    /**
     * 石英磨具
     */
    public static final DeferredBlock<QuartzGrindstoneBlock> QUARTZ_GRINDSTONE_BLOCK = registerOtherBlock(AECSBlockIds.QUARTZ_GRINDSTONE, () -> new QuartzGrindstoneBlock(copy(Blocks.STONE)));

    /**
     * 晶能粉碎机
     */
    public static final DeferredBlock<CrystalPulverizerBlock> CRYSTAL_PULVERIZER_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_PULVERIZER, () -> new CrystalPulverizerBlock(copy(Blocks.IRON_BLOCK)));

    /**
     * 晶能谐振器
     */
    public static final DeferredBlock<CrystalVibrationChamberBlock> CRYSTAL_VIBRATION_CHAMBER_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_VIBRATION_CHAMBER, () -> new CrystalVibrationChamberBlock(AEBaseBlock.metalProps().strength(4.5f)));

    /**
     * 水晶聚合器
     */
    public static final DeferredBlock<CrystalAggregatorBlock> CRYSTAL_AGGREGATOR_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_AGGREGATOR, () -> new CrystalAggregatorBlock(AEBaseBlock.metalProps()));

    /**
     * 熵变反应仓
     */
    public static final DeferredBlock<EntropyVariationReactionChamberBlock> ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK = registerOtherBlock(AECSBlockIds.ENTROPY_VARIATION_REACTION_CHAMBER, () -> new EntropyVariationReactionChamberBlock(AEBaseBlock.metalProps()));

    /**
     * 末影广播装置
     */
    public static final DeferredBlock<EnderBroadcasterBlock> ENDER_BROADCASTER_BLOCK = registerOtherBlock(AECSBlockIds.ENDER_BROADCASTER, () -> new EnderBroadcasterBlock(AEBaseBlock.metalProps()));

    /**
     * 末影发信器
     */
    public static final DeferredBlock<EnderEmitterBlock> ENDER_EMITTER_BLOCK = registerOtherBlock(AECSBlockIds.ENDER_EMITTER, () -> new EnderEmitterBlock(AEBaseBlock.metalProps()));

    /**
     * 末影接口
     */
    public static final DeferredBlock<InterfaceBlock> ENDER_INTERFACE_BLOCK = registerOtherBlock(AECSBlockIds.ENDER_INTERFACE, InterfaceBlock::new);

    /**
     * 扩展末影接口
     */
    public static final DeferredBlock<InterfaceBlock> EX_ENDER_INTERFACE_BLOCK = registerOtherBlock(AECSBlockIds.EX_ENDER_INTERFACE, InterfaceBlock::new);

    /**
     * ME集成接口
     */
    public static final DeferredBlock<IntegratedInterfaceBlock> INTEGRATED_INTERFACE_BLOCK = registerOtherBlock(AECSBlockIds.INTEGRATED_INTERFACE, () -> new IntegratedInterfaceBlock(AEBaseBlock.metalProps()));

    /**
     * 扩展ME集成接口
     */
    public static final DeferredBlock<IntegratedInterfaceBlock> EX_INTEGRATED_INTERFACE_BLOCK = registerOtherBlock(AECSBlockIds.EX_INTEGRATED_INTERFACE, () -> new IntegratedInterfaceBlock(AEBaseBlock.metalProps()));

    /**
     * 谐振样板供应器
     */
    public static final DeferredBlock<PatternProviderBlock> RESONATING_PATTERN_PROVIDER_BLOCK = registerOtherBlock(AECSBlockIds.RESONATING_PATTERN_PROVIDER, PatternProviderBlock::new);

    /**
     * 扩展谐振样板供应器
     */
    public static final DeferredBlock<PatternProviderBlock> EX_RESONATING_PATTERN_PROVIDER_BLOCK = registerOtherBlock(AECSBlockIds.EX_RESONATING_PATTERN_PROVIDER, PatternProviderBlock::new);

    /**
     * 初级样板供应器
     */
    public static final DeferredBlock<PatternProviderBlock> SIMPLE_PATTERN_PROVIDER_BLOCK = registerOtherBlock(AECSBlockIds.SIMPLE_PATTERN_PROVIDER, PatternProviderBlock::new);

    /**
     * 陨石样板供应器
     */
    public static final DeferredBlock<PatternProviderBlock> METEORITE_PATTERN_PROVIDER_BLOCK = registerOtherBlock(AECSBlockIds.METEORITE_PATTERN_PROVIDER, PatternProviderBlock::new);

    /**
     * 石英震荡钟
     */
    public static final DeferredBlock<QuartzOscillatorClockBlock> QUARTZ_OSCILLATOR_CLOCK_BLOCK = registerOtherBlock(AECSBlockIds.QUARTZ_OSCILLATOR_CLOCK, () -> new QuartzOscillatorClockBlock(AEBaseBlock.metalProps()));

    public static BlockBehaviour.Properties copy(BlockBehaviour behaviour) {
        return BlockBehaviour.Properties.ofFullCopy(behaviour);
    }

    // getter
    public static List<DeferredBlock<? extends Block>> getALL() {
        return Collections.unmodifiableList(ALL);
    }

    public static List<DeferredBlock<? extends Block>> getOthers() {
        return Collections.unmodifiableList(OTHERS);
    }

    public static List<DeferredBlock<? extends Block>> getCrystalBlocks() {
        return CRYSTAL_BLOCKS;
    }

    public static List<DeferredBlock<? extends Block>> getNotSelfDrop() {
        return NOT_SELF_DROP;
    }

    // 工具方法
    private static <T extends Block> DeferredBlock<T> registerNotSelfDropBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = registerBlock(name, block);
        NOT_SELF_DROP.add(toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerOtherBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = registerBlock(name, block);
        OTHERS.add(toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerCrystalBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = registerBlock(name, block);
        CRYSTAL_BLOCKS.add(toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = registerOnlyBlock(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerOnlyBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ALL.add(toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        AECSItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // 注册监听
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
