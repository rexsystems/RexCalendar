---
title: GUI
description: Chest size, filler, day slots, decoration, and nav buttons.
sidebar:
  order: 3
---

`gui.rows` is the number of chest rows, from 1 to 6. The inventory size is that number times 9, so 6 rows is 54 slots. A value outside 1-6 is clamped, and the clamp is logged.

`gui.title` is the chest title. `{date}` is today in `settings.timezone`. `{day}` is today's day of the month. `{page}` is the page number, starting at 1.

## Filler and day icons

`gui.filler.material` fills every slot first. A blank `gui.filler.name` hides the tooltip.

`gui.day` is the default door:

| Key | What it does |
|---|---|
| `name` | Door name. `{day}` and `{status}` are filled. |
| `lore` | One list entry per line. `{status}` is the line from `gui.status`. |
| `available-material` | Door that can be claimed |
| `claimed-material` | Door this player already claimed for that date |
| `missed-material` | Earlier door while `missed-days` is `today-only` |
| `locked-material` | Door that is still in the future, or a day number this month does not have |

`gui.status` has `open`, `claimed`, `missed`, and `locked`.

## Slots

`days.<n>.slot` is the door's index. `0` is the top-left slot.

`-1` hides that door.

Slots continue onto later pages. With 54 slots, slot 54 is the first slot of page 2. Slots above 539 are hidden, and a warning is logged.

`days.<n>.material`, `name`, and `lore` replace the available icon for that door. Claimed, missed, and locked icons still come from `gui.day`.

`gui.decoration` is a list. Each entry has `slot`, `material`, `name`, `lore`, and `custom-model-data`. `custom-model-data` of `0` leaves the model unset. Slot `-1` hides that decoration. Decoration uses the same page rule as doors.

## Nav

`gui.nav.previous`, `gui.nav.next`, and `gui.nav.close` each have `slot`, `material`, `name`, and `lore`.

The slot is inside a single chest, not a later page. `-1` hides the button. A slot past the last slot of the chest is hidden, and a warning is logged.

Paint order is filler, decoration, doors, then nav. A nav button covers a door that shares its slot, and the click runs the button.

Previous and next do nothing on the first and last page. Close closes the chest.

A click in the calendar is cancelled. A click in the player's own inventory is left alone, except a shift-click or a double-click collect that would pull the icons out. Those two are cancelled so the icons stay in the chest.
