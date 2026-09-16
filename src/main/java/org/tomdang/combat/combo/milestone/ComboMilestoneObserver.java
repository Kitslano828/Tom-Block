package org.tomdang.combat.combo.milestone;

import org.tomdang.combat.PlayerCombatHitContext;
import org.tomdang.combat.combo.ChargedHitComboProgress;
import org.tomdang.combat.combo.ConsecutiveChargedHitTracker;
import org.tomdang.combat.combo.requirement.ComboRequirement;
import org.tomdang.combat.combo.requirement.ComboRequirementEvaluator;
import org.tomdang.combat.hit.PlayerCombatHitObserver;

import java.util.Optional;
import java.util.UUID;

public class ComboMilestoneObserver implements PlayerCombatHitObserver {
	private final ConsecutiveChargedHitTracker tracker;
	private final ComboRequirementEvaluator evaluator;
	private final ComboRequirement requirement;
	private final ComboMilestoneAction action;
	private final boolean consumeOnTrigger;

	public ComboMilestoneObserver(ConsecutiveChargedHitTracker tracker,
	                              ComboRequirementEvaluator evaluator,
	                              ComboRequirement requirement,
	                              ComboMilestoneAction action,
	                              boolean consumeOnTrigger) {
		if (tracker == null) throw new IllegalArgumentException("tracker cannot be null");
		if (evaluator == null) throw new IllegalArgumentException("evaluator cannot be null");
		if (requirement == null) throw new IllegalArgumentException("requirement cannot be null");
		if (action == null) throw new IllegalArgumentException("action cannot be null");
		this.tracker = tracker;
		this.evaluator = evaluator;
		this.requirement = requirement;
		this.action = action;
		this.consumeOnTrigger = consumeOnTrigger;
	}

	@Override
	public void onHit(PlayerCombatHitContext context) {
		if (context == null) throw new IllegalArgumentException("context cannot be null");
		UUID playerId = context.attacker().getUniqueId();
		Optional<ChargedHitComboProgress> progress = tracker.getProgress(playerId);
		if (progress.isEmpty()) return;

		ChargedHitComboProgress current = progress.orElseThrow();
		if (current.completedHits() != requirement.requiredHits()) return;
		if (!evaluator.evaluate(requirement, context, progress).satisfied()) return;

		action.execute(context, current);
		if (consumeOnTrigger) tracker.clear(playerId);
	}
}
