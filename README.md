# TomBlock

For a categorized index of YAML files and their loading behavior, see [CONFIGURATION.md](CONFIGURATION.md).

TomBlock is an in-development Minecraft Paper MMORPG plugin written in Java. The goal is to build reusable systems for custom combat, abilities, items, crafting, NPCs, dialogue, mining, player progression, and eventually a complete multiplayer gameplay experience.

This is my first substantial programming project and is currently being developed as both a game project and a way for me to learn programming while making something fun in a game I enjoy.

## Why I Am Building It

TomBlock is a rewrite of an earlier project with the same name. The previous version was largely created through AI-generated code that I did not properly understand.

For this version, I started over and made a deliberate effort to write, test, and understand the implementation. I still use AI for guidance, unfamiliar syntax, debugging, and code review, but I implement the systems myself and work to understand why each component exists and how it communicates with the rest of the project. I am working on this project alongside learning to program.

One of my current goals is to become more independent in planning features and making architectural decisions.

## Current Features

- Custom item, weapon, armor, and mining-tool systems
- Custom combat statistics and damage calculations
- Reusable ability framework
- Combat and non-combat abilities
- Ability energy costs and cooldowns
- Automatic ability lore rendering
- Custom mobs, health, drops, and respawning
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
- Persistent private islands with generated ocean presets and idle unloading
- Registered renewable foraging trees with sequential breaking and regeneration
- Region-aware minimap HUD and exported-world map items
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
