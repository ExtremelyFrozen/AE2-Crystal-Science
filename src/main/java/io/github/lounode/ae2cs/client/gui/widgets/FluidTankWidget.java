package io.github.lounode.ae2cs.client.gui.widgets;

import io.github.lounode.ae2cs.api.networking.FluidTankState;

import appeng.client.gui.widgets.ITooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Displays the liquid over the tank artwork; container transfer is handled by the menu action. */
public class FluidTankWidget extends AbstractWidget implements ITooltip {

    private final Supplier<FluidTankState> state;
    private final Runnable clickAction;
    private final Supplier<List<Component>> additionalTooltip;

    public FluidTankWidget(int x, int y, Supplier<FluidTankState> state, Runnable clickAction) {
        this(x, y, 18, 60, state, clickAction, List::of);
    }

    public FluidTankWidget(int x, int y, Supplier<FluidTankState> state, Runnable clickAction,
                           Supplier<List<Component>> additionalTooltip) {
        this(x, y, 18, 60, state, clickAction, additionalTooltip);
    }

    public FluidTankWidget(int x, int y, int width, int height, Supplier<FluidTankState> state,
                           Runnable clickAction) {
        this(x, y, width, height, state, clickAction, List::of);
    }

    public FluidTankWidget(int x, int y, int width, int height, Supplier<FluidTankState> state,
                           Runnable clickAction, Supplier<List<Component>> additionalTooltip) {
        super(x, y, width, height, Component.empty());
        this.state = state;
        this.clickAction = clickAction;
        this.additionalTooltip = additionalTooltip;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        FluidTankState tank = state.get();
        FluidStack fluid = tank.fluid();
        if (fluid.isEmpty() || tank.capacity() <= 0) return;

        int fillHeight = Math.max(1, Math.min(height - 2, fluid.getAmount() * (height - 2) / tank.capacity()));
        int tint = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(IClientFluidTypeExtensions.of(fluid.getFluid()).getStillTexture(fluid));
        int x = getX() + 1;
        int y = getY() + height - 1 - fillHeight;
        int remaining = fillHeight;

        RenderSystem.setShaderColor(((tint >> 16) & 0xFF) / 255.0F, ((tint >> 8) & 0xFF) / 255.0F,
                (tint & 0xFF) / 255.0F, 1.0F);
        while (remaining > 0) {
            int tileHeight = Math.min(16, remaining);
            graphics.blit(x, y, 0, width - 2, tileHeight, sprite);
            y += tileHeight;
            remaining -= tileHeight;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && active && visible && isMouseOver(mouseX, mouseY)) {
            clickAction.run();
            return true;
        }
        return false;
    }

    @Override
    public List<Component> getTooltipMessage() {
        FluidTankState tank = state.get();
        FluidStack fluid = tank.fluid();
        Component name = fluid.isEmpty() ? Component.translatable("gui.ae2cs.fluid.empty") : fluid.getHoverName();
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(name);
        tooltip.add(Component.literal(formatBuckets(fluid.getAmount()) + " / " + formatBuckets(tank.capacity())));
        tooltip.addAll(additionalTooltip.get());
        return tooltip;
    }

    private static String formatBuckets(int amount) {
        if (amount % 1000 == 0) return amount / 1000 + " B";
        return amount + " mB";
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(getX(), getY(), width, height);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return visible;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {}
}
