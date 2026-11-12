---
title: Messages
description: Prefix, claim text, and the colors RexCalendar accepts.
sidebar:
  order: 2
---

Every string under `messages`, plus the GUI, titles, the action bar, and the boss bar, goes through the same parser.

You can mix all of these in one line:

- MiniMessage: `<gray>`, `<red>`, `<bold>`, `<gradient:#ff0000:#00ff00>`
- Legacy: `&a`, `&l`
- Hex: `&#ff0000` or `#ff0000`

`{prefix}` inserts `messages.prefix`.

A broken MiniMessage tag does not stop the message. That line falls back to legacy codes.

If PlaceholderAPI is installed, its placeholders are filled in player-facing text. `{player}`, `{uuid}`, `{day}`, and `{date}` are filled first, as plain text. A name that contains `<` or `&` cannot recolor the rest of the line.

## Keys

| Key | When it is sent |
|---|---|
| `messages.prefix` | Inserted where a line contains `{prefix}` |
| `messages.help-header` | First line of `/rexcalendar help` |
| `messages.help` | The rest of the help, one list entry per line |
| `messages.reload` | After a successful reload |
| `messages.no-permission` | Missing `rexcalendar.use`, `rexcalendar.reload`, or `rexcalendar.day.<n>` |
| `messages.players-only` | Console tried to open the calendar |
| `messages.world` | The player's world is not allowed |
| `messages.claim` | A claim paid at least one item or command |
| `messages.no-reward` | The door was claimed and every chance missed, or the day has no rewards |
| `messages.already-claimed` | That date is already claimed, or a second click arrived while the first was still paying |
| `messages.too-early` | The door is later than today, or that day does not exist this month |
| `messages.missed` | `today-only` and the door is earlier than today |
| `messages.ip-limit` | The address hash is already at `settings.ip-limit` for that date |
| `messages.inventory-full` | The item rewards do not fit, so the date was not claimed |
| `messages.claim-failed` | The reward could not be given. When the rollback succeeds, the date was not claimed |

`{day}` is the door number. `{date}` is that door's `yyyy-MM-dd` in the configured zone.
