package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.me.logic.QuartzOscillatorClockHost;
import io.github.lounode.ae2cs.common.me.logic.QuartzOscillatorClockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class QuartzOscillatorClockBlockEntity extends AENetworkedBlockEntity implements QuartzOscillatorClockHost,
        IConfigurableObject, IUpgradeableObject
{
    private final QuartzOscillatorClockLogic logic;

    public QuartzOscillatorClockBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
        this.logic = createLogic();
    }

    protected QuartzOscillatorClockLogic createLogic()
    {
        return new QuartzOscillatorClockLogic(getMainNode(), this, getItemFromBlockEntity().asItem());
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return getLogic().getUpgrades();
    }

    @Override
    public IConfigManager getConfigManager()
    {
        return getLogic().getConfigManager();
    }

    @Override
    public QuartzOscillatorClockLogic getLogic()
    {
        return this.logic;
    }

    @Override
    public EnumSet<Direction> getTargets()
    {
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public void setPulseActive(boolean active)
    {
        var level = getLevel();
        if (level == null || level.isClientSide) return;

        var pos = getBlockPos();
        var state = getBlockState();

        if (!state.hasProperty(AECSBlockProperties.ACTIVE))
        {
            return;
        }

        boolean old = state.getValue(AECSBlockProperties.ACTIVE);
        if (old == active) return;

        var newState = state.setValue(AECSBlockProperties.ACTIVE, active);

        level.setBlock(pos, newState, 3);

        var block = newState.getBlock();
        level.updateNeighborsAt(pos, block);
        for (var dir : getTargets())
        {
            level.updateNeighborsAt(pos.relative(dir), block);
        }

        setChanged();
    }

    @Override
    public boolean isPulseActive()
    {
        var state = getBlockState();
        if (!state.hasProperty(AECSBlockProperties.ACTIVE))
        {
            return false;
        }
        return state.getValue(AECSBlockProperties.ACTIVE);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries)
    {
        super.saveAdditional(data, registries);
        this.logic.writeToNBT(data, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries)
    {
        super.loadTag(data, registries);
        this.logic.readFromNBT(data, registries);
    }


}
