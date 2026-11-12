---
title: Rewards
description: Chance, commands, and items on a calendar day.
sidebar:
  order: 4
---

`days.<n>.rewards` is a list. `n` is the day of the month, from 1 to 31. A key that is not a number in that range is ignored.

Each entry rolls on its own.

| Key | What it does |
|---|---|
| `chance` | `0` to `100`. Missing means `100`. Values outside that range are clamped. |
| `commands` | Console commands. A leading `/` is stripped. |
| `items` | Item stacks to give |

Command tokens are `{player}`, `{uuid}`, `{day}`, and `{date}`. They are plain text. Commands are not parsed as MiniMessage.

An item has:

| Key | What it does |
|---|---|
| `material` | Bukkit material name, such as `COOKIE` or `minecraft:cookie` |
| `amount` | Stack count. Below 1 becomes 1. Above 2304 is clamped to 2304. The plugin splits the amount by the material's max stack size. |
| `name` | Display name. MiniMessage is allowed. |
| `lore` | Lore lines |
| `custom-model-data` | Custom model data. `0` leaves the model unset. |

An unknown material is logged once and skipped.

## What gets claimed

The plugin locks the player and the date, inserts the claim row, then gives the items and runs the commands.

- Item rewards are checked against the inventory first. If they do not fit, the row is not inserted.
- If the give fails after the insert, the row is deleted and the items that were added are removed.
- A day with an empty `rewards` list sends `messages.no-reward` and does not insert a row.
- If the list has entries and every chance misses, the row is inserted and `messages.no-reward` is sent. The player cannot roll that date again.
- `messages.claim` is sent when at least one item or command was granted.

Command rewards are not measured against free slots. A console `give` that drops leftovers on the ground still counts as a successful command.

On Paper, a command that returns failure deletes the claim and restores the inventory. On Folia, commands run on the global region after the claim is saved, and a failure there does not undo the claim.
