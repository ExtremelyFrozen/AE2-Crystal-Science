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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResonatingPatternProviderUpgradeItem extends UpgradeItem
{

    public ResonatingPatternProviderUpgradeItem(Properties properties)
    {
        super(properties);

        registerBlockReplaceInfo(BlockDefinitionSupplier.of(AEBlocks.PATTERN_PROVIDER), AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK);
        registerPartReplaceInfo(AEParts.PATTERN_PROVIDER::asItem, AECSParts.RESONATING_PATTERN_PROVIDER_PART);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("ae2cs.item.resonating_pattern_provider_upgrade.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
