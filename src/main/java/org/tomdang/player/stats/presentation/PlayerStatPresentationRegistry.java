package org.tomdang.player.stats.presentation;

import org.tomdang.player.stats.PlayerStatCategory;
import org.tomdang.player.stats.PlayerStatType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlayerStatPresentationRegistry {

	private final Map<PlayerStatType, PlayerStatPresentation> presentations =
			new EnumMap<>(PlayerStatType.class);
	private final List<PlayerStatType> displayOrder = new ArrayList<>();

	public void register(PlayerStatPresentation presentation) {
		if (presentation == null) throw new IllegalArgumentException("presentation cannot be null");
		PlayerStatType statType = presentation.statType();
		if (presentations.containsKey(statType)) {
			throw new IllegalStateException("Presentation already registered for " + statType.name());
		}
		presentations.put(statType, presentation);
		displayOrder.add(statType);
	}

	public void registerAll(List<PlayerStatPresentation> entries) {
		if (entries == null) throw new IllegalArgumentException("entries cannot be null");
		for (PlayerStatPresentation entry : entries) register(entry);
	}

	public PlayerStatPresentation get(PlayerStatType statType) {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		PlayerStatPresentation presentation = presentations.get(statType);
		if (presentation == null) {
			throw new IllegalStateException("No presentation registered for " + statType.name());
		}
		return presentation;
	}

	public List<PlayerStatPresentation> getVisibleByCategory(PlayerStatCategory category) {
		if (category == null) throw new IllegalArgumentException("category cannot be null");
		return displayOrder.stream()
				.map(presentations::get)
				.filter(PlayerStatPresentation::visible)
				.filter(presentation -> presentation.statType().getCategory() == category)
				.toList();
	}

	public List<PlayerStatPresentation> getAllInDisplayOrder() {
		return displayOrder.stream().map(presentations::get).toList();
	}

	public Map<PlayerStatType, PlayerStatPresentation> asMap() {
		return Collections.unmodifiableMap(presentations);
	}
}
