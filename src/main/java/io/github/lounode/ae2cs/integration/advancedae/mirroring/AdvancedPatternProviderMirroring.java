package io.github.lounode.ae2cs.integration.advancedae.mirroring;

import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderLogic;
import io.github.lounode.ae2cs.common.me.logic.mirroring.MirrorProviderGridNode;
import io.github.lounode.ae2cs.common.me.logic.mirroring.MirrorProviderRouting;
import io.github.lounode.ae2cs.common.me.logic.mirroring.MirroredPatternSource;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.locator.MenuHostLocator;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost;

import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/** 仅在 AdvancedAE 存在时加载此入口，避免普通镜像依赖它的宿主和逻辑类型。 */
public final class AdvancedPatternProviderMirroring {

    private AdvancedPatternProviderMirroring() {}

    public static @Nullable MirroredPatternSource fromHost(@Nullable Object host) {
        return host instanceof AdvPatternProviderLogicHost provider ? new AdvancedPatternSource(provider) : null;
    }

    public static MirrorProviderRouting createRouting(MirrorProviderGridNode node, MirrorPatternProviderLogic mirror,
                                                      PatternProviderLogicHost host) {
        var routingHost = new AdvancedMirrorHost(host);
        var routing = new AdvancedMirrorPatternProviderLogic(node, mirror, routingHost);
        routingHost.logic = routing;
        return routing;
    }

    private record AdvancedPatternSource(AdvPatternProviderLogicHost host) implements MirroredPatternSource {

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

    private static final class AdvancedMirrorHost implements AdvPatternProviderLogicHost {

        private final PatternProviderLogicHost host;
        private @Nullable AdvPatternProviderLogic logic;

        private AdvancedMirrorHost(PatternProviderLogicHost host) {
            this.host = host;
        }

        @Override
        public AdvPatternProviderLogic getLogic() {
            return Objects.requireNonNull(logic, "Mirror routing has not finished construction");
        }

        @Override
        public BlockEntity getBlockEntity() {
            return host.getBlockEntity();
        }

        @Override
        public EnumSet<Direction> getTargets() {
            return EnumSet.copyOf(host.getTargets());
        }

        @Override
        public void saveChanges() {
            host.saveChanges();
        }

        @Override
        public AEItemKey getTerminalIcon() {
            return host.getTerminalIcon();
        }

        @Override
        public ItemStack getMainMenuIcon() {
            return host.getMainMenuIcon();
        }
    }
}
