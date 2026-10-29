---
navigation:
  parent: crystal_aggregator.md
  title: 谐振晶能聚合工厂
  icon: ae2cs:resonating_aggregator_factory
  position: 10
item_ids:
  - ae2cs:resonating_aggregator_factory
---

# 谐振晶能聚合工厂

<Row gap="16">
  <BlockImage id="ae2cs:resonating_aggregator_factory" scale="8" />
</Row>

**谐振晶能聚合工厂**是[晶能聚合器](crystal_aggregator.md)的批量加工版本，使用同一套配方制作高级组件、水晶种子等产物。

## 库存与加工

- 提供 **9 个输入槽**（3×3）与 **3 个输出槽**。
- 输入、输出流体槽各容纳 **16,000 mB**，可通过流体槽按钮操作流体容器。
- 每份实际完成的配方都完整消耗材料、流体与能量。

陨石超频卡决定一批加工的最大份数：

| 陨石卡数量 | 最大并行份数 | 目标周期 |
| --- | --- | --- |
| 0 | 8 | 按配方能量推进 |
| 1 | 32 | 64 tick |
| 2 | 64 | 16 tick |
| 3 | 256 | 4 tick |
| 4 | 1024 | 1 tick |

实际加工份数还受原料、流体、供能与输出空间限制。缺少资源时暂停加工，已支付的能量保留在当前批次中。

---

## 升级卡与存储容量

- 四个专用槽支持最多 **4 张 <ItemLink id="ae2cs:overload_card" />**。
- 两个专用槽支持最多 **2 张 <ItemLink id="ae2cs:resonating_stacking_card" />**。
- 每格限放一张卡，不支持普通加速卡。
- 悬停升级栏可查看“**可用升级**”列表与整机安装上限。

对原本最多堆叠 64 个的物品，安装 0、1、2 张堆叠卡时，每格容量分别为 **64、256、1024 个**。原本堆叠上限较小的物品按比例扩容，不可堆叠物品仍限一个。若取下堆叠卡会导致任意库存超出新上限，机器会阻止取卡；请先取出多余物品。

## 界面与自动化

左侧“**?**”按钮打开本机指南，相邻按钮打开[面配置](side_config.md)。可在面配置中设置各面的输入、输出权限及自动输入、自动输出。
