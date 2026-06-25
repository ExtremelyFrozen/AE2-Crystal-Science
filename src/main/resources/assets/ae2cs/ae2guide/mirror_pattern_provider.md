---
navigation:
  parent: index.md
  title: Mirror Pattern Provider
  icon: ae2cs:mirror_pattern_provider
  position: 155
item_ids:
  - ae2cs:mirror_pattern_provider
  - ae2cs:mirror_pattern_provider_part
---

# Mirror Pattern Provider

<Row gap="16">
  <BlockImage id="ae2cs:mirror_pattern_provider" scale="2" />
  <ItemImage id="ae2cs:mirror_pattern_provider_part" scale="2" />
</Row>

The **Mirror Pattern Provider** is a dedicated provider for reusing an existing provider's encoded patterns.
Instead of storing its own local pattern inventory,
it mirrors the currently available pattern list from another provider and executes crafts locally.

---

## Binding a Target

The target must be bound while the block or part item is still in item form:

- Hold the Mirror Pattern Provider item
- Right-click another pattern provider to bind it as the mirror target
- **Shift + right-click** in the air to clear the saved target

After placement, the provider will read the target's available patterns.

---

## Restrictions

The Mirror Pattern Provider can bind to ordinary pattern providers,
including both block and part forms.

However:

- It **cannot mirror another Mirror Pattern Provider**

This prevents mirror chains and cyclic references.

---

## UI Behavior

The Mirror Pattern Provider does not have its own independent local setup screen.

- If it has a valid target, opening it redirects you to the target provider's UI
- If it has no target, or the target is no longer valid, it cannot open a UI and will instead prompt the player to bind one first

---

## How It Works

The provider mirrors pattern information only.

- The pattern list comes from the bound target
- Blocking mode, locking behavior, and priority are inherited from the bound target
- Direction can still be configured locally on the Mirror Pattern Provider itself
- Actual crafting requests are still handled by the Mirror Pattern Provider itself

This makes it useful for reusing one pattern setup in multiple places without duplicating the encoded patterns themselves.

---

## Limits of Mirroring

The Mirror Pattern Provider only copies the target's available patterns.

- It does **not** copy the target's more complex provider-specific logic
- For example, it does not reproduce advanced dispatch rules from providers such as the Resonating Pattern Provider or other specialized providers
- The Mirror Pattern Provider still dispatches items using ordinary Pattern Provider behavior on its own side

In short, it mirrors the pattern list, but not the target provider's special execution logic.
