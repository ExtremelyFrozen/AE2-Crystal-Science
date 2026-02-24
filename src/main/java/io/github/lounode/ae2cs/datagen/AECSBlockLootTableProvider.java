package io.github.lounode.ae2cs.datagen;

import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AECSBlockLootTableProvider extends BlockLootSubProvider
{
    protected AECSBlockLootTableProvider()
    {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate()
    {
        for (RegistryObject<? extends Block> block : AECSBlocks.getALL())
        {
            if (AECSBlocks.getNotSelfDrop().contains(block)) continue;
            dropSelf(block.get());
        }

        add(AECSBlocks.CERTUS_QUARTZ_ORE.get(),
                createOreLikeDrops(AECSBlocks.CERTUS_QUARTZ_ORE.get(), AEItems.CERTUS_QUARTZ_CRYSTAL, 1.0f, 2.0f));
        add(AECSBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE.get(),
                createOreLikeDrops(AECSBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE.get(), AEItems.CERTUS_QUARTZ_CRYSTAL, 1.0f, 2.0f));

        add(AECSBlocks.CHARGED_CERTUS_QUARTZ_ORE.get(),
                createOreLikeDrops(AECSBlocks.CHARGED_CERTUS_QUARTZ_ORE.get(), AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 1.0f, 2.0f));
        add(AECSBlocks.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE.get(),
                createOreLikeDrops(AECSBlocks.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE.get(), AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 1.0f, 2.0f));

    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks()
    {
        return AECSBlocks.BLOCKS.getEntries().stream().flatMap(RegistryObject::stream)::iterator;
    }

    /**
     * 生成类矿石掉落表
     *
     * @param selfBlock 方块本体
     * @param dropItem  目标掉落物
     * @param min       不附加时运情况下的最少掉落
     * @param max       不附加时运情况下的最大掉落
     */
    protected LootTable.Builder createOreLikeDrops(Block selfBlock, ItemLike dropItem, float min, float max)
    {
        // 精准采集掉落自身
        // 普通采集掉落min~max个，按时运倍增
        return this.createSilkTouchDispatchTable(
                selfBlock,
                this.applyExplosionDecay(
                        selfBlock,
                        LootItem.lootTableItem(dropItem)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
                )
        );
    }
}
