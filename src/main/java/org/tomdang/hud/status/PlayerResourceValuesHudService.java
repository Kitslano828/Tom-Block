package org.tomdang.hud.status;

import org.bukkit.entity.Player;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.presentation.*;
import org.tomdang.hud.presentation.scheduling.HudUpdatePolicy;
import org.tomdang.player.playerresource.*;
import java.util.UUID;
import java.util.function.LongSupplier;

/** Event-driven bridge for the two numeric labels above the native resource bars. */
public final class PlayerResourceValuesHudService implements AutoCloseable, PlayerResourceListener {
	public static final HudElementSpec VALUES = HudElementSpec.persistent(
			HudElementId.of("tomblock", "resource-values"), HudRegion.STATUS, 200);
	private final PlayerResourceService resources;
	private final ProductionHudService hud;
	private final LongSupplier tick;
	private final AutoCloseable subscription;

	public PlayerResourceValuesHudService(PlayerResourceService resources, ProductionHudService hud, LongSupplier tick) {
		this.resources = java.util.Objects.requireNonNull(resources);
		this.hud = java.util.Objects.requireNonNull(hud);
		this.tick = java.util.Objects.requireNonNull(tick);
		this.subscription = resources.addListener(this);
	}

	public void refresh(Player player) { changed(resources.snapshot(player)); }

	@Override public void changed(PlayerResourceSnapshot snapshot) {
		long now = tick.getAsLong();
		hud.showIfChanged(snapshot.playerId(), VALUES,
				new PlayerResourceValuesHudModel(snapshot.health(), snapshot.energy()),
				revision(snapshot.health(), snapshot.energy()), now, HudUpdatePolicy.ON_CHANGE);
	}

	private long revision(double health, double energy) {
		return 31L * Double.doubleToLongBits(health) + Double.doubleToLongBits(energy);
	}

	public void forget(UUID playerId) { hud.forget(playerId); }

	@Override public void close() {
		try { subscription.close(); }
		catch (Exception exception) { throw new IllegalStateException("Could not detach resource-value HUD", exception); }
	}
}
