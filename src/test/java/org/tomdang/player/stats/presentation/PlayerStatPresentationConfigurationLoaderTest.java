package org.tomdang.player.stats.presentation;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatPresentationConfigurationLoaderTest {

	private final PlayerStatPresentationConfigurationLoader loader =
			new PlayerStatPresentationConfigurationLoader();

	@Test
	void parsesAllFieldsAndPreservesConfiguredOrder() {
		List<PlayerStatPresentation> presentations = load(completeConfiguration());

		PlayerStatPresentation first = presentations.getFirst();
		assertAll(
				() -> assertEquals(PlayerStatType.MINING_SPEED, first.statType()),
				() -> assertEquals("⛏", first.symbol()),
				() -> assertEquals(0xEDC140, first.color().value()),
				() -> assertEquals("Mining speed description", first.description()),
				() -> assertEquals(Material.DIAMOND_PICKAXE, first.menuMaterial()),
				() -> assertFalse(first.visible()),
				() -> assertEquals(PlayerStatType.MAX_HEALTH, presentations.get(1).statType()),
				() -> assertTrue(presentations.get(1).visible())
		);
	}

	@Test
	void rejectsNullReaderAndMissingOrEmptyRoot() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> loader.load(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> load("other: {}")),
				() -> assertThrows(IllegalArgumentException.class, () -> load("stat-presentations: {}"))
		);
	}

	@Test
	void rejectsUnknownAndDuplicateStatIds() {
		String unknown = completeConfiguration().replace("MINING_SPEED:", "LUCK:");
		String duplicateAlias = completeConfiguration().replace(
				"  MAX_ENERGY:\n" + entry("⚡", "#00AA00", "Energy", "EMERALD"),
				"  MAX-HEALTH:\n" + entry("❤", "#FF5555", "Health again", "RED_DYE")
		);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load(unknown)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(duplicateAlias))
		);
	}

	@Test
	void rejectsMissingPresentationsAndInvalidValues() {
		String missing = completeConfiguration().replace(
				"  ABILITY_HASTE:\n" + entry("⧖", "#B0AD0C", "Haste", "CLOCK"), "");
		String invalidColor = completeConfiguration().replace("#EDC140", "yellow");
		String invalidMaterial = completeConfiguration().replace("DIAMOND_PICKAXE", "NOT_A_MATERIAL");
		String malformedVisible = completeConfiguration().replace("    visible: false", "    visible: sometimes");
		String blankDescription = completeConfiguration().replace("    description: \"Mining speed description\"", "    description: '   '");

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load(missing)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(invalidColor)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(invalidMaterial)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(malformedVisible)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(blankDescription))
		);
	}

	private List<PlayerStatPresentation> load(String yaml) {
		return loader.load(new StringReader(yaml));
	}

	private String completeConfiguration() {
		return """
				stat-presentations:
				  MINING_SPEED:
				    symbol: "⛏"
				    color: "#EDC140"
				    description: "Mining speed description"
				    material: DIAMOND_PICKAXE
				    visible: false
				  MAX_HEALTH:
				""" + entry("❤", "#FF5555", "Health", "RED_DYE") + """
				  DEFENSE:
				""" + entry("❈", "#55FFFF", "Defense", "IRON_CHESTPLATE") + """
				  STRENGTH:
				""" + entry("❁", "#FF5555", "Strength", "BLAZE_POWDER") + """
				  DAMAGE:
				""" + entry("⚔", "#FF5555", "Damage", "IRON_SWORD") + """
				  MINING_FORTUNE:
				""" + entry("⛏❀", "#EDC140", "Fortune", "GOLDEN_PICKAXE") + """
				  MAX_ENERGY:
				""" + entry("⚡", "#00AA00", "Energy", "EMERALD") + """
				  ABILITY_HASTE:
				""" + entry("⧖", "#B0AD0C", "Haste", "CLOCK");
	}

	private static String entry(String symbol, String color, String description, String material) {
		return """
				    symbol: "%s"
				    color: "%s"
				    description: "%s"
				    material: %s
				""".formatted(symbol, color, description, material);
	}
}
