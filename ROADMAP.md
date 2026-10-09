# TomBlock roadmap

This is a living plan for TomBlock's current direction. It was updated as the
project developed, so it should not be treated as a design document written
before the current systems were implemented.

## Current direction

TomBlock started as a way for me to learn Java through Minecraft and gradually
grew into a much larger MMORPG project. Trying to design the whole game at once
made it difficult to tell whether any individual activity was actually fun.

For now, I am focusing on one smaller playable slice: hunting on the Southwest
Island. The purpose of this slice is to test a complete activity instead of
continuing to add disconnected systems.

The current hunting slice should let a player:

- meet Critter Hunter Will and learn the basic idea of hunting;
- observe a critter and understand how it reacts to the player and environment;
- use different methods to catch different critters;
- record discoveries and captures in the Critterdex;
- gain Hunting experience and useful rewards; and
- experience the activity within the island's quests, HUD, regions, and global
  time system.

## Why I chose hunting

Hunting gives me room to work on both gameplay programming and technical
design. It can involve movement, observation, timing, positioning, tools,
environmental clues, AI behaviour, progression, and quest design without being
another version of normal Minecraft combat.

The first Mossback hunt teaches the player to position a snare and pressure the
critter toward it. The other critters should not simply repeat that interaction.
Each one is meant to test a different idea, such as baiting, predicting movement,
following sound, reading a sequence, or interacting during a short opportunity.

## What currently exists

- Critter Hunter Will's introductory quest and Mossback hunt
- Several additional critters with different habitats and interaction rules
- Reusable encounter state and cleanup
- Critter observation and capture events
- Persistent Critterdex discoveries and capture counts
- Hunting experience, rewards, and progression
- Habitat and rarity data used to decide where critters belong
- A shared entity-AI foundation used by the current critter behaviours
- A global calendar and time system that can influence future spawning and
  statistics
- HUD presentation for quests, player status, notifications, time, and date

The code also contains older and broader TomBlock systems for items, combat,
mining, foraging, actors, dialogue, islands, persistence, and other MMORPG
features. Those systems are still part of the project, but they are not the
current design focus.

## Questions I need to answer through playtesting

- Does Will's quest teach hunting clearly without explaining every step in
  text?
- Do the critters feel meaningfully different from one another?
- Is observing a critter interesting, or does it feel like waiting?
- Are clues, failure states, and capture opportunities understandable?
- Does the island support the activity naturally, or does hunting feel placed
  on top of a map that was not designed for it?
- Does the Critterdex give the player a reason to find and understand more
  species?
- Which framework code is actually helping me make content, and which parts are
  more complicated than the current game needs?

## Next steps

1. Record and review a complete playthrough of the current hunting slice.
2. Watch another player try it without coaching them through the mechanics.
3. Fix the clearest problems found during playtesting before adding more
   critters or framework features.
4. Compare the different hunts and remove or redesign interactions that feel
   repetitive.
5. Improve authoring and diagnostic tools only where the current workflow is
   causing a real problem.
6. Decide what the next playable slice should be after the hunting loop has
   been evaluated.

## Possible later work

These are ideas rather than commitments:

- natural critter populations affected by habitat, time, season, weather, and
  player activity;
- more hunting tools and ways of reading animal behaviour;
- changing trap, bait, and clue interactions based on playtesting;
- party encounters and shared contribution rules;
- more reasons to revisit known habitats;
- stronger connections between hunting, crafting, collections, and the island
  economy; and
- further HUD and presentation polish.

Large networking changes, additional server instances, and expansion into the
full MMORPG are intentionally deferred until the smaller gameplay slices give
me a reason to build them.

## Related documentation

- `README.md` gives an overview of the project and its current systems.
- `CONFIGURATION.md` indexes the project's configuration files.
- `src/main/resources/critters/README.md` explains how critters are authored.
- `src/main/resources/encounters/README.md` explains the encounter definitions
  and currently supported hunting mechanics.
