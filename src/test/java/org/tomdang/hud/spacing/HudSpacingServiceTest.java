package org.tomdang.hud.spacing;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HudSpacingServiceTest {

	private final HudSpacingService spacingService = new HudSpacingService();

	@Test
	void zeroDistanceProducesAnEmptyComponent() {
		Component spacing = spacingService.createSpacing(0);

		assertEquals(Component.empty(), spacing);
	}

	@Test
	void positiveDistanceIsDecomposedIntoSpacingGlyphs() {
		TextComponent spacing = assertInstanceOf(
				TextComponent.class,
				spacingService.createSpacing(178)
		);

		assertEquals("\uE117\uE115\uE114\uE111", spacing.content());
	}

	@Test
	void negativeDistanceIsDecomposedIntoSpacingGlyphs() {
		TextComponent spacing = assertInstanceOf(
				TextComponent.class,
				spacingService.createSpacing(-245)
		);

		assertEquals("\uE107\uE106\uE105\uE104\uE102\uE100", spacing.content());
	}

	@Test
	void exactLargestPositiveDistanceUsesLargestPositiveGlyph() {
		TextComponent spacing = assertInstanceOf(
				TextComponent.class,
				spacingService.createSpacing(256)
		);

		assertEquals("\uE118", spacing.content());
	}

	@Test
	void exactLargestNegativeDistanceUsesLargestNegativeGlyph() {
		TextComponent spacing = assertInstanceOf(
				TextComponent.class,
				spacingService.createSpacing(-256)
		);

		assertEquals("\uE108", spacing.content());
	}

	@Test
	void nonzeroSpacingUsesTheSpacingFont() {
		TextComponent spacing = assertInstanceOf(
				TextComponent.class,
				spacingService.createSpacing(1)
		);

		assertEquals(Key.key("tomblock", "spacing"), spacing.style().font());
	}

	@Test
	void distanceAboveMaximumIsClampedToPositiveMaximum() {
		assertEquals(
				spacingService.createSpacing(4096),
				spacingService.createSpacing(Integer.MAX_VALUE)
		);
	}

	@Test
	void distanceBelowMinimumIsClampedToNegativeMaximum() {
		assertEquals(
				spacingService.createSpacing(-4096),
				spacingService.createSpacing(Integer.MIN_VALUE)
		);
	}
}
