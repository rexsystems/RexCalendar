# RexCalendar

An advent calendar chest for Paper, Purpur, and Folia. Each day of the month has a door. A player claims that door once for the date in your timezone.

<p>
  <img src="https://img.shields.io/badge/Minecraft-1.21%2B-62B47A?style=for-the-badge" alt="Minecraft 1.21+">
  <img src="https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
</p>

## Features

- Doors for days 1 through 31, placed in a chest you lay out
- The current day follows `settings.timezone` and rolls over at midnight with no restart
- `today-only` or `claim-past` for doors that already passed
- Item rewards and console commands, each with its own chance
- One claim per player and date, with a lock so a double click cannot pay twice
- Optional limit on how many accounts share an address hash
- SQLite by default, MySQL if you turn it on
- MiniMessage, legacy `&` codes, `&#RRGGBB`, and `#RRGGBB`
- PlaceholderAPI placeholders in player-facing text, when that plugin is installed

## Requirements

- Java 21
- Paper, Purpur, or Folia 1.21+
- PlaceholderAPI is optional

## Install

1. Put the jar in `plugins/`
2. Restart
3. Edit `plugins/RexCalendar/config.yml`
4. Run `/rexcalendar reload`

## Commands

| Command | Permission |
|---|---|
| `/rexcalendar help` | everyone |
| `/rexcalendar open` | `rexcalendar.use` |
| `/rexcalendar reload` | `rexcalendar.reload` |

Aliases: `/rcal`, `/rcalendar`

## Documentation

The pages in [docs/](docs/index.md) drop into a Starlight site at `src/content/docs/rexcalendar/`. Paste `docs/sidebar.mjs` into the site sidebar.

Full docs: [https://rexsystems.me/docs/rexcalendar](https://rexsystems.me/docs/rexcalendar)
