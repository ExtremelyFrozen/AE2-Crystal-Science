# 末影石英母岩与晶簇

材料 ID：`ender_quartz`。包含五个成长阶段，其中成熟晶簇是否掉落及是否继续生长待玩法确认；目录位置不变。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `ender_quartz_mother_rock.png` | `block/ender_quartz_mother_rock.png` | 母岩。 |
| `ender_quartz_small_crystal_bud.png` | `block/ender_quartz_small_crystal_bud.png` | 小型晶芽。 |
| `ender_quartz_medium_crystal_bud.png` | `block/ender_quartz_medium_crystal_bud.png` | 中型晶芽。 |
| `ender_quartz_large_crystal_bud.png` | `block/ender_quartz_large_crystal_bud.png` | 大型晶芽。 |
| `ender_quartz_crystal_cluster.png` | `block/ender_quartz_crystal_cluster.png` | 晶簇。 |
| `ender_quartz_mature_crystal_cluster.png` | `block/ender_quartz_mature_crystal_cluster.png` | 成熟晶簇。 |

## 实施结论

- 完整注册六个方块和方块物品：`ender_quartz_mother_rock`、`ender_quartz_small_crystal_bud`、`ender_quartz_medium_crystal_bud`、`ender_quartz_large_crystal_bud`、`ender_quartz_crystal_cluster`、`ender_quartz_mature_crystal_cluster`。第五张纹理对应独立的成熟晶簇方块。
- 母岩随机 tick 依次放置和替换五个独立阶段方块；每个阶段都有自己的附着面模型、`FACING`、`WATERLOGGED`、战利品和方块物品，不使用 `AGE`。
- 成熟晶簇的普通挖掘产物应接入既有 `AECSItems.PURE_ENDER_QUARTZ`（`purified_ender_quartz`）。全部六个方块补齐模型、标签、语言、创造栏和战利品；当前不做任何配方。
