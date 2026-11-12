---
title: Commands
description: RexCalendar commands and their permissions.
sidebar:
  order: 1
---

| Command | Description | Permission |
|---|---|---|
| `/rexcalendar` | Show help | everyone |
| `/rexcalendar help` | Show help | everyone |
| `/rexcalendar open` | Open the calendar | `rexcalendar.use` |
| `/rexcalendar reload` | Reload `config.yml` | `rexcalendar.reload` |

Aliases: `/rcal`, `/rcalendar`

`/rexcalendar open` from the console sends `messages.players-only`.

Help text comes from `messages.help-header` and `messages.help`.

Reload rereads `config.yml`, including the timezone. It does not delete claims and it does not reconnect storage.
