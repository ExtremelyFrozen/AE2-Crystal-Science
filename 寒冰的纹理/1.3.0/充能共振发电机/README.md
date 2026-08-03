# 充能共振发电机

该目录暂未确认是替换现有 `crystal_vibration_chamber`，还是新增机器；因此只确定纹理文件名，不冻结注册 ID。

| 当前文件名 | 预期资源 ID | 用途 |
| --- | --- | --- |
| `charged_resonating_generator_sprite_sheet.png` | `gui/charged_resonating_generator_sprite_sheet.png` | 32x64 图集，需在模型或 GUI 定义确认后再切分。 |

## 项目内推断与实施

- 现有最接近的发电机器是 `crystal_vibration_chamber`：它已有 `CrystalVibrationChamberBlock`、`CrystalVibrationChamberBlockEntity`、`CrystalVibrationChamberMenu`、`CrystalVibrationChamberGUI` 以及 `assets/ae2/screens/crystal_vibration_chamber_menu.json`。
- 不能直接替换该机器：现有 Screen 需要 256x256 的 `crystal_vibration_chamber_menu.png`，本目录只提供 32x64 图集，缺少菜单背景和全部六面方块材质。将本图改名并不构成可替换资源。
- 若确认“充能共振发电机”只是晶能谐振器的改名或局部动画升级，只新增该图集在 GUI 样式 JSON 中的 `images` 条目，并按实际帧区域调整渲染；保持现有方块、BlockEntity、菜单 ID、配方和存档键不变。
- 若确认是新机器，才新增 `charged_resonating_generator` 的 Block ID、方块、BlockEntity、Menu、Screen 和语言文本；方块状态模型使用 `AECSBlockStateProvider.genSixFaceLike`，因此还必须补齐 `on/off` 的六面材质。
