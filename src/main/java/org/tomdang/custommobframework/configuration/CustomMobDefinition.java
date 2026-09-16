package org.tomdang.custommobframework.configuration;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.tomdang.custommobframework.MobType;

import java.util.List;

public record CustomMobDefinition(
		String id,
		EntityType entityType,
		String displayName,
		double maxHealth,
		double damage,
		MobType mobType,
		int xp,
		boolean burnsInDaylight,
		List<CustomMobDropDefinition> drops
) {
	public CustomMobDefinition {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("id cannot be blank");
		if (entityType == null) throw new IllegalArgumentException("entityType cannot be null");
		if (entityType.getEntityClass() == null
				|| !LivingEntity.class.isAssignableFrom(entityType.getEntityClass())) {
			throw new IllegalArgumentException("entityType must represent a living entity");
		}
		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("displayName cannot be blank");
		}
		if (!Double.isFinite(maxHealth) || maxHealth <= 0) {
			throw new IllegalArgumentException("maxHealth must be finite and positive");
		}
		if (!Double.isFinite(damage) || damage < 0) {
			throw new IllegalArgumentException("damage must be finite and non-negative");
		}
		if (mobType == null) throw new IllegalArgumentException("mobType cannot be null");
		if (xp < 0) throw new IllegalArgumentException("xp cannot be negative");
		if (drops == null) throw new IllegalArgumentException("drops cannot be null");
		drops = List.copyOf(drops);
	}
}
