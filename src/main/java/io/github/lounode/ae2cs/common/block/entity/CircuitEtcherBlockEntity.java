package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.config.AccessRestriction;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.lounode.ae2cs.api.util.ForgeEnergyAdapterUpgrade;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CircuitEtcherBlockEntity extends AENetworkedSelfPoweredBlockEntity implements IUpgradeableObject
{
    /**
     * 输入仓
     */
    private final AppEngInternalInventory inputInv = new AppEngInternalInventory(3)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    /**
     * 输出仓
     */
    private final AppEngInternalInventory outputInv = new AppEngInternalInventory(1)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    /**
     * 能力暴露-输入
     */
    FilteredInternalInventory filteredInputInv = new FilteredInternalInventory(inputInv, new IAEItemFilter()
    {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount)
        {
            return false;
        }
    });

    /**
     * 能力暴露-输出
     */
    FilteredInternalInventory filteredOutputInv = new FilteredInternalInventory(outputInv, new IAEItemFilter()
    {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack)
        {
            return false;
        }
    });

    /**
     * 能力暴露-结合库存
     */
    CombinedInternalInventory combinedInv = new CombinedInternalInventory(filteredInputInv, filteredOutputInv);

    /**
     * 升级仓
     */
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.CIRCUIT_ETCHER_BLOCK,
            4, this::saveChanges);

    /** 当前执行的配方信息 */

    /**
     * 当前配方运行时间
     */
    private int recipeProgress = 0;

    public CircuitEtcherBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(AECSBlockEntities.CIRCUIT_ETCHER_BLOCK_ENTITY.get(), pos, blockState, 40000);

        getMainNode().setIdlePowerUsage(0)
                .setFlags(GridFlags.CANNOT_CARRY);
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                AECSBlockEntities.CIRCUIT_ETCHER_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                AECSBlockEntities.CIRCUIT_ETCHER_BLOCK_ENTITY.get(),
                (be, direction) -> new ForgeEnergyAdapterUpgrade(be, AccessRestriction.WRITE)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                AECSBlockEntities.CIRCUIT_ETCHER_BLOCK_ENTITY.get(),
                (be, direction) -> be.combinedInv.toItemHandler()
        );
    }

    public AppEngInternalInventory getInputInv()
    {
        return inputInv;
    }

    public AppEngInternalInventory getOutputInv()
    {
        return outputInv;
    }

    public int getRecipeProgress()
    {
        return recipeProgress;
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    @Override
    public boolean isAEPublicPowerStorage()
    {
        return false;
    }

    @Override
    public AccessRestriction getPowerFlow()
    {
        return AccessRestriction.WRITE;
    }
}
