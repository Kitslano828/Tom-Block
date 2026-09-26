package org.tomdang.hud.hunting;

import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.tomdang.hud.composition.HudElementId;
import org.tomdang.hud.composition.HudRegion;
import org.tomdang.hud.presentation.HudElementSpec;
import org.tomdang.hud.presentation.ProductionHudService;

public final class HuntingHudService {
    private static final HudElementId ID = HudElementId.of("tomblock", "hunting-encounter");
    private static final HudElementSpec SPEC = new HudElementSpec(ID, HudRegion.HUNTING, 100,
            true, Long.MAX_VALUE, Set.of());
    private final ProductionHudService hud;
    public HuntingHudService(ProductionHudService hud) {
        this.hud = java.util.Objects.requireNonNull(hud);
    }
    public void show(UUID playerId, HuntingHudModel model) {
        hud.show(playerId, SPEC, model, Bukkit.getCurrentTick());
    }
    public void hide(UUID playerId) { hud.hide(playerId, SPEC, Bukkit.getCurrentTick()); }
}
