package io.github.lounode.ae2cs.common.menu.submenu;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.ClientActionKey;
import appeng.menu.guisync.GuiSync;
import io.netty.buffer.ByteBuf;
import io.github.lounode.ae2cs.api.networking.SideConfigField;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenu;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.machine.IMachineHost;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;
import io.github.lounode.ae2cs.common.machine.component.SidePolicy;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class SideConfigMenu extends AEBaseMenu implements CustomReturnableSubMenu
{
    private static final ClientActionKey<SideConfigChoice> changeSideDirPolicy = new ClientActionKey<>("change_side_dir_policy");
    private static final ClientActionKey<Void> clearSideDirPolicy = new ClientActionKey<>("clear_side_dir_policy");
    private static final ClientActionKey<Boolean> changeAutoImport = new ClientActionKey<>("change_auto_import");
    private static final ClientActionKey<Boolean> changeAutoExport = new ClientActionKey<>("change_auto_export");

    private MenuType<?> returnToMenuType;
    private final CustomReturnableSubMenuHost host;
    private final SideConfigComponent sideConfig;

    @GuiSync(10)
    public SideConfigField sidePolicies;

    public SideConfigMenu(MenuType<?> menuType, int id, Inventory playerInventory, CustomReturnableSubMenuHost host)
    {
        super(menuType, id, playerInventory, host);

        // 返回至构造发生时的menu，这里做保底防止点击按钮失效，我们在实际打开前后手动设定这个类型
        this.returnToMenuType = menuType;
        this.host = host;

        if (!(this.host instanceof IMachineHost machineHost))
        {
            throw new IllegalArgumentException("Host must be an instance of IMachineHost");
        }
        this.sideConfig = machineHost.getMachineComponents().getService(SideConfigComponent.class);

        registerClientAction(changeSideDirPolicy, SideConfigChoice.STREAM_CODEC, this::onChangeSideDirPolicy);
        registerClientAction(clearSideDirPolicy, this::onClearSideDirPolicy);
        registerClientAction(changeAutoImport, ByteBufCodecs.BOOL, this::onChangeAutoImport);
        registerClientAction(changeAutoExport, ByteBufCodecs.BOOL, this::onChangeAutoExport);
    }

    public void setReturnToMenuType(MenuType<?> returnToMenuType)
    {
        this.returnToMenuType = returnToMenuType;
    }

    @Override
    public void broadcastChanges()
    {
        sidePolicies = new SideConfigField(sideConfig.getPolicies().clone(), sideConfig.isAutoImport(), sideConfig.isAutoExport());
        super.broadcastChanges();
    }

    public void sendChangeSideDirPolicy(SideConfigChoice choice)
    {
        sendClientAction(changeSideDirPolicy, choice);
    }

    public void sendClearSideDirPolicy()
    {
        sendClientAction(clearSideDirPolicy);
    }

    public void sendChangeAutoImport(boolean autoImport)
    {
        sendClientAction(changeAutoImport, autoImport);
    }

    public void sendChangeAutoExport(boolean autoExport)
    {
        sendClientAction(changeAutoExport, autoExport);
    }

    private void onChangeSideDirPolicy(SideConfigChoice choice)
    {
        this.sideConfig.set(choice.dir(), choice.policy());
    }

    private void onClearSideDirPolicy()
    {
        for (Direction dir : Direction.values())
        {
            this.sideConfig.set(dir, SidePolicy.NONE);
        }
    }

    private void onChangeAutoImport(boolean autoImport)
    {
        this.sideConfig.setAutoImport(autoImport);
    }

    private void onChangeAutoExport(boolean autoExport)
    {
        this.sideConfig.setAutoExport(autoExport);
    }

    @Override
    public MenuType<?> getReturnToMenuType()
    {
        return this.returnToMenuType;
    }

    @Override
    public CustomReturnableSubMenuHost getHost()
    {
        return this.host;
    }

    public record SideConfigChoice(Direction dir, SidePolicy policy)
    {
        public static final StreamCodec<ByteBuf, SideConfigChoice> STREAM_CODEC = StreamCodec.composite(
                Direction.STREAM_CODEC,
                SideConfigChoice::dir,
                SidePolicy.STREAM_CODEC,
                SideConfigChoice::policy,
                SideConfigChoice::new
        );
    }
}
