# 脉冲离心机

该目录尚未确认对应既有机器还是新增玩法；以下仅确认 GUI 资源命名。

| 当前文件名 | 预期资源 ID | 用途 |
| --- | --- | --- |
| `pulse_centrifuge_menu.png` | `gui/pulse_centrifuge_menu.png` | 256x256 菜单背景。 |
| `pulse_centrifuge_rotor.png` | `gui/pulse_centrifuge_rotor.png` | 16x32 转子或处理槽元素。 |
| `pulse_centrifuge_rotor_animation.png` | `gui/pulse_centrifuge_rotor_animation.png` | 16x192 转子动画条。 |
| `pulse_centrifuge_slot_frame.png` | `gui/pulse_centrifuge_slot_frame.png` | 16x16 槽位边框。 |
| `pulse_centrifuge_energy_indicator.png` | `gui/pulse_centrifuge_energy_indicator.png` | 16x16 能量或状态指示元素。 |

## 项目内推断与实施

- 项目内没有 `pulse_centrifuge` 的 Block、BlockEntity、Menu、Screen、RecipeType 或 JSON 样式，因此这些纹理不能替换现有机器，也不能仅放进资源目录就生效。
- 玩法确认新增后，创建 `PulseCentrifugeBlock`、`PulseCentrifugeBlockEntity`、`PulseCentrifugeMenu`、`PulseCentrifugeGUI`，并分别接入 `AECSBlockIds`、`AECSBlocks`、`AECSBlockEntities`、`AECSMenus`、`AECSScreens` 与 `AECSCapabilities`。
- 创建 `assets/ae2/screens/pulse_centrifuge_menu.json`，背景使用 256x256 菜单图；`rotor_animation` 是 16x192 的 12 帧动画，必须由受控 UV 帧或 `.mcmeta` 驱动，不能凭文件名自动播放。
- 机器方块若沿用现有可旋转 ACTIVE 模板，仍须补齐 `textures/block/pulse_centrifuge/on|off/` 下十二张六面材质；GUI 图不足以生成方块模型、战利品、标签和配方。
- Screen JSON 的目标路径是 `src/main/resources/assets/ae2/screens/pulse_centrifuge_menu.json`，不是 `assets/ae2cs/screens/`。这是 AE2 `StyleManager` 的命名空间约束；纹理目标才是 `assets/ae2cs/textures/gui/`。
