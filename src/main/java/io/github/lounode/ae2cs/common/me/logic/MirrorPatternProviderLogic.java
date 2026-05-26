package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.util.MirrorConfigManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

public class MirrorPatternProviderLogic extends PatternProviderLogic {
    private final IManagedGridNode mainNode;
    private final PatternProviderLogicHost host;
    private @Nullable MirroredPatternProviderTarget mirrorTarget;
    public WeakReference<PatternProviderLogicHost> cachedTarget = new WeakReference<>(null);

    public MirrorPatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host) {
        super(mainNode, host, 0);
        this.mainNode = mainNode;
        this.host = host;
        this.configManager = new MirrorConfigManager(this, this.configManager);
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

    public @Nullable PatternProviderLogicHost resolveMirrorTargetHost() {
        if (mirrorTarget == null) {
            return null;
        }

        PatternProviderLogicHost target = cachedTarget.get();
        if (target == null || target.getBlockEntity().isRemoved() || target.getBlockEntity().getLevel() == null) {
            target = mirrorTarget.resolve(host.getBlockEntity().getLevel());
            if (target != null) {
                cachedTarget = new WeakReference<>(target);
            }
        }

        if (target instanceof MirrorPatternProviderHost mirrored && mirrored.getMirroringLogic().isMirroring()) {
            cachedTarget = new WeakReference<>(null);
            return null;
        }

        return target;
    }

    @Override
    public int getPriority() {
        PatternProviderLogicHost target = resolveMirrorTargetHost();
        if (target != null) {
            return target.getPriority();
        }

        return super.getPriority();
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

        return super.pushPattern(patternDetails, inputHolder);
    }

    @Override
    public void updatePatterns() {
        if (!isMirroring()) {
            super.updatePatterns();
            return;
        }

        PatternProviderLogicHost target = resolveMirrorTargetHost();

        if (target == null) {
            if (!patterns.isEmpty() || !patternInputs.isEmpty()) {
                patterns.clear();
                patternInputs.clear();
                ICraftingProvider.requestUpdate(mainNode);
            }
            return;
        }

        var targetPatterns = target.getLogic().getAvailablePatterns();

        if (patterns.equals(targetPatterns)) {
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
        PatternProviderLogicHost target = resolveMirrorTargetHost();

        if (target != null) {
            updatePatterns();
            return target.getLogic().getAvailablePatterns();
        }

        return super.getAvailablePatterns();
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
        MirroredPatternProviderTarget.write(mirrorTarget, tag);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        this.mirrorTarget = MirroredPatternProviderTarget.read(tag);
        this.cachedTarget = new WeakReference<>(null);
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
