package org.tomdang.combat.configuration;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CombatTimingConfigurationLoaderTest {

	private final CombatTimingConfigurationLoader loader = new CombatTimingConfigurationLoader();

	@Test
	void loadsDefaultBasicAttackRecovery() {
		CombatTimingConfiguration configuration = loader.load(new StringReader("""
				combat:
				  default-basic-attack-recovery-ticks: 10
				"""));

		assertEquals(10, configuration.defaultBasicAttackRecoveryTicks());
	}

	@Test
	void rejectsMissingMalformedAndNegativeValues() {
		assertThrows(IllegalArgumentException.class, () -> loader.load(null));
		assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("other: {}")));
		assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("combat: {}")));
		assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("""
				combat:
				  default-basic-attack-recovery-ticks: 1.5
				""")));
		assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("""
				combat:
				  default-basic-attack-recovery-ticks: -1
				""")));
	}
}
