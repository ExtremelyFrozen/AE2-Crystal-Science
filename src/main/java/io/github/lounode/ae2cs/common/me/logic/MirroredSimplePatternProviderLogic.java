package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.config.Setting;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import io.github.lounode.ae2cs.util.MirrorConfigManager;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MirroredSimplePatternProviderLogic extends PatternProviderLogic
{
    private final PatternProviderLogicHost host;
    private @Nullable MirroredPatternProviderTarget mirrorTarget;
    public WeakReference<PatternProviderLogicHost> cachedTarget = new WeakReference<>(null);

    public MirroredSimplePatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize)
    {
        super(mainNode, host, patternInventorySize);
        this.host = host;
        var newConfigManager = new MirrorConfigManager(this, this::configChanged);
        this.configManager.getSettings().forEach(
                s -> newConfigManager.registerSetting((Setting)s, configManager.getSetting((Setting)s))
        );
        this.configManager = newConfigManager;
    }

    public boolean isMirroring()
    {
        return mirrorTarget != null;
    }

    public @Nullable MirroredPatternProviderTarget getMirrorTarget()
    {
        return mirrorTarget;
    }

    public void setMirrorTarget(@Nullable MirroredPatternProviderTarget mirrorTarget)
    {
        this.mirrorTarget = mirrorTarget;
        this.cachedTarget = new WeakReference<>(null);
        updatePatterns();
        host.saveChanges();
    }

    public @Nullable PatternProviderLogicHost resolveMirrorTargetHost()
    {
        if (mirrorTarget == null)
        {
            return null;
        }

        PatternProviderLogicHost target = cachedTarget.get();
        if (target == null || target.getBlockEntity().isRemoved() || target.getBlockEntity().getLevel() == null)
        {
            target = mirrorTarget.resolve(host.getBlockEntity().getLevel());
            if (target != null)
            {
                cachedTarget = new WeakReference<>(target);
            }
        }

        if (target instanceof MirroredSimplePatternProviderHost mirrored && mirrored.getMirroringLogic().isMirroring())
        {
            cachedTarget = new WeakReference<>(null);
            return null;
        }

        return target;
    }

    @Override
    public int getPriority() {
        PatternProviderLogicHost target = resolveMirrorTargetHost();
        if(target != null) {
            return target.getPriority();
        }

        return super.getPriority();
    }

    @Override
    public void updatePatterns()
    {
        if (!isMirroring())
        {
            super.updatePatterns();
            return;
        }

        PatternProviderLogicHost target = resolveMirrorTargetHost();

        if (target != null)
        {
            var targetPatterns = target.getLogic().getAvailablePatterns();

            if(patterns.equals(targetPatterns)) return;

            patterns.clear();
            patternInputs.clear();

            patterns.addAll(targetPatterns);
            for (var details : patterns)
            {
                for (var iinput : details.getInputs())
                {
                    for (var inputCandidate : iinput.getPossibleInputs())
                    {
                        patternInputs.add(inputCandidate.what().dropSecondary());
                    }
                }
            }
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        PatternProviderLogicHost target = resolveMirrorTargetHost();

        if(target != null) {
            updatePatterns();
            return target.getLogic().getAvailablePatterns();
        }

        return super.getAvailablePatterns();
    }

    @Override
    public void writeToNBT(CompoundTag tag)
    {
        super.writeToNBT(tag);
        MirroredPatternProviderTarget.write(mirrorTarget, tag);
    }

    @Override
    public void readFromNBT(CompoundTag tag)
    {
        super.readFromNBT(tag);
        this.mirrorTarget = MirroredPatternProviderTarget.read(tag);
        this.cachedTarget = new WeakReference<>(null);
        if (isMirroring())
        {
            updatePatterns();
        }
    }

    public void writeMirrorSettings(CompoundTag tag)
    {
        MirroredPatternProviderTarget.write(mirrorTarget, tag);
    }

    public void readMirrorSettings(CompoundTag tag)
    {
        this.mirrorTarget = MirroredPatternProviderTarget.read(tag);
        this.cachedTarget = new WeakReference<>(null);
        updatePatterns();
    }
}
