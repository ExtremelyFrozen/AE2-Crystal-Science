package io.github.lounode.ae2cs.common.item.tools;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public enum AECSToolType
{
    METEOR("meteor", (int) (Tiers.NETHERITE.getUses() * 0.9), Tiers.DIAMOND.getSpeed(), Tiers.NETHERITE.getAttackDamageBonus(),
            Tiers.NETHERITE.getIncorrectBlocksForDrops(), Tiers.NETHERITE.getEnchantmentValue(),
            () -> Ingredient.of(AECSItems.PURE_METEOR_CRYSTAL)),
    ENDER("ender", Tiers.DIAMOND.getUses(), Tiers.DIAMOND.getSpeed(), Tiers.DIAMOND.getAttackDamageBonus(),
            Tiers.DIAMOND.getIncorrectBlocksForDrops(), Tiers.DIAMOND.getEnchantmentValue(),
            () -> Ingredient.of(AECSItems.PURE_ENDER_QUARTZ)),
    RESONATING("resonating", (int) (Tiers.NETHERITE.getUses() * 1.8), Tiers.GOLD.getSpeed(), Tiers.NETHERITE.getAttackDamageBonus(),
            Tiers.NETHERITE.getIncorrectBlocksForDrops(), Tiers.NETHERITE.getEnchantmentValue(),
            () -> Ingredient.of(AECSItems.PURE_RESONATING_CRYSTAL));


    private final String name;
    private final Tier toolTier;

    AECSToolType(String name, int uses, float speed, float attackDamageBonus, TagKey<Block> incorrectBlocksForDrops,
                 int enchantmentValue, Supplier<Ingredient> repairIngredient)
    {
        this.name = name;
        this.toolTier = new Tier()
        {
            @Override
            public int getUses()
            {
                return uses;
            }

            @Override
            public float getSpeed()
            {
                return speed;
            }

            @Override
            public float getAttackDamageBonus()
            {
                return attackDamageBonus;
            }

            @Override
            public TagKey<Block> getIncorrectBlocksForDrops()
            {
                return incorrectBlocksForDrops;
            }

            @Override
            public int getEnchantmentValue()
            {
                return enchantmentValue;
            }

            @Override
            public Ingredient getRepairIngredient()
            {
                return repairIngredient.get();
            }

            @Override
            public String toString()
            {
                return AECSConstants.MODID + ":" + name;
            }
        };
    }

    public final String getName()
    {
        return name;
    }

    public final Tier getToolTier()
    {
        return toolTier;
    }
}
