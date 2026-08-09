package io.github.lounode.ae2cs.integration.emi;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;

import net.minecraft.world.inventory.Slot;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;

import java.util.ArrayList;
import java.util.List;

public class MachineEmiRecipeHandler<T extends AEBaseMenu> implements StandardRecipeHandler<T> {

    private final EmiRecipeCategory category;

    public MachineEmiRecipeHandler(EmiRecipeCategory category) {
        this.category = category;
    }

    @Override
    public List<Slot> getInputSources(T menu) {
        var slots = new ArrayList<Slot>();
        slots.addAll(menu.getSlots(SlotSemantics.PLAYER_INVENTORY));
        slots.addAll(menu.getSlots(SlotSemantics.PLAYER_HOTBAR));
        slots.addAll(menu.getSlots(SlotSemantics.MACHINE_INPUT));
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(T menu) {
        return menu.getSlots(SlotSemantics.MACHINE_INPUT);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory().equals(category);
    }
}
