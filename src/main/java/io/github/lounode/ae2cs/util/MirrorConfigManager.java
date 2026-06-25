package io.github.lounode.ae2cs.util;

import appeng.api.config.Setting;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerListener;
import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderLogic;

public class MirrorConfigManager extends ConfigManager {
    private final MirrorPatternProviderLogic host;

    public MirrorConfigManager(MirrorPatternProviderLogic host, IConfigManagerListener listener) {
        super(listener);
        this.host = host;
    }

    @Override
    public <T extends Enum<T>> T getSetting(Setting<T> setting) {
        PatternProviderLogicHost target = host.cachedTarget.get();
        if(target != null) {
            return target.getConfigManager().getSetting(setting);
        }

        return super.getSetting(setting);
    }
}
