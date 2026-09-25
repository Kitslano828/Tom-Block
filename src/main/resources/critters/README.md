# Critter authoring

Each YAML file under this directory defines one critter. Files are discovered
recursively; IDs are stable references used by encounters, quests, journals,
and persisted player progress.

The definition separates independent dimensions:

- `family` groups related species for the journal.
- `rarity` controls ecological weighting, not interaction difficulty.
- `habitats` determine where ecology may select the critter.
- `temperaments` are composable behavioral traits.
- `movement` describes navigation requirements.
- `hunting` defines interaction archetypes, alertness range, relocation limit,
  capture window, and the successful outcome.
- `rewards` grants Hunting XP and custom-item materials.
- `presentation` selects replaceable visual code. `PLACEHOLDER` requires no
  final art model.
- `relations` links variants, life stages, or related species without imposing
  a Pokemon-style evolution system.
- `ai.navigator` selects `DIRECT_FLIGHT` for display/flying bodies or
  `NATIVE_GROUND` for Paper mobs.
- `ai.goals` is a priority-ordered behavior profile. Goal IDs are author-facing
  stable labels; each entry declares a reusable `type`, numeric `priority`, and
  scalar tuning parameters. Higher priorities interrupt lower ones. Equal
  priorities resolve by goal ID, so behavior remains deterministic.

Available goal types are `IDLE`, `REST`, `WANDER`, `PATROL`,
`INVESTIGATE_PLAYER`, `FLEE_FROM_PLAYER`, `FOLLOW_OWNER`, `RETURN_HOME`,
`CHASE`, `ATTACK`, and `SETTLE`. Unsupported navigator and goal names fail
configuration loading instead of degrading silently. Combat adapters implement
the optional combat-agent contract; non-combat agents simply cannot select an
`ATTACK` goal.

The runtime publishes `CritterObserved` and `CritterCaptured`. The journal,
Hunting skill, quest system, rewards, and counters consume those facts
independently. New species therefore do not require quest-specific listeners.

Natural spawning is selected through habitat eligibility and rarity weighting.
Wild Fortune increases eligible rare-tier weights only; it never bypasses a
habitat requirement. Population scheduling should be enabled after an authored
encounter proves that a species' interaction is understandable and enjoyable.
