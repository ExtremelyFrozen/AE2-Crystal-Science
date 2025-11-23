package io.github.lounode.ae2cs.api.util;


import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.networking.energy.IAEPowerStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ForgeEnergyAdapterUpgrade implements IEnergyStorage
{
    private final IAEPowerStorage aePowerStorage;
    private final AccessRestriction adapterRestriction;

    public ForgeEnergyAdapterUpgrade(IAEPowerStorage aePowerStorage, AccessRestriction adapterRestriction)
    {
        this.aePowerStorage = aePowerStorage;
        this.adapterRestriction = adapterRestriction;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate)
    {
        if (!canReceive()) return 0;

        final Actionable mode = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        final double remaining = PowerUnit.AE.convertTo(PowerUnit.FE,
                this.aePowerStorage.injectAEPower(PowerUnit.FE.convertTo(PowerUnit.AE, amount), mode));

        return (int) (amount - remaining);
    }

    @Override
    public int extractEnergy(int amount, boolean simulate)
    {
        if (!canExtract()) return 0;

        final Actionable mode = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        final double extracted = PowerUnit.AE.convertTo(PowerUnit.FE,
                this.aePowerStorage.extractAEPower(PowerUnit.FE.convertTo(PowerUnit.AE, amount), mode, PowerMultiplier.ONE));

        return (int) extracted;
    }

    @Override
    public int getEnergyStored()
    {
        return (int) Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, this.aePowerStorage.getAECurrentPower()));
    }

    @Override
    public int getMaxEnergyStored()
    {
        return (int) Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, this.aePowerStorage.getAEMaxPower()));
    }

    @Override
    public boolean canExtract()
    {
        return adapterRestriction.isAllowExtraction();
    }

    @Override
    public boolean canReceive()
    {
        return adapterRestriction.isAllowInsertion();
    }
}
