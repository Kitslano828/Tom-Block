package org.tomdang.hud.protocol;

import java.util.*;

public final class HudPackStateRegistry {
	private final Map<UUID, HudPackState> states = new HashMap<>();
	public HudPackState state(UUID playerId) { return states.getOrDefault(playerId, HudPackState.UNKNOWN); }
	public void update(UUID playerId, HudPackState state) {
		if (playerId == null || state == null) throw new IllegalArgumentException("Pack state update is incomplete");
		states.compute(playerId, (ignored, current) -> transition(current == null ? HudPackState.UNKNOWN : current, state));
	}
	public boolean canRender(UUID playerId) { return state(playerId) == HudPackState.LOADED; }
	public void remove(UUID playerId) { states.remove(playerId); }
	private HudPackState transition(HudPackState current, HudPackState incoming) {
		if (incoming == HudPackState.DECLINED || incoming == HudPackState.FAILED) return incoming;
		if (current == HudPackState.DECLINED || current == HudPackState.FAILED) return current;
		if (current == HudPackState.LOADED) return current;
		if (incoming == HudPackState.UNKNOWN) return current;
		return incoming;
	}
}
