package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatMenuItemDefinitionTest {

	@Test
	void defensivelyCopiesLore() {
		List<Component> lore = new ArrayList<>(List.of(Component.text("Line")));
		PlayerStatMenuItemDefinition definition = new PlayerStatMenuItemDefinition(
				Material.STONE, Component.text("Name"), lore
		);
		lore.clear();

		assertAll(
				() -> assertEquals(1, definition.lore().size()),
				() -> assertThrows(UnsupportedOperationException.class, () -> definition.lore().clear())
		);
	}

	@Test
	void rejectsInvalidData() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatMenuItemDefinition(null, Component.text("Name"), List.of())),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatMenuItemDefinition(Material.STONE, null, List.of())),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatMenuItemDefinition(Material.STONE, Component.text("Name"), null)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatMenuItemDefinition(Material.STONE, Component.text("Name"), java.util.Arrays.asList((Component) null)))
		);
	}
}
