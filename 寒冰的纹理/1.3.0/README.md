# 1.3.0 纹理清单

本目录内的 PNG 已改为 AECS 风格的语义化文件名；所有文件均保持在原有目录，尚未复制到模组资源目录。

## 机器

- [充能共振发电机](充能共振发电机/README.md)
- [晶体注能器](晶体注能器/README.md)
- [熵变反应室](熵变反应室/README.md)
- [粉碎工厂](粉碎工厂/README.md)
- [聚合器](聚合器/README.md)
- [脉冲离心机](脉冲离心机/README.md)

## 母岩与晶簇

查看 [母岩以及晶簇](母岩以及晶簇/README.md) 获取各材料的对应说明，并查看 [前置模组内容审计](母岩以及晶簇/DEPENDENCY_AUDIT.md) 确认本轮范围与前置兼容边界。

## 接入原则

- 这里的目录是美术源文件，当前只改名、不移动。真正接入时复制到 `src/main/resources/assets/ae2cs/textures/` 对应目录，保留本目录作为可追溯源。
- 已有机器的 GUI 由 `src/main/resources/assets/ae2/screens/<machine>_menu.json` 引用，不是 Java 类直接加载 PNG。替换背景后必须同时核对 JSON 的 `srcRect`、槽位坐标和进度条切图区域。
- 256x256 菜单图和局部 GUI 元素不能用作机器六面材质。可旋转机器仍需 `textures/block/<machine>/on|off/{bottom,top,front,back,left,right}.png`。
- 未确认归属的机器在确认玩法前不得新增注册，也不得覆盖现有机器资源。

## AE2 依赖依据

- 当前 AE2 1.21.1 依赖（19.2.8）的 `StyleManager.loadStyleDoc` 通过 `AppEng.makeId` 读取以 `/screens/` 开头的路径，并递归合并 `includes`。因此本项目的机器样式文件必须继续位于 `src/main/resources/assets/ae2/screens/`；把 JSON 放进 `assets/ae2cs/screens/` 不会被现有 GUI 加载。
- AE2 的 `MenuTypeBuilder` 在构建时同时登记网络反序列化和 `MenuOpener`。新机器必须以其 BlockEntity 类作为 host 建立一个唯一菜单；不能用客户端 Screen 绕过菜单注册，也不能复用不兼容的既有 MenuType。
- AE2 的 `AEBaseEntityBlock` 专用于持有 `AEBaseBlockEntity` 的方块，负责 ticker、额外掉落与设置卡交互。只有机器使用这条链路；母岩和晶簇不应为了“统一”而继承它。
- AECS 的 `AECSBlockEntities.create` 负责将 BlockEntityType 与 AE2 方块实体、客户端/服务端 ticker 绑定；`@ProvideCaps` 由 `AECSCapabilities` 收集。新机器需要这两处接入，纯母岩和晶簇不需要。
