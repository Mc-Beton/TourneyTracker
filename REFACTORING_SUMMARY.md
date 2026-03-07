# Tournament-League Relationship Refactoring

## What Changed and Why

### Problem

Previously, tournaments and leagues had a **many-to-many** relationship through a `league_tournaments` join table. This was overly complex for the use case where:

- A tournament should belong to **ONE league or NONE**
- Tournaments needed to be created with a league assigned upfront
- The submission/approval workflow was unnecessary complexity

### Solution

Refactored to a **one-to-one** (or one-to-zero) relationship where Tournament directly references League.

---

## Database Changes

### Before

```
leagues table
league_tournaments table (id, league_id, tournament_id, status, submitted_by, etc.)
tournaments table
```

### After

```
leagues table
tournaments table (with league_id column, nullable FK to leagues)
```

**Migration:** Database was dropped and recreated from scratch with Hibernate auto-generating the schema.

---

## Code Changes

### 1. **Tournament.java** (Domain Entity)

**Added:**

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "league_id")
private com.tourney.domain.league.League league;
```

- Tournament now has direct reference to League
- Nullable (tournament can exist without a league)
- Foreign key with ON DELETE SET NULL

### 2. **CreateTournamentDTO.java** (Request DTO)

**Added:**

```java
private Long leagueId; // Opcjonalna liga
```

- Clients can now pass `leagueId` when creating a tournament

### 3. **TournamentManagementService.java** (Business Logic)

**Added league assignment in `createTournament()`:**

```java
if (dto.getLeagueId() != null) {
    League league = leagueRepository.findById(dto.getLeagueId())
        .orElseThrow(() -> new RuntimeException("Nie znaleziono ligi"));
    tournament.setLeague(league);
}
```

### 4. **TournamentRepository.java** (Data Access)

**Added:**

```java
Page<Tournament> findByLeague(League league, Pageable pageable);
```

- Query tournaments by league directly

### 5. **LeagueTournament.java** (Deprecated Entity)

**Commented out `@Entity` and `@Table`:**

```java
// @Entity // Commented out - using direct Tournament.league relationship instead
// @Table(name = "league_tournaments") // Table dropped in V14 migration
@Deprecated // Replaced by Tournament.league relationship
public class LeagueTournament {
```

- Class kept for backward compatibility of mapper
- No longer a JPA entity (Hibernate won't manage it)

### 6. **LeagueTournamentRepository.java**

**Renamed to `.deprecated`**

- Spring won't register it as a repository
- Prevents "Not a managed type" errors

### 7. **LeagueService.java** (Major Cleanup)

**Commented out repository dependency:**

```java
// private final LeagueTournamentRepository leagueTournamentRepository; // Deprecated
```

**Updated `getLeagueTournaments()`:**

```java
// OLD: leagueTournamentRepository.findByLeagueAndTournamentStatus(...)
// NEW: tournamentRepository.findByLeague(league, pageable)
return tournamentRepository.findByLeague(league, pageable)
        .map(leagueTournamentMapper::toTournamentDto);
```

**Deprecated and disabled old methods:**

- `getPendingTournaments()` - throws UnsupportedOperationException
- `submitTournament()` - throws UnsupportedOperationException
- `approveTournament()` - throws UnsupportedOperationException
- `processTournamentPoints()` - throws UnsupportedOperationException

These methods are no longer needed because:

- Tournaments are created with league directly (no submission)
- No approval workflow needed
- Points processing needs reimplementation (TODO)

### 8. **LeagueTournamentMapper.java** (Mapper)

**Added new mapping method:**

```java
public LeagueTournamentDTO toTournamentDto(Tournament tournament) {
    dto.setLeagueId(tournament.getLeague() != null ? tournament.getLeague().getId() : null);
    dto.setSubmittedBy(userMapper.toDto(tournament.getOrganizer()));
    // submitDate, processedDate no longer tracked
}
```

- Maps Tournament → LeagueTournamentDTO
- Old `toDto(LeagueTournament)` kept for backward compatibility

---

## Frontend Changes

### 1. **Tournament Form** (`/tournaments/new/page.tsx`)

**Added:**

- `useSearchParams()` to read `?leagueId=X` from URL
- League dropdown (shows only user's joined leagues)
- Pre-population: if `leagueId` in URL, populate and disable dropdown
- Sends `leagueId` in CreateTournamentDTO to backend

### 2. **League Detail Page** (`/leagues/[id]/page.tsx`)

**Added:**

- "Dodaj Turniej" button that routes to `/tournaments/new?leagueId={id}`
- Tournaments tab displays tournaments with matching `league_id`
- Tournament status badges

---

## What Still Works

✅ **Create tournament without league** - works (league_id is nullable)
✅ **Create tournament with league** - works via form dropdown or URL param
✅ **View league tournaments** - works via direct FK query
✅ **Add Tournament from league view** - works with pre-populated league

## What Doesn't Work (Deprecated)

❌ **Submit completed tournament to league** - method removed, no longer supported
❌ **Pending tournament approvals** - workflow removed
❌ **Tournament points processing** - needs reimplementation

---

## Migration Path

### Database

- **Production:** Would run V14 migration to add league_id, migrate data, drop old table
- **Development:** Dropped database and recreated from scratch (no important data)

### Code

- Deprecated methods throw `UnsupportedOperationException` with helpful messages
- Old repository renamed to prevent Spring registration
- Entity kept but not managed by JPA (no validation errors)

---

## Summary

**The refactoring simplifies the architecture by:**

1. ✅ Removing unnecessary join table
2. ✅ Eliminating complex submission/approval workflow
3. ✅ Allowing tournaments to declare league at creation
4. ✅ Making league-tournament relationship explicit and simple

**Trade-offs:**

- ⚠️ Lost submission tracking (submitted_by, submitDate, processedDate)
- ⚠️ Lost approval workflow (no pending status for league tournaments)
- ⚠️ Tournament points processing needs reimplementation

**Current Status:**

- ✅ Backend compiling and running
- ✅ Database schema updated
- ✅ Frontend forms updated
- ✅ Direct league-tournament queries working
