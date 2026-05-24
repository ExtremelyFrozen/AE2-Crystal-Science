package io.github.lounode.ae2cs.util;

import appeng.api.config.Setting;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderLogic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.Set;

public class MirrorConfigManager implements IConfigManager {
    private final MirrorPatternProviderLogic host;
    private final IConfigManager delegate;

    public MirrorConfigManager(MirrorPatternProviderLogic host, IConfigManager delegate) {
        this.host = host;
        this.delegate = delegate;
    }

    @Override
    public Set<Setting<?>> getSettings() {
        return this.delegate.getSettings();
    }

    @Override
    public <T extends Enum<T>> T getSetting(Setting<T> setting) {
        PatternProviderLogicHost target = host.cachedTarget.get();
        if (target != null) {
            return target.getConfigManager().getSetting(setting);
        }

        return this.delegate.getSetting(setting);
    }

    @Override
    public <T extends Enum<T>> void putSetting(Setting<T> setting, T newValue) {
        this.delegate.putSetting(setting, newValue);
    }

    @Override
    public void writeToNBT(CompoundTag destination, HolderLookup.Provider registries) {
        this.delegate.writeToNBT(destination, registries);
    }

    @Override
    public boolean readFromNBT(CompoundTag src, HolderLookup.Provider registries) {
        return this.delegate.readFromNBT(src, registries);
    }

    @Override
    public boolean importSettings(Map<String, String> settings) {
        return this.delegate.importSettings(settings);
    }

    @Override
    public Map<String, String> exportSettings() {
        return this.delegate.exportSettings();
    }
}
