package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatPresentationTest {

	@Test
	void rejectsInvalidPresentationData() {
		TextColor color = TextColor.color(0xFF5555);
		assertAll(
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatPresentation(null, "❤", color, "Description", Material.RED_DYE, true)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatPresentation(PlayerStatType.MAX_HEALTH, " ", color, "Description", Material.RED_DYE, true)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatPresentation(PlayerStatType.MAX_HEALTH, "❤", null, "Description", Material.RED_DYE, true)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatPresentation(PlayerStatType.MAX_HEALTH, "❤", color, " ", Material.RED_DYE, true)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatPresentation(PlayerStatType.MAX_HEALTH, "❤", color, "Description", null, true)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatPresentation(PlayerStatType.MAX_HEALTH, "❤", color, "Description", Material.AIR, true))
		);
	}
}
