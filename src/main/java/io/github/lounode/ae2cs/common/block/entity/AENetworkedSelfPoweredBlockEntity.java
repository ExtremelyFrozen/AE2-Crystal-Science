package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;
import io.github.lounode.ae2cs.common.machine.component.EnergyComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 根据其确定的能量流向，这个机器会自动与AE网络进行能量交互
 */
public class AENetworkedSelfPoweredBlockEntity extends AENetworkedComponentBlockEntity implements
        IAEPowerStorage
{
    /**
     * @param maxEnergy 最大能量容量
     */
    public AENetworkedSelfPoweredBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState,
                                             double maxEnergy, boolean isAEPublicPowerStorage, AccessRestriction accessRestriction)
    {
        super(blockEntityType, pos, blockState);

        EnergyComponent component = new EnergyComponent(getMainNode(), maxEnergy, isAEPublicPowerStorage, accessRestriction);
        getMachineComponents().add(component);
        getMachineComponents().exposeService(EnergyComponent.class, component);
    }

    // IAEPowerStorage---------
    @Override
    public double injectAEPower(double amt, Actionable mode)
    {
        return getMachineComponents().getService(EnergyComponent.class).injectAEPower(amt, mode);
    }

    @Override
    public double getAEMaxPower()
    {
        return getMachineComponents().getService(EnergyComponent.class).getAEMaxPower();
    }

    @Override
    public double getAECurrentPower()
    {
        return getMachineComponents().getService(EnergyComponent.class).getAECurrentPower();
    }

    @Override
    public boolean isAEPublicPowerStorage()
    {
        return getMachineComponents().getService(EnergyComponent.class).isAEPublicPowerStorage();
    }

    @Override
    public AccessRestriction getPowerFlow()
    {
        return getMachineComponents().getService(EnergyComponent.class).getPowerFlow();
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier multiplier)
    {
        return getMachineComponents().getService(EnergyComponent.class).extractAEPower(amt, mode, multiplier);
    }

    public double extractAEPower(double amt, Actionable mode)
    {
        return getMachineComponents().getService(EnergyComponent.class).extractAEPower(amt, mode);
    }
}
