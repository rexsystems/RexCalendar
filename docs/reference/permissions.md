---
title: Permissions
description: Permission nodes shipped with RexCalendar.
sidebar:
  order: 2
---

| Permission | Default | Description |
|---|---|---|
| `rexcalendar.use` | true | Open the calendar and claim a day |
| `rexcalendar.reload` | op | Reload the configuration |
| `rexcalendar.admin` | op | Claim a missed day and skip the address limit |
| `rexcalendar.day.<n>` | true | Claim door `n`, where `n` is 1 through 31 |
| `rexcalendar.day.*` | true | Parent of every `rexcalendar.day.<n>` node |

`rexcalendar.day.<n>` starts allowed. Set that node to false to close one door. The player still needs `rexcalendar.use` to open the chest.

`rexcalendar.admin` can claim a missed door while `missed-days` is `today-only`, and it skips `settings.ip-limit`. A future door stays closed. An admin claim does not write an address hash.

`rexcalendar.reload` and `rexcalendar.admin` default to op. `rexcalendar.use` and the day nodes default to true for every player.
