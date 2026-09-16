package org.tomdang.custommobframework.configuration;

/** One ambient population of a mob in a region. Distances are in blocks. */
public record MobPopulationRule(
		String mobId,
		String regionId,
		int maxAlive,
		long intervalTicks,
		int activationRadius,
		int despawnRadius,
		long despawnGraceTicks,
		int minimumSpawnDistance,
		int maximumSpawnDistance
) {
	public MobPopulationRule {
		if (mobId == null || mobId.isBlank() || regionId == null || regionId.isBlank()) {
			throw new IllegalArgumentException("mobId and regionId are required");
		}
		if (maxAlive < 1 || intervalTicks < 20 || activationRadius < 1
				|| despawnRadius <= activationRadius || despawnGraceTicks < 0
				|| minimumSpawnDistance < 1 || maximumSpawnDistance < minimumSpawnDistance
				|| maximumSpawnDistance > activationRadius) {
			throw new IllegalArgumentException("Invalid population limits for " + mobId);
		}
	}

	public String id() {
		return mobId + "@" + regionId;
	}
}
