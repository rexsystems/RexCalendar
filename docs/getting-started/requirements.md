---
title: Requirements
description: Server and Java versions RexCalendar runs on.
sidebar:
  order: 2
---

| | |
|---|---|
| Java | 21 |
| Server | Paper, Purpur, or Folia 1.21 or newer |
| Optional | PlaceholderAPI |
| Optional | MySQL, if `storage.type` is `mysql` |

SQLite is included. You do not install it yourself.

On Folia, reward commands are handed to the global region after the claim is saved. A command that fails there does not undo the claim. Item rewards are still checked on the player before the claim is saved.
