---
navigation:
  parent: circuit_etcher.md
  title: 谐振电路蚀刻工厂
  icon: ae2cs:resonating_circuit_etcher_factory
  position: 10
item_ids:
  - ae2cs:resonating_circuit_etcher_factory
---

# 谐振电路蚀刻工厂

<Row gap="16">
  <BlockImage id="ae2cs:resonating_circuit_etcher_factory" scale="8" />
</Row>

**谐振电路蚀刻工厂**是[电路蚀刻器](circuit_etcher.md)的扩展库存版本，沿用相同配方，把块状材料直接加工为处理器成品。

## 库存与加工

- 提供 **9 个输入槽**（3×3）与 **3 个输出槽**。
- 配方材料可以放在任意匹配的输入槽中，不必只占用前三格。
- 输出槽只接收机器产物，不接受手动放入物品。
- 安装 0 至 4 张陨石超频卡时，每批最多处理 **8、16、64、128、256** 份配方。每份完整消耗配方材料与能量。
- 点击加工进度箭头可查看电路蚀刻配方。

谐振堆叠卡同时扩大输入槽与输出槽的存储容量。

---

## 升级卡与存储容量

- 六个槽可混放两种卡，超频卡最多 **4 张 <ItemLink id="ae2cs:overload_card" />**。
- 堆叠卡最多 **2 张 <ItemLink id="ae2cs:resonating_stacking_card" />**。
- 每格限放一张卡，不支持普通加速卡。
- 悬停升级栏可查看“**可用升级**”列表与整机安装上限。

对原本最多堆叠 64 个的物品，安装 0、1、2 张堆叠卡时，每格容量分别为 **64、256、1024 个**。原本堆叠上限较小的物品按比例扩容，不可堆叠物品仍限一个。若取下堆叠卡会导致任意库存超出新上限，机器会阻止取卡；请先取出多余物品。

## 界面与自动化

左侧“**?**”按钮打开本机指南，相邻按钮打开[面配置](side_config.md)。可在面配置中设置各面的输入、输出权限及自动输入、自动输出。
