package org.tomdang.playernpc.runtime;

import java.util.*;

public class PlayerNpcVisibilityRegistry {

	private final Map<UUID, Set<UUID>> npcVisibilityMap = new HashMap<>();

	public boolean addViewer(UUID npcUUID, UUID viewerUUID) {
		if (viewerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		if (npcUUID == null) throw new IllegalArgumentException("NPC UUID cannot be null");

		Set<UUID> viewers =
				npcVisibilityMap.computeIfAbsent(npcUUID, ignored -> new HashSet<>());

		return viewers.add(viewerUUID);
	}

	public boolean isVisibleTo(UUID npcUUID, UUID viewerUUID) {
		if (viewerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		if (npcUUID == null) throw new IllegalArgumentException("NPC UUID cannot be null");

		if (npcVisibilityMap.get(npcUUID) == null) return false;

		return npcVisibilityMap.get(npcUUID).contains(viewerUUID);
	}

	public boolean removeViewer(UUID npcUUID, UUID viewerUUID) {
		if (viewerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		if (npcUUID == null) throw new IllegalArgumentException("NPC UUID cannot be null");

		Set<UUID> npcUUIDSet = npcVisibilityMap.get(npcUUID);
		if (npcUUIDSet == null) return false;
		boolean removedViewer = npcUUIDSet.remove(viewerUUID);
		if (npcUUIDSet.isEmpty()) npcVisibilityMap.remove(npcUUID);
		return removedViewer;
	}

	public Set<UUID> getViewers(UUID npcUUID) {
		if (npcUUID == null) throw new IllegalArgumentException("NPC UUID cannot be null");

		if (npcVisibilityMap.get(npcUUID) == null) return Set.of();
		if (npcVisibilityMap.get(npcUUID).isEmpty()) return Set.of();
		return Set.copyOf(npcVisibilityMap.get(npcUUID));
	}

	public Set<UUID> clearNpc(UUID npcUUID) {
		if (npcUUID == null) throw new IllegalArgumentException("npcUUID cannot be null");


		Set<UUID> removedViewerSet = npcVisibilityMap.remove(npcUUID);
		if (removedViewerSet == null) return Set.of();
		return Set.copyOf(removedViewerSet);
	}

	public void clearViewer(UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");

		for (Set<UUID> viewers : npcVisibilityMap.values()) {
			viewers.remove(playerUUID);
		}

		npcVisibilityMap.entrySet()
				.removeIf(entry -> entry.getValue().isEmpty());
	}

}
