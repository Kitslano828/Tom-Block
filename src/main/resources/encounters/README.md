# Encounter authoring

Encounter definitions are content. Store one encounter in each YAML file under
this directory; nested folders are discovered recursively.

```yaml
id: TUTORIAL_GLIMMERFLY
behavior: GLIMMERFLY_HUNT
mode: PLAYER
timeout-seconds: 300
disconnect-grace-seconds: 60
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
