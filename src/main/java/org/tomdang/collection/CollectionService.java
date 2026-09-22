package org.tomdang.collection;

import org.tomdang.player.counter.PlayerCounterService;

import java.util.Collection;
import java.util.UUID;

public final class CollectionService {
	private final CollectionRegistry registry;
	private final PlayerCounterService counters;
	public CollectionService(CollectionRegistry registry, PlayerCounterService counters) { this.registry = registry; this.counters = counters; }
	public Collection<CollectionDefinition> definitions() { return registry.all(); }
	public long amount(UUID playerId, CollectionDefinition definition) { return counters.get(playerId, definition.counterKey()); }
}
