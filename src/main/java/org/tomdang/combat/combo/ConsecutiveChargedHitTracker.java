package org.tomdang.combat.combo;

import org.tomdang.combat.PlayerCombatHitContext;
import org.tomdang.combat.hit.PlayerCombatHitObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;

public class ConsecutiveChargedHitTracker implements PlayerCombatHitObserver {
	private final ComboTimingCalculator timingCalculator;
	private final LongSupplier currentTick;
	private final Map<UUID, ChargedHitComboProgress> progressByPlayer = new HashMap<>();

	public ConsecutiveChargedHitTracker(ComboTimingCalculator timingCalculator, LongSupplier currentTick) {
		if (timingCalculator == null) throw new IllegalArgumentException("timingCalculator cannot be null");
		if (currentTick == null) throw new IllegalArgumentException("currentTick cannot be null");
		this.timingCalculator = timingCalculator;
		this.currentTick = currentTick;
	}

	@Override
	public void onHit(PlayerCombatHitContext context) {
		recordHit(context);
	}

	public Optional<ChargedHitComboProgress> getProgress(UUID playerId) {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		ChargedHitComboProgress progress = progressByPlayer.get(playerId);
		if (progress == null) return Optional.empty();
		long now = currentTick.getAsLong();
		if (!continuesCombo(progress, progress.targetId(), now)) {
			progressByPlayer.remove(playerId);
			return Optional.empty();
		}
		return Optional.of(progress);
	}

	public void clear(UUID playerId) {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		progressByPlayer.remove(playerId);
	}

	private boolean continuesCombo(ChargedHitComboProgress previous, UUID targetId, long now) {
		if (previous == null || !previous.targetId().equals(targetId) || now < previous.lastHitTick()) {
			return false;
		}
		long elapsedTicks = now - previous.lastHitTick();
		long maximumGapTicks = timingCalculator.calculate(
				previous.completedHits(), previous.effectiveRecoveryTicks()).maximumGapTicks();
		return elapsedTicks <= maximumGapTicks;
	}

	public Optional<ChargedHitComboProgress> recordHit(PlayerCombatHitContext context) {
		if (context == null) throw new IllegalArgumentException("context cannot be null");
		UUID playerId = context.attacker().getUniqueId();
		if (!context.fullyCharged()) {
			clear(playerId);
			return Optional.empty();
		}

		long now = currentTick.getAsLong();
		if (now < 0) throw new IllegalStateException("current tick cannot be negative");
		UUID targetId = context.target().getUniqueId();
		ChargedHitComboProgress previous = progressByPlayer.get(playerId);
		int completedHits = continuesCombo(previous, targetId, now)
				? Math.addExact(previous.completedHits(), 1)
				: 1;

		Optional<String> itemId = context.item().map(item -> item.getId());
		ChargedHitComboProgress updatedProgress = new ChargedHitComboProgress(
				targetId,
				completedHits,
				now,
				context.readiness().effectiveRecoveryTicks(),
				itemId
		);
		progressByPlayer.put(playerId, updatedProgress);
		return Optional.of(updatedProgress);
	}
}
