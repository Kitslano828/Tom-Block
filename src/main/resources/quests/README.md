# Quest definition layout

Store one quest in each `.yml` or `.yaml` file. Folders are organizational and
may be nested by island, storyline, or quest type. TomBlock recursively discovers
all quest files at startup in a deterministic path order.

```text
quests/
  southwest-island/
    main/
      introduction-to-hunting.yml
    side/
    repeatable/
```

Each file uses a singular `quest` root and declares its stable ID:

```yaml
quest:
  id: INTRO_TO_HUNTING
  display-name: "A Hunter's First Steps"
  category: HUNTING
  start-policy: AUTO_ON_JOIN # or MANUAL
  start-stage: ARRIVE
  stages: {}
```

Optional orchestration is definition-driven. Handler `type` values must be
registered by a content module before quest validation seals the registries.

```yaml
quest:
  start-conditions:
    HAS_ACCESS:
      type: EXAMPLE_CONDITION
      parameters: {value: example}
  rewards:
    FIRST_REWARD:
      type: EXAMPLE_REWARD
      parameters: {amount: 1}
  stages:
    ARRIVE:
      on-enter:
        SHOW_GUIDANCE:
          type: EXAMPLE_ACTION
      on-exit:
        CLEAR_GUIDANCE:
          type: EXAMPLE_ACTION
      completion-conditions:
        AREA_IS_SAFE:
          type: EXAMPLE_CONDITION
      objectives:
        REACH_WILL: {type: INTERACT_WITH_ACTOR, target: CRITTER_HUNTER_WILL}
```

Quest gates are authored separately in `quest-gates.yml`. The generic gate
listener contains no quest IDs, NPC identities, dialogue IDs, or coordinates.

Quest IDs are persisted in PostgreSQL. Do not rename a released quest ID without
an accompanying data migration. File names and folders may be reorganized without
affecting player progress.

All files are loaded before prerequisites are checked. Duplicate IDs, missing
prerequisites, invalid stage graphs, or attempts to mutate the sealed runtime
registry stop plugin startup with a source-specific error.
