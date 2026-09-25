package org.tomdang.platform.presentation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Arbitrates logical HUD surfaces without coupling gameplay to a rendering technique. */
@Deprecated(forRemoval = true)
public final class PresentationCoordinator {
	private final Map<UUID, Map<PresentationHandle, Entry>> requests = new LinkedHashMap<>();
	private long sequence;

	public PresentationHandle show(UUID playerId, PresentationRequest request) {
		if (playerId == null || request == null) throw new IllegalArgumentException("playerId and request cannot be null");
		PresentationHandle handle = new PresentationHandle(UUID.randomUUID());
		requests.computeIfAbsent(playerId, ignored -> new LinkedHashMap<>())
				.put(handle, new Entry(request, sequence++));
		return handle;
	}

	public void hide(UUID playerId, PresentationHandle handle) {
		Map<PresentationHandle, Entry> playerRequests = requests.get(playerId);
		if (playerRequests == null) return;
		playerRequests.remove(handle);
		if (playerRequests.isEmpty()) requests.remove(playerId);
	}

	public Optional<PresentationRequest> active(UUID playerId, PresentationChannel channel) {
		Map<PresentationHandle, Entry> playerRequests = requests.get(playerId);
		if (playerRequests == null) return Optional.empty();
		return playerRequests.values().stream()
				.filter(entry -> entry.request.channel() == channel)
				.max(Comparator.comparingInt((Entry entry) -> entry.request.priority()).thenComparingLong(Entry::sequence))
				.map(Entry::request);
	}

	public Map<PresentationChannel, PresentationRequest> active(UUID playerId) {
		Map<PresentationChannel, PresentationRequest> result = new EnumMap<>(PresentationChannel.class);
		for (PresentationChannel channel : PresentationChannel.values()) active(playerId, channel).ifPresent(value -> result.put(channel, value));
		return Map.copyOf(result);
	}

	public void clearOwner(UUID playerId, org.tomdang.platform.identity.ContentKey<?> owner) {
		Map<PresentationHandle, Entry> playerRequests = requests.get(playerId);
		if (playerRequests == null) return;
		new ArrayList<>(playerRequests.entrySet()).stream()
				.filter(entry -> entry.getValue().request.owner().equals(owner))
				.map(Map.Entry::getKey).forEach(playerRequests::remove);
		if (playerRequests.isEmpty()) requests.remove(playerId);
	}

	public void clearPlayer(UUID playerId) { requests.remove(playerId); }

	private record Entry(PresentationRequest request, long sequence) {}
}
