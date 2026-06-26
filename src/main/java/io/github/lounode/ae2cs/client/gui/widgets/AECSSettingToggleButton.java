package io.github.lounode.ae2cs.client.gui.widgets;

import io.github.lounode.ae2cs.api.settings.AECSSettingAppearances;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;

import appeng.api.config.Setting;
import appeng.client.gui.AEBaseScreen;
import appeng.util.EnumCycler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * AE2 SettingToggleButton 的 AECS 版本：
 * - 外观来自 AECSSettingAppearances（IButtonIcon / Item overlay / tooltip）
 * - 左键正向循环，右键反向循环（依赖 AEBaseScreen#isHandlingRightClick）
 */
public class AECSSettingToggleButton<T extends Enum<T>> extends AECSIconButton {

    private final Setting<T> buttonSetting;
    private final IHandler<AECSSettingToggleButton<T>> onPress;
    private final EnumSet<T> validValues;
    private T currentValue;

    @FunctionalInterface
    public interface IHandler<B extends AECSSettingToggleButton<?>> {

        void handle(B button, boolean backwards);
    }

    public AECSSettingToggleButton(Setting<T> setting, T val, IHandler<AECSSettingToggleButton<T>> onPress) {
        this(setting, val, t -> true, onPress);
    }

    public AECSSettingToggleButton(Setting<T> setting, T val, Predicate<T> isValidValue,
                                   IHandler<AECSSettingToggleButton<T>> onPress) {
        super(AECSSettingToggleButton::onPressBridge);
        this.onPress = onPress;

        EnumSet<T> vv = EnumSet.allOf(val.getDeclaringClass());
        vv.removeIf(isValidValue.negate());
        vv.removeIf(s -> !setting.getValues().contains(s));
        this.validValues = vv;

        this.buttonSetting = setting;
        this.currentValue = val;
    }

    private static void onPressBridge(Button btn) {
        if (btn instanceof AECSSettingToggleButton<?> stb) {
            stb.triggerPress();
        }
    }

    private void triggerPress() {
        boolean backwards = false;

        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof AEBaseScreen<?> ae) {
            backwards = ae.isHandlingRightClick();
        }

        onPress.handle(this, backwards);
    }

    public Setting<T> getSetting() {
        return this.buttonSetting;
    }

    public T getCurrentValue() {
        return this.currentValue;
    }

    public void set(T e) {
        if (this.currentValue != e) {
            this.currentValue = e;
        }
    }

    public T getNextValue(boolean backwards) {
        return EnumCycler.rotateEnum(currentValue, backwards, validValues);
    }

    @Nullable
    private AECSSettingAppearances.Appearance getAppearance() {
        if (this.buttonSetting == null || this.currentValue == null) {
            return null;
        }
        return AECSSettingAppearances.getOrNull(this.buttonSetting, this.currentValue);
    }

    @Override
    protected @Nullable IButtonIcon getIcon() {
        var app = getAppearance();
        return app != null ? app.icon() : null;
    }

    @Override
    protected @Nullable Item getItemOverlay() {
        var app = getAppearance();
        return app != null ? app.item() : null;
    }

    @Override
    public @NotNull List<Component> getTooltipMessage() {
        if (this.buttonSetting == null || this.currentValue == null) {
            return Collections.emptyList();
        }

        var app = getAppearance();
        if (app == null) {
            return Collections.emptyList();
        }

        return app.tooltipLines();
    }
}
