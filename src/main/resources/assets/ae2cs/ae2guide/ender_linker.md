---
navigation:
  parent: index.md
  title: Ender Linker
  icon: ae2cs:ender_linker
  position: 205
item_ids:
  - ae2cs:ender_linker
---

# Ender Linker

<Row gap="16">
  <ItemImage id="ae2cs:ender_linker" scale="4" />
</Row>

The **Ender Linker** is used to manually manage wireless links for the **<ItemLink id="ae2cs:ender_emitter" />**.
It is useful when automatic linking is disabled or when a precise wireless layout is needed.

---

## Binding an Emitter

Before use, the tool must be bound to an Ender Emitter:

- Hold the tool and **Shift + right-click** an Ender Emitter

Once bound, the tool remembers that emitter and uses it for later link operations.

---

## Manual Linking

After binding:

- Right-click any valid ME network block or part to add it to the bound emitter
- Right-click an already linked target again to remove it from that emitter

This allows the player to decide exactly which devices should belong to a specific wireless emitter.

---

## Visualization

While holding the Ender Linker,
nearby Ender Emitters will temporarily display:

- Their automatic link range
- Their maximum link range
- Their pending and established link beams

This temporary visualization ignores the emitter's own range display setting,
making the tool useful for both setup and troubleshooting.
