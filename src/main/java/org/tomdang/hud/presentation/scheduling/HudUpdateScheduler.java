package org.tomdang.hud.presentation.scheduling;

import org.tomdang.hud.composition.HudElementId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Change-aware throttle. Callers provide a stable semantic revision, not rendered text. */
public final class HudUpdateScheduler {
	private final Map<Key, State> states = new HashMap<>();
	public boolean shouldPublish(UUID playerId, HudElementId elementId, long semanticRevision,
			long tick, HudUpdatePolicy policy) {
		if (playerId == null || elementId == null || policy == null) throw new IllegalArgumentException("HUD update key is incomplete");
		Key key = new Key(playerId, elementId);
		State previous = states.get(key);
		if (previous != null && previous.revision == semanticRevision) return false;
		if (previous != null && tick - previous.tick < policy.minimumIntervalTicks()) return false;
		states.put(key, new State(semanticRevision, tick));
		return true;
	}
	public void forget(UUID playerId, HudElementId elementId) { states.remove(new Key(playerId, elementId)); }
	public void forget(UUID playerId) { states.keySet().removeIf(key -> key.playerId.equals(playerId)); }
	private record Key(UUID playerId, HudElementId elementId) {}
	private record State(long revision, long tick) {}
}
