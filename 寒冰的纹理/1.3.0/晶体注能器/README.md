# 晶体注能器

该目录尚未确认对应既有机器还是新增玩法；以下仅确认 GUI 资源命名。

| 当前文件名 | 预期资源 ID | 用途 |
| --- | --- | --- |
| `crystal_infuser_menu.png` | `gui/crystal_infuser_menu.png` | 256x256 菜单背景。 |
| `crystal_infuser_vertical_meter.png` | `gui/crystal_infuser_vertical_meter.png` | 16x32 竖向量表。 |
| `crystal_infuser_energy_slot.png` | `gui/crystal_infuser_energy_slot.png` | 16x16 能量槽元素。 |
| `crystal_infuser_operation_slot.png` | `gui/crystal_infuser_operation_slot.png` | 16x16 操作槽元素。 |
| `crystal_infuser_slot_frame.png` | `gui/crystal_infuser_slot_frame.png` | 16x32 槽位边框。 |

## 项目内推断与实施

- 项目不存在 `crystal_infuser` 的 Block、BlockEntity、Menu、Screen、配方类型或 Screen JSON；它不能替换为粉碎机、聚合器、熵变反应室或晶能谐振器中的任意一个。
- 在玩法确认“新增机器”后，按现有机器链路新增：`AECSBlockIds`、`AECSBlocks`、`AECSBlockEntities`、`AECSMenus`、`AECSScreens`、`AECSCapabilities`，并建立 `CrystalInfuserBlock`、`CrystalInfuserBlockEntity`、`CrystalInfuserMenu`、`CrystalInfuserGUI` 和 `assets/ae2/screens/crystal_infuser_menu.json`。
- 方块实体应以 `AENetworkedSelfPoweredBlockEntity` 和粉碎机的库存、升级、侧面配置模式为模板；配方逻辑必须独立注册 RecipeType 与 Serializer，不能放在 Screen 或 Menu 中。
- 5 张图只能完成 GUI。若采用 `ACTIVE` 机器模型，还需提供 `textures/block/crystal_infuser/on|off/` 下各六张面材质；缺失前不应创建数据生成调用。
- Screen JSON 的目标路径是 `src/main/resources/assets/ae2/screens/crystal_infuser_menu.json`，不是 `assets/ae2cs/screens/`。这是 AE2 `StyleManager` 的命名空间约束；纹理目标才是 `assets/ae2cs/textures/gui/`。
