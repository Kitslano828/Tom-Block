package org.tomdang.combat.combo;

public class ComboTimingCalculator {
	private final ComboTimingConfiguration configuration;

	public ComboTimingCalculator(ComboTimingConfiguration configuration) {
		if (configuration == null) throw new IllegalArgumentException("configuration cannot be null");
		this.configuration = configuration;
	}

	public ComboTimingCalculation calculate(int completedHits, long effectiveRecoveryTicks) {
		if (completedHits < 1) throw new IllegalArgumentException("completedHits must be at least 1");
		if (effectiveRecoveryTicks < 0) {
			throw new IllegalArgumentException("effectiveRecoveryTicks cannot be negative");
		}

		int boundedHits = Math.min(completedHits, configuration.hitsToMinimumGrace());
		double progress = (double) (boundedHits - 1) / (configuration.hitsToMinimumGrace() - 1);
		double graceRange = configuration.baseGraceTicks() - configuration.minimumGraceTicks();
		long graceTicks = (long) Math.ceil(configuration.baseGraceTicks() - (graceRange * progress));

		return new ComboTimingCalculation(graceTicks, Math.addExact(effectiveRecoveryTicks, graceTicks));
	}
}
