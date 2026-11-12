---
title: Installation
description: Put RexCalendar on a Paper, Purpur, or Folia server.
sidebar:
  order: 1
---

1. Download the jar.
2. Put it in the server `plugins/` folder.
3. Start the server once so `plugins/RexCalendar/config.yml` and `calendar.db` are written.
4. Edit the file.
5. Run `/rexcalendar reload`, or restart.

PlaceholderAPI is not required. If it is installed, its placeholders are filled in player-facing text.

A change under `storage` is read on the next startup. Reload does not switch the database.
