package org.tomdang.combat.hit;

import org.tomdang.combat.PlayerCombatHitContext;

@FunctionalInterface
public interface PlayerCombatHitObserver {
	void onHit(PlayerCombatHitContext context);
}
