package io.github.lounode.ae2cs.api.util;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.networking.energy.IAEPowerStorage;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ForgeEnergyAdapterUpgrade implements EnergyHandler {
    private final IAEPowerStorage aePowerStorage;
    private final AccessRestriction adapterRestriction;

    public ForgeEnergyAdapterUpgrade(IAEPowerStorage aePowerStorage, AccessRestriction adapterRestriction) {
        this.aePowerStorage = aePowerStorage;
        this.adapterRestriction = adapterRestriction;
    }

    @Override
    public long getAmountAsLong() {
        return (long) Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, this.aePowerStorage.getAECurrentPower()));
    }

    @Override
    public long getCapacityAsLong() {
        return (long) Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, this.aePowerStorage.getAEMaxPower()));
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (!adapterRestriction.isAllowInsertion()) return 0;

        final Actionable mode = Actionable.MODULATE;
        final double remaining = PowerUnit.AE.convertTo(PowerUnit.FE,
                this.aePowerStorage.injectAEPower(PowerUnit.FE.convertTo(PowerUnit.AE, amount), mode));

        return (int) (amount - remaining);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (!adapterRestriction.isAllowExtraction()) return 0;

        final Actionable mode = Actionable.MODULATE;
        final double extracted = PowerUnit.AE.convertTo(PowerUnit.FE,
                this.aePowerStorage.extractAEPower(PowerUnit.FE.convertTo(PowerUnit.AE, amount), mode, PowerMultiplier.ONE));

        return (int) extracted;
    }
}
