package io.github.lounode.ae2cs.common.item.upgrades;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import io.github.lounode.ae2cs.api.util.BlockDefinitionSupplier;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSParts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PatternProviderUpgradeItem extends UpgradeItem
{

    public PatternProviderUpgradeItem(Properties properties)
    {
        super(properties);

        registerBlockReplaceInfo(AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK, BlockDefinitionSupplier.of(AEBlocks.PATTERN_PROVIDER));
        registerPartReplaceInfo(AECSParts.SIMPLE_PATTERN_PROVIDER_PART, AEParts.PATTERN_PROVIDER);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("ae2cs.item.pattern_provider_upgrade.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
