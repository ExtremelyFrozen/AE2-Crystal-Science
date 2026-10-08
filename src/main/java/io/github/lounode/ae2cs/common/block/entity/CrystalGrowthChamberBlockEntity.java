package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.cap.ProvideCaps;
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
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.decorative.solid.BuddingCertusQuartzBlock;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.List;

@ProvideCaps(IItemHandler.class)
public class CrystalGrowthChamberBlockEntity extends AENetworkedSelfPoweredBlockEntity implements IUpgradeableObject,
                                             CustomReturnableSubMenuHost {

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
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK, 4, this::onUpgradesChanged);

    private int workTickCountDown = 10;
    private int speedCards = 0;
    private int overclockCards = 0;

    /**
     * The accelerator's slot inventory is server-side machine data and is not guaranteed to
     * be present on the client. Keep this compact render-only copy synchronized explicitly.
     */
    @Nullable
    private Block acceleratorDisplayBlock;

    public CrystalGrowthChamberBlockEntity(BlockPos pos, BlockState state) {
        super(AECSBlockEntities.CRYSTAL_GROWTH_CHAMBER.get(), pos, state,
                40000, false, AccessRestriction.WRITE);

        getMainNode().setIdlePowerUsage(0.0);

        boolean accelerator = state.is(AECSBlocks.CRYSTAL_GROWTH_ACCELERATOR_BLOCK.get());
        AppEngInternalInventory inventory = new AppEngInternalInventory(accelerator ? 1 : 54) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
                if (accelerator && level != null && !level.isClientSide()) {
                    updateAcceleratorDisplayBlock();
                    checkActive(!getStackInSlot(0).isEmpty());
                    markForUpdate();
                }
            }
        };
        if (accelerator) {
            // A catalyst is consumed/degraded as one physical mother rock. Stacking here
            // would make the rendered top and the per-cycle degradation ambiguous.
            inventory.setMaxStackSize(0, 1);
        }
        inventory.setFilter(new IAEItemFilter() {

            // 这里限定只允许水晶种子进入，但不限制输出，以便UI中能手动拿取任意物品
            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
                return stack != null && !stack.isEmpty()
                        && (accelerator ? isAcceleratorInput(stack) : stack.is(AECSTags.Items.CRYSTAL_SEEDS));
            }
        });

        FilteredInternalInventory exposeInv = new FilteredInternalInventory(inventory, new IAEItemFilter() {

            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
                return stack != null && !stack.isEmpty()
                        && (accelerator ? isAcceleratorInput(stack) : stack.is(AECSTags.Items.CRYSTAL_SEEDS));
            }

            @Override
            public boolean allowExtract(InternalInventory inv, int slot, int amount) {
                return accelerator || inv.getStackInSlot(slot).is(AECSTags.Items.PURE_CRYSTAL);
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

    public void checkActive(boolean active) {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (state.hasProperty(AECSBlockProperties.ACTIVE) && state.getValue(AECSBlockProperties.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(AECSBlockProperties.ACTIVE, active), 2);
        }
    }

    private void updateAcceleratorDisplayBlock() {
        if (!isAccelerator()) {
            acceleratorDisplayBlock = null;
            return;
        }

        ItemStack stack = getInternalInventory().getStackInSlot(0);
        Block block = stack.isEmpty() ? null : Block.byItem(stack.getItem());
        acceleratorDisplayBlock = block != null && isAcceleratorInput(stack) ? block : null;
    }

    @Nullable
    public Block getAcceleratorDisplayBlock() {
        return acceleratorDisplayBlock;
    }

    /* 对外返回升级仓 */
    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    // inv的save与load均已由AEBaseInvBlockEntity处理
    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.upgrades.writeToNBT(tag, "interface_upgrades", registries);
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        this.upgrades.readFromNBT(tag, "interface_upgrades", registries);
    }

    // load完成之后，且level被注入后
    @Override
    public void onLoad() {
        super.onLoad();
        onUpgradesChanged(); // 加载后刷新一次升级状态
        updateGrowthNum();
        if (isAccelerator() && level != null && !level.isClientSide()) {
            updateAcceleratorDisplayBlock();
            markForUpdate();
        }
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(acceleratorDisplayBlock != null);
        if (acceleratorDisplayBlock != null) {
            data.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(acceleratorDisplayBlock));
        }
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean redraw = super.readFromStream(data);
        Block previous = acceleratorDisplayBlock;
        acceleratorDisplayBlock = data.readBoolean()
                ? BuiltInRegistries.BLOCK.get(data.readResourceLocation())
                : null;
        return redraw || previous != acceleratorDisplayBlock;
    }

    private void onUpgradesChanged() {
        this.overclockCards = Math.min(2, upgrades.getInstalledUpgrades(AECSItems.OVERLOAD_CARD));
        this.speedCards = overclockCards > 0 ? 0 : upgrades.getInstalledUpgrades(AEItems.SPEED_CARD);
        this.workTickCountDown = Math.min(workTickCountDown, getWorkInterval());
        setChanged();
    }

    private int getWorkInterval() {
        return switch (overclockCards) {
            case 1 -> 4;
            case 2 -> 1;
            default -> 10;
        };
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (level == null || level.isClientSide) return;

        if (isAccelerator()) {
            serverTickAccelerator();
            return;
        }

        --workTickCountDown;
        if (workTickCountDown > 0) return;

        if (level.getRandom().nextFloat() < 0.1F)
            updateGrowthNum();

        checkActive(getAECurrentPower() > 0);

        double energyCost = (1 + speedCards) * energyPerGrowth;
        if (getAECurrentPower() < energyCost) return;

        int growthTick = growthProgressBase + speedCards * growthProgressPerSpeedCard + growthNum * growthProgressPerGrowth;

        boolean worked = false;
        for (int i = 0; i < getInternalInventory().size(); ++i) {
            ItemStack stack = getInternalInventory().getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(AECSTags.Items.CRYSTAL_SEEDS)) continue;

            worked = true;
            ItemStack result = CrystalSeedItem.grow(stack, growthTick * 10);
            getInternalInventory().setItemDirect(i, result);
        }
        if (worked)
            extractAEPower(energyCost, Actionable.MODULATE);

        setChanged();

        workTickCountDown = getWorkInterval();
    }

    private boolean isAccelerator() {
        return getBlockState().is(AECSBlocks.CRYSTAL_GROWTH_ACCELERATOR_BLOCK.get());
    }

    private static boolean isAcceleratorInput(ItemStack stack) {
        Block block = Block.byItem(stack.getItem());
        return block instanceof BuddingCertusQuartzBlock
                || block instanceof io.github.lounode.ae2cs.common.block.CrystalMotherRockBlock;
    }

    private void serverTickAccelerator() {
        --workTickCountDown;
        if (workTickCountDown > 0) return;
        workTickCountDown = getWorkInterval();

        ItemStack stack = getInternalInventory().getStackInSlot(0);
        if (!isAcceleratorInput(stack)) {
            checkActive(false);
            return;
        }

        double energyCost = energyPerGrowth;
        if (getAECurrentPower() < energyCost) {
            checkActive(false);
            return;
        }

        Block block = Block.byItem(stack.getItem());
        if (!forceAcceleratorGrowth(block)) {
            checkActive(false);
            return;
        }

        extractAEPower(energyCost, Actionable.MODULATE);
        ItemStack degraded = getAeDegradedStack(block, stack.getCount());
        if (!degraded.isEmpty()) {
            getInternalInventory().setItemDirect(0, degraded);
        }
        checkActive(true);
        setChanged();
    }

    private boolean forceAcceleratorGrowth(Block block) {
        if (block instanceof io.github.lounode.ae2cs.common.block.CrystalMotherRockBlock motherRock) {
            return motherRock.forceTopGrowth((net.minecraft.server.level.ServerLevel) level, worldPosition);
        }
        if (!(block instanceof BuddingCertusQuartzBlock)) {
            return false;
        }

        BlockPos targetPos = worldPosition.above();
        BlockState targetState = level.getBlockState(targetPos);
        Block nextStage = null;
        if (io.github.lounode.ae2cs.common.block.CrystalMotherRockBlock.canClusterGrowAtState(targetState)) {
            nextStage = AEBlocks.SMALL_QUARTZ_BUD.block();
        } else if (targetState.is(AEBlocks.SMALL_QUARTZ_BUD.block())
                && targetState.getValue(net.minecraft.world.level.block.AmethystClusterBlock.FACING) == Direction.UP) {
            nextStage = AEBlocks.MEDIUM_QUARTZ_BUD.block();
        } else if (targetState.is(AEBlocks.MEDIUM_QUARTZ_BUD.block())
                && targetState.getValue(net.minecraft.world.level.block.AmethystClusterBlock.FACING) == Direction.UP) {
            nextStage = AEBlocks.LARGE_QUARTZ_BUD.block();
        } else if (targetState.is(AEBlocks.LARGE_QUARTZ_BUD.block())
                && targetState.getValue(net.minecraft.world.level.block.AmethystClusterBlock.FACING) == Direction.UP) {
            nextStage = AEBlocks.QUARTZ_CLUSTER.block();
        }
        if (nextStage == null) return false;

        BlockState nextState = nextStage.defaultBlockState()
                .setValue(net.minecraft.world.level.block.AmethystClusterBlock.FACING, Direction.UP)
                .setValue(net.minecraft.world.level.block.AmethystClusterBlock.WATERLOGGED,
                        targetState.getFluidState().getType() == net.minecraft.world.level.material.Fluids.WATER);
        level.setBlockAndUpdate(targetPos, nextState);
        return true;
    }

    private ItemStack getAeDegradedStack(Block block, int count) {
        Block degraded = null;
        if (block == AEBlocks.FLAWED_BUDDING_QUARTZ.block()
                || block == AEBlocks.CHIPPED_BUDDING_QUARTZ.block()
                || block == AEBlocks.DAMAGED_BUDDING_QUARTZ.block()) {
            if (level.getRandom().nextInt(BuddingCertusQuartzBlock.DECAY_CHANCE) != 0) {
                return ItemStack.EMPTY;
            }
            degraded = block == AEBlocks.FLAWED_BUDDING_QUARTZ.block()
                    ? AEBlocks.CHIPPED_BUDDING_QUARTZ.block()
                    : block == AEBlocks.CHIPPED_BUDDING_QUARTZ.block()
                            ? AEBlocks.DAMAGED_BUDDING_QUARTZ.block()
                            : AEBlocks.QUARTZ_BLOCK.block();
        }
        return degraded == null ? ItemStack.EMPTY : new ItemStack(degraded, count);
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
                if (upgradeableObject.isUpgradedWith(AECSItems.crystalGrowthCard))
                    growthNum++;
            } else if (level.getBlockEntity(getBlockPos().relative(dir)) instanceof CableBusBlockEntity cabe) {
                if (cabe.getCableBus().getPart(dir.getOpposite()) instanceof IUpgradeableObject upgradeableObject &&
                        upgradeableObject.isUpgradedWith(AECSItems.crystalGrowthCard))
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
