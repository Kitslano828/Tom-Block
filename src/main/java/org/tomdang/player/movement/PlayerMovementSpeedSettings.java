package org.tomdang.player.movement;

public record PlayerMovementSpeedSettings(
		double referenceStat,
		double referenceWalkSpeed,
		double minimumWalkSpeed,
		double maximumWalkSpeed
) {
	public PlayerMovementSpeedSettings {
		if (!Double.isFinite(referenceStat) || referenceStat <= 0) {
			throw new IllegalArgumentException("referenceStat must be finite and positive");
		}
		if (!Double.isFinite(referenceWalkSpeed) || referenceWalkSpeed < 0) {
			throw new IllegalArgumentException("referenceWalkSpeed must be finite and non-negative");
		}
		if (!Double.isFinite(minimumWalkSpeed) || minimumWalkSpeed < 0) {
			throw new IllegalArgumentException("minimumWalkSpeed must be finite and non-negative");
		}
		if (!Double.isFinite(maximumWalkSpeed) || maximumWalkSpeed > 1 || maximumWalkSpeed < minimumWalkSpeed) {
			throw new IllegalArgumentException("maximumWalkSpeed must be between minimumWalkSpeed and 1");
		}
		if (referenceWalkSpeed < minimumWalkSpeed || referenceWalkSpeed > maximumWalkSpeed) {
			throw new IllegalArgumentException("referenceWalkSpeed must be within the configured walk-speed range");
		}
	}
}
