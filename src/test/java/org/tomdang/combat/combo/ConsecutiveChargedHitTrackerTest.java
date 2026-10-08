package org.tomdang.combat.combo;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.PlayerCombatHitContext;
import org.tomdang.combat.attackspeed.AttackReadinessCalculation;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsecutiveChargedHitTrackerTest {
	private final AtomicLong tick = new AtomicLong(100);
	private final ConsecutiveChargedHitTracker tracker = new ConsecutiveChargedHitTracker(
			new ComboTimingCalculator(new ComboTimingConfiguration(20, 6, 42)), tick::get);

	@Test
	void firstFullyChargedHitStartsComboAndSubsequentHitContinuesIt() {
		UUID playerId = UUID.randomUUID();
		UUID targetId = UUID.randomUUID();
		ChargedHitComboProgress firstHit = tracker.recordHit(
				context(playerId, targetId, true, 40)).orElseThrow();
		tick.set(160);
		ChargedHitComboProgress progress = tracker.recordHit(
				context(playerId, targetId, true, 40)).orElseThrow();

		assertEquals(1, firstHit.completedHits());
		assertEquals(2, progress.completedHits());
		assertEquals(targetId, progress.targetId());
		assertEquals(160, progress.lastHitTick());
	}

	@Test
	void hitAfterCalculatedMaximumGapRestartsAtOne() {
		UUID playerId = UUID.randomUUID();
		UUID targetId = UUID.randomUUID();
		tracker.onHit(context(playerId, targetId, true, 40));
		tick.set(161);
		tracker.onHit(context(playerId, targetId, true, 40));

		assertEquals(1, tracker.getProgress(playerId).orElseThrow().completedHits());
	}

	@Test
	void changingTargetRestartsAtOne() {
		UUID playerId = UUID.randomUUID();
		tracker.onHit(context(playerId, UUID.randomUUID(), true, 40));
		tick.incrementAndGet();
		UUID newTarget = UUID.randomUUID();
		tracker.onHit(context(playerId, newTarget, true, 40));

		ChargedHitComboProgress progress = tracker.getProgress(playerId).orElseThrow();
		assertEquals(1, progress.completedHits());
		assertEquals(newTarget, progress.targetId());
	}

	@Test
	void underchargedHitClearsExistingCombo() {
		UUID playerId = UUID.randomUUID();
		UUID targetId = UUID.randomUUID();
		tracker.onHit(context(playerId, targetId, true, 40));
		Optional<ChargedHitComboProgress> result = tracker.recordHit(
				context(playerId, targetId, false, 40));

		assertFalse(result.isPresent());
		assertFalse(tracker.getProgress(playerId).isPresent());
	}

	@Test
	void queriedProgressExpiresAfterItsCalculatedMaximumGap() {
		UUID playerId = UUID.randomUUID();
		tracker.onHit(context(playerId, UUID.randomUUID(), true, 40));
		tick.set(160);
		assertEquals(1, tracker.getProgress(playerId).orElseThrow().completedHits());

		tick.set(161);
		assertFalse(tracker.getProgress(playerId).isPresent());
	}

	@Test
	void clearRemovesProgressAndNullInputsAreRejected() {
		UUID playerId = UUID.randomUUID();
		tracker.onHit(context(playerId, UUID.randomUUID(), true, 40));
		tracker.clear(playerId);

		assertFalse(tracker.getProgress(playerId).isPresent());
		assertThrows(IllegalArgumentException.class, () -> new ConsecutiveChargedHitTracker(null, tick::get));
		assertThrows(IllegalArgumentException.class, () -> new ConsecutiveChargedHitTracker(
				new ComboTimingCalculator(new ComboTimingConfiguration(20, 6, 42)), null));
		assertThrows(IllegalArgumentException.class, () -> tracker.onHit(null));
		assertThrows(IllegalArgumentException.class, () -> tracker.getProgress(null));
		assertThrows(IllegalArgumentException.class, () -> tracker.clear(null));
	}

	private PlayerCombatHitContext context(UUID playerId, UUID targetId, boolean fullyCharged,
	                                      long effectiveRecoveryTicks) {
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(playerId);
		LivingEntity target = mock(LivingEntity.class);
		when(target.getUniqueId()).thenReturn(targetId);
		PlayerCombatHitContext context = mock(PlayerCombatHitContext.class);
		when(context.attacker()).thenReturn(player);
		when(context.target()).thenReturn(target);
		when(context.fullyCharged()).thenReturn(fullyCharged);
		when(context.item()).thenReturn(Optional.empty());
		when(context.readiness()).thenReturn(new AttackReadinessCalculation(
				fullyCharged ? 1 : 0.5, fullyCharged, effectiveRecoveryTicks));
		return context;
	}
}
