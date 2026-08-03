# 陨石母岩与晶簇

材料 ID：`meteor`。包含五个成长阶段，其中成熟晶簇是否掉落及是否继续生长待玩法确认；目录位置不变。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `meteor_mother_rock.png` | `block/meteor_mother_rock.png` | 母岩。 |
| `meteor_small_crystal_bud.png` | `block/meteor_small_crystal_bud.png` | 小型晶芽。 |
| `meteor_medium_crystal_bud.png` | `block/meteor_medium_crystal_bud.png` | 中型晶芽。 |
| `meteor_large_crystal_bud.png` | `block/meteor_large_crystal_bud.png` | 大型晶芽。 |
| `meteor_crystal_cluster.png` | `block/meteor_crystal_cluster.png` | 晶簇。 |
| `meteor_mature_crystal_cluster.png` | `block/meteor_mature_crystal_cluster.png` | 成熟晶簇。 |

## 实施结论

- 完整注册六个方块和方块物品：`meteor_mother_rock`、`meteor_small_crystal_bud`、`meteor_medium_crystal_bud`、`meteor_large_crystal_bud`、`meteor_crystal_cluster`、`meteor_mature_crystal_cluster`。
- 母岩随机 tick 依次放置和替换五个独立阶段方块；每个阶段都有自己的附着面模型、`FACING`、`WATERLOGGED`、战利品和方块物品，不使用 `AGE`。
- 成熟晶簇的普通挖掘产物应接入既有 `AECSItems.PURE_METEOR_CRYSTAL`（`purified_meteor_crystal`）。六个方块完整生成模型、标签、语言、创造栏和战利品；本目录本身不代表陨石结构生成，当前不做任何配方。
