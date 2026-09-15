package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatCategory;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.PlayerStatBreakdown;
import org.tomdang.player.stats.evaluation.PlayerStatEvaluation;

import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatCategoryMenuItemRendererTest {
	@Test
	void rendersVisibleStatsInConfiguredOrder() {
		PlayerStatPresentationRegistry registry = new PlayerStatPresentationRegistry();
		registry.register(stat(PlayerStatType.STRENGTH, "❁"));
		registry.register(stat(PlayerStatType.MAX_HEALTH, "❤"));
		PlayerStatCategoryPresentation category = new PlayerStatCategoryPresentation(PlayerStatCategory.COMBAT,
				"Combat Stats", TextColor.color(0xFF5555), "Combat description", Material.IRON_SWORD, 21, true);

		PlayerStatMenuItemDefinition result = new PlayerStatCategoryMenuItemRenderer().render(category, registry, evaluation());

		assertEquals(Material.IRON_SWORD, result.material());
		assertEquals(6, result.lore().size());
		assertEquals("❁ Strength: 12.35", plain(result.lore().get(2)));
		assertEquals("❤ Health: 100", plain(result.lore().get(3)));
	}

	@Test
	void rejectsNullInputs() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatCategoryMenuItemRenderer().render(null, null, null));
	}

	private PlayerStatPresentation stat(PlayerStatType type, String symbol) {
		return new PlayerStatPresentation(type, symbol, TextColor.color(0xFF5555), "Description", Material.STONE, true);
	}

	private PlayerStatEvaluation evaluation() {
		EnumMap<PlayerStatType, PlayerStatBreakdown> values = new EnumMap<>(PlayerStatType.class);
		for (PlayerStatType type : PlayerStatType.values()) {
			double value = type == PlayerStatType.STRENGTH ? 12.345 : type.getDefaultValue();
			values.put(type, new PlayerStatBreakdown(type, value, List.of(), value));
		}
		return new PlayerStatEvaluation(values);
	}

	private String plain(net.kyori.adventure.text.Component component) {
		return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component);
	}
}
