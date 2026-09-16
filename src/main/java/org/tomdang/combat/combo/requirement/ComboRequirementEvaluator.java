package org.tomdang.combat.combo.requirement;

import org.tomdang.combat.PlayerCombatHitContext;
import org.tomdang.combat.combo.ChargedHitComboProgress;

import java.util.Optional;

public class ComboRequirementEvaluator {
	public ComboRequirementResult evaluate(ComboRequirement requirement,
	                                      PlayerCombatHitContext context,
	                                      Optional<ChargedHitComboProgress> progress) {
		if (requirement == null) throw new IllegalArgumentException("requirement cannot be null");
		if (context == null) throw new IllegalArgumentException("context cannot be null");
		if (progress == null) throw new IllegalArgumentException("progress cannot be null");
		if (progress.isEmpty()) return result(ComboRequirementStatus.NO_ACTIVE_COMBO);

		ChargedHitComboProgress current = progress.orElseThrow();
		if (!current.targetId().equals(context.target().getUniqueId())) {
			return result(ComboRequirementStatus.TARGET_MISMATCH);
		}
		if (current.completedHits() < requirement.requiredHits()) {
			return result(ComboRequirementStatus.INSUFFICIENT_HITS);
		}
		if (requirement.weightClass().isPresent()
				&& !requirement.weightClass().equals(context.weightClass())) {
			return result(ComboRequirementStatus.WEIGHT_CLASS_MISMATCH);
		}
		if (requirement.damageType().isPresent()
				&& !requirement.damageType().equals(context.damageType())) {
			return result(ComboRequirementStatus.DAMAGE_TYPE_MISMATCH);
		}
		if (requirement.itemId().isPresent() && !requirement.itemId().equals(current.itemId())) {
			return result(ComboRequirementStatus.ITEM_MISMATCH);
		}
		return result(ComboRequirementStatus.SATISFIED);
	}

	private ComboRequirementResult result(ComboRequirementStatus status) {
		return new ComboRequirementResult(status);
	}
}
