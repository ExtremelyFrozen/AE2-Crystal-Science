package io.github.lounode.ae2cs.integration.jei;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MachineRecipeTransferInfo<C extends AEBaseMenu, R> implements IRecipeTransferInfo<C, R> {

    private final Class<? extends C> menuClass;
    private final MenuType<C> menuType;
    private final RecipeType<R> recipeType;

    public MachineRecipeTransferInfo(Class<? extends C> menuClass, MenuType<C> menuType, RecipeType<R> recipeType) {
        this.menuClass = menuClass;
        this.menuType = menuType;
        this.recipeType = recipeType;
    }

    @Override
    public Class<? extends C> getContainerClass() {
        return menuClass;
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return Optional.of(menuType);
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return recipeType;
    }

    @Override
    public boolean canHandle(C menu, R recipe) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(C menu, R recipe) {
        return menu.getSlots(SlotSemantics.MACHINE_INPUT);
    }

    @Override
    public List<Slot> getInventorySlots(C menu, R recipe) {
        var slots = new ArrayList<Slot>();
        slots.addAll(menu.getSlots(SlotSemantics.PLAYER_INVENTORY));
        slots.addAll(menu.getSlots(SlotSemantics.PLAYER_HOTBAR));
        return slots;
    }
}
