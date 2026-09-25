# HUD Phase 4: health and energy production slice

Phase 4 is the first live gameplay feature carried by the production HUD engine.

## Runtime flow

`PlayerResourceService -> PlayerResourceSnapshot -> PlayerStatusHudService -> Health/Energy models -> presenters -> STATUS region`

- Resource mutations publish one complete immutable snapshot.
- Health and energy have independent semantic revisions, so unchanged values are not recomposed.
- Both widgets share the bottom-center STATUS region and compose horizontally.
- Presenters resolve every color, gap, width and image through Phase 3 theme/asset contracts.
- The protocol palette carries themed text colors without exposing marker encoding to presenters.
- Existing TomBlock heart, energy and bar textures are reused through the generated protocol font.
- Temporary legacy action-bar messages now enter the same carrier instead of overwriting it.

Quest, dialogue, boss, notification and map presentation are not migrated by this phase.
