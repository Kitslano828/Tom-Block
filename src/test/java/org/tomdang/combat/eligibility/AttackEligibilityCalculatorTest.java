package org.tomdang.combat.eligibility;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttackEligibilityCalculatorTest {
	private final AttackEligibilityCalculator calculator = new AttackEligibilityCalculator();
	private final AttackSource fist = new AttackSource(AttackDelivery.BASIC_ATTACK, Set.of());
	private final AttackSource net = new AttackSource(AttackDelivery.BASIC_ATTACK,
			Set.of(AttackCapability.JELLYFISH_HUNTING));

	@Test
	void unrestrictedRulePreservesExistingCombatBehavior() {
		AttackEligibilityRule unrestricted = new AttackEligibilityRule(Set.of());

		assertTrue(calculator.canDamage(unrestricted, fist));
		assertTrue(calculator.canDamage(unrestricted, net));
	}

	@Test
	void jellyfishRuleRejectsOrdinaryHitAndAcceptsNetHit() {
		AttackEligibilityRule jellyfish = new AttackEligibilityRule(Set.of(AttackCapability.JELLYFISH_HUNTING));

		assertFalse(calculator.canDamage(jellyfish, fist));
		assertTrue(calculator.canDamage(jellyfish, net));
	}

	@Test
	void abilityMustAlsoCarryRequiredCapability() {
		AttackEligibilityRule jellyfish = new AttackEligibilityRule(Set.of(AttackCapability.JELLYFISH_HUNTING));
		AttackSource ordinaryAbility = new AttackSource(AttackDelivery.ABILITY, Set.of());
		AttackSource huntingAbility = new AttackSource(AttackDelivery.ABILITY,
				Set.of(AttackCapability.JELLYFISH_HUNTING));

		assertFalse(calculator.canDamage(jellyfish, ordinaryAbility));
		assertTrue(calculator.canDamage(jellyfish, huntingAbility));
	}

	@Test
	void ruleMakesDefensiveCopyOfAcceptedCapabilities() {
		Set<AttackCapability> supplied = new HashSet<>();
		AttackEligibilityRule rule = new AttackEligibilityRule(supplied);
		supplied.add(AttackCapability.JELLYFISH_HUNTING);

		assertTrue(rule.acceptedCapabilities().isEmpty());
		assertThrows(UnsupportedOperationException.class,
				() -> rule.acceptedCapabilities().add(AttackCapability.JELLYFISH_HUNTING));
	}

	@Test
	void rejectsMissingInputs() {
		assertThrows(IllegalArgumentException.class, () -> new AttackEligibilityRule(null));
		assertThrows(IllegalArgumentException.class, () -> calculator.canDamage(null, fist));
		assertThrows(IllegalArgumentException.class,
				() -> calculator.canDamage(new AttackEligibilityRule(Set.of()), null));
	}
}
