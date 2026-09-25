package org.tomdang.hud.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScaledHudTextWidthServiceTest {
	private final HudTextWidthService sixPixelGlyph = ignored -> 6;

	@Test void roundsScaledGlyphWidthToNearestPixelInsteadOfCeiling() {
		assertEquals(5, new ScaledHudTextWidthService(sixPixelGlyph, 7, 3).measure("A"));
		assertEquals(6, new ScaledHudTextWidthService(sixPixelGlyph, 8, 4).measure("A"));
		assertEquals(7, new ScaledHudTextWidthService(sixPixelGlyph, 10, 5).measure("A"));
	}

	@Test void preservesConfiguredSpaceAdvance() {
		assertEquals(13, new ScaledHudTextWidthService(sixPixelGlyph, 7, 3).measure("A A"));
	}

	@Test void accumulatedLineWidthUsesTheSamePerGlyphRoundingAsMinecraft() {
		assertEquals(50, new ScaledHudTextWidthService(sixPixelGlyph, 7, 3).measure("AAAAAAAAAA"));
	}
}
