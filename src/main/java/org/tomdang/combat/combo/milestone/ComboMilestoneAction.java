package org.tomdang.combat.combo.milestone;

import org.tomdang.combat.PlayerCombatHitContext;
import org.tomdang.combat.combo.ChargedHitComboProgress;

@FunctionalInterface
public interface ComboMilestoneAction {
	void execute(PlayerCombatHitContext context, ChargedHitComboProgress progress);
}
