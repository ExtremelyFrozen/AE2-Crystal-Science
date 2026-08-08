package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.cap.ProvideCaps;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.machine.MachineFluidHost;
import io.github.lounode.ae2cs.common.machine.MachineFluidTanks;
import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;
import io.github.lounode.ae2cs.common.recipe.pulse_centrifuge.PulseCentrifugeRecipe;

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
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@ProvideCaps(IItemHandler.class)
@ProvideCaps(IFluidHandler.class)
public class PulseCentrifugeBlockEntity extends AENetworkedSelfPoweredBlockEntity implements IUpgradeableObject,
                                        CustomReturnableSubMenuHost,
                                        MachineFluidHost {

    private static final double BASIC_ENERGY_COST_PER_TICK = 200;
    private static final int FLUID_TANK_CAPACITY = 16000;

    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.PULSE_CENTRIFUGE_BLOCK,
            4, this::onUpgradesChanged);
    private final MachineFluidTanks fluidTanks = new MachineFluidTanks(FLUID_TANK_CAPACITY, this::setChanged,
            this::setChanged);

    private int speedMultiplier = 1;
    private int overloadCards;
    private int activeRecipeEnergyCost;
    private int recipeProgress;
    private boolean needRefreshRecipeState = true;
    private boolean processing;

    @Nullable
    private RecipeHolder<PulseCentrifugeRecipe> activeRecipe;
    @Nullable
    private ResourceLocation activeRecipeId;

    public PulseCentrifugeBlockEntity(BlockPos pos, BlockState blockState) {
        super(AECSBlockEntities.PULSE_CENTRIFUGE_BLOCK_ENTITY.get(), pos, blockState,
                80000, false, AccessRestriction.WRITE);
        getMainNode().setIdlePowerUsage(0);

        AppEngInternalInventory input = new AppEngInternalInventory(1) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                needRefreshRecipeState = true;
                setChanged();
            }
        };
        AppEngInternalInventory output = new AppEngInternalInventory(4) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
            }
        };

        AppEngInvComponent inventory = new AppEngInvComponent();
        inventory.addPort(InvPort.INPUT, input);
        inventory.addPort(InvPort.WORK, input);
        inventory.addPort(InvPort.OUTPUT, output);
        getMachineComponents().add(inventory);
        getMachineComponents().add(new SideConfigComponent());
    }

    public AppEngInternalInventory getInputInv() {
        return getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.INPUT);
    }

    public AppEngInternalInventory getOutputInv() {
        return getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.OUTPUT);
    }

    public MachineFluidTanks getFluidTanks() {
        return fluidTanks;
    }

    @Override
    public IFluidHandler getFluidHandler() {
        return fluidTanks;
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getActiveRecipeEnergyCost() {
        return activeRecipeEnergyCost;
    }

    public boolean isProcessing() {
        return processing;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    private void onUpgradesChanged() {
        overloadCards = Math.min(2, upgrades.getInstalledUpgrades(AECSItems.OVERLOAD_CARD));
        speedMultiplier = overloadCards > 0 ? 1 : 1 << Math.min(4, upgrades.getInstalledUpgrades(AEItems.SPEED_CARD));
        saveChanges();
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (level == null || level.isClientSide()) return;

        setProcessing(processRecipeTick());
    }

    private boolean processRecipeTick() {
        if (needRefreshRecipeState || recipeCacheChanged() || activeRecipe == null && level.getGameTime() % 20 == 0) {
            updateActiveRecipe();
            needRefreshRecipeState = false;
        }
        if (activeRecipe == null) {
            recipeProgress = 0;
            return false;
        }

        PulseCentrifugeRecipe recipe = activeRecipe.value();
        List<ItemStack> outputPlan = planOutputInsertion(getOutputInv(), recipe.results());
        if (outputPlan == null) return false;

        boolean consumedEnergy = false;
        if (recipeProgress < activeRecipeEnergyCost) {
            if (getAECurrentPower() <= 0) return false;

            double neededEnergy = Math.min(getEnergyPerTick(), activeRecipeEnergyCost - recipeProgress);
            int availableEnergy = (int) Math.floor(extractAEPower(neededEnergy, Actionable.SIMULATE));
            if (availableEnergy <= 0) return false;
            int actualCost = (int) Math.floor(extractAEPower(availableEnergy, Actionable.MODULATE));
            if (actualCost <= 0) return false;

            recipeProgress = Math.min(recipeProgress + actualCost, activeRecipeEnergyCost);
            consumedEnergy = true;
            setChanged();
        }
        if (recipeProgress < activeRecipeEnergyCost) return consumedEnergy;

        if (!consumeInput(recipe)) {
            clearRecipeState();
            return consumedEnergy;
        }

        commitOutputPlan(outputPlan);
        recipeProgress = 0;
        setChanged();
        return consumedEnergy;
    }

    private boolean recipeCacheChanged() {
        if (activeRecipe == null) return false;
        return level.getRecipeManager().byKey(activeRecipe.id())
                .map(current -> current.value() != activeRecipe.value())
                .orElse(true);
    }

    private double getEnergyPerTick() {
        double normalEnergy = BASIC_ENERGY_COST_PER_TICK * speedMultiplier;
        if (overloadCards == 0 || activeRecipeEnergyCost <= 0) return normalEnergy;

        int targetTicks = overloadCards == 1 ? 4 : 1;
        return Math.max(normalEnergy, Math.ceil((double) activeRecipeEnergyCost / targetTicks));
    }

    private void updateActiveRecipe() {
        if (level == null || level.isClientSide()) return;

        SingleRecipeInput input = new SingleRecipeInput(getInputInv().getStackInSlot(0));
        var match = level.getRecipeManager().getRecipeFor(AECSRecipeTypes.PULSE_CENTRIFUGE.get(), input, level);
        if (match.isEmpty()) {
            clearRecipeState();
            return;
        }

        RecipeHolder<PulseCentrifugeRecipe> holder = match.get();
        if (activeRecipe == null || !activeRecipe.id().equals(holder.id())) {
            recipeProgress = 0;
        }
        activeRecipe = holder;
        activeRecipeEnergyCost = holder.value().energyCost();
    }

    @Nullable
    static List<ItemStack> planOutputInsertion(AppEngInternalInventory output, List<ItemStack> results) {
        AppEngInternalInventory simulated = new AppEngInternalInventory(4);
        for (int slot = 0; slot < simulated.size(); slot++) {
            simulated.setItemDirect(slot, output.getStackInSlot(slot).copy());
        }
        for (ItemStack result : results) {
            if (!simulated.addItems(result.copy(), false).isEmpty()) return null;
        }

        List<ItemStack> finalSlots = new ArrayList<>(simulated.size());
        for (int slot = 0; slot < simulated.size(); slot++) {
            finalSlots.add(simulated.getStackInSlot(slot).copy());
        }
        return List.copyOf(finalSlots);
    }

    private void commitOutputPlan(List<ItemStack> plan) {
        for (int slot = 0; slot < plan.size(); slot++) {
            getOutputInv().setItemDirect(slot, plan.get(slot).copy());
        }
    }

    private boolean consumeInput(PulseCentrifugeRecipe recipe) {
        SizedIngredient required = recipe.input();
        ItemStack extracted = getInputInv().extractItem(0, required.count(), true);
        if (extracted.getCount() < required.count() || !required.test(extracted)) return false;

        getInputInv().extractItem(0, required.count(), false);
        return true;
    }

    private void clearRecipeState() {
        activeRecipe = null;
        activeRecipeEnergyCost = 0;
        recipeProgress = 0;
    }

    private void setProcessing(boolean processing) {
        this.processing = processing;
        BlockState state = getBlockState();
        if (state.hasProperty(AECSBlockProperties.ACTIVE) && state.getValue(AECSBlockProperties.ACTIVE) != processing) {
            level.setBlock(worldPosition, state.setValue(AECSBlockProperties.ACTIVE, processing), 2);
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        upgrades.writeToNBT(data, "upgrades", registries);
        fluidTanks.writeToNbt(data, registries);
        data.putInt("recipe_progress", recipeProgress);
        if (activeRecipe != null) {
            data.putString("active_recipe_id", activeRecipe.id().toString());
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        upgrades.readFromNBT(data, "upgrades", registries);
        fluidTanks.readFromNbt(data, registries);
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
                if (holder.value() instanceof PulseCentrifugeRecipe) {
                    activeRecipe = (RecipeHolder<PulseCentrifugeRecipe>) (RecipeHolder<?>) holder;
                }
            });
        }
        updateActiveRecipe();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        upgrades.clear();
        fluidTanks.clear();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getItemFromBlockEntity());
    }
}
