---
navigation:
  parent: index.md
  title: 高效的水晶发电
  icon: ae2cs:charged_certus_quartz_ore
  position: 40
item_ids:
  - ae2cs:crystal_vibration_chamber

---

# 更高效的水晶发电

## 水晶蕴含着能量

AECS为高纯水晶赋予了能量密度与燃烧时间双重属性，显而易见的是，它们代表着一个水晶用于发电时的效率和持久情况。
以下是高纯水晶们的能量密度与燃烧时间表（也包含了所有的联动水晶），你也可以在配置文件中修改水晶的能量密度：
| 水晶图标 | 名称 | 能量密度（AE/tick） | 燃烧时间（tick） | 能量总量（AE） |
|:-------:|:----:|:--------:|:--------:|:--------:|
| <Column alignItems="center"><ItemImage id="ae2cs:purified_certus_quartz_crystal" /></Column> | <Column alignItems="center">高纯赛特斯石英水晶</Column> | <Column alignItems="center">500</Column> | <Column alignItems="center">600</Column> | <Column alignItems="center">300,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_fluix_crystal" /></Column> | <Column alignItems="center">高纯福鲁伊克斯水晶</Column> | <Column alignItems="center">1500</Column> | <Column alignItems="center">1200</Column> | <Column alignItems="center">1,800,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_nether_quartz_crystal" /></Column> | <Column alignItems="center">高纯下界石英水晶</Column> | <Column alignItems="center">800</Column> | <Column alignItems="center">900</Column> | <Column alignItems="center">720,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_ender_quartz" /></Column> | <Column alignItems="center">高纯末影石英</Column> | <Column alignItems="center">900</Column> | <Column alignItems="center">800</Column> | <Column alignItems="center">720,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_meteor_crystal" /></Column> | <Column alignItems="center">高纯陨石水晶</Column> | <Column alignItems="center">1200</Column> | <Column alignItems="center">1500</Column> | <Column alignItems="center">1,800,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_resonating_crystal" /></Column> | <Column alignItems="center">高纯谐振水晶</Column> | <Column alignItems="center">2500</Column> | <Column alignItems="center">2400</Column> | <Column alignItems="center">6,000,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_entro_crystal" /></Column> | <Column alignItems="center">高纯恩特罗水晶</Column> | <Column alignItems="center">2000</Column> | <Column alignItems="center">2400</Column> | <Column alignItems="center">4,800,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_redstone_crystal" /></Column> | <Column alignItems="center">高纯红石水晶</Column> | <Column alignItems="center">1800</Column> | <Column alignItems="center">2000</Column> | <Column alignItems="center">3,600,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_quantum_crystal" /></Column> | <Column alignItems="center">高纯量子水晶</Column> | <Column alignItems="center">2320</Column> | <Column alignItems="center">3000</Column> | <Column alignItems="center">6,960,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_rose_quartz" /></Column> | <Column alignItems="center">高纯玫瑰石英</Column> | <Column alignItems="center">2000</Column> | <Column alignItems="center">1800</Column> | <Column alignItems="center">3,600,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_irradiated_crystal" /></Column> | <Column alignItems="center">高纯辐射水晶</Column> | <Column alignItems="center">2900</Column> | <Column alignItems="center">2400</Column> | <Column alignItems="center">6,960,000</Column> |
| <Column alignItems="center"><ItemImage id="ae2cs:purified_ember_crystal" /></Column> | <Column alignItems="center">高纯余烬水晶</Column> | <Column alignItems="center">2000</Column> | <Column alignItems="center">3600</Column> | <Column alignItems="center">7,200,000</Column> |

## 晶能谐振仓

<Row>
  <BlockImage id="crystal_vibration_chamber" scale="4" />
</Row>

正如同<ItemLink id="ae2:vibration_chamber" />通过燃烧煤炭获得能量，<ItemLink id="crystal_vibration_chamber" />通过燃烧水晶获得能量。
其本身支持最多3张加速卡，每张加速卡可以增加50%的燃烧速度，以及100%燃烧消耗。
其能量可以传入所连接的ME网络，也可以使用其他模组的能量线缆导出为FE使用。

在默认配置下，以谐振水晶进行供能，你可以从每台<ItemLink id="crystal_vibration_chamber" />上获得12500FE/tick的发电量，如果你觉得过高或者过低，可以在配置文件中调整水晶的能量密度系数。





