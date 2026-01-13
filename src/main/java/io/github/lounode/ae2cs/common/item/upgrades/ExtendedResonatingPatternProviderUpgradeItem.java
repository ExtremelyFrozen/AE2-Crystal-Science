package io.github.lounode.ae2cs.common.item.upgrades;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSParts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExtendedResonatingPatternProviderUpgradeItem extends UpgradeItem
{
    public ExtendedResonatingPatternProviderUpgradeItem(Properties properties)
    {
        super(properties);

        registerBlockReplaceInfo(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK, AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK);
        registerPartReplaceInfo(AECSParts.RESONATING_PATTERN_PROVIDER_PART, AECSParts.EX_RESONATING_PATTERN_PROVIDER_PART);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("ae2cs.item.extended_resonating_pattern_provider_upgrade.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
