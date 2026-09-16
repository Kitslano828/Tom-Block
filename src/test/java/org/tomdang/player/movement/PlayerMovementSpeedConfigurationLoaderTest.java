package org.tomdang.player.movement;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerMovementSpeedConfigurationLoaderTest {
	private final PlayerMovementSpeedConfigurationLoader loader = new PlayerMovementSpeedConfigurationLoader();

	@Test
	void loadsMovementConversionSettings() {
		PlayerMovementSpeedSettings settings = loader.load(new StringReader(validYaml()));

		assertEquals(100, settings.referenceStat());
		assertEquals(0.2, settings.referenceWalkSpeed());
		assertEquals(0, settings.minimumWalkSpeed());
		assertEquals(1, settings.maximumWalkSpeed());
	}

	@Test
	void rejectsMissingMalformedAndInvalidSettings() {
		assertThrows(IllegalArgumentException.class, () -> loader.load(null));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(new StringReader(validYaml().replace("  reference-stat: 100.0\n", ""))));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(new StringReader(validYaml().replace("reference-stat: 100.0", "reference-stat: fast"))));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(new StringReader(validYaml().replace("maximum-walk-speed: 1.0", "maximum-walk-speed: 2.0"))));
	}

	private String validYaml() {
		return """
				movement-speed:
				  reference-stat: 100.0
				  reference-walk-speed: 0.2
				  minimum-walk-speed: 0.0
				  maximum-walk-speed: 1.0
				""";
	}
}
