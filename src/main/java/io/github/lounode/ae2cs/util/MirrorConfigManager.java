package io.github.lounode.ae2cs.util;

import appeng.api.config.Setting;
import appeng.api.util.IConfigManager;
import appeng.api.util.UnsupportedSettingException;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerListener;
import io.github.lounode.ae2cs.common.me.logic.MirroredSimplePatternProviderLogic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class MirrorConfigManager extends ConfigManager {
    private final MirroredSimplePatternProviderLogic host;

    public MirrorConfigManager(MirroredSimplePatternProviderLogic host, IConfigManagerListener listener) {
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
