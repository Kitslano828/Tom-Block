package org.tomdang.combat.combo;

import java.util.Optional;
import java.util.UUID;

public record ChargedHitComboProgress(
		UUID targetId,
		int completedHits,
		long lastHitTick,
		long effectiveRecoveryTicks,
		Optional<String> itemId
) {
	public ChargedHitComboProgress {
		if (targetId == null) throw new IllegalArgumentException("targetId cannot be null");
		if (completedHits < 1) throw new IllegalArgumentException("completedHits must be at least 1");
		if (lastHitTick < 0) throw new IllegalArgumentException("lastHitTick cannot be negative");
		if (effectiveRecoveryTicks < 0) {
			throw new IllegalArgumentException("effectiveRecoveryTicks cannot be negative");
		}
		if (itemId == null) throw new IllegalArgumentException("itemId cannot be null");
	}
}
