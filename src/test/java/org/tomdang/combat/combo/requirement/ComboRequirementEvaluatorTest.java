package org.tomdang.combat.combo.requirement;

import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.PlayerCombatHitContext;
import org.tomdang.combat.combo.ChargedHitComboProgress;
import org.tomdang.customitemframework.combat.CombatDamageType;
import org.tomdang.customitemframework.combat.CombatWeightClass;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComboRequirementEvaluatorTest {
	private final ComboRequirementEvaluator evaluator = new ComboRequirementEvaluator();

	@Test
	void satisfiesMatchingRestrictedCombo() {
		UUID targetId = UUID.randomUUID();
		ComboRequirement requirement = new ComboRequirement(3,
				Optional.of(CombatWeightClass.HEAVY), Optional.of(CombatDamageType.BLUNT),
				Optional.of("SUPER_PICKAXE"));

		ComboRequirementResult result = evaluator.evaluate(requirement,
				context(targetId, CombatWeightClass.HEAVY, CombatDamageType.BLUNT),
				Optional.of(progress(targetId, 3, "SUPER_PICKAXE")));

		assertTrue(result.satisfied());
		assertEquals(ComboRequirementStatus.SATISFIED, result.status());
	}

	@Test
	void unrestrictedRequirementOnlyChecksTargetAndHitCount() {
		UUID targetId = UUID.randomUUID();
		ComboRequirementResult result = evaluator.evaluate(ComboRequirement.hits(2),
				context(targetId, null, null), Optional.of(progress(targetId, 2, null)));

		assertTrue(result.satisfied());
	}

	@Test
	void reportsEachUnsatisfiedReason() {
		UUID targetId = UUID.randomUUID();
		PlayerCombatHitContext context = context(targetId, CombatWeightClass.HEAVY, CombatDamageType.BLUNT);
		assertStatus(ComboRequirementStatus.NO_ACTIVE_COMBO,
				evaluator.evaluate(ComboRequirement.hits(1), context, Optional.empty()));
		assertStatus(ComboRequirementStatus.TARGET_MISMATCH,
				evaluator.evaluate(ComboRequirement.hits(1), context,
						Optional.of(progress(UUID.randomUUID(), 3, "SUPER_PICKAXE"))));
		assertStatus(ComboRequirementStatus.INSUFFICIENT_HITS,
				evaluator.evaluate(ComboRequirement.hits(4), context,
						Optional.of(progress(targetId, 3, "SUPER_PICKAXE"))));
		assertStatus(ComboRequirementStatus.WEIGHT_CLASS_MISMATCH,
				evaluator.evaluate(new ComboRequirement(3, Optional.of(CombatWeightClass.LIGHT),
						Optional.empty(), Optional.empty()), context,
						Optional.of(progress(targetId, 3, "SUPER_PICKAXE"))));
		assertStatus(ComboRequirementStatus.DAMAGE_TYPE_MISMATCH,
				evaluator.evaluate(new ComboRequirement(3, Optional.empty(),
						Optional.of(CombatDamageType.PIERCING), Optional.empty()), context,
						Optional.of(progress(targetId, 3, "SUPER_PICKAXE"))));
		assertStatus(ComboRequirementStatus.ITEM_MISMATCH,
				evaluator.evaluate(new ComboRequirement(3, Optional.empty(), Optional.empty(),
						Optional.of("OTHER_ITEM")), context,
						Optional.of(progress(targetId, 3, "SUPER_PICKAXE"))));
	}

	@Test
	void rejectsNullInputs() {
		PlayerCombatHitContext context = context(UUID.randomUUID(), null, null);
		assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(null, context, Optional.empty()));
		assertThrows(IllegalArgumentException.class,
				() -> evaluator.evaluate(ComboRequirement.hits(1), null, Optional.empty()));
		assertThrows(IllegalArgumentException.class,
				() -> evaluator.evaluate(ComboRequirement.hits(1), context, null));
		assertThrows(IllegalArgumentException.class, () -> new ComboRequirementResult(null));
	}

	private void assertStatus(ComboRequirementStatus expected, ComboRequirementResult result) {
		assertEquals(expected, result.status());
		assertFalse(result.satisfied());
	}

	private PlayerCombatHitContext context(UUID targetId, CombatWeightClass weightClass,
	                                       CombatDamageType damageType) {
		LivingEntity target = mock(LivingEntity.class);
		when(target.getUniqueId()).thenReturn(targetId);
		PlayerCombatHitContext context = mock(PlayerCombatHitContext.class);
		when(context.target()).thenReturn(target);
		when(context.weightClass()).thenReturn(Optional.ofNullable(weightClass));
		when(context.damageType()).thenReturn(Optional.ofNullable(damageType));
		return context;
	}

	private ChargedHitComboProgress progress(UUID targetId, int hits, String itemId) {
		return new ChargedHitComboProgress(targetId, hits, 100, 40, Optional.ofNullable(itemId));
	}
}
