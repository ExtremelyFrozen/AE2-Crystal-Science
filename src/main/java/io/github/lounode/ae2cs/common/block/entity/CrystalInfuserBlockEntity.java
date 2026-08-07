package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.cap.ProvideCaps;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;
import io.github.lounode.ae2cs.common.recipe.crystal_infuser.CrystalInfuserRecipe;
import io.github.lounode.ae2cs.common.recipe.input.FourItemStackRecipeInput;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@ProvideCaps(IItemHandler.class)
@ProvideCaps(IFluidHandler.class)
public class CrystalInfuserBlockEntity extends AENetworkedSelfPoweredBlockEntity implements IUpgradeableObject,
                                       CustomReturnableSubMenuHost,
                                       IFluidHandler {

    private static final double BASIC_ENERGY_COST_PER_TICK = 200;
    private static final int FLUID_PER_OPERATION = 1000;

    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.CRYSTAL_INFUSER_BLOCK,
            4, this::onUpgradesChanged);

    private final FluidTank fluidTank = new FluidTank(4000,
            fluid -> fluid.is(net.minecraft.world.level.material.Fluids.WATER)) {

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private int speedMultiplier = 1;
    private int overclockCards;

    @Nullable
    private RecipeHolder<CrystalInfuserRecipe> activeRecipe;

    @Nullable
    private ResourceLocation activeRecipeId;

    private int[] activeMatch;
    private int activeRecipeEnergyCost;
    private int recipeProgress;
    private boolean needRefreshRecipeState = true;

    public CrystalInfuserBlockEntity(BlockPos pos, BlockState blockState) {
        super(AECSBlockEntities.CRYSTAL_INFUSER_BLOCK_ENTITY.get(), pos, blockState,
                128000, false, AccessRestriction.WRITE);
        getMainNode().setIdlePowerUsage(0);

        AppEngInternalInventory input = new AppEngInternalInventory(4) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                needRefreshRecipeState = true;
                setChanged();
            }
        };
        AppEngInternalInventory output = new AppEngInternalInventory(1) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
            }
        };

        AppEngInvComponent invComponent = new AppEngInvComponent();
        invComponent.addPort(InvPort.INPUT, input);
        invComponent.addPort(InvPort.WORK, input);
        invComponent.addPort(InvPort.OUTPUT, output);
        getMachineComponents().add(invComponent);
        getMachineComponents().add(new SideConfigComponent());
    }

    public AppEngInternalInventory getInputInv() {
        return getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.INPUT);
    }

    public AppEngInternalInventory getOutputInv() {
        return getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.OUTPUT);
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getActiveRecipeEnergyCost() {
        return activeRecipeEnergyCost;
    }

    public void checkActive(boolean active) {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (state.hasProperty(AECSBlockProperties.ACTIVE) && state.getValue(AECSBlockProperties.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(AECSBlockProperties.ACTIVE, active), 2);
        }
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    private void onUpgradesChanged() {
        overclockCards = Math.min(2, upgrades.getInstalledUpgrades(AECSItems.OVERLOAD_CARD));
        speedMultiplier = overclockCards > 0 ? 1 : 1 << Math.min(4, upgrades.getInstalledUpgrades(AEItems.SPEED_CARD));
        saveChanges();
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (level == null || level.isClientSide()) return;

        if (needRefreshRecipeState) {
            updateActiveRecipe();
            needRefreshRecipeState = false;
        }
        checkActive(activeRecipe != null && getAECurrentPower() > 0 && fluidTank.getFluidAmount() >= FLUID_PER_OPERATION);

        if (activeRecipe == null || activeMatch == null) {
            recipeProgress = 0;
            return;
        }

        CrystalInfuserRecipe recipe = activeRecipe.value();
        if (recipeProgress < activeRecipeEnergyCost) {
            if (getAECurrentPower() <= 0 || fluidTank.getFluidAmount() < FLUID_PER_OPERATION) return;

            double neededEnergy = Math.min(getEnergyPerTick(), activeRecipeEnergyCost - recipeProgress);
            double actualCost = extractAEPower(neededEnergy, Actionable.MODULATE);
            recipeProgress = Math.min(recipeProgress + (int) actualCost, activeRecipeEnergyCost);
            setChanged();
        }

        if (recipeProgress >= activeRecipeEnergyCost) {
            FourItemStackRecipeInput input = getRecipeInput();
            ItemStack result = recipe.assemble(input, level.registryAccess());
            if (result.isEmpty()) {
                clearRecipeState();
                return;
            }

            if (!getOutputInv().insertItem(0, result, true).isEmpty()) {
                return;
            }

            if (!consumeInputs(recipe, activeMatch)) {
                clearRecipeState();
                return;
            }

            fluidTank.drain(FLUID_PER_OPERATION, IFluidHandler.FluidAction.EXECUTE);
            getOutputInv().insertItem(0, result, false);
            recipeProgress = 0;
            setChanged();
        }
    }

    private double getEnergyPerTick() {
        double normalEnergy = BASIC_ENERGY_COST_PER_TICK * speedMultiplier;
        if (overclockCards == 0 || activeRecipeEnergyCost <= 0) return normalEnergy;

        int targetTicks = overclockCards == 1 ? 4 : 1;
        return Math.max(normalEnergy, Math.ceil((double) activeRecipeEnergyCost / targetTicks));
    }

    private FourItemStackRecipeInput getRecipeInput() {
        return FourItemStackRecipeInput.of(
                getInputInv().getStackInSlot(0),
                getInputInv().getStackInSlot(1),
                getInputInv().getStackInSlot(2),
                getInputInv().getStackInSlot(3));
    }

    private void updateActiveRecipe() {
        if (level == null || level.isClientSide()) return;

        FourItemStackRecipeInput input = getRecipeInput();
        Optional<RecipeHolder<CrystalInfuserRecipe>> match = level.getRecipeManager().getRecipeFor(
                AECSRecipeTypes.CRYSTAL_INFUSER.get(), input, level);
        if (match.isEmpty()) {
            clearRecipeState();
            return;
        }

        RecipeHolder<CrystalInfuserRecipe> holder = match.get();
        int[] slotMatch = holder.value().findMatch(input);
        if (slotMatch == null) {
            clearRecipeState();
            return;
        }

        if (activeRecipe == null || !activeRecipe.id().equals(holder.id())) {
            recipeProgress = 0;
        }
        activeRecipe = holder;
        activeMatch = slotMatch;
        activeRecipeEnergyCost = holder.value().energyCost();
    }

    private void clearRecipeState() {
        activeRecipe = null;
        activeMatch = null;
        activeRecipeEnergyCost = 0;
        recipeProgress = 0;
    }

    private boolean consumeInputs(CrystalInfuserRecipe recipe, int[] match) {
        List<SizedIngredient> required = recipe.required();
        for (int i = 0; i < required.size(); i++) {
            SizedIngredient ingredient = required.get(i);
            int slot = match[i];
            ItemStack stack = getInputInv().getStackInSlot(slot);
            if (stack.getCount() < ingredient.count() || !ingredient.ingredient().test(stack)) return false;
        }
        for (int i = 0; i < required.size(); i++) {
            getInputInv().extractItem(match[i], required.get(i).count(), false);
        }
        return true;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        upgrades.writeToNBT(data, "upgrades", registries);
        data.put("fluid_tank", fluidTank.writeToNBT(registries, new CompoundTag()));
        data.putInt("recipe_progress", recipeProgress);
        if (activeRecipe != null) {
            data.putString("active_recipe_id", activeRecipe.id().toString());
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        upgrades.readFromNBT(data, "upgrades", registries);
        if (data.contains("fluid_tank")) {
            fluidTank.readFromNBT(registries, data.getCompound("fluid_tank"));
        }
        recipeProgress = data.getInt("recipe_progress");
        if (data.contains("active_recipe_id")) {
            activeRecipeId = ResourceLocation.parse(data.getString("active_recipe_id"));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onLoad() {
        super.onLoad();
        onUpgradesChanged();
        if (activeRecipeId != null && level != null) {
            level.getRecipeManager().byKey(activeRecipeId).ifPresent(holder -> {
                if (holder.value() instanceof CrystalInfuserRecipe) {
                    activeRecipe = (RecipeHolder<CrystalInfuserRecipe>) (RecipeHolder<?>) holder;
                }
            });
        }
        updateActiveRecipe();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack stack : upgrades) {
            drops.add(stack);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        upgrades.clear();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getItemFromBlockEntity());
    }

    @Override
    public int getTanks() {
        return fluidTank.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluidTank.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluidTank.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return fluidTank.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        return fluidTank.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        return fluidTank.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        return fluidTank.drain(maxDrain, action);
    }
}
