package org.tomdang.combat.attackspeed;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerAttackCooldownServiceTest {

	@Test
	void blocksUntilTheCalculatedExpirationTick() {
		AtomicLong tick = new AtomicLong(100);
		PlayerAttackCooldownService service = new PlayerAttackCooldownService(
				new AttackCooldownCalculator(), tick::get
		);
		UUID playerId = UUID.randomUUID();

		assertEquals(PlayerAttackCooldownResult.STARTED, service.tryStart(playerId, 20, 200));
		tick.set(109);
		assertEquals(PlayerAttackCooldownResult.ON_COOLDOWN, service.tryStart(playerId, 20, 200));
		tick.set(110);
		assertEquals(PlayerAttackCooldownResult.STARTED, service.tryStart(playerId, 20, 200));
	}

	@Test
	void zeroSpeedDisablesAttacksAndZeroCooldownNeverBlocks() {
		PlayerAttackCooldownService service = new PlayerAttackCooldownService(
				new AttackCooldownCalculator(), () -> 100
		);
		UUID playerId = UUID.randomUUID();

		assertEquals(PlayerAttackCooldownResult.ATTACKS_DISABLED, service.tryStart(playerId, 20, 0));
		assertEquals(PlayerAttackCooldownResult.STARTED, service.tryStart(playerId, 0, 100));
		assertEquals(PlayerAttackCooldownResult.STARTED, service.tryStart(playerId, 0, 100));
	}

	@Test
	void clearRemovesAPlayersCooldown() {
		PlayerAttackCooldownService service = new PlayerAttackCooldownService(
				new AttackCooldownCalculator(), () -> 100
		);
		UUID playerId = UUID.randomUUID();
		service.tryStart(playerId, 20, 100);

		service.clear(playerId);

		assertEquals(PlayerAttackCooldownResult.STARTED, service.tryStart(playerId, 20, 100));
	}

	@Test
	void rejectsNullDependenciesAndPlayerIds() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerAttackCooldownService(null, () -> 0));
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerAttackCooldownService(new AttackCooldownCalculator(), null));
		PlayerAttackCooldownService service = new PlayerAttackCooldownService(
				new AttackCooldownCalculator(), () -> 0
		);
		assertThrows(IllegalArgumentException.class, () -> service.tryStart(null, 20, 100));
		assertThrows(IllegalArgumentException.class, () -> service.clear(null));
	}
}
