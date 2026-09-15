package org.tomdang.combat.attackspeed;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerAttackReadinessServiceTest {
	@Test
	void firstAttackIsFullAndFollowingAttacksUseElapsedTime() {
		AtomicLong tick = new AtomicLong(100);
		PlayerAttackReadinessService service = service(tick);
		UUID playerId = UUID.randomUUID();

		assertTrue(service.consume(playerId, 20, 0).fullyCharged());
		tick.set(110);
		AttackReadinessCalculation partial = service.consume(playerId, 20, 0);
		assertEquals(0.5, partial.readiness());
		assertFalse(partial.fullyCharged());
		tick.set(130);
		assertTrue(service.consume(playerId, 20, 0).fullyCharged());
	}

	@Test
	void everyAttemptRestartsRecoveryAndClearRestoresAFullFirstAttack() {
		AtomicLong tick = new AtomicLong(0);
		PlayerAttackReadinessService service = service(tick);
		UUID playerId = UUID.randomUUID();
		service.consume(playerId, 20, 0);
		tick.set(10);
		assertEquals(0.5, service.consume(playerId, 20, 0).readiness());
		tick.set(15);
		assertEquals(0.25, service.consume(playerId, 20, 0).readiness());
		service.clear(playerId);
		assertTrue(service.consume(playerId, 20, 0).fullyCharged());
	}

	@Test
	void rejectsNulls() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerAttackReadinessService(null, () -> 0));
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerAttackReadinessService(new AttackReadinessCalculator(), null));
		assertThrows(IllegalArgumentException.class, () -> service(new AtomicLong()).consume(null, 20, 100));
	}

	private PlayerAttackReadinessService service(AtomicLong tick) {
		return new PlayerAttackReadinessService(new AttackReadinessCalculator(), tick::get);
	}
}
