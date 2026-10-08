package org.tomdang.combat.combo.milestone;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.PlayerCombatHitContext;
import org.tomdang.combat.combo.ChargedHitComboProgress;
import org.tomdang.combat.combo.ConsecutiveChargedHitTracker;
import org.tomdang.combat.combo.requirement.ComboRequirement;
import org.tomdang.combat.combo.requirement.ComboRequirementEvaluator;
import org.tomdang.combat.combo.requirement.ComboRequirementResult;
import org.tomdang.combat.combo.requirement.ComboRequirementStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ComboMilestoneObserverTest {
	@Test
	void executesAndConsumesComboExactlyAtRequiredHitCount() {
		Fixture fixture = fixture(3, ComboRequirementStatus.SATISFIED, true);

		fixture.observer().onHit(fixture.context());

		verify(fixture.action()).execute(fixture.context(), fixture.progress());
		verify(fixture.tracker()).clear(fixture.playerId());
	}

	@Test
	void canTriggerWithoutConsumingCombo() {
		Fixture fixture = fixture(3, ComboRequirementStatus.SATISFIED, false);

		fixture.observer().onHit(fixture.context());

		verify(fixture.action()).execute(fixture.context(), fixture.progress());
		verify(fixture.tracker(), never()).clear(fixture.playerId());
	}

	@Test
	void doesNotRetriggerAfterRequiredHitCount() {
		Fixture fixture = fixture(4, ComboRequirementStatus.SATISFIED, true);

		fixture.observer().onHit(fixture.context());

		verify(fixture.action(), never()).execute(fixture.context(), fixture.progress());
		verify(fixture.tracker(), never()).clear(fixture.playerId());
	}

	@Test
	void doesNotTriggerWhenRestrictionsAreNotSatisfied() {
		Fixture fixture = fixture(3, ComboRequirementStatus.ITEM_MISMATCH, true);

		fixture.observer().onHit(fixture.context());

		verify(fixture.action(), never()).execute(fixture.context(), fixture.progress());
		verify(fixture.tracker(), never()).clear(fixture.playerId());
	}

	@Test
	void rejectsNullCollaboratorsAndContext() {
		ConsecutiveChargedHitTracker tracker = mock(ConsecutiveChargedHitTracker.class);
		ComboRequirementEvaluator evaluator = mock(ComboRequirementEvaluator.class);
		ComboRequirement requirement = ComboRequirement.hits(3);
		ComboMilestoneAction action = mock(ComboMilestoneAction.class);
		assertThrows(IllegalArgumentException.class,
				() -> new ComboMilestoneObserver(null, evaluator, requirement, action, true));
		assertThrows(IllegalArgumentException.class,
				() -> new ComboMilestoneObserver(tracker, null, requirement, action, true));
		assertThrows(IllegalArgumentException.class,
				() -> new ComboMilestoneObserver(tracker, evaluator, null, action, true));
		assertThrows(IllegalArgumentException.class,
				() -> new ComboMilestoneObserver(tracker, evaluator, requirement, null, true));
		assertThrows(IllegalArgumentException.class,
				() -> new ComboMilestoneObserver(tracker, evaluator, requirement, action, true).onHit(null));
	}

	private Fixture fixture(int completedHits, ComboRequirementStatus status, boolean consume) {
		UUID playerId = UUID.randomUUID();
		UUID targetId = UUID.randomUUID();
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(playerId);
		PlayerCombatHitContext context = mock(PlayerCombatHitContext.class);
		when(context.attacker()).thenReturn(player);
		ChargedHitComboProgress progress = new ChargedHitComboProgress(
				targetId, completedHits, 100, 40, Optional.of("SUPER_PICKAXE"));
		ConsecutiveChargedHitTracker tracker = mock(ConsecutiveChargedHitTracker.class);
		when(tracker.recordHit(context)).thenReturn(Optional.of(progress));
		ComboRequirement requirement = ComboRequirement.hits(3);
		ComboRequirementEvaluator evaluator = mock(ComboRequirementEvaluator.class);
		when(evaluator.evaluate(requirement, context, Optional.of(progress)))
				.thenReturn(new ComboRequirementResult(status));
		ComboMilestoneAction action = mock(ComboMilestoneAction.class);
		ComboMilestoneObserver observer = new ComboMilestoneObserver(
				tracker, evaluator, requirement, action, consume);
		return new Fixture(playerId, context, progress, tracker, action, observer);
	}

	private record Fixture(UUID playerId, PlayerCombatHitContext context,
	                       ChargedHitComboProgress progress,
	                       ConsecutiveChargedHitTracker tracker,
	                       ComboMilestoneAction action,
	                       ComboMilestoneObserver observer) {
	}
}
