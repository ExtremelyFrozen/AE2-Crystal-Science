package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.machine.ResonatingFactoryParallelism;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 谐振电路蚀刻工厂，使用九个输入槽和三个输出槽批量加工普通蚀刻配方。
 */
public class ResonatingCircuitEtcherFactoryBlockEntity extends CircuitEtcherBlockEntity {

    public ResonatingCircuitEtcherFactoryBlockEntity(BlockPos pos, BlockState blockState) {
        super(AECSBlockEntities.RESONATING_CIRCUIT_ETCHER_FACTORY_BLOCK_ENTITY.get(), pos, blockState,
                AECSBlocks.RESONATING_CIRCUIT_ETCHER_FACTORY_BLOCK, 9, 3);
    }

    @Override
    public int getParallelism() {
        return ResonatingFactoryParallelism.forCards(getUpgrades().getInstalledUpgrades(AECSItems.OVERLOAD_CARD));
    }

    @Override
    protected Optional<RecipeHolder<CircuitEtcherRecipe>> findRecipe(Level level) {
        return level.getRecipeManager().byType(AECSRecipeTypes.CIRCUIT_ETCHER.get()).stream()
                .filter(holder -> findRecipeMatch(holder.value()) != null)
                .findFirst();
    }

    @Override
    protected int @Nullable [] findRecipeMatch(CircuitEtcherRecipe recipe) {
        int[] match = new int[recipe.required().size()];
        return match.length > 0 && matchInputs(recipe, match, 0) ? match : null;
    }

    private boolean matchInputs(CircuitEtcherRecipe recipe, int[] match, int ingredient) {
        if (ingredient == match.length) return true;
        for (int slot = 0; slot < getInputInv().size(); slot++) {
            boolean used = false;
            for (int previous = 0; previous < ingredient; previous++) {
                if (match[previous] == slot) {
                    used = true;
                    break;
                }
            }
            if (used || !recipe.required().get(ingredient).test(getInputInv().getStackInSlot(slot))) continue;
            match[ingredient] = slot;
            if (matchInputs(recipe, match, ingredient + 1)) return true;
        }
        return false;
    }
}
