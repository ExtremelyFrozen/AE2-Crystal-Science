package io.github.lounode.ae2cs.integration.jade;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.block.entity.SimplePatternProviderBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public final class SimplePatternProviderDataProvider implements IBlockComponentProvider
{
    public static final SimplePatternProviderDataProvider INSTANCE = new SimplePatternProviderDataProvider();
    private static final ResourceLocation UID = AE2CrystalScience.makeId("jade/simple_pattern_provider");

    private SimplePatternProviderDataProvider()
    {
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
    {
        if (!(accessor.getBlockEntity() instanceof SimplePatternProviderBlockEntity be))
        {
            return;
        }

        var target = be.getMirroringLogic().getMirrorTarget();
        if (target == null)
        {
            tooltip.add(Component.translatable("jade.ae2cs.simple_pattern_provider.mode",
                    Component.translatable("jade.ae2cs.simple_pattern_provider.mode.normal")));
            return;
        }

        tooltip.add(Component.translatable("jade.ae2cs.simple_pattern_provider.mode",
                Component.translatable("jade.ae2cs.simple_pattern_provider.mode.mirror")));

        var pos = target.pos().pos();
        tooltip.add(Component.translatable("jade.ae2cs.simple_pattern_provider.target.pos", pos.getX(), pos.getY(), pos.getZ()));
        tooltip.add(Component.translatable("jade.ae2cs.simple_pattern_provider.target.side",
                target.side() == null ? Component.translatable("jade.ae2cs.simple_pattern_provider.target.side.block") : Component.literal(target.side().getName())));
    }

    @Override
    public ResourceLocation getUid()
    {
        return UID;
    }
}
