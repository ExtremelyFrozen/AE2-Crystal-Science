package io.github.lounode.ae2cs.common.item.tools;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum AECSToolType {

    METEOR("meteor", (int) (Tiers.NETHERITE.getUses() * 0.9), Tiers.DIAMOND.getSpeed(), Tiers.NETHERITE.getAttackDamageBonus(),
            Tiers.NETHERITE.getLevel(), Tiers.NETHERITE.getEnchantmentValue(),
            () -> Ingredient.of(WarpAsItemLike.of(AECSItems.PURE_METEOR_CRYSTAL))),
    ENDER("ender", Tiers.DIAMOND.getUses(), Tiers.DIAMOND.getSpeed(), Tiers.DIAMOND.getAttackDamageBonus(),
            Tiers.DIAMOND.getLevel(), Tiers.DIAMOND.getEnchantmentValue(),
            () -> Ingredient.of(WarpAsItemLike.of(AECSItems.PURE_ENDER_QUARTZ))),
    RESONATING("resonating", (int) (Tiers.NETHERITE.getUses() * 1.8), Tiers.GOLD.getSpeed(), Tiers.NETHERITE.getAttackDamageBonus(),
            Tiers.NETHERITE.getLevel(), Tiers.NETHERITE.getEnchantmentValue(),
            () -> Ingredient.of(WarpAsItemLike.of(AECSItems.PURE_RESONATING_CRYSTAL)));

    private final String name;
    private final Tier toolTier;

    AECSToolType(String name, int uses, float speed, float attackDamageBonus, int level,
                 int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.toolTier = new Tier() {

            @Override
            public int getUses() {
                return uses;
            }

            @Override
            public float getSpeed() {
                return speed;
            }

            @Override
            public float getAttackDamageBonus() {
                return attackDamageBonus;
            }

            @Override
            public int getLevel() {
                return level;
            }

            @Override
            public int getEnchantmentValue() {
                return enchantmentValue;
            }

            @Override
            public @NotNull Ingredient getRepairIngredient() {
                return repairIngredient.get();
            }

            @Override
            public String toString() {
                return AECSConstants.MODID + ":" + name;
            }
        };
    }

    public final String getName() {
        return name;
    }

    public final Tier getToolTier() {
        return toolTier;
    }

    public static class WarpAsItemLike implements ItemLike {

        private final @NotNull Supplier<? extends Item> item;

        private WarpAsItemLike(@NotNull Supplier<? extends Item> item) {
            this.item = item;
        }

        public static WarpAsItemLike of(Supplier<? extends Item> item) {
            return new WarpAsItemLike(item);
        }

        @Override
        public @NotNull Item asItem() {
            return item.get();
        }
    }
}
