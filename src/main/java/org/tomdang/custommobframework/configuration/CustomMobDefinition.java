package org.tomdang.custommobframework.configuration;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.tomdang.custommobframework.MobType;
import org.tomdang.combat.eligibility.AttackEligibilityRule;
import org.tomdang.custommobframework.behavior.MobBehaviorType;

import java.util.List;
import java.util.Set;

public record CustomMobDefinition(
		String id,
		EntityType entityType,
		String displayName,
		double maxHealth,
		double damage,
		MobType mobType,
		int xp,
		boolean burnsInDaylight,
	List<String> allowedSpawnRegions,
	MobPopulationRule population,
	AttackEligibilityRule attackEligibilityRule,
	List<CustomMobDropDefinition> drops,
	MobBehaviorType behavior
) {
	public CustomMobDefinition(String id, EntityType entityType, String displayName, double maxHealth,
	                           double damage, MobType mobType, int xp, boolean burnsInDaylight,
	                           List<String> allowedSpawnRegions, MobPopulationRule population,
	                           AttackEligibilityRule attackEligibilityRule, List<CustomMobDropDefinition> drops) {
		this(id, entityType, displayName, maxHealth, damage, mobType, xp, burnsInDaylight,
				allowedSpawnRegions, population, attackEligibilityRule, drops, MobBehaviorType.VANILLA);
	}
	public CustomMobDefinition(String id, EntityType entityType, String displayName, double maxHealth,
	                           double damage, MobType mobType, int xp, boolean burnsInDaylight,
	                           List<String> allowedSpawnRegions, MobPopulationRule population,
	                           List<CustomMobDropDefinition> drops) {
		this(id, entityType, displayName, maxHealth, damage, mobType, xp, burnsInDaylight,
				allowedSpawnRegions, population, new AttackEligibilityRule(Set.of()), drops, MobBehaviorType.VANILLA);
	}

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
		if (allowedSpawnRegions == null) throw new IllegalArgumentException("allowedSpawnRegions cannot be null");
		if (allowedSpawnRegions.stream().anyMatch(regionId -> regionId == null || regionId.isBlank())) {
			throw new IllegalArgumentException("allowedSpawnRegions cannot contain blank IDs");
		}
		if (allowedSpawnRegions.stream().distinct().count() != allowedSpawnRegions.size()) {
			throw new IllegalArgumentException("allowedSpawnRegions cannot contain duplicate IDs");
		}
		if (drops == null) throw new IllegalArgumentException("drops cannot be null");
		if (attackEligibilityRule == null) throw new IllegalArgumentException("attackEligibilityRule cannot be null");
		if (behavior == null) throw new IllegalArgumentException("behavior cannot be null");
		allowedSpawnRegions = List.copyOf(allowedSpawnRegions);
		if (population != null && !population.mobId().equals(id)) {
			throw new IllegalArgumentException("population mobId must match definition ID");
		}
		drops = List.copyOf(drops);
	}
}
