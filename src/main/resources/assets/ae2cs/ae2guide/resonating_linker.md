---
navigation:
  parent: index.md
  title: Resonating Binder
  icon: ae2cs:resonating_linker
  position: 207
item_ids:
  - ae2cs:resonating_linker
---

# Resonating Binder

<Row gap="16">
  <ItemImage id="ae2cs:resonating_linker" scale="4" />
</Row>

The **Resonating Binder** stores one complete set of resonating routing targets in item form.
It uses the same targeting workflow as Resonating Pattern routing,
but the stored logic can be written directly into a **<ItemLink id="ae2cs:resonating_pattern_provider" />**.

---

## Storing Routing Logic

- **Shift + mouse wheel** switches the selected input slot
- Right-click a block face to bind the current input slot to that position and side
- Right-click the same face again to clear that slot

The binder stores a full default routing set using the same 81-slot structure as the Resonating Pattern Provider.

---

## Writing to a Provider

- **Shift + right-click** a Resonating Pattern Provider with the binder

This writes the stored routing logic into the provider's default dispatch configuration.

---

## Clearing the Binder

The stored routing data can be cleared by crafting the binder by itself once in a crafting grid.

This keeps the item while resetting the stored routing logic.
