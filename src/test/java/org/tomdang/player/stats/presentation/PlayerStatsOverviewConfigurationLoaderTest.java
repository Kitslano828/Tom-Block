package org.tomdang.player.stats.presentation;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatCategory;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class PlayerStatsOverviewConfigurationLoaderTest {
	private final PlayerStatsOverviewConfigurationLoader loader = new PlayerStatsOverviewConfigurationLoader();

	@Test
	void loadsLayoutAndPreservesCategoryOrder() {
		PlayerStatsOverviewConfiguration result = load(validConfiguration());
		assertAll(
				() -> assertEquals("Your Stats", result.title()),
				() -> assertEquals(54, result.size()),
				() -> assertEquals(Material.BLACK_STAINED_GLASS_PANE, result.borderMaterial()),
				() -> assertEquals(49, result.closeSlot()),
				() -> assertEquals(Material.BARRIER, result.closeMaterial()),
				() -> assertEquals(PlayerStatCategory.COMBAT, result.categories().getFirst().category()),
				() -> assertEquals(PlayerStatCategory.UTILITY, result.categories().getLast().category()),
				() -> assertEquals(21, result.categories().getFirst().slot()),
				() -> assertEquals(Material.BLACK_STAINED_GLASS_PANE, result.categories().getFirst().borderMaterial()),
				() -> assertTrue(result.categories().getFirst().visible())
		);
	}

	@Test
	void rejectsBadDimensionsSlotsAndMissingCategories() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load(validConfiguration().replace("size: 54", "size: 27"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validConfiguration().replace("slot: 21", "slot: 49"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validConfiguration().replace("    UTILITY:\n" + category("Utility", "CLOCK", 32), "")))
		);
	}

	@Test
	void rejectsInvalidPresentationValues() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> loader.load(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validConfiguration().replace("#FF5555", "red"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validConfiguration().replace("IRON_SWORD", "NOT_REAL"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validConfiguration().replace("COMBAT:", "MAGIC:")))
		);
	}

	private PlayerStatsOverviewConfiguration load(String yaml) {
		return loader.load(new StringReader(yaml));
	}

	private String validConfiguration() {
		return """
				stats-overview:
				  title: "Your Stats"
				  size: 54
				  border-material: BLACK_STAINED_GLASS_PANE
				  close-button:
				    material: BARRIER
				    name: "Close"
				    color: "#FF5555"
				    slot: 49
				  categories:
				    COMBAT:
				""" + category("Combat", "IRON_SWORD", 21) + """
				    MINING:
				""" + category("Mining", "DIAMOND_PICKAXE", 22) + """
				    FORAGING:
				""" + category("Foraging", "DIAMOND_AXE", 23) + """
				    FISHING:
				""" + category("Fishing", "FISHING_ROD", 30) + """
				    FARMING:
				""" + category("Farming", "GOLDEN_HOE", 31) + """
				    UTILITY:
				""" + category("Utility", "CLOCK", 32);
	}

	private static String category(String name, String material, int slot) {
		return """
				      name: "%s Stats"
				      color: "#FF5555"
				      description: "%s description"
				      material: %s
				      border-material: BLACK_STAINED_GLASS_PANE
				      slot: %d
				""".formatted(name, name, material, slot);
	}
}
