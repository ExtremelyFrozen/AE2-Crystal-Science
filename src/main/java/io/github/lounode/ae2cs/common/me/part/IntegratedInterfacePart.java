package io.github.lounode.ae2cs.common.me.part;

import appeng.api.AECapabilities;
import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.helpers.externalstorage.GenericStackFluidStorage;
import appeng.helpers.externalstorage.GenericStackItemStorage;
import appeng.items.parts.PartModels;
import appeng.menu.locator.MenuLocators;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import appeng.util.SettingsFrom;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceHost;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceLogic;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class IntegratedInterfacePart extends AEBasePart implements IntegratedInterfaceHost
{
    public static final ResourceLocation MODEL_BASE = AE2CrystalScience.makeId("part/integrate_interface/base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, AppEng.makeId("part/interface_has_channel"));

    public static final ResourceLocation MODEL_EXTENDED = AE2CrystalScience.makeId(
            "part/integrate_interface/extended");

    @PartModels
    public static final PartModel EXTENDED_MODELS_OFF = new PartModel(MODEL_EXTENDED,
            AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel EXTENDED_MODELS_ON = new PartModel(MODEL_EXTENDED,
            AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel EXTENDED_MODELS_HAS_CHANNEL = new PartModel(MODEL_EXTENDED,
            AppEng.makeId("part/interface_has_channel"));

    IntegratedInterfaceLogic logic = createLogic();
    private int priority;

    public IntegratedInterfacePart(IPartItem<?> partItem)
    {
        super(partItem);
    }

    /**
     * 注册能力
     */
    public static void onRegisterCaps(RegisterPartCapabilitiesEvent event)
    {
        event.register(
                AECapabilities.GENERIC_INTERNAL_INV,
                (part, direction) -> part.getLogic().getStorageInv(),
                IntegratedInterfacePart.class
        );
        event.register(
                AECapabilities.ME_STORAGE,
                (part, direction) -> part.getLogic().getExposedMEStorage(direction),
                IntegratedInterfacePart.class
        );
        event.register(
                Capabilities.ItemHandler.BLOCK,
                (part, direction) -> {
                    var inv = part.getLogic().getStorageInv();
                    return inv != null ? new GenericStackItemStorage(inv) : null;
                },
                IntegratedInterfacePart.class
        );
        event.register(
                Capabilities.FluidHandler.BLOCK,
                (part, direction) -> {
                    var inv = part.getLogic().getStorageInv();
                    return inv != null ? new GenericStackFluidStorage(inv) : null;
                },
                IntegratedInterfacePart.class
        );
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.logic.onMainNodeStateChanged();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch)
    {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public IPartModel getStaticModels()
    {
        if (this.isActive() && this.isPowered())
        {
            return isExtended() ? EXTENDED_MODELS_HAS_CHANNEL : MODELS_HAS_CHANNEL;
        }
        else if (this.isPowered())
        {
            return isExtended() ? EXTENDED_MODELS_ON : MODELS_ON;
        }
        else
        {
            return isExtended() ? EXTENDED_MODELS_OFF : MODELS_OFF;
        }
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
    public IntegratedInterfaceLogic getLogic()
    {
        return logic;
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
    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(getPartItem());
    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void setPriority(int newValue)
    {
        this.priority = newValue;
        getHost().markForSave();
    }

    @Override
    public boolean isExtended()
    {
        return getPartItem() == AECSParts.EX_INTEGRATE_INTERFACE_PART.get();
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(getPartItem());
    }

    protected IntegratedInterfaceLogic createLogic()
    {
        int size = isExtended() ? 36 : 9;
        return new IntegratedInterfaceLogic(this.getMainNode(), this, size, size);
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player)
    {
        super.importSettings(mode, input, player);
        if (mode == SettingsFrom.MEMORY_CARD)
        {
            logic.importSettings(input, player);
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder)
    {
        super.exportSettings(mode, builder);
        if (mode == SettingsFrom.MEMORY_CARD)
        {
            logic.exportSettings(builder);
        }
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries)
    {
        super.writeToNBT(data, registries);
        this.logic.save(data, registries);
        data.putInt("priority", this.priority);
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries)
    {
        super.readFromNBT(data, registries);
        this.logic.load(data, registries);
        this.priority = data.getInt("priority");
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
        this.logic.cleanContent();
    }
}
