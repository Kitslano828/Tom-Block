# TomBlock configuration map

The source templates are grouped by feature under `src/main/resources/`. Most
gameplay/content files are read **from the built JAR** at startup; editing them
requires a rebuild and a full server restart. Region and movement templates stay
at the resource root and are copied to `plugins/TomBlock/` when missing. Existing
server copies are not overwritten by a new JAR.

| Area | Source YAML files | Loaded from |
| --- | --- | --- |
| Items | `items/items.yml`, `items/weapons.yml`, `items/armor.yml` | JAR |
| Mining and crafting | `mining/mining-tools.yml`, `mining/mining-blocks.yml`, `crafting/recipes.yml` | JAR |
| Mobs | `mobs/mobs.yml` | JAR |
| Actors and dialogue | `actors/actors.yml`, `actors/skins.yml`, `actors/dialogues.yml`, `actors/spawn-points.yml` | JAR |
| Combat and stats | `combat/combat.yml`, `stats/stat-rules.yml`, `stats/stat-presentations.yml`, `stats/stat-categories.yml` | JAR |
| World and movement | `regions.yml`, `region-stat-caps.yml`, `region-visualization.yml`, `movement-speed.yml` | server directory |
| Progression counters | `counter-definitions.yml` | JAR, synchronized into PostgreSQL |
| Database connection | `database.yml` | server directory; password comes from the environment |
| Private foraging trees | `foraging-trees.yml` | runtime server data |
| Plugin declaration | `plugin.yml` | JAR |

Regions may set `display-name` for player-facing text (for example, `Mushroom Island`). The YAML key remains the stable internal ID used by mob spawning, stat caps, and other references. When `display-name` is omitted, it defaults to the ID. Existing server-side `plugins/TomBlock/regions.yml` files are not overwritten by a plugin build; add new display names there explicitly when deploying.

Region shapes can combine `shape.cuboids` and `shape.polygons`. Each polygon has a `world`, inclusive `minimum-y` and `maximum-y`, and at least three `{ x, z }` vertices. Polygon edges count as inside. Locations should use a higher `priority` than surrounding wilderness territories. The local server's region file has been synced with the current source template; deploy the updated plugin JAR before restarting against polygon definitions.

Runtime state is not content to casually replace:

- PostgreSQL stores profiles, skills, counters, and private-island identity.
- `plugins/TomBlock/playerprofiles.yml` is retained only as a legacy import source.
- `plugins/TomBlock/foraging-trees.yml` stores registered tree positions.
- `plugins/TomBlock/region-overrides.yml` stores block-level region brush edits.
- Private island world folders store the actual Minecraft blocks.

## PostgreSQL player profiles

TomBlock can use PostgreSQL for player profiles while retaining `playerprofiles.yml` as a read-only migration source. PostgreSQL is disabled by default. On the first join of a player who exists only in YAML, TomBlock loads the legacy profile, writes it to PostgreSQL, and logs the imported UUID. Subsequent loads use PostgreSQL. The YAML file is not deleted or rewritten by this migration path.

Create a dedicated role and database from an administrator `psql` session, substituting a private password:

```sql
CREATE ROLE tomblock LOGIN PASSWORD 'replace-this-password';
CREATE DATABASE tomblock OWNER tomblock;
```

Start the server once to create `plugins/TomBlock/database.yml`, stop it, then set `database.enabled` to `true`. Do not place the password in YAML. Set the password in the environment that launches Paper:

```powershell
$env:TOMBLOCK_DB_PASSWORD = 'your-private-password'
```

On Linux/systemd, use an environment file readable only by the service account and reference it with `EnvironmentFile=`. Never commit that file. The configured PostgreSQL role needs ownership of the `tomblock` database. Flyway applies the versioned SQL files under `src/main/resources/db/migration/` before repositories start. Existing prototype databases are baselined at V1, so the counter framework begins with V2 without recreating profile tables.

Profile reads happen during Paper's asynchronous pre-login event. Saves use a single transaction so the profile row and all base-stat rows either succeed or roll back together.

### Expandable counters

Open-ended values such as blocks mined, mob kills, collection progress, and activity completions use the counter framework rather than new profile columns. Definitions live in the JAR-bundled `src/main/resources/counter-definitions.yml` and synchronize into PostgreSQL whenever TomBlock starts. This prevents an older server-side copy from hiding definitions added by a later build. The key is a permanent namespaced identifier:

```yaml
counters:
  BLOCK_MINED:OAK_LOG:
    display-name: Oak Logs Mined
    category: COLLECTIONS
    description: Total oak logs mined by this player.
    unit: COUNT
    default: 0
    minimum: 0
    enabled: true
```

Gameplay systems receive `PlayerCounterService` and call `increment(playerId, CounterKey.of("BLOCK_MINED:OAK_LOG"))`. Adding another material or mob normally requires a definition and an integration call, not a database migration. Fixed attributes involved in formulas remain in the typed player-stat system.

Never edit an already deployed migration. Add the next `V<number>__description.sql` file. Flyway records completed versions in `tomblock.flyway_schema_history` and refuses inconsistent migration histories.
`src/main/resources/playerprofiles.yml` is not loaded by the current profile
bootstrap. The authoritative profile file is the runtime file under
`plugins/TomBlock/`; do not edit the bundled sample expecting player changes.

## Mob population settings

Each mob in `mobs/mobs.yml` may have `allowed-spawn-regions` (an empty or absent list
means unrestricted) and an optional `population` section. The latter currently
supports one ambient population per mob definition. `placement` defaults to
`GROUND`; `AIR` requires `minimum-y` and `maximum-y`:

```yaml
population:
  region: BLACKSMITH_DEVELOPMENT_AREA
  max-alive: 3
  interval-ticks: 100
  activation-radius: 48
  despawn-radius: 80
  despawn-grace-ticks: 200
  minimum-spawn-distance: 16
  maximum-spawn-distance: 40
```

`max-near-player` optionally limits the number of matching mobs near a spawn
candidate; it defaults to `max-alive`. The configured `max-alive` remains the
whole-region ceiling. Spawn candidates are only searched near online players.

Mob `behavior` defaults to `VANILLA`. `FLOATING_WANDER` disables the carrier's
vanilla AI and item pickup, then moves it slowly through unobstructed, loaded
blocks inside its allowed region and configured population Y range. The jellyfish
uses this behavior while the training zombie keeps vanilla AI. This movement is
local wandering, not an obstacle-routing pathfinder.

The population rule checks already-loaded chunks near players; it does not load
distant chunks. It counts loaded mobs of that type in the region, including old
fixed-point mobs until those die. Only mobs created by this population rule are
removed after the no-player grace period. Existing fixed-point infrastructure
remains for future scripted encounters, but the Training Zombie's two old hard-
coded spawn points were removed. Ambient mobs are confined to their population
region; manually spawned mobs with allowed regions are confined to those regions.

Moving the JAR-bundled templates does not move any server-side files or reset
player profiles and region brush edits.

## Player NPC skins

An actor using `PLAYER_NPC` may specify `skin: BLACKSMITH` in `actors/actors.yml`.
The ID must exist in `actors/skins.yml`:

```yaml
skins:
  BLACKSMITH:
    value: "<base64-encoded textures property>"
    signature: "<matching signature, if supplied>"
```

Use the `textures` property value and its matching signature from a skin profile,
not a PNG path or a resource-pack model ID. The signature is optional in the
configuration but recommended when available. Unknown IDs and invalid base64
values fail during startup. Actors without `skin` keep the default player-NPC
appearance. Both YAML files are bundled in the JAR; edit, rebuild, and restart.

## Basic attack calculation profiles

Generic items may set `basic-attack-calculation` in `items/items.yml`. If omitted,
the profile is `COMBAT`, which uses Damage, Strength, Crit Chance, and Crit Damage.
The Jellyfish Net uses `JELLYFISH_HUNTING`, which instead reads the Fishing stats
`jellyfish-power` and `jellyfish-damage-bonus`. A bonus of 50 means 50% more
capture damage: `power * (1 + bonus / 100)`. Ordinary combat Attack Speed does
not shorten net recovery; net upgrades can set `base-recovery-ticks` directly.

`attack-capabilities` remains a separate target-eligibility rule. A calculation
profile chooses numbers; it does not grant permission to damage a mob. During
development, `/setstat jellyfish-damage-bonus 50` can simulate a future skill
shop bonus. That command changes the player's base stat, not a purchased perk.
