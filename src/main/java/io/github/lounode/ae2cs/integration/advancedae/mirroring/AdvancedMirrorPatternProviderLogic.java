package io.github.lounode.ae2cs.integration.advancedae.mirroring;

import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderLogic;
import io.github.lounode.ae2cs.common.me.logic.mirroring.MirrorProviderGridNode;
import io.github.lounode.ae2cs.common.me.logic.mirroring.MirrorProviderRouting;
import io.github.lounode.ae2cs.util.MirrorConfigManager;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.crafting.ICraftingProvider;

import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 使用 AdvancedAE 原有的定向发送、重试、回收和锁定算法，样板物品仍只存在于主源。 */
final class AdvancedMirrorPatternProviderLogic extends AdvPatternProviderLogic implements MirrorProviderRouting {

    private final MirrorProviderGridNode mainNode;
    private final MirrorPatternProviderLogic mirror;
    private @Nullable IStackWatcher lastWatcher;

    AdvancedMirrorPatternProviderLogic(MirrorProviderGridNode mainNode, MirrorPatternProviderLogic mirror,
                                       AdvPatternProviderLogicHost host) {
        super(mainNode, host, 0);
        this.mainNode = mainNode;
        this.mirror = mirror;
        this.configManager = new MirrorConfigManager(mirror, super.getConfigManager());
    }

    @Override
    public void updatePatterns() {
        var source = mirror.resolveMirrorTargetSource();
        List<IPatternDetails> sourcePatterns = source == null ? List.of() : List.copyOf(source.getAvailablePatterns());
        List<IPatternDetails> patterns = super.getAvailablePatterns();
        IStackWatcher watcher = mainNode.getCraftingWatcher();
        if (patterns.equals(sourcePatterns) && lastWatcher == watcher) {
            return;
        }

        patterns.clear();
        patterns.addAll(sourcePatterns);
        patternInputs.clear();
        getOutputCache().clear();
        if (watcher != null) {
            watcher.reset();
        }
        for (IPatternDetails pattern : patterns) {
            for (var input : pattern.getInputs()) {
                for (var candidate : input.getPossibleInputs()) {
                    patternInputs.add(candidate.what().dropSecondary());
                }
            }
            for (var output : pattern.getOutputs()) {
                getOutputCache().add(output.what());
                if (watcher != null) {
                    watcher.add(output.what());
                }
            }
        }
        lastWatcher = watcher;
        ICraftingProvider.requestUpdate(mainNode);
    }
}
