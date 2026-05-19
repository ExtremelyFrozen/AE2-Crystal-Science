package io.github.lounode.ae2cs.common.item.tools.resonating;

import appeng.hooks.IntrinsicEnchantItem;
import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.IntrinsicEnchantment;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ResonatingShovelItem extends Item implements LinkableTool, IntrinsicEnchantItem {
    private final IntrinsicEnchantment intrinsicEnchantment = new IntrinsicEnchantment(Enchantments.SILK_TOUCH, 1);

    public ResonatingShovelItem(Properties properties) {
        super(properties.shovel(AECSToolType.RESONATING.getToolMaterial(), 1.5F, -3.0F)
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
    public @NotNull ItemEnchantments getAllEnchantments(@NotNull ItemStack stack, HolderLookup.@NotNull RegistryLookup<Enchantment> lookup) {
        // 精准采集只认这一条路径
        // 也许后续可以考虑把精准采集的附魔原生写入，不过这样会影响到tooltip和其他东西
        ItemEnchantments base = super.getAllEnchantments(stack, lookup);
        var opt = lookup.get(Enchantments.SILK_TOUCH);
        if (opt.isEmpty()) return base;

        var silk = opt.get();
        if (base.getLevel(silk) >= 1) return base;

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(base);
        mutable.set(silk, 1);
        return mutable.toImmutable();
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }
}