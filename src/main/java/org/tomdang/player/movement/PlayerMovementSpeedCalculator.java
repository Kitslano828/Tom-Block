package org.tomdang.player.movement;

public final class PlayerMovementSpeedCalculator {
	private final PlayerMovementSpeedSettings settings;

	public PlayerMovementSpeedCalculator(PlayerMovementSpeedSettings settings) {
		if (settings == null) throw new IllegalArgumentException("settings cannot be null");
		this.settings = settings;
	}

	public float calculate(double effectiveSpeedStat) {
		if (!Double.isFinite(effectiveSpeedStat)) throw new IllegalArgumentException("effectiveSpeedStat must be finite");
		double converted = settings.referenceWalkSpeed() * effectiveSpeedStat / settings.referenceStat();
		double clamped = Math.max(settings.minimumWalkSpeed(), Math.min(settings.maximumWalkSpeed(), converted));
		return (float) clamped;
	}

	public float defaultWalkSpeed() {
		return (float) settings.referenceWalkSpeed();
	}
}
