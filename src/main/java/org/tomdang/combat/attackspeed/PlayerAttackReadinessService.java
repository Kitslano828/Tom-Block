package org.tomdang.combat.attackspeed;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

public class PlayerAttackReadinessService {
	private final AttackReadinessCalculator calculator;
	private final LongSupplier currentTick;
	private final Map<UUID, Long> lastAttackTicks = new HashMap<>();

	public PlayerAttackReadinessService(AttackReadinessCalculator calculator, LongSupplier currentTick) {
		if (calculator == null) throw new IllegalArgumentException("calculator cannot be null");
		if (currentTick == null) throw new IllegalArgumentException("currentTick cannot be null");
		this.calculator = calculator;
		this.currentTick = currentTick;
	}

	public AttackReadinessCalculation consume(UUID playerId, long baseRecoveryTicks, double attackSpeed) {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		long now = currentTick.getAsLong();
		Long previousAttackTick = lastAttackTicks.put(playerId, now);
		if (previousAttackTick == null) {
			AttackReadinessCalculation calculation = calculator.calculate(
					Long.MAX_VALUE, baseRecoveryTicks, attackSpeed);
			return calculation.readiness() == 0
					? calculation
					: new AttackReadinessCalculation(1, true, calculation.effectiveRecoveryTicks());
		}
		long elapsedTicks = now >= previousAttackTick ? now - previousAttackTick : 0;
		return calculator.calculate(elapsedTicks, baseRecoveryTicks, attackSpeed);
	}

	public void clear(UUID playerId) {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		lastAttackTicks.remove(playerId);
	}
}
