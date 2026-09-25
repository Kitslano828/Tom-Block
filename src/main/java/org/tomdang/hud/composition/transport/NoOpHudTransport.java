package org.tomdang.hud.composition.transport;

import org.tomdang.hud.composition.*;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/** Explicit non-rendering endpoint used while no client transport is installed. */
public final class NoOpHudTransport implements HudTransport {
	private final Set<HudRegion> regions;
	public NoOpHudTransport() { this(EnumSet.allOf(HudRegion.class)); }
	public NoOpHudTransport(Set<HudRegion> regions) {
		if (regions == null || regions.isEmpty()) throw new IllegalArgumentException("No-op HUD regions are required");
		this.regions = Set.copyOf(regions);
	}
	@Override public Set<HudRegion> regions() { return regions; }
	@Override public void apply(HudFrame frame) {}
	@Override public void clear(UUID playerId) {}
}
