package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyService;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.energy.StoredEnergyAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 根据其确定的能量流向，这个机器会自动与AE网络进行能量交互
 */
public abstract class AENetworkedSelfPoweredBlockEntity extends AENetworkedBlockEntity implements ServerTickingBlockEntity,
        IAEPowerStorage
{

    private final StoredEnergyAmount storedEnergy;

    /**
     * @param maxEnergy 最大能量容量
     */
    public AENetworkedSelfPoweredBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState,
                                             double maxEnergy)
    {
        super(blockEntityType, pos, blockState);

        this.storedEnergy = new StoredEnergyAmount(0, maxEnergy, type -> saveChanges());
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries)
    {
        super.saveAdditional(data, registries);
        data.putDouble("stored_energy", this.storedEnergy.getAmount());
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries)
    {
        super.loadTag(data, registries);
        this.storedEnergy.setStored(data.getDouble("stored_energy"));
    }

    // ServerTickingBlockEntity-------------
    @Override
    public void serverTick()
    {
        if(level == null || level.isClientSide) return;

        // 公共储能设备不做主动交互
        if(isAEPublicPowerStorage()) return;

        IGrid grid = getMainNode().getGrid();
        if(grid == null) return;
        IEnergyService energyService = grid.getEnergyService();
        if(energyService == null) return;

        AccessRestriction flowDirection = getPowerFlow();
        switch (flowDirection)
        {
            case NO_ACCESS -> {}
            case READ -> {
                // 从自身提取，向AE网络输入
                double remaining = energyService.injectPower(getAECurrentPower(), Actionable.MODULATE);
                double needExtract = getAECurrentPower() - remaining;
                extractAEPower(needExtract, Actionable.MODULATE, PowerMultiplier.ONE);
            }
            case WRITE, READ_WRITE -> {
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
    public final double extractAEPower(double amt, Actionable mode, PowerMultiplier multiplier)
    {
        return multiplier.divide(this.extractAEPower(multiplier.multiply(amt), mode));
    }

    public double extractAEPower(double amt, Actionable mode)
    {
        return this.storedEnergy.extract(amt, mode == Actionable.MODULATE);
    }
}
