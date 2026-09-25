package org.tomdang.hud.composition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlayerHudSession {
	private final UUID playerId;
	private final Map<HudElementId, Entry> elements = new LinkedHashMap<>();
	private long sequence;
	private long revision;

	public PlayerHudSession(UUID playerId) {
		this.playerId = java.util.Objects.requireNonNull(playerId);
	}

	public UUID playerId() { return playerId; }
	public long revision() { return revision; }

	public void put(HudElement element) {
		if (element == null) throw new IllegalArgumentException("HUD element cannot be null");
		Entry previous = elements.get(element.id());
		long order = previous == null ? sequence++ : previous.sequence();
		elements.put(element.id(), new Entry(element, order));
		revision++;
	}

	public void remove(HudElementId id) {
		if (elements.remove(id) != null) revision++;
	}

	public List<Entry> entries() { return List.copyOf(elements.values()); }

	public record Entry(HudElement element, long sequence) {}
}
