package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.core.settings.TickRates;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import appeng.util.ConfigInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 在常规接口上添加掉落物吸收
 */
public class EnderInterfaceLogic extends InterfaceLogic
{
    private static final int MAX_ABSORB_RANGE = 9;

    private final InterfaceLogicHost host;

    private final ConfigInventory absorbConfigInventory;
    private boolean blackListMode = false; // 黑名单还是白名单？
    private int range = 3; // 吸收半径，切比雪夫距离

    private Set<AEKey> absorbFilterdKeys = new HashSet<>();

    public EnderInterfaceLogic(IManagedGridNode gridNode, InterfaceLogicHost host, Item is)
    {
        this(gridNode, host, is, 9, 18);
    }

    public EnderInterfaceLogic(IManagedGridNode gridNode, InterfaceLogicHost host, Item is, int slots, int absorbConfigSlots)
    {
        super(gridNode, host, is, slots);

        this.host = host;

        this.absorbConfigInventory = ConfigInventory.configTypes(absorbConfigSlots).changeListener(this::onAbsorbConfigChange).build();
        this.absorbConfigInventory.useRegisteredCapacities();

        mainNode.addService(IGridTickable.class, new Ticker());
    }

    public ConfigInventory getAbsorbConfigInventory()
    {
        return absorbConfigInventory;
    }

    public boolean isBlackListMode()
    {
        return blackListMode;
    }

    public void setBlackListMode(boolean blackListMode)
    {
        if (this.blackListMode != blackListMode)
        {
            this.blackListMode = blackListMode;
            mainNode.ifPresent((iGrid, iGridNode) -> iGrid.getTickManager().alertDevice(iGridNode));
            this.host.saveChanges();
        }
    }

    public int getRange()
    {
        return range;
    }

    public void setRange(int range)
    {
        int newValue = Math.max(1, Math.min(range, MAX_ABSORB_RANGE));
        if (newValue != this.range)
        {
            this.range = newValue;
            mainNode.ifPresent((iGrid, iGridNode) -> iGrid.getTickManager().alertDevice(iGridNode));
            this.host.saveChanges();
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.writeToNBT(tag, registries);
        absorbConfigInventory.writeToChildTag(tag, "absorb_config", registries);
        tag.putInt("range", range);
        tag.putBoolean("black_list_mode", blackListMode);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.readFromNBT(tag, registries);
        absorbConfigInventory.readFromChildTag(tag, "absorb_config", registries);
        range = tag.getInt("range");
        blackListMode = tag.getBoolean("black_list_mode");

        onAbsorbConfigChange();
    }

    private void onAbsorbConfigChange()
    {
        this.host.saveChanges();
        mainNode.ifPresent((iGrid, iGridNode) -> iGrid.getTickManager().alertDevice(iGridNode));
        this.absorbFilterdKeys = absorbConfigInventory.keySet();
    }

    private boolean hasAbsorbWork()
    {
        if (blackListMode) return true;
        else return !absorbConfigInventory.isEmpty();
    }

    private boolean validAbsorbThing(AEKey key)
    {
        if (blackListMode)
        {
            return !absorbFilterdKeys.contains(key);
        }
        else
        {
            return absorbFilterdKeys.contains(key);
        }
    }

    private boolean doAbsorbWork()
    {
        if (!(this.host.getBlockEntity().getLevel() instanceof ServerLevel level)) return false;
        MEStorage storage = this.getInventory();
        if (storage == null) return false;


        BlockPos centerPos = this.host.getBlockEntity().getBlockPos();
        AABB absorbAABB = new AABB(
                centerPos.getX() - range,
                centerPos.getY() - range,
                centerPos.getZ() - range,
                centerPos.getX() + range,
                centerPos.getY() + range,
                centerPos.getZ() + range
        );
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(
                ItemEntity.class,
                absorbAABB
        );
        boolean hasAbsorbed = false;
        for (ItemEntity itemEntity : itemEntities)
        {
            AEItemKey itemKey = AEItemKey.of(itemEntity.getItem());
            int count = itemEntity.getItem().getCount();
            if (!validAbsorbThing(itemKey)) continue;

            if (storage.insert(itemKey, count, Actionable.SIMULATE, actionSource) == count)
            {
                storage.insert(itemKey, count, Actionable.MODULATE, actionSource);
                itemEntity.discard();
                hasAbsorbed = true;
            }
        }
        return hasAbsorbed;
    }

    private class Ticker implements IGridTickable
    {
        @Override
        public TickingRequest getTickingRequest(IGridNode node)
        {
            return new TickingRequest(TickRates.Interface, !hasWorkToDo() && !hasAbsorbWork());
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall)
        {
            if (!mainNode.isActive())
            {
                return TickRateModulation.SLEEP;
            }

            boolean couldDoWork = updateStorage() || doAbsorbWork();
            return hasWorkToDo() || hasAbsorbWork() ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
                    : TickRateModulation.SLEEP;
        }
    }
}
