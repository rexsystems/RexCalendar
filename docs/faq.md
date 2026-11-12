---
title: FAQ
description: Timezone, double click, address hash, a full inventory, and reload.
sidebar:
  order: 1
---

## The wrong day is open

The door follows `settings.timezone`. The default is `Europe/Bucharest`. Set the zone to `UTC` when the calendar should follow UTC.

In winter that zone is two hours ahead of UTC. 22:30 UTC is already the next calendar day in Bucharest.

An unknown zone id is logged, and the plugin uses `Europe/Bucharest`.

The day changes at midnight in the zone with no restart. If you edited the zone string, run `/rexcalendar reload` so the new id is loaded.

A door number that does not exist this month, such as door 30 in February, stays locked.

## One click paid twice

The plugin locks that player and that date before it pays. The claim row is inserted before items or commands are given. The second click finds the lock, or the row, and sends `messages.already-claimed`.

If the give fails, the row is deleted and the lock is released. A later click can try again.

On Folia, console commands run after the row is saved. A command that fails on that later step does not delete the row. Item rewards are still checked before the row is inserted.

## The address limit is blocking the wrong accounts

`settings.ip-limit` counts accounts per date for one hash. The hash is the SHA-256 hex of the address Java reports. The same address always hashes to the same 64-character string. The raw address is not written to the database or the log.

`0` stores no hash and applies no limit.

Two accounts behind one address count separately toward the limit. `rexcalendar.admin` skips the limit and does not add a hash row. A player with no socket address is not counted.

## The inventory was full and the reward vanished

Item rewards that do not fit do not claim the day. The player keeps the door, and `messages.inventory-full` is sent. A later click rolls the chances again.

Command rewards are not measured against free slots. A console `give` can drop leftovers on the ground and still leave the day claimed.

A day with no `rewards` list sends `messages.no-reward` and does not claim the day. If the list exists and every chance misses, the day is claimed and the same message is sent.

## Reload wiped a claim, or ignored a storage change

Reload rereads `config.yml`. It adds missing keys and writes `config.yml.bak.<timestamp>` when it changes the file. Values you already set stay. Claim rows stay.

Reload does not reconnect SQLite or MySQL. A new `storage.type`, host, database, user, or password is used on the next startup.

A timezone edit in the file is picked up by reload. The midnight rollover does not need one.
