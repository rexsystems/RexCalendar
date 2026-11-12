---
title: Days and timezone
description: How a door maps to a calendar date, and when a missed day can still be claimed.
sidebar:
  order: 1
---

The current day is the local date in `settings.timezone`. The plugin reads it when the chest opens and again when a door is clicked. Midnight in that zone changes the open door with no restart.

The zone string is a `ZoneId`, such as `Europe/Bucharest`, `UTC`, or `America/New_York`. The default is `Europe/Bucharest`. An unknown string uses that default and logs a warning. After you change the string, run `/rexcalendar reload`.

## Door and date

Door `5` is the 5th day of the current month. The stored key is the full date.

On 20 March 2026, door 5 is `2026-03-05`. On 2 April 2026, door 5 is `2026-04-05`. Those are two claims.

A door number past the last day of the month has no date. Door 30 in February 2026 cannot be claimed. The icon stays locked and the click sends `messages.too-early`.

## Missed days

`settings.missed-days` has two values.

| Value | Door 4 when today is the 5th |
|---|---|
| `today-only` | Missed. The click sends `messages.missed`. |
| `claim-past` | Open, if that date is not already claimed. |

Door 6 on the 5th is too early for both values.

`rexcalendar.admin` may claim the missed door. It does not open a future door.

## A zone boundary

`Europe/Bucharest` is two hours ahead of UTC in winter. `2026-12-01T22:30:00Z` is still 1 December in UTC and already 2 December in Bucharest. The door follows Bucharest when that zone is configured.
