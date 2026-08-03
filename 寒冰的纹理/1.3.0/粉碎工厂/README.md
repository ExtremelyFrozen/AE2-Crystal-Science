# 粉碎工厂

对应既有机器 `crystal_pulverizer`，仅替换或扩展 GUI 资源，不注册第二台同功能机器。

| 当前文件名 | 预期资源 ID | 用途 |
| --- | --- | --- |
| `crystal_pulverizer_menu.png` | `gui/crystal_pulverizer_menu.png` | 256x256 菜单背景。 |
| `crystal_pulverizer_crushing_chamber.png` | `gui/crystal_pulverizer_crushing_chamber.png` | 16x16 粉碎腔元素。 |
| `crystal_pulverizer_crushing_chamber_animation.png` | `gui/crystal_pulverizer_crushing_chamber_animation.png` | 16x32 粉碎腔动画条。 |
| `crystal_pulverizer_progress_indicator.png` | `gui/crystal_pulverizer_progress_indicator.png` | 16x16 进度指示元素。 |
| `crystal_pulverizer_progress_indicator_animation.png` | `gui/crystal_pulverizer_progress_indicator_animation.png` | 16x32 进度动画条。 |

## 项目内推断与实施

- 这是既有 `crystal_pulverizer` 的界面升级。替换目标为 `src/main/resources/assets/ae2cs/textures/gui/crystal_pulverizer_menu.png`，并审阅 `assets/ae2/screens/crystal_pulverizer_menu.json`。
- 当前 JSON 从完整菜单图切出 `energyRateBar` 和 `workingProgressBar`；两张独立指示器与粉碎腔图片不会被自动加载。若新图不再保留原切图区域，应在该 JSON 新增或改用独立 `images` 条目，再同步更新 `CrystalPulverizerGUI` 注册的 widget 名称与坐标。
- 已有服务端链路为 `CrystalPulverizerBlock`、`CrystalPulverizerBlockEntity`、`CrystalPulverizerMenu`、`AECSRecipeTypes.CRYSTAL_PULVERIZER`、JEI/EMI 分类和 `AECSPulverizerRecipeProvider`。本次不得新建第二套注册或配方类型。
- 方块仍由 `AECSBlockStateProvider.genSixFaceLike` 为 `ACTIVE` 状态生成模型；本目录 GUI 图不能覆盖 `textures/block/crystal_pulverizer/on|off/` 的六面材质。
- 样式 JSON 必须继续写在 `src/main/resources/assets/ae2/screens/`，因为 `StyleManager.loadStyleDoc` 按 AE2 命名空间解析该路径；只在 `assets/ae2cs/textures/gui/` 放置纹理。
