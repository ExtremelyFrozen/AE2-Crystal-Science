package io.github.lounode.ae2cs.api.util;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.energy.IAEPowerStorage;

import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyAdapterUpgrade implements IEnergyStorage {

    private final IAEPowerStorage aePowerStorage;
    private final AccessRestriction adapterRestriction;

    public ForgeEnergyAdapterUpgrade(IAEPowerStorage aePowerStorage, AccessRestriction adapterRestriction) {
        this.aePowerStorage = aePowerStorage;
        this.adapterRestriction = adapterRestriction;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        if (!canReceive()) return 0;

        final Actionable mode = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        final double remaining = PowerUnits.AE.convertTo(PowerUnits.FE,
                this.aePowerStorage.injectAEPower(PowerUnits.FE.convertTo(PowerUnits.AE, amount), mode));

        return (int) (amount - remaining);
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        if (!canExtract()) return 0;

        final Actionable mode = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        final double extracted = PowerUnits.AE.convertTo(PowerUnits.FE,
                this.aePowerStorage.extractAEPower(PowerUnits.FE.convertTo(PowerUnits.AE, amount), mode, PowerMultiplier.ONE));

        return (int) extracted;
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.floor(PowerUnits.AE.convertTo(PowerUnits.FE, this.aePowerStorage.getAECurrentPower()));
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.floor(PowerUnits.AE.convertTo(PowerUnits.FE, this.aePowerStorage.getAEMaxPower()));
    }

    @Override
    public boolean canExtract() {
        return adapterRestriction.isAllowExtraction();
    }

    @Override
    public boolean canReceive() {
        return adapterRestriction.isAllowInsertion();
    }
}
