package io.github.lounode.ae2cs.common.me.part;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.locator.MenuLocators;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceHost;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceLogic;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class IntegratedInterfacePart extends AEBasePart implements IntegratedInterfaceHost
{
    public static final ResourceLocation MODEL_BASE = AE2CrystalScience.makeId("part/integrate_interface/base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, AppEng.makeId("part/interface_has_channel"));

    IntegratedInterfaceLogic logic = createLogic();
    private int priority;

    public IntegratedInterfacePart(IPartItem<?> partItem)
    {
        super(partItem);
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch)
    {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos)
    {
        if (!player.getCommandSenderWorld().isClientSide()) {
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
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(getPartItem());
    }

    protected IntegratedInterfaceLogic createLogic()
    {
        return new IntegratedInterfaceLogic(this.getMainNode(), this, 9, 9);
    }
}
