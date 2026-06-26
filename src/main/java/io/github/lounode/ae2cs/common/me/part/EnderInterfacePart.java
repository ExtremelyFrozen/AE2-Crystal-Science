package io.github.lounode.ae2cs.common.me.part;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceHost;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceLogic;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.helpers.InterfaceLogic;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.parts.PartModel;
import appeng.parts.misc.InterfacePart;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

public class EnderInterfacePart extends InterfacePart implements EnderInterfaceHost {

    public static final ResourceLocation MODEL_BASE = AE2CrystalScience.makeId(
            "part/ender_interface/base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            AppEng.makeId("part/interface_has_channel"));

    public static final ResourceLocation MODEL_EXTENDED = AE2CrystalScience.makeId(
            "part/ender_interface/extended");

    @PartModels
    public static final PartModel EXTENDED_MODELS_OFF = new PartModel(MODEL_EXTENDED,
            AppEng.makeId("part/interface_off"));

    @PartModels
    public static final PartModel EXTENDED_MODELS_ON = new PartModel(MODEL_EXTENDED,
            AppEng.makeId("part/interface_on"));

    @PartModels
    public static final PartModel EXTENDED_MODELS_HAS_CHANNEL = new PartModel(MODEL_EXTENDED,
            AppEng.makeId("part/interface_has_channel"));

    public EnderInterfacePart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return isExtended() ? EXTENDED_MODELS_HAS_CHANNEL : MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return isExtended() ? EXTENDED_MODELS_ON : MODELS_ON;
        } else {
            return isExtended() ? EXTENDED_MODELS_OFF : MODELS_OFF;
        }
    }

    @Override
    public boolean isExtended() {
        return this.getPartItem() == AECSParts.EX_ENDER_INTERFACE_PART.get();
    }

    @Override
    protected InterfaceLogic createLogic() {
        int slotSize = 9;
        int absorbConfigSlots = isExtended() ? 36 : 18;
        return new EnderInterfaceLogic(getMainNode(), this, getPartItem().asItem(), slotSize, absorbConfigSlots);
    }

    @Override
    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(AECSMenus.ENDER_INTERFACE_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(AECSMenus.ENDER_INTERFACE_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getPartItem());
    }

    @Override
    public EnderInterfaceLogic getEnderInterfaceLogic() {
        return (EnderInterfaceLogic) getInterfaceLogic();
    }

    @Override
    public void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.getEnderInterfaceLogic().isRenderRangeInClient());
        data.writeInt(this.getEnderInterfaceLogic().getRange());
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data) {
        super.readFromStream(data);
        this.getEnderInterfaceLogic().setRenderRangeInClient(data.readBoolean());
        this.getEnderInterfaceLogic().setRange(data.readInt());
        return true;
    }

    @Override
    public void markForLogicClientUpdate() {
        if (this.getBlockEntity().getLevel() != null && !this.getBlockEntity().getLevel().isClientSide()) {
            this.getHost().markForUpdate();
        }
    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderDynamic(float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
        if (!getEnderInterfaceLogic().isRenderRangeInClient()) return;

        super.renderDynamic(partialTicks, poseStack, buffers, combinedLightIn, combinedOverlayIn);

        var logic = this.getEnderInterfaceLogic();
        if (logic == null || !logic.isRenderRangeInClient()) {
            return;
        }

        int r = logic.getRange();
        if (r <= 0) {
            return;
        }

        var aabb = new AABB(
                -r, -r, -r,
                r + 1.0, r + 1.0, r + 1.0);

        var consumer = buffers.getBuffer(RenderType.lines());

        float red = 0.2f, green = 0.9f, blue = 0.9f, alpha = 0.8f;

        LevelRenderer.renderLineBox(
                poseStack, consumer, aabb,
                red, green, blue, alpha);
    }
}
