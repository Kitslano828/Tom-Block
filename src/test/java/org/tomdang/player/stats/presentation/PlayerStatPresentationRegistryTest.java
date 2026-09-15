package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatCategory;
import org.tomdang.player.stats.PlayerStatType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatPresentationRegistryTest {

	@Test
	void categoryLookupPreservesRegistrationOrderAndExcludesHiddenStats() {
		PlayerStatPresentationRegistry registry = new PlayerStatPresentationRegistry();
		registry.register(presentation(PlayerStatType.STRENGTH, true));
		registry.register(presentation(PlayerStatType.MAX_HEALTH, true));
		registry.register(presentation(PlayerStatType.MINING_SPEED, true));
		registry.register(presentation(PlayerStatType.DEFENSE, false));

		assertEquals(
				List.of(PlayerStatType.STRENGTH, PlayerStatType.MAX_HEALTH),
				registry.getVisibleByCategory(PlayerStatCategory.COMBAT).stream()
						.map(PlayerStatPresentation::statType)
						.toList()
		);
	}

	@Test
	void rejectsDuplicateRegistrationAndMissingLookup() {
		PlayerStatPresentationRegistry registry = new PlayerStatPresentationRegistry();
		registry.register(presentation(PlayerStatType.MAX_HEALTH, true));

		assertAll(
				() -> assertThrows(IllegalStateException.class,
						() -> registry.register(presentation(PlayerStatType.MAX_HEALTH, true))),
				() -> assertThrows(IllegalStateException.class,
						() -> registry.get(PlayerStatType.MAX_ENERGY))
		);
	}

	private PlayerStatPresentation presentation(PlayerStatType statType, boolean visible) {
		return new PlayerStatPresentation(
				statType, "*", TextColor.color(0xFFFFFF), "Description", Material.STONE, visible
		);
	}
}
