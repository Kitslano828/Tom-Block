package org.tomdang.combat.eligibility;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttackSourceTest {
	@Test
	void basicAttackCanCarryJellyfishHuntingCapability() {
		AttackSource source = new AttackSource(AttackDelivery.BASIC_ATTACK,
				Set.of(AttackCapability.JELLYFISH_HUNTING));

		assertEquals(AttackDelivery.BASIC_ATTACK, source.delivery());
		assertTrue(source.attackCapabilities().contains(AttackCapability.JELLYFISH_HUNTING));
	}

	@Test
	void fistAttackCanHaveNoSpecialCapabilities() {
		AttackSource source = new AttackSource(AttackDelivery.BASIC_ATTACK, Set.of());

		assertTrue(source.attackCapabilities().isEmpty());
		assertFalse(source.attackCapabilities().contains(AttackCapability.JELLYFISH_HUNTING));
	}

	@Test
	void abilityIsASeparateDeliveryKind() {
		AttackSource source = new AttackSource(AttackDelivery.ABILITY,
				Set.of(AttackCapability.JELLYFISH_HUNTING));

		assertEquals(AttackDelivery.ABILITY, source.delivery());
		assertTrue(source.attackCapabilities().contains(AttackCapability.JELLYFISH_HUNTING));
	}

	@Test
	void makesDefensiveCopyOfCapabilities() {
		Set<AttackCapability> supplied = new HashSet<>();
		AttackSource source = new AttackSource(AttackDelivery.BASIC_ATTACK, supplied);
		supplied.add(AttackCapability.JELLYFISH_HUNTING);

		assertTrue(source.attackCapabilities().isEmpty());
		assertThrows(UnsupportedOperationException.class,
				() -> source.attackCapabilities().add(AttackCapability.JELLYFISH_HUNTING));
	}

	@Test
	void rejectsMissingDeliveryOrCapabilities() {
		assertThrows(IllegalArgumentException.class, () -> new AttackSource(null, Set.of()));
		assertThrows(IllegalArgumentException.class,
				() -> new AttackSource(AttackDelivery.BASIC_ATTACK, null));
	}
}
