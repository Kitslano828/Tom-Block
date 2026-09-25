package org.tomdang.hud.status;

import org.tomdang.hud.composition.*;
import org.tomdang.hud.presentation.*;
import org.tomdang.hud.presentation.scheduling.HudUpdatePolicy;
import org.tomdang.player.playerresource.*;
import java.util.UUID;
import java.util.function.LongSupplier;

/** Event-driven bridge from player resources into the production HUD pipeline. */
public final class PlayerStatusHudService implements AutoCloseable, PlayerResourceListener {
	public static final HudElementSpec HEALTH = HudElementSpec.persistent(
			HudElementId.of("tomblock", "health"), HudRegion.STATUS, 200);
	public static final HudElementSpec ENERGY = HudElementSpec.persistent(
			HudElementId.of("tomblock", "energy"), HudRegion.STATUS, 190);
	private final ProductionHudService hud;
	private final LongSupplier tick;
	private final AutoCloseable subscription;

	public PlayerStatusHudService(PlayerResourceService resources, ProductionHudService hud, LongSupplier tick) {
		this.hud = java.util.Objects.requireNonNull(hud);
		this.tick = java.util.Objects.requireNonNull(tick);
		this.subscription = java.util.Objects.requireNonNull(resources).addListener(this);
	}
	@Override public void changed(PlayerResourceSnapshot snapshot) {
		long now = tick.getAsLong();
		hud.showIfChanged(snapshot.playerId(), HEALTH,
				new HealthHudModel(snapshot.health(), snapshot.maximumHealth()),
				revision(snapshot.health(), snapshot.maximumHealth()), now, HudUpdatePolicy.ON_CHANGE);
		hud.showIfChanged(snapshot.playerId(), ENERGY,
				new EnergyHudModel(snapshot.energy(), snapshot.maximumEnergy()),
				revision(snapshot.energy(), snapshot.maximumEnergy()), now, HudUpdatePolicy.ON_CHANGE);
	}
	private long revision(double current, double maximum) {
		return 31L * Double.doubleToLongBits(current) + Double.doubleToLongBits(maximum);
	}
	public void forget(UUID playerId) { hud.forget(playerId); }
	@Override public void close() {
		try { subscription.close(); }
		catch (Exception exception) { throw new IllegalStateException("Could not detach status HUD", exception); }
	}
}
