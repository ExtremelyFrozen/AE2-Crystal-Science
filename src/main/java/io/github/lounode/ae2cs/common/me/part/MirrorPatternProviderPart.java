package io.github.lounode.ae2cs.common.me.part;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.items.parts.PartModels;
import appeng.menu.locator.MenuHostLocator;
import appeng.parts.PartModel;
import appeng.parts.crafting.PatternProviderPart;
import appeng.util.SettingsFrom;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderLogic;
import io.github.lounode.ae2cs.common.me.logic.MirroredPatternProviderTarget;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MirrorPatternProviderPart extends PatternProviderPart implements MirrorPatternProviderHost
{
    public static final ResourceLocation MODEL_BASE = AE2CrystalScience.makeId(
            "part/mirror_pattern_provider/base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_has_channel"));

    public MirrorPatternProviderPart(IPartItem<?> partItem)
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
        return new MirrorPatternProviderLogic(getMainNode(), this);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator)
    {
        var targetRef = getMirroringLogic().getMirrorTarget();
        if (targetRef != null)
        {
            MenuHostLocator targetLocator = targetRef.toMenuLocator(getBlockEntity().getLevel());
            var mirrorTarget = getMirroringLogic().resolveMirrorTargetHost();
            if (mirrorTarget != null && targetLocator != null)
            {
                mirrorTarget.openMenu(player, targetLocator);
                return;
            }
        }

        if (!player.level().isClientSide())
        {
            player.displayClientMessage(Component.translatable("ae2cs.msg.mirror_pattern_provider.target_missing"), true);
        }
    }

    @Override
    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(AECSParts.MIRROR_PATTERN_PROVIDER_PART.get());
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(AECSParts.MIRROR_PATTERN_PROVIDER_PART.get());
    }

    @Override
    public MirrorPatternProviderLogic getMirroringLogic()
    {
        return (MirrorPatternProviderLogic) getLogic();
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player)
    {
        super.importSettings(mode, input, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM || mode == SettingsFrom.MEMORY_CARD)
        {
            getMirroringLogic().readMirrorSettings(input);
            saveChanges();
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder output)
    {
        super.exportSettings(mode, output);
        if (mode == SettingsFrom.DISMANTLE_ITEM || mode == SettingsFrom.MEMORY_CARD)
        {
            getMirroringLogic().writeMirrorSettings(output);
        }
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data)
    {
        super.writeToStream(data);
        var target = getMirroringLogic().getMirrorTarget();
        data.writeBoolean(target != null);
        if (target != null)
        {
            target.write(data);
        }
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data)
    {
        boolean redraw = super.readFromStream(data);
        getMirroringLogic().setMirrorTarget(data.readBoolean() ? MirroredPatternProviderTarget.read(data) : null);
        return redraw || true;
    }

    @Override
    public void markForLogicClientUpdate()
    {
        if (this.getBlockEntity().getLevel() != null && !this.getBlockEntity().getLevel().isClientSide())
        {
            this.getHost().markForUpdate();
        }
    }
}
