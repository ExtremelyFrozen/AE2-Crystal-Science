package io.github.lounode.ae2cs.common.item;

import appeng.items.parts.PartItem;
import io.github.lounode.ae2cs.common.me.part.ResonatingPatternProviderPart;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class ResonatingPatternProviderPartItem extends PartItem<ResonatingPatternProviderPart> implements IResonatingTargetModeItem
{
    public ResonatingPatternProviderPartItem(Properties properties)
    {
        super(properties, ResonatingPatternProviderPart.class, ResonatingPatternProviderPart::new);
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        return ResonatingProviderItemHelper.onItemUseFirst(stack, context);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context)
    {
        return super.useOn(context);
    }

    @Override
    public void scrollSelectedInputAndToast(Player player, ItemStack stack, boolean next)
    {
        ResonatingProviderItemHelper.scrollSelectedInputAndToast(player, stack, next);
    }
}
