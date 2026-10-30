package io.github.lounode.ae2cs.common.me.logic;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.me.logic.mirroring.MirrorProviderGridNode;
import io.github.lounode.ae2cs.common.me.logic.mirroring.MirrorProviderRouting;
import io.github.lounode.ae2cs.common.me.logic.mirroring.MirroredPatternSource;
import io.github.lounode.ae2cs.common.me.logic.mirroring.PatternProviderSources;
import io.github.lounode.ae2cs.integration.advancedae.mirroring.AdvancedPatternProviderMirroring;
import io.github.lounode.ae2cs.util.MirrorConfigManager;

import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

public class MirrorPatternProviderLogic extends PatternProviderLogic {

    private final IManagedGridNode mainNode;
    private final PatternProviderLogicHost host;
    private final @Nullable MirrorProviderRouting routing;
    private @Nullable MirroredPatternProviderTarget mirrorTarget;
    public WeakReference<PatternProviderLogicHost> cachedTarget = new WeakReference<>(null);

    public MirrorPatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host) {
        this(new MirrorProviderGridNode(mainNode), host);
    }

    private MirrorPatternProviderLogic(MirrorProviderGridNode mainNode, PatternProviderLogicHost host) {
        super(mainNode, host, 0);
        this.mainNode = mainNode;
        this.host = host;
        // Use one engine for every source so rebinding cannot strand a second queue or return inventory.
        this.routing = ModList.get().isLoaded(AECSConstants.AAE_ID) ? AdvancedPatternProviderMirroring.createRouting(mainNode, this, host) : null;
        this.configManager = routing == null ? new MirrorConfigManager(this, this.configManager) : routing.getConfigManager();
    }

    public boolean isMirroring() {
        return mirrorTarget != null;
    }

    public @Nullable MirroredPatternProviderTarget getMirrorTarget() {
        return mirrorTarget;
    }

    public void setMirrorTarget(@Nullable MirroredPatternProviderTarget mirrorTarget) {
        this.mirrorTarget = mirrorTarget;
        this.cachedTarget = new WeakReference<>(null);
        updatePatterns();
        host.saveChanges();
        if (host instanceof MirrorPatternProviderHost mirrorHost) {
            mirrorHost.markForLogicClientUpdate();
        }
    }

    public boolean readMirrorTargetFromStream(@Nullable MirroredPatternProviderTarget mirrorTarget) {
        boolean changed = !Objects.equals(this.mirrorTarget, mirrorTarget);
        this.mirrorTarget = mirrorTarget;
        this.cachedTarget = new WeakReference<>(null);
        return changed;
    }

    public @Nullable PatternProviderLogicHost resolveMirrorTargetHost() {
        PatternProviderLogicHost target = mirrorTarget == null ? null : mirrorTarget.resolve(host.getBlockEntity().getLevel());
        if (cachedTarget.get() != target) {
            cachedTarget = new WeakReference<>(target);
        }
        return target;
    }

    public @Nullable MirroredPatternSource resolveMirrorTargetSource() {
        // Resolve the part at its current side as well: replacing a part keeps its cable bus alive.
        if (mirrorTarget == null) {
            return null;
        }
        PatternProviderLogicHost target = resolveMirrorTargetHost();
        return target == null ? mirrorTarget.resolveSource(host.getBlockEntity().getLevel()) : PatternProviderSources.fromHost(target);
    }

    @Override
    public int getPriority() {
        MirroredPatternSource target = resolveMirrorTargetSource();
        if (target != null) {
            return target.getPriority();
        }

        return routing == null ? super.getPriority() : routing.getPatternPriority();
    }

    @Override
    public int getPatternPriority() {
        return getPriority();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (isMirroring()) {
            updatePatterns();
        }

        return routing == null ? super.pushPattern(patternDetails, inputHolder) : routing.pushPattern(patternDetails, inputHolder);
    }

    @Override
    public void updatePatterns() {
        updateMirroredPatterns(false);
    }

    /**
     * Re-registers the mirrored patterns after this provider returns to an active grid.
     *
     * <p>
     * AE2 removes a node's crafting-provider cache while rebuilding a grid. A mirror can keep the
     * same pattern list across that transition, in which case {@link #updatePatterns()} correctly sees
     * no content change but would otherwise skip the cache refresh.
     * </p>
     */
    public void refreshMirroredPatternsAfterGridReconnect() {
        if (!isMirroring() || !mainNode.isActive()) {
            return;
        }

        updateMirroredPatterns(true);
    }

    private void updateMirroredPatterns(boolean forceCraftingProviderRefresh) {
        if (routing != null) {
            routing.updatePatterns();
            if (forceCraftingProviderRefresh) {
                ICraftingProvider.requestUpdate(mainNode);
            }
            return;
        }
        if (!isMirroring()) {
            super.updatePatterns();
            return;
        }

        MirroredPatternSource target = resolveMirrorTargetSource();

        if (target == null) {
            if (!patterns.isEmpty() || !patternInputs.isEmpty()) {
                patterns.clear();
                patternInputs.clear();
                ICraftingProvider.requestUpdate(mainNode);
            } else if (forceCraftingProviderRefresh) {
                ICraftingProvider.requestUpdate(mainNode);
            }
            return;
        }

        var targetPatterns = target.getAvailablePatterns();

        if (patterns.equals(targetPatterns)) {
            if (forceCraftingProviderRefresh) {
                ICraftingProvider.requestUpdate(mainNode);
            }
            return;
        }

        patterns.clear();
        patternInputs.clear();

        patterns.addAll(targetPatterns);
        for (var details : patterns) {
            for (var input : details.getInputs()) {
                for (var inputCandidate : input.getPossibleInputs()) {
                    patternInputs.add(inputCandidate.what().dropSecondary());
                }
            }
        }

        ICraftingProvider.requestUpdate(mainNode);
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        if (isMirroring()) {
            updatePatterns();
        }
        return routing == null ? super.getAvailablePatterns() : routing.getAvailablePatterns();
    }

    @Override
    public boolean isBusy() {
        return routing == null ? super.isBusy() : routing.isBusy();
    }

    @Override
    public PatternProviderReturnInventory getReturnInv() {
        return routing == null ? super.getReturnInv() : routing.getReturnInv();
    }

    @Override
    public LockCraftingMode getCraftingLockedReason() {
        return routing == null ? super.getCraftingLockedReason() : routing.getCraftingLockedReason();
    }

    @Override
    public @Nullable GenericStack getUnlockStack() {
        return routing == null ? super.getUnlockStack() : routing.getUnlockStack();
    }

    @Override
    public void resetCraftingLock() {
        if (routing == null) {
            super.resetCraftingLock();
        } else {
            routing.resetCraftingLock();
        }
    }

    @Override
    public void onMainNodeStateChanged() {
        if (routing == null) {
            super.onMainNodeStateChanged();
        } else {
            routing.onMainNodeStateChanged();
        }
    }

    @Override
    public void updateRedstoneState() {
        if (routing == null) {
            super.updateRedstoneState();
        } else {
            routing.updateRedstoneState();
        }
    }

    @Override
    public void addDrops(List<ItemStack> drops) {
        if (routing == null) {
            super.addDrops(drops);
        } else {
            routing.addDrops(drops);
        }
    }

    @Override
    public void clearContent() {
        if (routing == null) {
            super.clearContent();
        } else {
            routing.clearContent();
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (routing == null) {
            super.writeToNBT(tag, registries);
        } else {
            routing.writeToNBT(tag, registries);
        }
        MirroredPatternProviderTarget.write(mirrorTarget, tag);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        this.mirrorTarget = MirroredPatternProviderTarget.read(tag);
        this.cachedTarget = new WeakReference<>(null);
        if (routing == null) {
            super.readFromNBT(tag, registries);
        } else {
            routing.readFromNBT(tag, registries);
        }
        if (isMirroring()) {
            updatePatterns();
        }
    }

    public void writeMirrorSettings(DataComponentMap.Builder builder) {
        if (mirrorTarget != null) {
            builder.set(AECSDataComponents.MIRROR_PATTERN_PROVIDER_TARGET.get(), mirrorTarget);
        }
    }

    public void readMirrorSettings(DataComponentMap input) {
        this.mirrorTarget = input.get(AECSDataComponents.MIRROR_PATTERN_PROVIDER_TARGET.get());
        this.cachedTarget = new WeakReference<>(null);
        updatePatterns();
        if (host instanceof MirrorPatternProviderHost mirrorHost) {
            mirrorHost.markForLogicClientUpdate();
        }
    }
}
