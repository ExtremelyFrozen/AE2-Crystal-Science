package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSBlockIds;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.CrystalGrowthChamberBlock;
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

    public static final DeferredBlock<CrystalGrowthChamberBlock> CRYSTAL_GROWTH_CHAMBER_BLOCK = registerOtherBlock(AECSBlockIds.CRYSTAL_GROWTH_CHAMBER, () -> new CrystalGrowthChamberBlock(copy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> circuitEtcher = registerOtherBlock(AECSBlockIds.CIRCUIT_ETCHER, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> quartzGrindstone = registerOtherBlock(AECSBlockIds.QUARTZ_GRINDSTONE, () -> new Block(copy(Blocks.STONE)));
    public static final DeferredBlock<Block> crystalVibrationChamber = registerOtherBlock(AECSBlockIds.CRYSTAL_VIBRATION_CHAMBER, () -> new Block(copy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> crusher = registerOtherBlock(AECSBlockIds.CRUSHER, () -> new Block(copy(Blocks.IRON_BLOCK)));


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
