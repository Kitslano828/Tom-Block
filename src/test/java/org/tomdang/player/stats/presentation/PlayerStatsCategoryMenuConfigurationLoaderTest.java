package org.tomdang.player.stats.presentation;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class PlayerStatsCategoryMenuConfigurationLoaderTest {
	private final PlayerStatsCategoryMenuConfigurationLoader loader = new PlayerStatsCategoryMenuConfigurationLoader();

	@Test void loadsNavigationAndTitleConfiguration() {
		PlayerStatsCategoryMenuConfiguration result = load(valid());
		assertAll(
				() -> assertEquals(54, result.size()),
				() -> assertEquals(48, result.backSlot()),
				() -> assertEquals(49, result.closeSlot()),
				() -> assertEquals(Material.ARROW, result.backMaterial()),
				() -> assertEquals("Combat Stats", result.titleFor("Combat Stats"))
		);
	}

	@Test void rejectsInvalidLayout() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> loader.load(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(valid().replace("size: 54", "size: 27"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(valid().replace("slot: 48", "slot: 49"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(valid().replace("{category}", "Stats"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(valid().replace("ARROW", "INVALID_MATERIAL")))
		);
	}

	private PlayerStatsCategoryMenuConfiguration load(String yaml) { return loader.load(new StringReader(yaml)); }
	private String valid() {
		return """
				category-menu:
				  title-format: "{category}"
				  size: 54
				  back-button:
				    material: ARROW
				    name: Back
				    color: "#FFFF55"
				    slot: 48
				  close-button:
				    material: BARRIER
				    name: Close
				    color: "#FF5555"
				    slot: 49
				""";
	}
}
