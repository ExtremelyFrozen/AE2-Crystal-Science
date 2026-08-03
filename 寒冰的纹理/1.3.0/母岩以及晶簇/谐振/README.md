# 谐振母岩与晶簇

材料 ID：`resonating`。包含五个成长阶段，其中成熟晶簇是否掉落及是否继续生长待玩法确认；目录位置不变。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `resonating_mother_rock.png` | `block/resonating_mother_rock.png` | 母岩。 |
| `resonating_small_crystal_bud.png` | `block/resonating_small_crystal_bud.png` | 小型晶芽。 |
| `resonating_medium_crystal_bud.png` | `block/resonating_medium_crystal_bud.png` | 中型晶芽。 |
| `resonating_large_crystal_bud.png` | `block/resonating_large_crystal_bud.png` | 大型晶芽。 |
| `resonating_crystal_cluster.png` | `block/resonating_crystal_cluster.png` | 晶簇。 |
| `resonating_mature_crystal_cluster.png` | `block/resonating_mature_crystal_cluster.png` | 成熟晶簇。 |

## 实施结论

- 完整注册六个方块和方块物品：`resonating_mother_rock`、`resonating_small_crystal_bud`、`resonating_medium_crystal_bud`、`resonating_large_crystal_bud`、`resonating_crystal_cluster`、`resonating_mature_crystal_cluster`。
- 母岩随机 tick 依次放置和替换五个独立阶段方块；每个阶段都有自己的附着面模型、`FACING`、`WATERLOGGED`、战利品和方块物品，不使用 `AGE`。
- 成熟晶簇的普通挖掘产物应接入既有 `AECSItems.PURE_RESONATING_CRYSTAL`（`purified_resonating_crystal`）。六个方块完整生成模型、标签、语言、创造栏和战利品；不接入现有谐振机器或样板供应器逻辑，当前不做任何配方。
