---
navigation:
  parent: index.md
  title: 镜像绑定器
  icon: ae2cs:mirror_linker
  position: 208
item_ids:
  - ae2cs:mirror_linker
---

# 镜像绑定器

<Row gap="16">
  <ItemImage id="ae2cs:mirror_linker" scale="4" />
</Row>

**镜像绑定器** 用于在物品中保存一个镜像目标，并将其写入 **<ItemLink id="ae2cs:mirror_pattern_provider" />**。

---

## 记录镜像目标

- 对非镜像样板供应器 **右键**，记录其为镜像目标
- 对空气 **Shift + 右键**，清空当前保存的镜像目标

镜像样板供应器自身不能作为镜像目标被记录。

---

## 写入镜像样板供应器

- 对镜像样板供应器 **右键**，将当前保存的目标写入该设备

写入时会立即刷新它的镜像缓存。

---

## 批量写入

- 对镜像样板供应器 **Ctrl + 右键**，会把当前保存的目标写入所有相邻连通的镜像样板供应器

这里的“相邻”不是只检查首层 6 个邻接位置，
而是会沿着整片相互邻接的镜像样板供应器持续传播。
