package org.tomdang.combat.combo;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComboTimingConfigurationLoaderTest {
	private final ComboTimingConfigurationLoader loader = new ComboTimingConfigurationLoader();

	@Test
	void loadsComboTimingValues() {
		ComboTimingConfiguration configuration = loader.load(new StringReader("""
				combat:
				  combo:
				    base-grace-ticks: 20
				    minimum-grace-ticks: 6
				    hits-to-minimum-grace: 42
				"""));

		assertEquals(new ComboTimingConfiguration(20, 6, 42), configuration);
	}

	@Test
	void rejectsMissingAndMalformedValues() {
		assertThrows(IllegalArgumentException.class, () -> loader.load(null));
		assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("combat: {}")));
		assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("""
				combat:
				  combo:
				    base-grace-ticks: 20
				    minimum-grace-ticks: 6
				    hits-to-minimum-grace: 4.2
				""")));
	}
}
