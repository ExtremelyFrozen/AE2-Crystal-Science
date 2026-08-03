# 聚合器

对应既有机器 `crystal_aggregator`，仅替换 GUI 资源，不新增注册。

| 当前文件名 | 预期资源 ID | 用途 |
| --- | --- | --- |
| `crystal_aggregator_menu.png` | `gui/crystal_aggregator_menu.png` | 256x256 菜单背景。 |

## 项目内推断与实施

- 这是既有 `crystal_aggregator` 的界面替换。替换目标为 `src/main/resources/assets/ae2cs/textures/gui/crystal_aggregator_menu.png`，然后核对 `assets/ae2/screens/crystal_aggregator_menu.json` 的背景、三输入槽、一输出槽及两个进度条区域。
- 现有服务端固定为 `CrystalAggregatorBlock`、`CrystalAggregatorBlockEntity`、`CrystalAggregatorMenu` 和 `AECSRecipeTypes.CRYSTAL_AGGREGATOR`；BlockEntity 使用三输入一输出、升级卡和侧面配置，全部保持原样。
- `CrystalAggregatorGUI` 读取 JSON 内 `energyRateBar` 与 `workingProgressBar`。若新背景切图区域不同，只改 JSON 的 `srcRect` 与 widget 坐标，不改菜单库存排列。
- 不新增方块、BlockEntity、Menu、RecipeType 或 JEI/EMI 分类；方块六面材质仍使用 `textures/block/crystal_aggregator/on|off/`，不由本图替代。
- 样式 JSON 必须继续写在 `src/main/resources/assets/ae2/screens/`，因为 `StyleManager.loadStyleDoc` 按 AE2 命名空间解析该路径；只在 `assets/ae2cs/textures/gui/` 放置纹理。
