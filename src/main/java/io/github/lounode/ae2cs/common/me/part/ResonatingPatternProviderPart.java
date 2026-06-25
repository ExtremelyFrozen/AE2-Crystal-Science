package io.github.lounode.ae2cs.common.me.part;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.locator.MenuLocator;
import appeng.util.SettingsFrom;
import appeng.parts.PartModel;
import appeng.parts.crafting.PatternProviderPart;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderLogic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResonatingPatternProviderPart extends PatternProviderPart implements ResonatingPatternProviderHost
{
    public static final ResourceLocation MODEL_BASE = AE2CrystalScience.makeId(
            "part/resonating_pattern_provider/base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_has_channel"));

    public static final ResourceLocation MODEL_EXTENDED = AE2CrystalScience.makeId(
            "part/resonating_pattern_provider/extended");

    @PartModels
    public static final PartModel EXTENDED_MODELS_OFF = new PartModel(MODEL_EXTENDED,
            AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel EXTENDED_MODELS_ON = new PartModel(MODEL_EXTENDED,
            AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel EXTENDED_MODELS_HAS_CHANNEL = new PartModel(MODEL_EXTENDED,
            AppEng.makeId("part/interface_has_channel"));

    public ResonatingPatternProviderPart(IPartItem<?> partItem)
    {
        super(partItem);
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
    public void openMenu(Player player, MenuLocator locator)
    {
        MenuOpener.open(AECSMenus.RESONATING_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos)
    {
        if (!player.getCommandSenderWorld().isClientSide())
        {
            openMenu(player, MenuLocators.forPart(this));
        }
        return true;
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

    @Override
    public void importSettings(SettingsFrom mode, net.minecraft.nbt.CompoundTag input, @Nullable Player player)
    {
        super.importSettings(mode, input, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM || mode == SettingsFrom.MEMORY_CARD)
        {
            getResonatingLogic().readDefaultsFromItemTag(input);
            saveChanges();
            markForLogicClientUpdate();
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, net.minecraft.nbt.CompoundTag output)
    {
        super.exportSettings(mode, output);
        if (mode == SettingsFrom.DISMANTLE_ITEM || mode == SettingsFrom.MEMORY_CARD)
        {
            getResonatingLogic().writeDefaultsToItemTag(output);
        }
    }

    @Override
    public void writeToStream(FriendlyByteBuf data)
    {
        super.writeToStream(data);
        getResonatingLogic().writeVisualSync(data);
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data)
    {
        boolean redraw = super.readFromStream(data);
        return getResonatingLogic().readVisualSync(data) || redraw;
    }

    @Override
    public void markForLogicClientUpdate()
    {
        if (this.getBlockEntity().getLevel() != null && !this.getBlockEntity().getLevel().isClientSide())
        {
            this.getHost().markForUpdate();
        }
    }

    @Override
    public void writeToNBT(net.minecraft.nbt.CompoundTag data)
    {
        super.writeToNBT(data);
        getResonatingLogic().writeToNBT(data);
    }

    @Override
    public void readFromNBT(net.minecraft.nbt.CompoundTag data)
    {
        super.readFromNBT(data);
        getResonatingLogic().readFromNBT(data);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched)
    {
        super.addAdditionalDrops(drops, wrenched);
        getResonatingLogic().addDrops(drops);
    }

    @Override
    public void clearContent()
    {
        super.clearContent();
        getResonatingLogic().clearContent();
    }
}
