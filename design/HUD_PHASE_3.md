# HUD Phase 3: production presentation foundation

Phase 3 is the boundary between gameplay state and the validated HUD composition/protocol engine.
It deliberately does not migrate health, energy, quests, dialogue, bosses, or the minimap.

## Data flow

`gameplay state -> HudViewModel -> HudPresenter -> HudContent -> HudCompositor -> HudTransport`

Gameplay owns semantic view models. Presenters own visual composition. The theme and asset
registries own reusable visual values. Only the protocol transport knows how a frame reaches the
Minecraft client.

## Contracts

- `HudPresenterRegistry` is sealed after bootstrap and rejects duplicate model ownership.
- `HudTheme` resolves named text and metric tokens loaded from `hud-theme.yml`.
- `HudAssetRegistry` resolves namespaced, typed assets loaded from `hud-assets.yml`.
- `HudElementSpec` holds placement, priority, lifetime, compact-mode and suppression policy.
- `ProductionHudService` is the future gameplay-facing entry point.
- `HudUpdateScheduler` publishes semantic changes with an optional minimum interval.
- `HudVisibilityPolicy` provides one central arbitration point for concurrent elements.
- `HudFailureReporter` isolates and reports visibility, presentation and layout failures per element.

## Migration rule

Future migrations register a typed presenter and assets, then publish a semantic model through
`ProductionHudService`. They must not construct protocol strings, reserve glyphs, or embed screen
coordinates in gameplay code.
