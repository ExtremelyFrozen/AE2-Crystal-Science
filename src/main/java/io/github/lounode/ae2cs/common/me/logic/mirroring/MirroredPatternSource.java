package io.github.lounode.ae2cs.common.me.logic.mirroring;

import appeng.api.crafting.IPatternDetails;
import appeng.api.util.IConfigManager;
import appeng.menu.locator.MenuHostLocator;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * 读取不同模组的主供应器，避免要求它们继承同一套样板逻辑。
 *
 * <p>
 * 实例仅在所属世界的线程上使用，不能跨越宿主卸载继续使用。源只提供样板和设置；
 * 原料发送及返回库存始终属于镜像。源不能是另一个镜像供应器，所有返回值均非空。
 * </p>
 */
public interface MirroredPatternSource {

    /** 返回源所在的方块实体，用于检查卸载状态；面板返回承载它的线缆总线。 */
    BlockEntity getBlockEntity();

    /** 在服务端读取当前样板，不转移或复制源的物品；调用方不得修改返回列表。 */
    List<IPatternDetails> getAvailablePatterns();

    /** 返回源的设置契约；读取某个设置前必须确认该设置已注册。 */
    IConfigManager getConfigManager();

    /** 返回源当前的合成优先级，不改变任一供应器的状态。 */
    int getPriority();

    /** 在服务端使用已经解析的源定位器打开源的菜单，不在镜像处创建一份配置。 */
    void openMenu(Player player, MenuHostLocator locator);
}
