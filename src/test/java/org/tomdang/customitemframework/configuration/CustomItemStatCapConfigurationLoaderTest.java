package org.tomdang.customitemframework.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomItemStatCapConfigurationLoaderTest {

	private final CustomItemStatCapConfigurationLoader loader = new CustomItemStatCapConfigurationLoader();

	@Test
	void absentSectionProducesEmptyModifiers() {
		assertTrue(loader.load(section("material: STONE\n"), "TEST_ITEM").asMap().isEmpty());
	}

	@Test
	void loadsKnownCapModifiers() {
		CustomItemStatCapModifiers modifiers = loader.load(section("""
				stat-cap-modifiers:
				  attack-speed: 50
				  maxEnergy: -25.5
				"""), "TEST_ITEM");

		assertEquals(50, modifiers.asMap().get(PlayerStatType.ATTACK_SPEED));
		assertEquals(-25.5, modifiers.asMap().get(PlayerStatType.MAX_ENERGY));
	}

	@Test
	void unknownAndNonNumericModifiersAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> loader.load(
				section("stat-cap-modifiers:\n  mystery-cap: 5\n"), "TEST_ITEM"));
		assertThrows(IllegalArgumentException.class, () -> loader.load(
				section("stat-cap-modifiers:\n  attack-speed: fast\n"), "TEST_ITEM"));
	}

	@Test
	void scalarSectionIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> loader.load(
				section("stat-cap-modifiers: attack-speed\n"), "TEST_ITEM"));
	}

	private ConfigurationSection section(String yaml) {
		return YamlConfiguration.loadConfiguration(new StringReader(yaml));
	}
}
