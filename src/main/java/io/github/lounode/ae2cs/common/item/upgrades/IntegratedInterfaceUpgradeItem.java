package io.github.lounode.ae2cs.common.item.upgrades;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import io.github.lounode.ae2cs.api.util.BlockDefinitionSupplier;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
//import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.init.AECSParts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public class IntegratedInterfaceUpgradeItem extends UpgradeItem
{

    public IntegratedInterfaceUpgradeItem(Properties properties)
    {
        super(properties);

        registerBlockReplaceInfo(BlockDefinitionSupplier.of(AEBlocks.INTERFACE), AECSBlocks.INTEGRATED_INTERFACE_BLOCK);
        registerBlockReplaceInfo(BlockDefinitionSupplier.of(AEBlocks.PATTERN_PROVIDER), AECSBlocks.INTEGRATED_INTERFACE_BLOCK);
        registerPartReplaceInfo(AEParts.INTERFACE, AECSParts.INTEGRATE_INTERFACE_PART);
        registerPartReplaceInfo(AEParts.PATTERN_PROVIDER, AECSParts.INTEGRATE_INTERFACE_PART);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder,
                                @NonNull TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);

        builder.accept(Component.translatable("ae2cs.item.integrated_interface_upgrade.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
