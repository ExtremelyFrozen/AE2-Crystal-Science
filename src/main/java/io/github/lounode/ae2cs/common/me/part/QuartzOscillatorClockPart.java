package io.github.lounode.ae2cs.common.me.part;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.menu.locator.MenuLocators;
import appeng.parts.AEBasePart;
import appeng.parts.automation.PartModelData;
import io.github.lounode.ae2cs.common.me.logic.QuartzOscillatorClockHost;
import io.github.lounode.ae2cs.common.me.logic.QuartzOscillatorClockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.EnumSet;
import java.util.List;

public class QuartzOscillatorClockPart extends AEBasePart implements QuartzOscillatorClockHost, IUpgradeableObject,
        IConfigurableObject
{
    private final QuartzOscillatorClockLogic logic;

    /**
     * 是否正在输出脉冲 客户端通过stream同步
     */
    private boolean pulseActive = false;


    public QuartzOscillatorClockPart(IPartItem<?> partItem)
    {
        super(partItem);
        this.logic = createLogic();
    }

    protected QuartzOscillatorClockLogic createLogic()
    {
        return new QuartzOscillatorClockLogic(getMainNode(), this, getPartItem().asItem());
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch)
    {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public void collectModelData(ModelData.Builder builder)
    {
        super.collectModelData(builder);
        builder.with(PartModelData.LEVEL_EMITTER_ON, this.pulseActive);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos)
    {
        if (!player.level().isClientSide())
        {
            openMenu(player, MenuLocators.forPart(this));
        }
        return true;
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
        return EnumSet.of(getSide());
    }

    @Override
    public void saveChanges()
    {
        getHost().markForSave();
    }

    @Override
    public boolean canConnectRedstone()
    {
        return true;
    }

    @Override
    public int isProvidingWeakPower()
    {
        return this.pulseActive ? 15 : 0;
    }

    @Override
    public int isProvidingStrongPower()
    {
        return this.pulseActive ? 15 : 0;
    }

    @Override
    public void setPulseActive(boolean active)
    {
        var be = getBlockEntity();
        if (be == null)
        {
            this.pulseActive = active;
            return;
        }

        var level = be.getLevel();
        if (level == null || level.isClientSide())
        {
            this.pulseActive = active;
            return;
        }

        if (this.pulseActive == active)
        {
            return;
        }

        this.pulseActive = active;

        if (getHost() != null)
        {
            getHost().markForUpdate();
        }

        // 通知邻居
        notifyRedstoneNeighbors(level, be.getBlockPos());
    }

    @Override
    public boolean isPulseActive()
    {
        return this.pulseActive;
    }

    private void notifyRedstoneNeighbors(Level level, BlockPos pos)
    {
        // 宿主方块位置
        var state = level.getBlockState(pos);
        var block = state.getBlock();

        // 通知宿主方块周围
        level.updateNeighborsAt(pos, block);
        level.updateNeighbourForOutputSignal(pos, block);

        // 通知 part 外侧相邻方块
        var outPos = pos.relative(getSide());
        var outState = level.getBlockState(outPos);
        var outBlock = outState.getBlock();

        level.updateNeighborsAt(outPos, outBlock);
        level.updateNeighbourForOutputSignal(outPos, outBlock);
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data)
    {
        super.writeToStream(data);
        data.writeBoolean(this.pulseActive);
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data)
    {
        boolean redraw = super.readFromStream(data);

        boolean newPulse = data.readBoolean();
        if (this.pulseActive != newPulse)
        {
            this.pulseActive = newPulse;
            redraw = true;
        }

        return redraw;
    }

    @Override
    public void writeToNBT(ValueOutput data)
    {
        super.writeToNBT(data);
        this.logic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(ValueInput input)
    {
        super.readFromNBT(input);
        this.logic.readFromNBT(input);
        this.pulseActive = false;
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched)
    {
        super.addAdditionalDrops(drops, wrenched);
        this.logic.addDrops(drops);
    }

    @Override
    public void clearContent()
    {
        super.clearContent();
        this.logic.clearContent();
    }
}
