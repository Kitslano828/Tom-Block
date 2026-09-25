package org.tomdang.player.playerresource;

import java.util.UUID;

public record PlayerResourceSnapshot(UUID playerId, double health, double maximumHealth,
		double energy, double maximumEnergy) {
	public PlayerResourceSnapshot {
		if (playerId == null || !valid(health, maximumHealth) || !valid(energy, maximumEnergy))
			throw new IllegalArgumentException("Invalid player resource snapshot");
		health = Math.min(health, maximumHealth);
		energy = Math.min(energy, maximumEnergy);
	}
	private static boolean valid(double current, double maximum) {
		return Double.isFinite(current) && Double.isFinite(maximum) && current >= 0 && maximum > 0;
	}
}
