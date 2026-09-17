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
		int maximumSpawnDistance,
		MobSpawnPlacement placement,
		Integer minimumY,
		Integer maximumY,
		int maxNearPlayer
) {
	public MobPopulationRule {
		if (mobId == null || mobId.isBlank() || regionId == null || regionId.isBlank()) {
			throw new IllegalArgumentException("mobId and regionId are required");
		}
		if (maxAlive < 1 || maxNearPlayer < 1 || maxNearPlayer > maxAlive
				|| intervalTicks < 20 || activationRadius < 1
				|| despawnRadius <= activationRadius || despawnGraceTicks < 0
				|| minimumSpawnDistance < 1 || maximumSpawnDistance < minimumSpawnDistance
				|| maximumSpawnDistance > activationRadius) {
			throw new IllegalArgumentException("Invalid population limits for " + mobId);
		}
		if (placement == null || (minimumY == null) != (maximumY == null)
				|| (minimumY != null && minimumY > maximumY)
				|| (placement == MobSpawnPlacement.AIR && minimumY == null)) {
			throw new IllegalArgumentException("Invalid population placement for " + mobId);
		}
	}

	public MobPopulationRule(String mobId, String regionId, int maxAlive, long intervalTicks,
	                         int activationRadius, int despawnRadius, long despawnGraceTicks,
	                         int minimumSpawnDistance, int maximumSpawnDistance,
	                         MobSpawnPlacement placement, Integer minimumY, Integer maximumY) {
		this(mobId, regionId, maxAlive, intervalTicks, activationRadius, despawnRadius,
				despawnGraceTicks, minimumSpawnDistance, maximumSpawnDistance,
				placement, minimumY, maximumY, maxAlive);
	}

	/** Existing populations without placement configuration remain ground based. */
	public MobPopulationRule(String mobId, String regionId, int maxAlive, long intervalTicks,
	                         int activationRadius, int despawnRadius, long despawnGraceTicks,
	                         int minimumSpawnDistance, int maximumSpawnDistance) {
		this(mobId, regionId, maxAlive, intervalTicks, activationRadius, despawnRadius,
				despawnGraceTicks, minimumSpawnDistance, maximumSpawnDistance,
				MobSpawnPlacement.GROUND, null, null);
	}

	public String id() {
		return mobId + "@" + regionId;
	}
}
