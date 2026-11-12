---
title: RexCalendar
description: An advent calendar chest for Paper, Purpur, and Folia.
---

RexCalendar opens a chest of days. A player claims the door for a date in `settings.timezone`, once. Rewards are items, console commands, or both.

Messages use MiniMessage. Legacy `&` codes, `&#RRGGBB`, and `#RRGGBB` also work. A broken MiniMessage line falls back to legacy codes.

## Files

```
plugins/RexCalendar/
  config.yml
  calendar.db
```

`calendar.db` is the SQLite file. MySQL does not create it.

A reload that changes the file writes `config.yml.bak.<timestamp>` first, then adds any keys that were missing. Values you already set stay as they are. Claims stay in the database.

## Next

- [Installation](/rexcalendar/getting-started/installation/)
- [Days and timezone](/rexcalendar/guides/days-and-timezone/)
- [Commands](/rexcalendar/reference/commands/)
