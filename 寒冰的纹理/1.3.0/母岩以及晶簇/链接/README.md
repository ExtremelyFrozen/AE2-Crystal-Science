# 链接母岩与晶簇

材料 ID：`link`。包含五个成长阶段，其中成熟晶簇是否掉落及是否继续生长待玩法确认；目录位置不变。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `link_mother_rock.png` | `block/link_mother_rock.png` | 母岩。 |
| `link_small_crystal_bud.png` | `block/link_small_crystal_bud.png` | 小型晶芽。 |
| `link_medium_crystal_bud.png` | `block/link_medium_crystal_bud.png` | 中型晶芽。 |
| `link_large_crystal_bud.png` | `block/link_large_crystal_bud.png` | 大型晶芽。 |
| `link_crystal_cluster.png` | `block/link_crystal_cluster.png` | 晶簇。 |
| `link_mature_crystal_cluster.png` | `block/link_mature_crystal_cluster.png` | 成熟晶簇。 |

## 实施结论

- 完整注册六个方块和方块物品：`link_mother_rock`、`link_small_crystal_bud`、`link_medium_crystal_bud`、`link_large_crystal_bud`、`link_crystal_cluster`、`link_mature_crystal_cluster`。
- 母岩随机 tick 依次放置和替换五个独立阶段方块；每个阶段都有自己的附着面模型、`FACING`、`WATERLOGGED`、战利品和方块物品，不使用 `AGE`。
- 成熟晶簇的普通挖掘产物应接入既有 `AECSItems.PURE_LINK_CRYSTAL`（`purified_link_crystal`）。六个方块完整生成模型、标签、语言、创造栏和战利品；该方块系列不等同于末影连接器或谐振绑定器，当前不做任何配方。
