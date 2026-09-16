package org.tomdang.region.tracking;

import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.definition.RegionDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class PlayerRegionTrackingService {
	private final RegionResolver resolver;
	private final BiConsumer<UUID, RegionMembershipTransition> publisher;
	private final Map<UUID, Tracked> tracked = new HashMap<>();

	public PlayerRegionTrackingService(RegionResolver resolver,
			BiConsumer<UUID, RegionMembershipTransition> publisher) {
		if (resolver == null || publisher == null) throw new IllegalArgumentException("resolver and publisher are required");
		this.resolver = resolver;
		this.publisher = publisher;
	}

	public Optional<RegionMembershipSnapshot> snapshot(UUID playerId) {
		Tracked state = tracked.get(playerId);
		return state == null ? Optional.empty() : Optional.of(state.snapshot());
	}

	/** Skips repeated movement within the same block. */
	public void update(UUID playerId, BlockPosition position) {
		update(playerId, position, false);
	}

	/** Re-evaluates even in the same block, for example after a brush edit. */
	public void refresh(UUID playerId, BlockPosition position) {
		update(playerId, position, true);
	}

	private void update(UUID playerId, BlockPosition position, boolean force) {
		if (playerId == null || position == null) throw new IllegalArgumentException("playerId and position are required");
		Tracked old = tracked.get(playerId);
		if (!force && old != null && old.position().equals(position)) return;
		RegionMembershipSnapshot previous = old == null ? RegionMembershipSnapshot.EMPTY : old.snapshot();
		List<String> direct = resolver.directRegionsAt(position).stream().map(RegionDefinition::id).toList();
		List<String> resolved = resolver.regionsAt(position).stream().map(RegionDefinition::id).toList();
		RegionMembershipSnapshot current = new RegionMembershipSnapshot(direct, resolved,
				resolved.isEmpty() ? Optional.empty() : Optional.of(resolved.getFirst()));
		tracked.put(playerId, new Tracked(position, current));
		List<String> entered = resolved.stream().filter(id -> !previous.resolved().contains(id)).toList();
		List<String> left = previous.resolved().stream().filter(id -> !resolved.contains(id)).toList();
		RegionMembershipTransition transition = new RegionMembershipTransition(previous, current, entered, left);
		if (!previous.equals(current)) publisher.accept(playerId, transition);
	}

	public void clear(UUID playerId) {
		tracked.remove(playerId);
	}

	private record Tracked(BlockPosition position, RegionMembershipSnapshot snapshot) {}
}
