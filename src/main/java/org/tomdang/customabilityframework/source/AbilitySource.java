package org.tomdang.customabilityframework.source;

import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItem;

public record AbilitySource(
		CustomAbility ability,
		CustomItem sourceItem,
		String sourceId,
		AbilitySourceType sourceType
) {
	public AbilitySource {
		if (ability == null) throw new IllegalArgumentException("ability cannot be null");
		if (sourceItem == null) throw new IllegalArgumentException("sourceItem cannot be null");
		if (sourceId == null || sourceId.isBlank()) {
			throw new IllegalArgumentException("sourceId cannot be null or blank");
		}
		if (sourceType == null) throw new IllegalArgumentException("sourceType cannot be null");
		sourceId = sourceId.trim();
	}
}
