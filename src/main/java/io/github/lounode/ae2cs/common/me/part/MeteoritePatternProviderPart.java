package io.github.lounode.ae2cs.common.me.part;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.parts.PartModel;
import appeng.parts.crafting.PatternProviderPart;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.me.logic.MeteoritePatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MeteoritePatternProviderLogic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MeteoritePatternProviderPart extends PatternProviderPart implements MeteoritePatternProviderHost
{
    public static final ResourceLocation MODEL_BASE = AE2CrystalScience.makeId(
            "part/meteorite_pattern_provider/base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_has_channel"));

    public MeteoritePatternProviderPart(IPartItem<?> partItem)
    {
        super(partItem);
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
    protected PatternProviderLogic createLogic()
    {
        return new MeteoritePatternProviderLogic(getMainNode(), this, 63);
    }

    @Override
    public void openMenu(Player player, MenuLocator locator)
    {
        MenuOpener.open(AECSMenus.METEORITE_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        MenuOpener.returnTo(AECSMenus.METEORITE_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(AECSParts.METEORITE_PATTERN_PROVIDER_PART.get());
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(AECSParts.METEORITE_PATTERN_PROVIDER_PART.get());
    }
}
