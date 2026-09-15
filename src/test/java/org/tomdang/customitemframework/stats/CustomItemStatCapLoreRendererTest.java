package org.tomdang.customitemframework.stats;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.presentation.PlayerStatPresentation;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;
import org.bukkit.Material;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomItemStatCapLoreRendererTest {

	private final CustomItemStatCapLoreRenderer renderer = new CustomItemStatCapLoreRenderer();

	@Test
	void rendersCapBonusesInStableStatOrder() {
		List<String> lore = renderer.render(new CustomItemStatCapModifiers(Map.of(
				PlayerStatType.ATTACK_SPEED, 50.0,
				PlayerStatType.MAX_HEALTH, -25.5
		))).stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();

		assertEquals(List.of(
				"Grants -25.5 Health Cap.",
				"Grants +50 Attack Speed Cap."
		), lore);
	}

	@Test
	void emptyModifiersProduceNoLoreAndNullIsRejected() {
		assertEquals(List.of(), renderer.render(CustomItemStatCapModifiers.empty()));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(null));
	}

	@Test
	void capNumberUsesTheStatsMenuPresentationColor() {
		TextColor attackSpeedColor = TextColor.color(0xEBCD13);
		PlayerStatPresentationRegistry presentations = new PlayerStatPresentationRegistry();
		presentations.register(new PlayerStatPresentation(PlayerStatType.ATTACK_SPEED, "⚔",
				attackSpeedColor, "Rate of attack", Material.ECHO_SHARD, true));

		var line = new CustomItemStatCapLoreRenderer(presentations).render(
				new CustomItemStatCapModifiers(Map.of(PlayerStatType.ATTACK_SPEED, 50.0))).getFirst();

		assertEquals(attackSpeedColor, line.children().getFirst().color());
	}
}
