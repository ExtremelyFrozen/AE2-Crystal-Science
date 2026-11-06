package io.github.lounode.ae2cs.common.me.logic.mirroring;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderHost;
import io.github.lounode.ae2cs.integration.advancedae.mirroring.AdvancedPatternProviderMirroring;

import appeng.api.crafting.IPatternDetails;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.locator.MenuHostLocator;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PatternProviderSources {

    private PatternProviderSources() {}

    public static @Nullable MirroredPatternSource fromHost(@Nullable Object host) {
        if (host instanceof MirrorPatternProviderHost) {
            return null;
        }
        if (host instanceof PatternProviderLogicHost provider) {
            return new Ae2PatternSource(provider);
        }
        if (ModList.get().isLoaded(AECSConstants.AAE_ID)) {
            return AdvancedPatternProviderMirroring.fromHost(host);
        }
        return null;
    }

    private record Ae2PatternSource(PatternProviderLogicHost host) implements MirroredPatternSource {

        @Override
        public BlockEntity getBlockEntity() {
            return host.getBlockEntity();
        }

        @Override
        public List<IPatternDetails> getAvailablePatterns() {
            return host.getLogic().getAvailablePatterns();
        }

        @Override
        public IConfigManager getConfigManager() {
            return host.getConfigManager();
        }

        @Override
        public int getPriority() {
            return host.getPriority();
        }

        @Override
        public void openMenu(Player player, MenuHostLocator locator) {
            host.openMenu(player, locator);
        }
    }
}
