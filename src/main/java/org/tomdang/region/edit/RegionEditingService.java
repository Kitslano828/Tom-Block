package org.tomdang.region.edit;

import org.tomdang.region.override.RegionOverrideService;
import org.tomdang.region.override.RegionOverrideState;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public final class RegionEditingService {
	private final RegionRegistry regionRegistry;
	private final RegionOverrideService overrideService;
	private final RegionEditSessionRegistry sessionRegistry;

	public RegionEditingService(RegionRegistry regionRegistry, RegionOverrideService overrideService,
			RegionEditSessionRegistry sessionRegistry) {
		if (regionRegistry == null) throw new IllegalArgumentException("regionRegistry cannot be null");
		if (overrideService == null) throw new IllegalArgumentException("overrideService cannot be null");
		if (sessionRegistry == null) throw new IllegalArgumentException("sessionRegistry cannot be null");
		this.regionRegistry = regionRegistry;
		this.overrideService = overrideService;
		this.sessionRegistry = sessionRegistry;
	}

	public RegionEditSession begin(UUID playerId, String regionId) {
		regionRegistry.require(regionId);
		return sessionRegistry.begin(playerId, regionId);
	}

	public RegionEditAction apply(UUID playerId, BlockPosition position, RegionOverrideState state) {
		RegionEditSession session = requireSession(playerId);
		RegionOverrideState previous = overrideService.setState(session.selectedRegionId(), position, state);
		RegionEditAction action = new RegionEditAction(session.selectedRegionId(), position, previous, state);
		if (previous != state) session.record(action);
		return action;
	}

	public Optional<RegionEditAction> undo(UUID playerId) {
		RegionEditSession session = requireSession(playerId);
		Optional<RegionEditAction> latest = session.takeLatest();
		latest.ifPresent(action -> overrideService.setState(
				action.regionId(), action.position(), action.previousState()));
		return latest;
	}

	public void save() throws IOException {
		overrideService.save();
	}

	public boolean hasUnsavedChanges() {
		return overrideService.isDirty();
	}

	private RegionEditSession requireSession(UUID playerId) {
		return sessionRegistry.find(playerId)
				.orElseThrow(() -> new IllegalStateException("Player does not have an active region edit session"));
	}
}
