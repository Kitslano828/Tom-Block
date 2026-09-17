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
| Actors and dialogue | `actors/actors.yml`, `actors/dialogues.yml`, `actors/spawn-points.yml` | JAR |
| Combat and stats | `combat/combat.yml`, `stats/stat-rules.yml`, `stats/stat-presentations.yml`, `stats/stat-categories.yml` | JAR |
| World and movement | `regions.yml`, `region-stat-caps.yml`, `region-visualization.yml`, `movement-speed.yml` | server directory |
| Plugin declaration | `plugin.yml` | JAR |

Two files are runtime data, not content templates to casually replace:

- `plugins/TomBlock/playerprofiles.yml` stores player progress and base stats.
- `plugins/TomBlock/region-overrides.yml` stores block-level region brush edits.

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
