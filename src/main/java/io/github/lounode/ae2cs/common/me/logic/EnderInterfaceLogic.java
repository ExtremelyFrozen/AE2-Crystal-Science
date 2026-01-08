package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.helpers.InterfaceLogic;
import appeng.util.ConfigInventory;
import appeng.util.ConfigManager;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.BlackListMode;
import io.github.lounode.ae2cs.api.settings.ShowRangeMode;
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

    private final EnderInterfaceHost host;

    private final ConfigInventory absorbConfigInventory;
    private boolean blackListMode = false; // 黑名单还是白名单？
    private int range = 3; // 吸收半径，切比雪夫距离
    private boolean renderRangeInClient;

    private Set<AEKey> absorbFilterdKeys = new HashSet<>();

    public EnderInterfaceLogic(IManagedGridNode gridNode, EnderInterfaceHost host, Item is)
    {
        this(gridNode, host, is, 9, 18);
    }

    public EnderInterfaceLogic(IManagedGridNode gridNode, EnderInterfaceHost host, Item is, int slots, int absorbConfigSlots)
    {
        super(gridNode, host, is, slots);

        this.host = host;

        this.absorbConfigInventory = ConfigInventory.configTypes(absorbConfigSlots).changeListener(this::onAbsorbConfigChange).build();
        this.absorbConfigInventory.useRegisteredCapacities();

        mainNode.addService(IGridTickable.class, new Ticker());

        if (getConfigManager() instanceof ConfigManager cm)
        {
            cm.registerSetting(AECSSettings.SHOW_RANGE_MODE, ShowRangeMode.HIDE_RANGE);
            cm.registerSetting(AECSSettings.BLACK_LIST_MODE, BlackListMode.WHITELIST);
        }
    }

    public ConfigInventory getAbsorbConfigInventory()
    {
        return absorbConfigInventory;
    }

    @Override
    protected void onConfigChanged()
    {
        super.onConfigChanged();
        boolean newRenderRangeInClient = getConfigManager().getSetting(AECSSettings.SHOW_RANGE_MODE) == ShowRangeMode.SHOW_RANGE;
        boolean newBlackListMode = getConfigManager().getSetting(AECSSettings.BLACK_LIST_MODE) == BlackListMode.BLACKLIST;
        if (this.renderRangeInClient != newRenderRangeInClient)
        {
            this.renderRangeInClient = newRenderRangeInClient;
            this.host.markForLogicClientUpdate();
        }
        if (this.blackListMode != newBlackListMode)
        {
            this.blackListMode = newBlackListMode;
            mainNode.ifPresent((iGrid, iGridNode) -> iGrid.getTickManager().alertDevice(iGridNode));
            this.host.saveChanges();
        }
    }

    public boolean isBlackListMode()
    {
        return blackListMode;
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
            this.host.markForLogicClientUpdate();
        }
    }

    public boolean isRenderRangeInClient()
    {
        return renderRangeInClient;
    }

    public void setRenderRangeInClient(boolean renderRangeInClient)
    {
        this.renderRangeInClient = renderRangeInClient;
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.writeToNBT(tag, registries);
        absorbConfigInventory.writeToChildTag(tag, "absorb_config", registries);
        tag.putInt("range", range);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.readFromNBT(tag, registries);
        absorbConfigInventory.readFromChildTag(tag, "absorb_config", registries);
        range = tag.getInt("range");
        this.onConfigChanged();
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

    private boolean validAbsorbThing(AEKey key)
    {
        boolean match;
        if (getUpgrades().isInstalled(AEItems.FUZZY_CARD))
        {
            match = isInAbsorbFilter(key);
        }
        else
        {
            match = absorbFilterdKeys.contains(key);
        }

        return blackListMode ? !match : match;
    }

    private boolean absorbKeyMatches(AEKey configured, AEKey candidate)
    {
        if (getUpgrades().isInstalled(AEItems.FUZZY_CARD) && configured.supportsFuzzyRangeSearch())
        {
            var fuzzyMode = getConfigManager().getSetting(Settings.FUZZY_MODE);
            return configured.fuzzyEquals(candidate, fuzzyMode);
        }
        return configured.equals(candidate);
    }

    /**
     * 模糊卡专用匹配
     */
    private boolean isInAbsorbFilter(AEKey key)
    {
        for (var configured : absorbFilterdKeys)
        {
            if (absorbKeyMatches(configured, key))
            {
                return true;
            }
        }
        return false;
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
