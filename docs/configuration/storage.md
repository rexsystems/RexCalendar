---
title: Storage
description: SQLite by default, MySQL when you set it, and the address hash.
sidebar:
  order: 5
---

`storage.type` is `sqlite` or `mysql`. The default is `sqlite`.

SQLite writes `plugins/RexCalendar/calendar.db`. The plugin creates the file on startup.

## MySQL

```
storage.mysql.host
storage.mysql.port
storage.mysql.database
storage.mysql.username
storage.mysql.password
storage.mysql.pool-size
storage.mysql.ssl
```

`port` is clamped to 1-65535. `pool-size` is clamped to 1-20. `database` may use letters, numbers, and underscore. `host` is a hostname or an IPv4 address.

If the host or database name is rejected, the plugin logs a warning and uses SQLite.

The MySQL driver and the pool are inside the jar. You do not add them to the server.

## Claims

A claim is one row per player and date. The date key is `yyyy-MM-dd` in `settings.timezone`. Next month, the same door number is a new date.

Reload reads `config.yml` and leaves the rows in place. It does not open a new connection. A change to `storage.type` or `storage.mysql` is applied on the next startup.

## Address limit

When `settings.ip-limit` is above 0, the plugin stores the SHA-256 hex of the address Java reports for that connection. The same address always produces the same 64-character hex string. The database and the log contain that hash. They do not contain the address.

The limit counts distinct accounts for that hash and date. The claim is refused when the count would pass the limit.

`0` stores no hash.

A player with no socket address is not counted, and no hash is written for that claim.

`rexcalendar.admin` skips the limit. That claim does not add a hash row.

The count is taken in the same transaction as the insert. Two servers that share one MySQL database can still both allow a claim if they write at the same moment and the database rejects neither transaction. One server does not have that gap.
