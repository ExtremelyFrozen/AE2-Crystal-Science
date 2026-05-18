package io.github.lounode.ae2cs.common.item.upgrades;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
//import io.github.lounode.ae2cs.common.init.AECSParts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ExtendedEnderInterfaceUpgradeItem extends UpgradeItem
{
    public ExtendedEnderInterfaceUpgradeItem(Properties properties)
    {
        super(properties);

        registerBlockReplaceInfo(AECSBlocks.ENDER_INTERFACE_BLOCK, AECSBlocks.EX_ENDER_INTERFACE_BLOCK);
//        registerPartReplaceInfo(AECSParts.ENDER_INTERFACE_PART, AECSParts.EX_ENDER_INTERFACE_PART);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder,
                                @NonNull TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);

        builder.accept(Component.translatable("ae2cs.item.extended_ender_interface_upgrade.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
