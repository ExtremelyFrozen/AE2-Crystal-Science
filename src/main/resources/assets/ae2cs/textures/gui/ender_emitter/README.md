# ME 末影发信器界面素材

本界面专用的背景、范围图、组件及滚动条素材统一存放在本目录。

## 原图保持原样

`background.png` 与用户提供的 `d78e6d8f8ba36e00ebb422985d07329b.png` 完全一致，
`range.png` 与用户提供的 `64c0a1f730d804d5d47750ffa1804489.png` 完全一致。
排版应调整组件坐标，保留原图的表格分隔线、轨道、网格和槽位阴影，不修改或重新绘制这些像素。

| 文件 | SHA-256 |
| --- | --- |
| background.png | 05c1031596abe8e05f1d9b78f2aa0161727f81153aada75afd97e196fa15c5a7 |
| range.png | 6bfe594d76e0bf7f48a1b5ea35c27d2dbcde7d6ee02dd628cec5abb0d2e33891 |

背景使用 `[0, 0, 253, 222]`，原图仍为 256×256。
左侧六行内容区域分别从 y=21、38、55、72、89、106 开始，宽 69、高 16；避开原有顶部阴影和行分隔线。
右下网格位于 `(198, 142)`，尺寸 43×43。
范围图集的每个 44×44 单元有一像素透明上边和左边，绘制时从 `(tier*44+1, 1)` 取 43×43。
白色选框保留原图提供的底部阴影，不叠画另一套网格或槽位背景。

## 组件来源

`widgets.png` 为 512×512 图集；坐标与绘制方法统一在 `EnderEmitterTextures.java`。

| 图集位置 | 尺寸 | 来源 |
| --- | --- | --- |
| (0, 0) | 256×256 | AE2CS `textures/gui/icons.png` 的完整副本 |
| (256, 0) | 256×256 | Applied Energistics 2 `textures/guis/states.png` 的完整副本，LGPL-3.0 |
| (0, 256) | 34×11 | NeoECOAE `textures/guis/storage/storage_widget.png` 的 `(0,104,34,11)` 卡片组件 |
| (40, 256) | 29×4 | 同一 NeoECOAE 图集的 `(68,105,29,4)` 蓝色条 |
| (40, 264) | 29×4 | 同一 NeoECOAE 图集的 `(68,109,29,4)` 粉色条 |

`scrollbar.png` 为同一 NeoECOAE 图集的 `(68,113,6,15)`，`scrollbar_disabled.png` 保留相同几何并使用灰暗配色。
六像素滑块以原轨道中心对齐，拖动、翻页和滚轮由 AE2 `Scrollbar` 处理。
两个滑块通过 `assets/minecraft/atlases/gui.json` 注册，纹理文件仍统一位于本目录。

来源仓库：`DancingSnow0517/NeoECOAEExtension`，本地素材路径为 `src/main/resources/assets/neoecoae/textures/guis/storage/storage_widget.png`。

文字采用 Minecraft 原字号；左侧表格和上方统计区采用 17 像素等行距，接收设置区采用 14 像素等行距。
接收设置区三行文字从 y=77、91、105 开始，四个按钮与数值框均位于 y=101、高 17，保留下边框留白。
增强器物品槽位于 `(178, 193)`，与原图占位组件的顶部对齐。
统计数值右端位于 x=231；已用量采用与左侧标记对应的蓝色或粉色，总量为绿色，斜线为灰色。
长名称省略，完整名称和精确数值通过悬停提示显示。
普通数值按钮由 AE2 `AE2Button` 绘制；图标按钮保留 AE2 的悬停位移、焦点外观和点击处理。
增强器槽采用 AE2 无线接入点同款的 `RANGE_BOOSTER` 过滤规则，堆叠上限为 64。
