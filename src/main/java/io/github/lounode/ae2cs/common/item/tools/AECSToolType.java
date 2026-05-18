package io.github.lounode.ae2cs.common.item.tools;

import io.github.lounode.ae2cs.common.init.AECSTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public enum AECSToolType
{
    METEOR("meteor", (int) (ToolMaterial.NETHERITE.durability() * 0.9), ToolMaterial.DIAMOND.speed(), ToolMaterial.NETHERITE.attackDamageBonus(),
            ToolMaterial.NETHERITE.incorrectBlocksForDrops(), ToolMaterial.NETHERITE.enchantmentValue(),
            () -> AECSTags.Items.PURE_METEOR_CRYSTAL),
    ENDER("ender", ToolMaterial.DIAMOND.durability(), ToolMaterial.DIAMOND.speed(), ToolMaterial.DIAMOND.attackDamageBonus(),
            ToolMaterial.DIAMOND.incorrectBlocksForDrops(), ToolMaterial.DIAMOND.enchantmentValue(),
            () ->  AECSTags.Items.PURE_ENDER_QUARTZ),
    RESONATING("resonating", (int) (ToolMaterial.NETHERITE.durability() * 1.8), ToolMaterial.GOLD.speed(), ToolMaterial.NETHERITE.attackDamageBonus(),
            ToolMaterial.NETHERITE.incorrectBlocksForDrops(), ToolMaterial.NETHERITE.enchantmentValue(),
            () ->  AECSTags.Items.PURE_RESONATING_CRYSTAL);


    private final String name;
    private final TagKey<Item> repairIngredient;
    private final ToolMaterial toolMaterial;

    AECSToolType(String name, int durability, float speed, float attackDamageBonus, TagKey<Block> incorrectBlocksForDrops,
                 int enchantmentValue, Supplier<TagKey<Item>> repairIngredient)
    {
        this.name = name;
        this.repairIngredient = repairIngredient.get();
        this.toolMaterial = new ToolMaterial(incorrectBlocksForDrops, durability, speed, attackDamageBonus, enchantmentValue, repairIngredient.get());
    }

    public final String getName()
    {
        return name;
    }

    public TagKey<Item> getRepairIngredient() {
        return repairIngredient;
    }


    public final ToolMaterial getToolMaterial()
    {
        return toolMaterial;
    }
}
