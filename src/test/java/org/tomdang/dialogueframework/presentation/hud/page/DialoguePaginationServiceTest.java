package org.tomdang.dialogueframework.presentation.hud.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.hud.glyph.HudGlyph;
import org.tomdang.hud.text.HudTextWrapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DialoguePaginationServiceTest {

	private HudTextWrapper textWrapper;
	private DialogueHudSkin skin;
	private DialoguePaginationService paginationService;

	@BeforeEach
	void setUp() {
		textWrapper = mock(HudTextWrapper.class);
		skin = mock(DialogueHudSkin.class);
		HudGlyph backgroundGlyph = mock(HudGlyph.class);

		when(skin.getBackgroundGlyph()).thenReturn(backgroundGlyph);
		when(backgroundGlyph.getPixelWidth()).thenReturn(256);
		when(skin.getTextLeftPadding()).thenReturn(12);
		when(skin.getTextRightPadding()).thenReturn(14);
		when(skin.getMaximumLines()).thenReturn(3);

		paginationService = new DialoguePaginationService(textWrapper);
	}

	@Test
	void wrapsUsingSkinUsableWidth() {
		when(textWrapper.wrap("One line", 230)).thenReturn(List.of("One line"));

		paginationService.paginate(skin, "One line");

		verify(textWrapper).wrap("One line", 230);
	}

	@Test
	void oneWrappedLineProducesOnePage() {
		when(textWrapper.wrap("One line", 230)).thenReturn(List.of("One line"));

		List<DialoguePage> pages = paginationService.paginate(skin, "One line");

		assertEquals(1, pages.size());
		assertPage(pages.getFirst(), List.of("One line"), 0, 8);
	}

	@Test
	void exactlyMaximumLinesProducesOnePage() {
		String text = "one two three";
		when(textWrapper.wrap(text, 230)).thenReturn(List.of("one", "two", "three"));

		List<DialoguePage> pages = paginationService.paginate(skin, text);

		assertEquals(1, pages.size());
		assertPage(pages.getFirst(), List.of("one", "two", "three"), 0, 13);
	}

	@Test
	void fourWrappedLinesProduceTwoPages() {
		String text = "one two three four";
		when(textWrapper.wrap(text, 230)).thenReturn(List.of("one", "two", "three", "four"));

		List<DialoguePage> pages = paginationService.paginate(skin, text);

		assertEquals(2, pages.size());
		assertPage(pages.get(0), List.of("one", "two", "three"), 0, 13);
		assertPage(pages.get(1), List.of("four"), 14, 18);
	}

	@Test
	void sevenWrappedLinesProduceThreePages() {
		String text = "a b c d e f g";
		when(textWrapper.wrap(text, 230)).thenReturn(List.of("a", "b", "c", "d", "e", "f", "g"));

		List<DialoguePage> pages = paginationService.paginate(skin, text);

		assertEquals(3, pages.size());
		assertPage(pages.get(0), List.of("a", "b", "c"), 0, 5);
		assertPage(pages.get(1), List.of("d", "e", "f"), 6, 11);
		assertPage(pages.get(2), List.of("g"), 12, 13);
	}

	@Test
	void repeatedLinesAreLocatedAfterPreviousOccurrences() {
		String text = "same same same same";
		when(textWrapper.wrap(text, 230)).thenReturn(List.of("same", "same", "same", "same"));

		List<DialoguePage> pages = paginationService.paginate(skin, text);

		assertPage(pages.get(0), List.of("same", "same", "same"), 0, 14);
		assertPage(pages.get(1), List.of("same"), 15, 19);
	}

	@Test
	void explicitBlankLineIsRetained() {
		String text = "Top\n\nBottom";
		when(textWrapper.wrap(text, 230)).thenReturn(List.of("Top", "", "Bottom"));

		List<DialoguePage> pages = paginationService.paginate(skin, text);

		assertEquals(1, pages.size());
		assertPage(pages.getFirst(), List.of("Top", "", "Bottom"), 0, 11);
	}

	@Test
	void emptyTextProducesOneEmptyPage() {
		when(textWrapper.wrap("", 230)).thenReturn(List.of(""));

		List<DialoguePage> pages = paginationService.paginate(skin, "");

		assertEquals(1, pages.size());
		assertPage(pages.getFirst(), List.of(""), 0, 0);
	}

	@Test
	void nullWrapperIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialoguePaginationService(null));
	}

	@Test
	void nullSkinIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> paginationService.paginate(null, "Text"));
	}

	@Test
	void nullTextIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> paginationService.paginate(skin, null));
	}

	private void assertPage(DialoguePage page, List<String> lines, int beginningIndex, int endingIndex) {
		assertEquals(lines, page.getLines());
		assertEquals(beginningIndex, page.getBeginningIndex());
		assertEquals(endingIndex, page.getEndingIndex());
	}
}
