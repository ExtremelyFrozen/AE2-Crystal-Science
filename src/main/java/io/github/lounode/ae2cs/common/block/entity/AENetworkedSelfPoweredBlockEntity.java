package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.common.machine.component.EnergyComponent;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 根据其确定的能量流向，这个机器会自动与AE网络进行能量交互
 */
public class AENetworkedSelfPoweredBlockEntity extends AENetworkedComponentBlockEntity implements
                                               IAEPowerStorage {

    LazyOptional<IEnergyStorage> energyOpt = LazyOptional.empty();

    /**
     * @param maxEnergy 最大能量容量
     */
    public AENetworkedSelfPoweredBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState,
                                             double maxEnergy, boolean isAEPublicPowerStorage, AccessRestriction accessRestriction) {
        super(blockEntityType, pos, blockState);

        EnergyComponent component = new EnergyComponent(getMainNode(), maxEnergy, isAEPublicPowerStorage, accessRestriction);
        getMachineComponents().add(component);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            if (!energyOpt.isPresent())
                energyOpt = LazyOptional.of(() -> this.getMachineComponents().getService(EnergyComponent.class).getForgeEnergyAdapter());
            return energyOpt.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        if (energyOpt.isPresent()) energyOpt.invalidate();
        energyOpt = LazyOptional.empty();
    }

    // IAEPowerStorage---------
    @Override
    public double injectAEPower(double amt, Actionable mode) {
        return getMachineComponents().getService(EnergyComponent.class).injectAEPower(amt, mode);
    }

    @Override
    public double getAEMaxPower() {
        return getMachineComponents().getService(EnergyComponent.class).getAEMaxPower();
    }

    @Override
    public double getAECurrentPower() {
        return getMachineComponents().getService(EnergyComponent.class).getAECurrentPower();
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return getMachineComponents().getService(EnergyComponent.class).isAEPublicPowerStorage();
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return getMachineComponents().getService(EnergyComponent.class).getPowerFlow();
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier multiplier) {
        return getMachineComponents().getService(EnergyComponent.class).extractAEPower(amt, mode, multiplier);
    }

    public double extractAEPower(double amt, Actionable mode) {
        return getMachineComponents().getService(EnergyComponent.class).extractAEPower(amt, mode);
    }
}
