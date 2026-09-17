package org.tomdang.customitemframework.combat;

import org.junit.jupiter.api.Test;
import org.tomdang.combat.eligibility.AttackCapability;

import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomItemCombatProfileTest {
	@Test
	void olderThreeArgumentConstructionHasNoAttackCapabilities() {
		CustomItemCombatProfile profile = new CustomItemCombatProfile(
				Optional.empty(), Optional.empty(), OptionalLong.empty());

		assertTrue(profile.attackCapabilities().isEmpty());
	}

	@Test
	void copiesAttackCapabilitiesSoItemDefinitionCannotChangeLater() {
		Set<AttackCapability> supplied = new HashSet<>();
		CustomItemCombatProfile profile = new CustomItemCombatProfile(
				Optional.empty(), Optional.empty(), OptionalLong.empty(), supplied);
		supplied.add(AttackCapability.JELLYFISH_HUNTING);

		assertTrue(profile.attackCapabilities().isEmpty());
		assertThrows(UnsupportedOperationException.class,
				() -> profile.attackCapabilities().add(AttackCapability.JELLYFISH_HUNTING));
	}

	@Test
	void rejectsNullAttackCapabilities() {
		assertThrows(IllegalArgumentException.class,
				() -> new CustomItemCombatProfile(Optional.empty(), Optional.empty(), OptionalLong.empty(), null));
	}
}
