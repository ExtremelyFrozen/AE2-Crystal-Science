package io.github.lounode.ae2cs.common.item.tools;

import appeng.core.localization.GuiText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * copy from applied energistics 2, and modified to fit 1.20.1
 */
public final class IntrinsicEnchantment
{
    private final Enchantment enchantment;
    private final int level;

    public IntrinsicEnchantment(Enchantment enchantment, int level)
    {
        this.enchantment = enchantment;
        this.level = level;
    }

    public void appendHoverText(List<Component> tooltipComponents)
    {
        tooltipComponents.add(GuiText.IntrinsicEnchant.text(enchantment.getFullname(level)));
    }

    public int getLevel(Enchantment enchantment)
    {
        return enchantment == this.enchantment ? level : 0;
    }
}