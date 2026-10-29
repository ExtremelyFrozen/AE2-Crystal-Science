---
navigation:
  parent: crystal_growth_chamber.md
  title: 谐振晶体催生工厂
  icon: ae2cs:resonating_growth_factory
  position: 10
item_ids:
  - ae2cs:resonating_growth_factory
---

# 谐振晶体催生工厂

<Row gap="16">
  <BlockImage id="ae2cs:resonating_growth_factory" scale="8" />
</Row>

**谐振晶体催生工厂**是[晶体催生仓](crystal_growth_chamber.md)的谐振版本。

## 当前功能

- 提供 **48 个库存槽位**，布局为八列、六行。
- 与普通催生仓的 54 个种子槽不同，当前这些槽位也可以存放普通物品。
- 通电外观跟随 ME 网络的供电状态。
- 左侧工具栏提供本页指南与[面配置](side_config.md)入口，可以设置各面的存取权限及自动传输。

**当前版本尚未接入水晶种子的催生流程。** 已有功能为库存、卡槽和面配置；安装陨石超频卡暂时不会提高种子催生速率。需要实际催生种子时，请使用[普通晶体催生仓](crystal_growth_chamber.md)。

---

## 升级卡与存储容量

- 四个专用槽支持最多 **4 张 <ItemLink id="ae2cs:overload_card" />**。
- 两个专用槽支持最多 **2 张 <ItemLink id="ae2cs:resonating_stacking_card" />**。
- 每格限放一张卡，不支持普通加速卡。
- 悬停升级栏可查看“**可用升级**”列表与整机安装上限。

对原本最多堆叠 64 个的物品，安装 0、1、2 张堆叠卡时，每格容量分别为 **64、256、1024 个**。原本堆叠上限较小的物品按比例扩容，不可堆叠物品仍限一个。若取下堆叠卡会导致任意库存超出新上限，机器会阻止取卡；请先取出多余物品。

## 界面与自动化

左侧“**?**”按钮打开本机指南，相邻按钮打开[面配置](side_config.md)。可在面配置中设置各面的输入、输出权限及自动输入、自动输出。
