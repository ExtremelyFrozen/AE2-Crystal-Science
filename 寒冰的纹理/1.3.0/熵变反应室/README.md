# 熵变反应室

对应既有机器 `entropy_variation_reaction_chamber`，仅替换或扩展 GUI 资源，不新增方块、方块实体或菜单注册。

| 当前文件名 | 预期资源 ID | 用途 |
| --- | --- | --- |
| `entropy_variation_reaction_chamber_menu.png` | `gui/entropy_variation_reaction_chamber_menu.png` | 256x256 菜单背景。 |
| `entropy_variation_reaction_chamber_status_cold.png` | `gui/entropy_variation_reaction_chamber_status_cold.png` | 16x16 低温状态图标。 |
| `entropy_variation_reaction_chamber_status_hot.png` | `gui/entropy_variation_reaction_chamber_status_hot.png` | 16x16 高温状态图标。 |

## 项目内推断与实施

- 直接替换目标为 `src/main/resources/assets/ae2cs/textures/gui/entropy_variation_reaction_chamber_menu.png`。客户端由 `EntropyVariationReactionChamberGUI` 和 `assets/ae2/screens/entropy_variation_reaction_chamber_menu.json` 驱动。
- 新背景同为 256x256，但必须核对该 JSON 的 `background.srcRect`、输入输出槽坐标和 `energyRateBar`、`workingProgressBar` 的切图区域；坐标不匹配时只改该 JSON，不改 `EntropyVariationReactionChamberMenu` 的槽位逻辑。
- `status_cold` 与 `status_hot` 目前没有独立资源引用点。只有在 Screen JSON 或 `AECSServerSettingToggleButton` 的样式明确使用它们后才复制到资源目录；不能仅复制文件后假定会显示。
- 不修改 `ENTROPY_VARIATION_REACTION_CHAMBER` 的方块、BlockEntity、菜单、熵变设置、配方类型或 `ACTIVE` 六面方块材质。
- 样式 JSON 必须继续写在 `src/main/resources/assets/ae2/screens/`，因为 AE2 `StyleManager` 以 AE2 命名空间解析 `/screens/entropy_variation_reaction_chamber_menu.json`；纹理本身才放在 `assets/ae2cs/textures/gui/`。
