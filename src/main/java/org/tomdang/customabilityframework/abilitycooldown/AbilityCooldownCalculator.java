package org.tomdang.customabilityframework.abilitycooldown;

public class AbilityCooldownCalculator {

	public long calculate(long cooldownInTicks, double abilityHaste) {
		if (cooldownInTicks < 0) throw new IllegalArgumentException("Negative cooldowns are not allowed");
		if (!Double.isFinite(abilityHaste)) throw new IllegalArgumentException("abilityHaste must be finite");
		if (abilityHaste < 0) throw new IllegalArgumentException("Negative abilityHaste is not allowed");

		if (cooldownInTicks == 0) return 0;

		double finalCooldown = Math.ceil(cooldownInTicks / (1 + abilityHaste / 100));
		return Math.max(1, (long) finalCooldown);
	}

}
