package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.*;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;

public class CrystalGrowthChamberBlockEntity extends AENetworkedSelfPoweredBlockEntity implements IUpgradeableObject,
                                             CustomReturnableSubMenuHost {

    private LazyOptional<IItemHandler> itemHandlerOpt = LazyOptional.empty();
    private final EnumMap<Direction, LazyOptional<IItemHandler>> sidedItemHandlerOpts = new EnumMap<>(Direction.class);

    /**
     * 晶体催生仓提供的基础生长值
     */
    private static final int growthProgressBase = 1;

    /**
     * 每张加速卡提供的生长值
     */
    private static final int growthProgressPerSpeedCard = 1;

    /**
     * 周围每个催生器提供的生长值
     */
    private static final int growthProgressPerGrowth = 1;

    /**
     * 每个工作消耗的AE能量，每张加速卡再额外增加一次该数值
     */
    private static final double energyPerGrowth = 100;

    /**
     * 周围六个方向晶体催生器的数量
     */
    private int growthNum = 0;

    // 升级卡仓 4卡槽 包含四个加速卡
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK.get(), 4, this::onUpgradesChanged);

    private int workTickCountDown = 10;

    public CrystalGrowthChamberBlockEntity(BlockPos pos, BlockState state) {
        super(AECSBlockEntities.CRYSTAL_GROWTH_CHAMBER.get(), pos, state,
                40000, false, AccessRestriction.WRITE);

        getMainNode().setIdlePowerUsage(0.0);

        AppEngInternalInventory inventory = new AppEngInternalInventory(54) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
            }
        };
        inventory.setFilter(new IAEItemFilter() {

            // 这里限定只允许水晶种子进入，但不限制输出，以便UI中能手动拿取任意物品
            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
                return stack != null && !stack.isEmpty() && stack.is(AECSTags.Items.CRYSTAL_SEEDS);
            }
        });

        FilteredInternalInventory exposeInv = new FilteredInternalInventory(inventory, new IAEItemFilter() {

            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
                return stack != null && !stack.isEmpty() && stack.is(AECSTags.Items.CRYSTAL_SEEDS);
            }

            @Override
            public boolean allowExtract(InternalInventory inv, int slot, int amount) {
                return inv.getStackInSlot(slot).is(AECSTags.Items.PURE_CRYSTAL);
            }
        });

        AppEngInvComponent invComponent = new AppEngInvComponent();
        invComponent.addPort(InvPort.WORK, inventory);
        getMachineComponents().add(invComponent);
        invComponent.setWrap(InvPort.WORK, exposeInv);
        getMachineComponents().add(new SideConfigComponent());
    }

    /**
     * 取当前网络的MEStorage
     */
    public @Nullable MEStorage getNetworkInventory() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return null;
        IStorageService ss = grid.getStorageService();
        return ss != null ? ss.getInventory() : null;
    }

    /**
     * 存储槽
     */
    public InternalInventory getInternalInventory() {
        return getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.WORK);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                if (!itemHandlerOpt.isPresent()) {
                    IItemHandler handler = resolveItemHandler(null);
                    itemHandlerOpt = handler == null ? LazyOptional.empty() : LazyOptional.of(() -> handler);
                }
                return itemHandlerOpt.cast();
            }

            var opt = sidedItemHandlerOpts.get(side);
            if (opt == null) {
                IItemHandler handler = resolveItemHandler(side);
                opt = handler == null ? LazyOptional.empty() : LazyOptional.of(() -> handler);
                sidedItemHandlerOpts.put(side, opt);
            }
            return opt.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        if (itemHandlerOpt.isPresent()) itemHandlerOpt.invalidate();
        itemHandlerOpt = LazyOptional.empty();

        for (var opt : sidedItemHandlerOpts.values()) {
            if (opt.isPresent()) opt.invalidate();
        }
        sidedItemHandlerOpts.clear();
    }

    private @Nullable IItemHandler resolveItemHandler(@Nullable Direction side) {
        if (getMachineComponents().hasService(SideConfigComponent.class)) {
            var inv = getMachineComponents().getService(SideConfigComponent.class).appEngInvForSide(side);
            return inv == null ? null : inv.toItemHandler();
        }
        return getMachineComponents().getService(AppEngInvComponent.class).combined().toItemHandler();
    }

    public void checkActive(boolean active) {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (state.hasProperty(AECSBlockProperties.ACTIVE) && state.getValue(AECSBlockProperties.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(AECSBlockProperties.ACTIVE, active), 2);
        }
    }

    /* 对外返回升级仓 */
    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    // inv的save与load均已由AEBaseInvBlockEntity处理
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.upgrades.writeToNBT(tag, "interface_upgrades");
    }

    @Override
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);
        this.upgrades.readFromNBT(tag, "interface_upgrades");
    }

    // load完成之后，且level被注入后
    @Override
    public void onLoad() {
        super.onLoad();
        onUpgradesChanged(); // 加载后刷新一次升级状态
        updateGrowthNum();
    }

    private void onUpgradesChanged() {
        setChanged();
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (level == null || level.isClientSide) return;

        --workTickCountDown;
        if (workTickCountDown > 0) return;

        checkActive(getAECurrentPower() > 0);

        int speedCard = upgrades.getInstalledUpgrades(AEItems.SPEED_CARD);
        double energyCost = (1 + speedCard) * energyPerGrowth;
        if (getAECurrentPower() < energyCost) return;

        int growthTick = growthProgressBase + speedCard * growthProgressPerSpeedCard + growthNum * growthProgressPerGrowth;

        boolean worked = false;
        for (int i = 0; i < getInternalInventory().size(); ++i) {
            ItemStack stack = getInternalInventory().getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(AECSTags.Items.CRYSTAL_SEEDS)) continue;

            worked = true;
            ItemStack result = CrystalSeedItem.grow(stack, growthTick * 10); // 每10tick运行一次，乘10以补足
            getInternalInventory().setItemDirect(i, result);
        }
        if (worked)
            extractAEPower(energyCost, Actionable.MODULATE);

        setChanged();

        workTickCountDown = 10; // 重置倒计时
    }

    /**
     * 设置周围晶体催生器的数量
     */
    public void updateGrowthNum() {
        if (level == null || level.isClientSide) return;

        growthNum = 0;
        for (Direction dir : Direction.values()) {
            BlockState state = level.getBlockState(getBlockPos().relative(dir));
            if (state.getBlock() == AEBlocks.GROWTH_ACCELERATOR.block()) {
                growthNum++;
            } else if (level.getBlockEntity(getBlockPos().relative(dir)) instanceof IUpgradeableObject upgradeableObject) {
                if (upgradeableObject.isUpgradedWith(AECSItems.crystalGrowthCard.get()))
                    growthNum++;
            }
        }
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);

        for (ItemStack stack : upgrades) {
            if (stack.isEmpty()) continue;
            drops.add(stack.copy());
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.upgrades.clear();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getItemFromBlockEntity());
    }
}
