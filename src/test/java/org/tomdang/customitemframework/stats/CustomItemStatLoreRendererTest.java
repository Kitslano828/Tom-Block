package org.tomdang.customitemframework.stats;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomItemStatLoreRendererTest {

	private final CustomItemStatLoreRenderer renderer = new CustomItemStatLoreRenderer();

	@Test
	void rendersConfiguredStatsInStableEnumOrder() {
		List<String> lines = renderer.render(new CustomItemStatModifiers(Map.of(
				PlayerStatType.MINING_FORTUNE, 7.5,
				PlayerStatType.STRENGTH, 4.0
		))).stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();

		assertEquals(List.of("Strength: 4", "Mining Fortune: 7.5"), lines);
	}

	@Test
	void emptyModifiersProduceNoLore() {
		assertEquals(List.of(), renderer.render(CustomItemStatModifiers.empty()));
	}

	@Test
	void nullModifiersAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> renderer.render(null));
	}
}
