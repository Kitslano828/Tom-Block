package org.tomdang.hud.text;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HudTextWrapperTest {

	private HudTextWrapper wrapper;

	@BeforeEach
	void setUp() {
		// Simple fake: Each character is worth 10 pixels
		HudTextWidthService fakeWidthService = text -> text.length() * 10;
		wrapper = new HudTextWrapper(fakeWidthService);
	}

	@Test
	void nullWidthServiceIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new HudTextWrapper(null)
		);
	}

	@Test
	void nullTextIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> wrapper.wrap(null, 100)
		);
	}

	@Test
	void zeroMaxWidthIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> wrapper.wrap("Hello", 0)
		);
	}

	@Test
	void negativeMaxWidthIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> wrapper.wrap("Hello", -50)
		);
	}

	@Test
	void emptyTextReturnsOneEmptyLine() {
		List<String> lines = wrapper.wrap("", 100);

		assertEquals(1, lines.size());
		assertEquals("", lines.getFirst());
	}

	@Test
	void textFittingInsideLimitRemainsOneLine() {
		// "Hello" is 5 chars * 10 = 50px (limit 100px)
		List<String> lines = wrapper.wrap("Hello", 100);

		assertEquals(List.of("Hello"), lines);
	}

	@Test
	void textExceedingLimitBecomesMultipleLines() {
		// "Hello World" is 11 chars * 10 = 110px total. Limit is 60px.
		// "Hello" = 50px (fits). "World" = 50px (exceeds 60px combined).
		List<String> lines = wrapper.wrap("Hello World", 60);

		assertEquals(List.of("Hello", "World"), lines);
	}

	@Test
	void spacesBetweenWordsAreMeasured() {
		// "Hi" (20px) + " " (10px) + "Yo" (20px) = 50px.
		// With limit 40px, "Hi Yo" will not fit on one line even though "Hi" and "Yo" individually fit.
		List<String> lines = wrapper.wrap("Hi Yo", 40);

		assertEquals(List.of("Hi", "Yo"), lines);
	}

	@Test
	void noResultingLineExceedsMaxWidth() {
		int maxWidth = 70; // 7 chars max per line
		String text = "The quick brown fox jumps over the lazy dog";

		List<String> lines = wrapper.wrap(text, maxWidth);

		for (String line : lines) {
			assertTrue(
					line.length() * 10 <= maxWidth,
					"Line '" + line + "' exceeded max width of " + maxWidth + "px"
			);
		}
	}

	@Test
	void oversizedIndividualWordIsSplit() {
		// "Supercalifragilistic" is 20 chars * 10 = 200px. Limit is 50px (5 chars max per chunk).
		List<String> lines = wrapper.wrap("Supercalifragilistic", 50);

		assertEquals(List.of("Super", "calif", "ragil", "istic"), lines);
	}

	@Test
	void explicitNewlineCreatesSeparateLines() {
		// Both fit inside 100px individually, but \\n forces a line split
		List<String> lines = wrapper.wrap("Line 1\nLine 2", 100);

		assertEquals(List.of("Line 1", "Line 2"), lines);
	}

	@Test
	void characterWiderThanMaxWidthThrowsException() {
		// Each character is 10px, max width is 5px
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> wrapper.wrap("A", 5)
		);

		assertTrue(exception.getMessage().contains("cannot fit single font character"));
	}
}