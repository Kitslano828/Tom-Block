# TomBlock

For a categorized index of YAML files and their loading behavior, see [CONFIGURATION.md](CONFIGURATION.md).

TomBlock is an in-development, server-side Minecraft MMORPG built with Java and Paper. It uses a mandatory resource pack for its visual identity while allowing players to connect without installing client mods.

I am developing TomBlock as a long-term gameplay-programming and technical-design project. Its architecture separates reusable frameworks from concrete game content so that mechanics can be composed into playable systems instead of being tied to individual quests or locations.

## Why I Am Building It

TomBlock began as a way to learn Java through Minecraft, then grew into a larger study of game systems, server architecture, persistence, user interface design, and content production under vanilla-client constraints.

The current focus is building cohesive playable slices and using them to test whether the underlying systems produce clear, enjoyable decisions. The hunting prototype is one example: quests introduce the activity, critters respond to different environmental conditions, progression is recorded in the Critterdex, and the global calendar can influence future spawning and stat rules.

## Current Features

- Custom item, weapon, armor, and mining-tool systems
- Custom combat statistics and damage calculations
- Reusable ability framework
- Combat and non-combat abilities
- Ability energy costs and cooldowns
- Automatic ability lore rendering
- Custom mobs, health, drops, and respawning
- Hunting encounters, traps, environment-specific critters, and Critterdex progression
- Quest state, objectives, guidance, and tracked-quest HUD presentation
- Custom mining blocks, fortune, regeneration, and Mining Spread
- Shaped custom crafting recipes
- Custom forge inventory
- Actor definitions, instances, spawn points, and lifecycles
- Bukkit and packet-based NPC presentations
- Actor collision, visibility, movement, following, and looking
- Animated NPC dialogue
- Clickable dialogue choices
- Registered dialogue actions that can trigger gameplay behavior
- PostgreSQL player profiles, skill progression, counters, and private-island ownership
- Flyway-managed database migrations with one-time legacy YAML profile import
- Version-controlled public/private island presets with classifications, gameplay tags,
  access policies, travel limits, generated oceans, and lifecycle-driven unloading
- Authoritative managed-island block policies with persistent player-placement
  origins and reward-safe mining/foraging integration
- Persistent collections with `/collections`, custom foraging axes, and
  configurable milestone rewards and durability-based connected-tree harvesting
- Registered renewable foraging trees with sequential breaking and regeneration
- Region-aware minimap HUD and exported-world map items
- Resource-pack-driven HUD elements for health, energy, time, and calendar state
- A synchronized global calendar and daylight cycle shared across public islands
- JUnit and Mockito tests for selected systems

## Example Gameplay Path

The current Blacksmith demonstration connects several frameworks:

1. The player interacts with an actor.
2. The actor begins a dialogue session.
3. Dialogue text is animated through the action bar.
4. Dialogue choices are displayed in chat.
5. The player selects the forge choice.
6. The dialogue session ends.
7. A registered dialogue action opens the custom forge menu.

This path demonstrates how the actor, dialogue, action, and crafting systems work together without directly depending on one another.

## Demonstration

A three-minute demonstration of the current project is available here:

[Watch the TomBlock demonstration](https://www.youtube.com/watch?v=oZo07PBEBMk)

## Project Structure

The project is divided into several major areas:

- `customitemframework` – custom item definitions, creation, registration, and resolution
- `customabilityframework` – reusable abilities, triggers, cooldowns, active effects, and lore
- `combat` – weapons, damage, combat statistics, and combat abilities
- `mining` – mining tools, mining blocks, fortune, regeneration, and mining abilities
- `crafting` – shaped recipes, recipe matching, crafting services, and the forge menu
- `custommobframework` – custom mobs, health, drops, spawn points, and respawning
- `actorframework` – actor definitions, instances, presentations, interactions, movement, and visibility
- `playernpc` – packet-based player NPC creation, lifecycle, visibility, and interaction
- `dialogueframework` – dialogue definitions, sessions, presentation, choices, and actions
- `critter` and `encounter` – critter spawning, traps, capture rules, and Critterdex progression
- `quest` – quest definitions, objectives, progress, guidance, and presentation
- `worldtime` – global game time, calendar state, daylight synchronization, and modifiers
- `hud` – player-facing overlays and resource-pack-backed presentation
- `island` – managed island definitions, lifecycle, access, travel, and block policy
- `player` – profiles, statistics, resources, action bars, and persistence
- `bootstrap` – dependency construction and framework composition
- `content` – concrete game content built using the frameworks

## Building

For the local Windows Paper server, run `scripts/run-local-server.bat` (the
IntelliJ `TomBlock` Run configuration points to this script). It first runs
`prepareLocalServer`: Gradle builds and tests the plugin, packages only
`pack.mcmeta` and `assets/`, copies the JAR and ZIP into the local server, and
updates its local resource-pack SHA-1. Stop the local Paper server before
running it. The Ubuntu build server and public GitHub release are separate;
this local task never deploys to either one.

An ordinary Gradle `build` only builds and tests; it does not deploy files.

To build, upload, and optionally restart the definitive Ubuntu test server, use:

```powershell
.\scripts\Deploy-BuildServer.ps1
.\scripts\Deploy-BuildServer.ps1 -Restart
```

The script runs the tests, builds the Paper plugin JAR, uploads it atomically,
deploys the external world-map image, retains `TomBlock.jar.previous`, and
restores that JAR if the restarted systemd service fails its health check.
Third-party database libraries are loaded through Paper and are not shaded into
the plugin. It does not publish the resource pack; use
`Publish-ResourcePack.ps1` for pack releases.

TomBlock currently targets:

- Java 25
- Paper 26.2
- Gradle
- Paperweight user development tooling
