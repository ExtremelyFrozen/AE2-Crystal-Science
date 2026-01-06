package io.github.lounode.ae2cs.common.me.part;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.locator.MenuLocators;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.me.logic.QuartzOscillatorClockHost;
import io.github.lounode.ae2cs.common.me.logic.QuartzOscillatorClockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class QuartzOscillatorClockPart extends AEBasePart implements QuartzOscillatorClockHost, IUpgradeableObject,
        IConfigurableObject
{
    public static final ResourceLocation MODEL_BASE_OFF = AE2CrystalScience.makeId(
            "part/quartz_oscillator_clock/base_off");
    public static final ResourceLocation MODEL_BASE_ON = AE2CrystalScience.makeId(
            "part/quartz_oscillator_clock/base_on");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE_OFF,
            AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE_ON,
            AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE_ON,
            AppEng.makeId("part/interface_has_channel"));


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

    @Override
    public IPartModel getStaticModels()
    {
        if (this.isActive() && this.isPowered())
        {
            return MODELS_HAS_CHANNEL;
        }
        else if (this.isPowered())
        {
            return MODELS_ON;
        }
        else
        {
            return MODELS_OFF;
        }
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
    public boolean onUseWithoutItem(Player player, Vec3 pos)
    {
        if (!player.getCommandSenderWorld().isClientSide())
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
        if (level == null || level.isClientSide)
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
        var state = level.getBlockState(pos);
        var block = state.getBlock();

        // 通知宿主方块周围
        level.updateNeighborsAt(pos, block);

        // 通知 part 外侧相邻方块
        var outPos = pos.relative(getSide());
        level.updateNeighborsAt(outPos, block);
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
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries)
    {
        super.writeToNBT(data, registries);
        this.logic.writeToNBT(data, registries);
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries)
    {
        super.readFromNBT(data, registries);
        this.logic.readFromNBT(data, registries);
        this.pulseActive = false;
    }
}
