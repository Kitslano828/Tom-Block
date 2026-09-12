package org.tomdang.dialogueframework.presentation.hud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.hud.glyph.HudGlyph;
import org.tomdang.hud.text.HudTextWrapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class DialogueVisibleLineServiceTest {

	@Mock
	private HudTextWrapper hudTextWrapper;

	@Mock
	private DialogueHudSkin skin;

	@Mock
	private HudGlyph backgroundGlyph;

	private DialogueVisibleLineService service;

	@BeforeEach
	void setUp() {
		// Manually initialize @Mock annotated fields without needing @ExtendWith
		MockitoAnnotations.openMocks(this);
		service = new DialogueVisibleLineService(hudTextWrapper);
	}

	private void setupSkinMock(int maxLines) {
		when(backgroundGlyph.getPixelWidth()).thenReturn(256);
		when(skin.getBackgroundGlyph()).thenReturn(backgroundGlyph);
		when(skin.getTextLeftPadding()).thenReturn(12);
		when(skin.getTextRightPadding()).thenReturn(14);
		when(skin.getMaximumLines()).thenReturn(maxLines);
	}

	@Test
	void nullWrapperDependencyIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueVisibleLineService(null));
	}

	@Test
	void nullSkinIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> service.prepare(null, "Hello", 0));
	}

	@Test
	void nullCompleteTextIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> service.prepare(skin, null, 0));
	}

	@Test
	void negativeRevealCountIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> service.prepare(skin, "Hello", -1));
	}

	@Test
	void revealCountBeyondTextLengthIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> service.prepare(skin, "Hello", 6));
	}

	@Test
	void zeroRevealReturnsEmptyVersionsOfEveryWrappedLine() {
		String text = "Welcome to my forge, traveler. What do you need?";
		setupSkinMock(3);
		when(hudTextWrapper.wrap(eq(text), anyInt()))
				.thenReturn(List.of("Welcome to my", "forge, traveler.", "What do you need?"));

		List<String> result = service.prepare(skin, text, 0);

		assertEquals(List.of("", "", ""), result);
	}

	@Test
	void partialFirstLineRevealsOnlyItsPrefix() {
		String text = "Welcome to my forge";
		setupSkinMock(2);
		when(hudTextWrapper.wrap(eq(text), anyInt()))
				.thenReturn(List.of("Welcome to", "my forge"));

		// "Welco" = 5 characters
		List<String> result = service.prepare(skin, text, 5);

		assertEquals(List.of("Welco", ""), result);
	}

	@Test
	void completedFirstLineLeavesLaterLinesEmpty() {
		String text = "Welcome to my forge";
		setupSkinMock(2);
		when(hudTextWrapper.wrap(eq(text), anyInt()))
				.thenReturn(List.of("Welcome to", "my forge"));

		// "Welcome to" = 10 characters
		List<String> result = service.prepare(skin, text, 10);

		assertEquals(List.of("Welcome to", ""), result);
	}

	@Test
	void partialSecondLineKeepsTheFirstLineComplete() {
		String text = "Welcome to my forge";
		setupSkinMock(2);
		when(hudTextWrapper.wrap(eq(text), anyInt()))
				.thenReturn(List.of("Welcome to ", "my forge"));

		// "Welcome to m" = 12 characters (11 characters for line 1 + 1 char "m" into line 2)
		List<String> result = service.prepare(skin, text, 12);

		assertEquals(List.of("Welcome to ", "m"), result);
	}

	@Test
	void fullRevealReturnsAllWrappedLines() {
		String text = "Welcome to my forge";
		setupSkinMock(2);
		when(hudTextWrapper.wrap(eq(text), anyInt()))
				.thenReturn(List.of("Welcome to ", "my forge"));

		List<String> result = service.prepare(skin, text, text.length());

		assertEquals(List.of("Welcome to ", "my forge"), result);
	}

	@Test
	void tooManyWrappedLinesForTheSkinAreRejected() {
		String text = "Line one Line two Line three Line four";
		setupSkinMock(3); // max 3 lines allowed
		when(hudTextWrapper.wrap(eq(text), anyInt()))
				.thenReturn(List.of("Line one", "Line two", "Line three", "Line four"));

		assertThrows(IllegalStateException.class, () -> service.prepare(skin, text, 5));
	}

	@Test
	void repeatedWordsAreLocatedInTheirCorrectSequentialPositions() {
		// "the" appears on line 1, line 2, and line 3
		String text = "the test that the test can test the test";
		setupSkinMock(3);
		when(hudTextWrapper.wrap(eq(text), anyInt()))
				.thenReturn(List.of("the test that ", "the test can ", "test the test"));

		// Reveal through line 1 ("the test that " = 14 chars) + 3 chars into line 2 ("the") = 17 chars total
		List<String> result = service.prepare(skin, text, 17);

		assertEquals(List.of("the test that ", "the", ""), result);
	}
}