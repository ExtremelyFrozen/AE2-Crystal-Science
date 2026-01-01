package io.github.lounode.ae2cs.common.me.part;

import appeng.api.AECapabilities;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.core.AppEng;
import appeng.helpers.InterfaceLogic;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.parts.PartModel;
import appeng.parts.misc.InterfacePart;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceHost;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceLogic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EnderInterfacePart extends InterfacePart implements EnderInterfaceHost
{
    public static final ResourceLocation MODEL_BASE = AE2CrystalScience.makeId(
            "part/ender_interface/base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_has_channel"));

    public EnderInterfacePart(IPartItem<?> partItem)
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
                (part, direction) -> part.getInterfaceLogic().getStorage(),
                EnderInterfacePart.class
        );
        event.register(
                AECapabilities.ME_STORAGE,
                (part, direction) -> part.getInterfaceLogic().getInventory(),
                EnderInterfacePart.class
        );
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

    @Override
    protected InterfaceLogic createLogic()
    {
        return new EnderInterfaceLogic(getMainNode(), this, getPartItem().asItem());
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator)
    {
        MenuOpener.open(AECSMenus.ENDER_INTERFACE_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        MenuOpener.returnTo(AECSMenus.ENDER_INTERFACE_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return AECSParts.ENDER_INTERFACE_PART.toStack();
    }

    @Override
    public EnderInterfaceLogic getEnderInterfaceLogic()
    {
        return (EnderInterfaceLogic) getInterfaceLogic();
    }
}
