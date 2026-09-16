package org.tomdang.combat.combo.requirement;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComboRequirementTest {
	@Test
	void hitsFactoryCreatesUnrestrictedRequirement() {
		ComboRequirement requirement = ComboRequirement.hits(3);
		assertEquals(3, requirement.requiredHits());
		assertEquals(Optional.empty(), requirement.weightClass());
		assertEquals(Optional.empty(), requirement.damageType());
		assertEquals(Optional.empty(), requirement.itemId());
	}

	@Test
	void rejectsInvalidValues() {
		assertThrows(IllegalArgumentException.class, () -> ComboRequirement.hits(0));
		assertThrows(IllegalArgumentException.class,
				() -> new ComboRequirement(3, null, Optional.empty(), Optional.empty()));
		assertThrows(IllegalArgumentException.class,
				() -> new ComboRequirement(3, Optional.empty(), null, Optional.empty()));
		assertThrows(IllegalArgumentException.class,
				() -> new ComboRequirement(3, Optional.empty(), Optional.empty(), null));
		assertThrows(IllegalArgumentException.class,
				() -> new ComboRequirement(3, Optional.empty(), Optional.empty(), Optional.of(" ")));
	}
}
