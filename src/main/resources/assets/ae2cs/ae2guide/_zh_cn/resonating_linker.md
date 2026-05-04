---
navigation:
  parent: index.md
  title: 谐振绑定器
  icon: ae2cs:resonating_linker
  position: 207
item_ids:
  - ae2cs:resonating_linker
---

# 谐振绑定器

<Row gap="16">
  <ItemImage id="ae2cs:resonating_linker" scale="4" />
</Row>

**谐振绑定器** 用于以物品形式保存一整套谐振发配逻辑。
它的标记操作与谐振样板供应器默认发配模式相同，
但保存好的逻辑可以直接写入 **<ItemLink id="ae2cs:resonating_pattern_provider" />**。

---

## 存储发配逻辑

- **Shift + 滚轮** 切换当前输入位
- 对方块面 **右键** 为当前输入位绑定目标位置与目标面
- 对同一个目标面再次右键可清空该输入位

它内部使用与谐振样板供应器相同的 **81 槽默认发配结构**。

---

## 写入谐振样板供应器

- 手持谐振绑定器，对谐振样板供应器 **Shift + 右键**

这样会把当前保存的整套发配逻辑写入该供应器的默认发配配置中。

---

## 清空绑定器

将谐振绑定器单独放入工作台合成一次，
即可保留物品本体并清空其中保存的发配逻辑。
