---
navigation:
  parent: index.md
  title: Resonating Memory Card
  icon: ae2cs:resonating_memory_card
  position: 206
item_ids:
  - ae2cs:resonating_memory_card
---

# Resonating Memory Card

<Row gap="16">
  <ItemImage id="ae2cs:resonating_memory_card" scale="4" />
</Row>

The **Resonating Memory Card** extends the normal AE memory card with multiple independent storage slots.
It is designed for players who frequently duplicate or automatically reapply different machine configurations.

---

## Multi-Slot Storage

The Resonating Memory Card has **9 separate slots**.

- Each slot stores one independent configuration
- Slots do not overwrite each other
- The tooltip shows both the current slot number and the stored configuration name

---

## Switching and Clearing Slots

The active slot can be changed quickly:

- **Shift + mouse wheel** changes the selected slot
- Slot switching also shows the selected slot's stored configuration name
- **Shift + right-click** clears the currently selected slot

---

## Automatic Application

If a player carries a Resonating Memory Card with valid data in the currently selected slot,
newly placed AE blocks and AE parts will automatically try to import that configuration.

When this happens,
the player is notified which slot was used and which saved configuration was applied.

This is especially useful when placing many machines with the same setup.
