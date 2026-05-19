package io.github.lounode.ae2cs.common.me.part;

import appeng.api.AECapabilities;
import appeng.api.parts.IPartItem;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.stacks.AEItemKey;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.parts.crafting.PatternProviderPart;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderLogic;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ResonatingPatternProviderPart extends PatternProviderPart implements ResonatingPatternProviderHost
{
    public ResonatingPatternProviderPart(IPartItem<?> partItem)
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
                (part, direction) -> part.getLogic().getReturnInv(),
                ResonatingPatternProviderPart.class
        );
    }

    @Override
    public boolean isExtended()
    {
        return getPartItem() == AECSParts.EX_RESONATING_PATTERN_PROVIDER_PART.get();
    }

    @Override
    protected PatternProviderLogic createLogic()
    {
        int patternSize = isExtended() ? 36 : 9;
        return new ResonatingPatternProviderLogic(getMainNode(), this, patternSize);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator)
    {
        MenuOpener.open(AECSMenus.RESONATING_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        MenuOpener.returnTo(AECSMenus.RESONATING_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(getPartItem());
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(getPartItem());
    }

    @Override
    public ResonatingPatternProviderLogic getResonatingLogic()
    {
        return (ResonatingPatternProviderLogic) getLogic();
    }
}
