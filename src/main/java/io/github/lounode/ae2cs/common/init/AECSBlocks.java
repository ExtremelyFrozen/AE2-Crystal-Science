package io.github.lounode.ae2cs.common.init;

import appeng.block.AEBaseBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.InterfaceBlock;
import io.github.lounode.ae2cs.api.ids.AECSBlockIds;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.*;
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

public class AECSBlocks
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AECSConstants.MODID);

    /**
     * 所有已注册方块
     */
    private static final List<DeferredBlock<? extends Block>> ALL = new ArrayList<>();

    /**
     * 杂项方块
     */
    private static final List<DeferredBlock<? extends Block>> OTHERS = new ArrayList<>();

    /**
     * 水晶催生仓
     */
    public static final DeferredBlock<CrystalGrowthChamberBlock> CRYSTAL_GROWTH_CHAMBER_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_GROWTH_CHAMBER, () -> new CrystalGrowthChamberBlock(copy(Blocks.IRON_BLOCK)));

    /**
     * ME集成接口
     */
    public static final DeferredBlock<IntegratedInterfaceBlock> INTEGRATED_INTERFACE_BLOCK = registerOtherBlock(AECSBlockIds.INTEGRATED_INTERFACE, () -> new IntegratedInterfaceBlock(AEBaseBlock.metalProps()));

    /**
     * 晶能谐振器
     */
    public static final DeferredBlock<CrystalVibrationChamberBlock> CRYSTAL_VIBRATION_CHAMBER_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_VIBRATION_CHAMBER, () -> new CrystalVibrationChamberBlock(AEBaseBlock.metalProps().strength(4.5f)));

    /**
     * 电路蚀刻器
     */
    public static final DeferredBlock<CircuitEtcherBlock> CIRCUIT_ETCHER_BLOCK = registerOtherBlock(AECSBlockIds.CIRCUIT_ETCHER, () -> new CircuitEtcherBlock(copy(Blocks.IRON_BLOCK)));

    /**
     * 陨石样板供应器
     */
    public static final DeferredBlock<PatternProviderBlock> METEORITE_PATTERN_PROVIDER_BLOCK = registerOtherBlock(AECSBlockIds.METEORITE_PATTERN_PROVIDER, PatternProviderBlock::new);

    /**
     * 晶能粉碎机
     */
    public static final DeferredBlock<CrystalPulverizerBlock> CRYSTAL_PULVERIZER_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_PULVERIZER, () -> new CrystalPulverizerBlock(copy(Blocks.IRON_BLOCK)));

    /**
     * 石英磨具
     */
    public static final DeferredBlock<QuartzGrindstoneBlock> QUARTZ_GRINDSTONE_BLOCK = registerOtherBlock(AECSBlockIds.QUARTZ_GRINDSTONE, () -> new QuartzGrindstoneBlock(copy(Blocks.STONE)));

    /**
     * 初级样板供应器
     */
    public static final DeferredBlock<PatternProviderBlock> SIMPLE_PATTERN_PROVIDER_BLOCK = registerOtherBlock(AECSBlockIds.SIMPLE_PATTERN_PROVIDER, PatternProviderBlock::new);

    /**
     * 水晶聚合器
     */
    public static final DeferredBlock<CrystalAggregatorBlock> CRYSTAL_AGGREGATOR_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_AGGREGATOR, () -> new CrystalAggregatorBlock(AEBaseBlock.metalProps()));

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


    public static BlockBehaviour.Properties copy(BlockBehaviour behaviour)
    {
        return BlockBehaviour.Properties.ofFullCopy(behaviour);
    }

    // getter
    public static List<DeferredBlock<? extends Block>> getALL()
    {
        return Collections.unmodifiableList(ALL);
    }

    public static List<DeferredBlock<? extends Block>> getOTHERS()
    {
        return Collections.unmodifiableList(OTHERS);
    }

    // 工具方法
    private static <T extends Block> DeferredBlock<T> registerOtherBlock(String name, Supplier<T> block)
    {
        DeferredBlock<T> toReturn = registerBlock(name, block);
        OTHERS.add(toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block)
    {
        DeferredBlock<T> toReturn = registerOnlyBlock(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerOnlyBlock(String name, Supplier<T> block)
    {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ALL.add(toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block)
    {
        AECSItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // 注册监听
    public static void register(IEventBus eventBus)
    {
        BLOCKS.register(eventBus);
    }
}
