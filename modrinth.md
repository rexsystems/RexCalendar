# RexCalendar

An advent calendar chest for Paper, Purpur, and Folia. Give each day of the month a door, a reward, and one claim per date.

**Java 21. Paper, Purpur, or Folia 1.21+.** PlaceholderAPI is optional.

## Features

- A chest of doors for the days you configure
- The open door follows `settings.timezone`, including the rollover at midnight
- `today-only` keeps missed doors closed. `claim-past` lets players take them later in the month
- Rewards are items, console commands, or both, and each entry has a chance
- A player claims a date once. A second click does not pay again
- Optional cap on accounts that share one address hash. The hash is what gets stored
- SQLite in the plugin folder, or MySQL
- MiniMessage, `&` codes, `&#RRGGBB`, and `#RRGGBB` on player-facing text
- Title, action bar, boss bar, and sound on a successful claim

## Commands

| Command | Description | Permission |
|---|---|---|
| `/rexcalendar help` | Show help | everyone |
| `/rexcalendar open` | Open the calendar | `rexcalendar.use` |
| `/rexcalendar reload` | Reload `config.yml` | `rexcalendar.reload` |

Aliases: `/rcal`, `/rcalendar`

## Permissions

| Permission | Default | Description |
|---|---|---|
| `rexcalendar.use` | true | Open the calendar and claim a day |
| `rexcalendar.reload` | op | Reload the configuration |
| `rexcalendar.admin` | op | Claim a missed day and skip the address limit |
| `rexcalendar.day.<n>` | true | Claim that day number. Set it to false to close the door |

Documentation: https://rexsystems.me/docs/rexcalendar
