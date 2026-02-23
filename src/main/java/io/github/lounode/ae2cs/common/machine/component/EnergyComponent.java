package io.github.lounode.ae2cs.common.machine.component;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyService;
import appeng.me.energy.StoredEnergyAmount;
import io.github.lounode.ae2cs.api.util.ForgeEnergyAdapterUpgrade;
import io.github.lounode.ae2cs.common.machine.MachineComponentContainer;
import io.github.lounode.ae2cs.common.machine.MachineContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class EnergyComponent extends NetworkMachineComponent implements IAEPowerStorage
{

    private final StoredEnergyAmount storedEnergy;
    private final boolean isAEPublicPowerStorage;
    private final AccessRestriction accessRestriction;

    /**
     * @param maxEnergy 最大能量容量
     */
    public EnergyComponent(IManagedGridNode node, double maxEnergy, boolean isAEPublicPowerStorage, AccessRestriction accessRestriction)
    {
        super(node);
        this.storedEnergy = new StoredEnergyAmount(0, maxEnergy, type -> markChanged());
        this.isAEPublicPowerStorage = isAEPublicPowerStorage;
        this.accessRestriction = accessRestriction;

        getMainNode().addService(IAEPowerStorage.class, this);
    }

    @Override
    public void onConstruct(MachineComponentContainer container)
    {
        super.onConstruct(container);
        container.exposeService(EnergyComponent.class, this);
    }

    @Override
    public void writeNbt(CompoundTag tag)
    {
        super.writeNbt(tag);
        tag.putDouble("stored_energy", this.storedEnergy.getAmount());
    }

    @Override
    public void readNbt(CompoundTag tag)
    {
        super.readNbt(tag);
        this.storedEnergy.setStored(tag.getDouble("stored_energy"));
    }

    @Override
    public void onServerTick(MachineContext ctx)
    {
        super.onServerTick(ctx);

        // 公共储能设备不做主动交互
        if (isAEPublicPowerStorage()) return;

        IGrid grid = getMainNode().getGrid();
        if (grid == null) return;
        IEnergyService energyService = grid.getEnergyService();
        if (energyService == null) return;

        AccessRestriction flowDirection = getPowerFlow();
        switch (flowDirection)
        {
            case NO_ACCESS ->
            {
            }
            case READ ->
            {
                // 从自身提取，向AE网络输入
                double remaining = energyService.injectPower(getAECurrentPower(), Actionable.MODULATE);
                double needExtract = getAECurrentPower() - remaining;
                extractAEPower(needExtract, Actionable.MODULATE, PowerMultiplier.ONE);
            }
            case WRITE, READ_WRITE ->
            {
                // 从AE网络提取，输入到自身
                double needInsert = getAEMaxPower() - getAECurrentPower();
                double actualCanInsert = energyService.extractAEPower(needInsert, Actionable.MODULATE, PowerMultiplier.ONE);
                injectAEPower(actualCanInsert, Actionable.MODULATE);
            }
        }
    }

    // IAEPowerStorage---------
    @Override
    public final double injectAEPower(double amt, Actionable mode)
    {
        markChanged();
        return amt - storedEnergy.insert(amt, mode == Actionable.MODULATE);
    }

    @Override
    public final double getAEMaxPower()
    {
        return this.storedEnergy.getMaximum();
    }

    @Override
    public final double getAECurrentPower()
    {
        return this.storedEnergy.getAmount();
    }

    @Override
    public boolean isAEPublicPowerStorage()
    {
        return this.isAEPublicPowerStorage;
    }

    @Override
    public AccessRestriction getPowerFlow()
    {
        return this.accessRestriction;
    }

    @Override
    public final double extractAEPower(double amt, Actionable mode, PowerMultiplier multiplier)
    {
        return multiplier.divide(this.extractAEPower(multiplier.multiply(amt), mode));
    }

    public double extractAEPower(double amt, Actionable mode)
    {
        markChanged();
        return this.storedEnergy.extract(amt, mode == Actionable.MODULATE);
    }

    public ForgeEnergyAdapterUpgrade getForgeEnergyAdapter()
    {
        return new ForgeEnergyAdapterUpgrade(this, this.accessRestriction);
    }
}
