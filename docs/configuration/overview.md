---
title: Configuration
description: Timezone, missed days, worlds, and the claim feedback in config.yml.
sidebar:
  order: 1
---

`config.yml` holds the calendar. `config-version` is internal. Leave it alone.

Claims live in the database. Reload does not delete them. See [Storage](/rexcalendar/configuration/storage/).

## settings

| Key | What it does |
|---|---|
| `timezone` | A `ZoneId`, default `Europe/Bucharest`. The current day is read when a player opens or claims. An unknown id uses `Europe/Bucharest`. |
| `missed-days` | `today-only` or `claim-past`. Anything else is treated as `today-only`. |
| `ip-limit` | How many accounts may claim one date for the same address hash. `0` turns the limit off. Negative values are treated as `0`. |

`today-only` opens the door whose number is today's day of the month. Earlier doors are missed. `claim-past` also opens those earlier doors. A later door stays closed. `rexcalendar.admin` can claim a missed door. A future door stays closed for admins too.

The day rolls over at midnight in `timezone` with no restart. After you edit the zone string, run `/rexcalendar reload`.

## worlds

If `worlds.enabled` has any names, the calendar opens only there. Otherwise `worlds.disabled` is a deny list. Both empty means every world.

## sounds, title, actionbar, bossbar

Each block has `enabled`. The player who claimed the day is the audience.

| Block | Text | Timing |
|---|---|---|
| `sounds` | `sound` is a Bukkit sound name, such as `ENTITY_PLAYER_LEVELUP`. An empty name skips it. | `volume` is clamped to 0-10. `pitch` at or below 0 becomes 1, and the maximum is 2. |
| `title` | `text` and `subtitle` | `fade-in`, `stay`, and `fade-out` are ticks. 20 ticks is one second. Each value is clamped to 0-1200. |
| `actionbar` | `text` | `ticks` is how long the line stays before it is cleared. 0 leaves the line. Clamped to 0-600. |
| `bossbar` | `text` | `seconds` is clamped to 1-60. |

Boss bar `color` is `PINK`, `BLUE`, `RED`, `GREEN`, `YELLOW`, `PURPLE`, or `WHITE`. An unknown color uses `YELLOW`.

Boss bar `style` is `SOLID`, `SEGMENTED_6`, `SEGMENTED_10`, `SEGMENTED_12`, or `SEGMENTED_20`. `SOLID` is a solid bar. An unknown style is solid too.

An unknown sound is logged once and skipped. The claim still stands.
