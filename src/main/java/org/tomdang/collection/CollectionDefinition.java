package org.tomdang.collection;

import org.bukkit.Material;
import org.tomdang.player.counter.CounterKey;

import java.util.List;

public record CollectionDefinition(String id, String displayName, String category, Material material,
		CounterKey counterKey, List<Long> milestones) {
	public CollectionDefinition {
		if (id == null || !id.matches("[A-Z][A-Z0-9_]*")) throw new IllegalArgumentException("Invalid collection id");
		if (displayName == null || displayName.isBlank() || category == null || category.isBlank() || material == null || counterKey == null)
			throw new IllegalArgumentException("Incomplete collection definition: " + id);
		milestones = List.copyOf(milestones);
		long previous = 0;
		for (long milestone : milestones) {
			if (milestone <= previous) throw new IllegalArgumentException("Collection milestones must be positive and ascending: " + id);
			previous = milestone;
		}
	}
	public Long nextMilestone(long amount) { return milestones.stream().filter(value -> value > amount).findFirst().orElse(null); }
}
