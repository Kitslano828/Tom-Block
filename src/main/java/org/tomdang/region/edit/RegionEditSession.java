package org.tomdang.region.edit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public final class RegionEditSession {
	private final int historyLimit;
	private final Deque<RegionEditAction> history = new ArrayDeque<>();
	private String selectedRegionId;

	public RegionEditSession(String selectedRegionId, int historyLimit) {
		selectRegion(selectedRegionId);
		if (historyLimit < 1) throw new IllegalArgumentException("historyLimit must be positive");
		this.historyLimit = historyLimit;
	}

	public String selectedRegionId() {
		return selectedRegionId;
	}

	public void selectRegion(String regionId) {
		if (regionId == null || regionId.isBlank()) throw new IllegalArgumentException("regionId cannot be null or blank");
		selectedRegionId = regionId.trim();
	}

	public void record(RegionEditAction action) {
		if (action == null) throw new IllegalArgumentException("action cannot be null");
		history.addFirst(action);
		while (history.size() > historyLimit) history.removeLast();
	}

	public Optional<RegionEditAction> takeLatest() {
		return Optional.ofNullable(history.pollFirst());
	}

	public int historySize() {
		return history.size();
	}
}
