package org.tomdang.customitemframework.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomItemStatConfigurationLoaderTest {

	private final CustomItemStatConfigurationLoader loader = new CustomItemStatConfigurationLoader();

	@Test
	void absentStatsSectionProducesEmptyModifiers() {
		CustomItemStatModifiers modifiers = loader.load(section("material: STONE\n"), "TEST_ITEM");

		assertTrue(modifiers.asMap().isEmpty());
	}

	@Test
	void loadsEveryKnownStatWithoutDependingOnItemCategory() {
		CustomItemStatModifiers modifiers = loader.load(section("""
				stats:
				  damage: 12.5
				  mining-fortune: 7
				  maxEnergy: -5
				"""), "TEST_ITEM");

		assertEquals(12.5, modifiers.get(PlayerStatType.DAMAGE));
		assertEquals(7, modifiers.get(PlayerStatType.MINING_FORTUNE));
		assertEquals(-5, modifiers.get(PlayerStatType.MAX_ENERGY));
	}

	@Test
	void unknownStatIsRejectedWithItemContext() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
				loader.load(section("stats:\n  mystery-power: 5\n"), "TEST_ITEM"));

		assertTrue(exception.getMessage().contains("TEST_ITEM"));
		assertTrue(exception.getMessage().contains("mystery-power"));
	}

	@Test
	void nonNumericStatIsRejected() {
		assertThrows(IllegalArgumentException.class, () ->
				loader.load(section("stats:\n  damage: powerful\n"), "TEST_ITEM"));
	}

	@Test
	void scalarStatsValueIsRejected() {
		assertThrows(IllegalArgumentException.class, () ->
				loader.load(section("stats: damage\n"), "TEST_ITEM"));
	}

	private ConfigurationSection section(String yaml) {
		return YamlConfiguration.loadConfiguration(new StringReader(yaml));
	}
}
