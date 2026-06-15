package io.github.lounode.ae2cs.common.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

public class ResonatingPatternProviderBlockItem extends BlockItem implements IResonatingTargetModeItem {

    public ResonatingPatternProviderBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return ResonatingProviderItemHelper.onItemUseFirst(stack, context);
    }

    @Override
    public void scrollSelectedInputAndToast(Player player, ItemStack stack, boolean next) {
        ResonatingProviderItemHelper.scrollSelectedInputAndToast(player, stack, next);
    }
}
