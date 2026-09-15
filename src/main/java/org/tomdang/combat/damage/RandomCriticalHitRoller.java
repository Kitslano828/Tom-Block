package org.tomdang.combat.damage;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

public class RandomCriticalHitRoller implements CriticalHitRoller {
	private final DoubleSupplier randomValue;

	public RandomCriticalHitRoller() {
		this(() -> ThreadLocalRandom.current().nextDouble());
	}

	public RandomCriticalHitRoller(DoubleSupplier randomValue) {
		if (randomValue == null) throw new IllegalArgumentException("randomValue cannot be null");
		this.randomValue = randomValue;
	}

	@Override
	public boolean isCritical(double criticalChancePercent) {
		if (!Double.isFinite(criticalChancePercent)) throw new IllegalArgumentException("criticalChancePercent must be finite");
		if (criticalChancePercent <= 0) return false;
		if (criticalChancePercent >= 100) return true;
		double roll = randomValue.getAsDouble();
		if (!Double.isFinite(roll) || roll < 0 || roll >= 1) throw new IllegalStateException("random value must be in [0, 1)");
		return roll * 100 < criticalChancePercent;
	}
}
