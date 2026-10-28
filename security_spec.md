# Security Specification (`security_spec.md`)

## 1. Data Invariants
- Every document in `/work_alarms/{alarmId}` must belong to an authenticated user (`request.auth != null`) and have `userId == request.auth.uid`.
- Document IDs (`alarmId`) must match `^[a-zA-Z0-9_\-]+$` and be `<= 128` characters.
- `userId` and `createdAt` are immutable across updates.
- `createdAt` and `updatedAt` must be valid Firestore `Timestamp` values `<= request.time`.
- `category` must strictly belong to `['DEEP_WORK', 'MEETING', 'BIO_INCUBATION', 'PCR_ASSAY', 'CELL_CULTURE', 'BREAK_CIRCADIAN']`.
- All strings (`title`, `notes`, `bioTempCelsius`) and arrays (`daysOfWeek`) have strict maximum length bounds.
- `allow list` enforces `resource.data.userId == request.auth.uid` so queries without `.whereEqualTo("userId", uid)` are rejected.

## 2. Dirty Dozen Payloads
1. Unauthenticated read/list on `/work_alarms`.
2. Unauthenticated create on `/work_alarms/alarm_1`.
3. Cross-user `get` where `alice` reads `bob`'s alarm.
4. Cross-user `list` where `alice` queries without `userId == 'alice_123'` filter.
5. Spoofed `userId` on `create` (`alice` sets `userId: 'bob_456'`).
6. Ghost field injection on `create` or `update` (`isVerified: true`).
7. Immutable field mutation on `update` (changing `userId` or `createdAt`).
8. Invalid `hour` (`25`) or `minute` (`-1`).
9. Oversized `title` (`> 120` chars) or `notes` (`> 500` chars).
10. Invalid `category` enum (`"HACKED_CATEGORY"`).
11. Unbounded `daysOfWeek` array (`8` items) or wrong element type (`["MON"]`).
12. Future timestamp spoofing (`createdAt > request.time`).
