package io.github.lounode.ae2cs.common.item.tools.resonating;

import appeng.hooks.IntrinsicEnchantItem;
import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.IntrinsicEnchantment;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ResonatingAxeItem extends Item implements LinkableTool, IntrinsicEnchantItem {
    private final IntrinsicEnchantment intrinsicEnchantment = new IntrinsicEnchantment(Enchantments.SHARPNESS, 3);

    public ResonatingAxeItem(Properties properties) {
        super(properties.axe(AECSToolType.RESONATING.getToolMaterial(), 5.0F, -3.0F)
                .repairable(AECSToolType.RESONATING.getRepairIngredient()));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull TooltipDisplay display, @NonNull Consumer<Component> builder,
                                @NotNull TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, display, builder, advancedTooltips);
        intrinsicEnchantment.appendHoverText(context, builder);
    }

    @Override
    public int getIntrinsicEnchantLevel(ItemInstance instance, Holder<Enchantment> enchantment) {
        return intrinsicEnchantment.getLevel(enchantment);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }
}
