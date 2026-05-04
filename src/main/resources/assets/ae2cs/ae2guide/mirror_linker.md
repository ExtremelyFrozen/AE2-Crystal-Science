---
navigation:
  parent: index.md
  title: Mirror Binder
  icon: ae2cs:mirror_linker
  position: 208
item_ids:
  - ae2cs:mirror_linker
---

# Mirror Binder

<Row gap="16">
  <ItemImage id="ae2cs:mirror_linker" scale="4" />
</Row>

The **Mirror Binder** stores a mirror target in item form and applies it to **<ItemLink id="ae2cs:mirror_pattern_provider" />** devices.

---

## Recording a Target

- Right-click a non-mirror Pattern Provider to record it as the mirror target
- **Shift + right-click** in the air to clear the stored target

Mirror Pattern Providers themselves cannot be recorded as mirror targets.

---

## Applying to a Mirror Pattern Provider

- Right-click a Mirror Pattern Provider to write the stored target into it

This also refreshes its internal mirror cache immediately.

---

## Batch Apply

- **Ctrl + right-click** a Mirror Pattern Provider to apply the stored target to all connected adjacent Mirror Pattern Providers

This follows the full adjacency chain rather than only the six directly neighboring positions.
