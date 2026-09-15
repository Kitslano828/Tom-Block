package org.tomdang.combat.attackspeed;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.LongSupplier;

public class PlayerAttackCooldownService {

	private final AttackCooldownCalculator calculator;
	private final LongSupplier currentTick;
	private final Map<UUID, Long> nextAttackTicks = new HashMap<>();

	public PlayerAttackCooldownService(AttackCooldownCalculator calculator, LongSupplier currentTick) {
		if (calculator == null) throw new IllegalArgumentException("calculator cannot be null");
		if (currentTick == null) throw new IllegalArgumentException("currentTick cannot be null");
		this.calculator = calculator;
		this.currentTick = currentTick;
	}

	public PlayerAttackCooldownResult tryStart(UUID playerId, long baseCooldownTicks, double attackSpeed) {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");

		OptionalLong effectiveCooldown = calculator.calculate(baseCooldownTicks, attackSpeed);
		if (effectiveCooldown.isEmpty()) return PlayerAttackCooldownResult.ATTACKS_DISABLED;

		long now = currentTick.getAsLong();
		long nextAttackTick = nextAttackTicks.getOrDefault(playerId, Long.MIN_VALUE);
		if (now < nextAttackTick) return PlayerAttackCooldownResult.ON_COOLDOWN;

		long cooldownTicks = effectiveCooldown.getAsLong();
		if (cooldownTicks == 0) {
			nextAttackTicks.remove(playerId);
		} else {
			try {
				nextAttackTicks.put(playerId, Math.addExact(now, cooldownTicks));
			} catch (ArithmeticException exception) {
				throw new IllegalStateException("attack cooldown expiration tick overflowed", exception);
			}
		}
		return PlayerAttackCooldownResult.STARTED;
	}

	public void clear(UUID playerId) {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		nextAttackTicks.remove(playerId);
	}
}
