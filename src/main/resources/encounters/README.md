# Encounter authoring

Encounter definitions are content. Store one encounter in each YAML file under
this directory; nested folders are discovered recursively.

```yaml
encounter:
  id: TUTORIAL_GLIMMERFLY
  behavior: GLIMMERFLY_HUNT
  mode: PLAYER
  timeout-seconds: 300
  disconnect-grace-seconds: 60
  activity:
    visibility: NEARBY
    interaction: OWNER
    contribution: BLOCK
    rewards: OWNER
  parameters:
    critter: GLIMMERFLY
```

- `id` is the stable identifier referenced by quests and persisted sessions.
- `behavior` selects runtime code registered by a content module before the
  encounter registry is sealed.
- `mode` is `PLAYER` for an isolated encounter or `PARTY` for shared ownership.
- `timeout-seconds` is the maximum total lifetime.
- `disconnect-grace-seconds` controls how long a suspended encounter can wait
  for its owner to reconnect.
- `parameters` contains behavior-specific content values.
- `activity` declares multiplayer authority independently of the behavior.
  Visibility is `OWNER`, `PARTICIPANTS`, `NEARBY`, or `WORLD`; interaction and
  rewards are `OWNER`, `PARTICIPANTS`, or `ANYONE`; contribution is `IGNORE`,
  `BLOCK`, or `CREDIT`. Omitting the section produces a private solo activity.

Quest stages start encounters through the shared action contract:

```yaml
on-enter:
  - type: START_ENCOUNTER
    parameters:
      encounter: TUTORIAL_GLIMMERFLY
```

An encounter completion publishes the typed `EncounterCompleted` gameplay
event. Quest objectives can consume that event through the existing
`COMPLETE_ENCOUNTER` objective type. This keeps quest files declarative: new
questlines select encounter IDs without adding quest-specific listeners.

The framework persists open sessions in PostgreSQL. A restart suspends them;
the owner can resume within the configured grace period. Completion, failure,
timeout, and plugin shutdown release encounter-owned runtime resources.

`GROUND_CRITTER_HUNT` adds a reusable track, trap, and flush loop. Its
parameters tune `clues-required`, `maximum-alertness`, `reckless-alertness`,
and `calm-recovery`; the species supplies its awareness radius and relocation
limit. Looking steadily at nearby tracks reads them without an interaction.
Completing the trail reveals authored cover points, one of which the player
selects for a snare. Movement pressure then makes the critter choose cover away
from the player's position. Reaching the snared cover completes the hunt;
reaching another cover consumes a relocation. Exhausting relocations fails the
encounter without publishing a capture, while success continues through the
standard critter event pipeline.

`VARIED_CRITTER_HUNT` supplies compact species-specific field mechanics. Set
`parameters.critter` and one of the following `parameters.mechanic` values:

- `GLIMMER_TIMING`: follow the moving flash and interact during its landing window.
- `BRAMBLE_BAIT`: place bait, withdraw and crouch until the animal feeds.
- `DEW_INTERCEPT`: read ripple telegraphs and intercept the predicted landing.
- `BURROW_EXITS`: seal two false exits and capture the animal at the live exit.
- `CANOPY_ECHO`: locate the true perch from three directional calls.
- `SPORE_SEQUENCE`: repeat the pulsing mushroom sequence to reveal the critter.

These mechanics still complete through `CritterRuntimeService`, so observation,
capture, Critterdex discovery, Hunting XP and material rewards use the same
authoritative progression pipeline as the Mossback hunt.
