package org.tomdang.dialogueframework.presentation.hud.layout;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.hud.glyph.HudGlyph;
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.hud.text.HudTextWidthService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DialogueHudLayoutComposerTest {
	private HudSpacingService spacingService;
	private HudTextWidthService widthService;
	private DialogueHudLayoutComposer composer;
	private DialogueHudSkin defaultSkin;
	private DialogueHudSkin threeLineSkin;
	private HudGlyph backgroundGlyph;

	@BeforeEach
	void setUp() {
		spacingService = mock(HudSpacingService.class);
		widthService = mock(HudTextWidthService.class);
		composer = new DialogueHudLayoutComposer(spacingService, widthService);

		backgroundGlyph = new HudGlyph(Key.key("tomblock", "dialogue"), "\uE001", 256);
		defaultSkin = new DialogueHudSkin(
				"DEFAULT_SKIN",
				backgroundGlyph,
				12,
				14,
				List.of(Key.key("tomblock", "dialogue_line_1"))
		);

		threeLineSkin = new DialogueHudSkin(
				"THREE_LINE_SKIN",
				backgroundGlyph,
				12,
				14,
				List.of(
						Key.key("tomblock", "dialogue_line_1"),
						Key.key("tomblock", "dialogue_line_2"),
						Key.key("tomblock", "dialogue_line_3")
				)
		);
	}

	@Test
	void nullSpacingServiceIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudLayoutComposer(null, widthService));
	}

	@Test
	void nullTextWidthServiceIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudLayoutComposer(spacingService, null));
	}

	@Test
	void nullSkinIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> composer.compose(null, "Hello"));
	}

	@Test
	void nullTextIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> composer.compose(defaultSkin, (String) null));
	}

	@Test
	void emptyTextIsAccepted() {
		when(widthService.measure("")).thenReturn(0);
		when(spacingService.createSpacing(anyInt())).thenReturn(Component.empty());

		assertDoesNotThrow(() -> composer.compose(defaultSkin, ""));
	}

	@Test
	void textWiderThanUsableAreaIsRejected() {
		// Usable width = 256 - 12 - 14 = 230
		when(widthService.measure(anyString())).thenReturn(231);
		// Stub spacing service to prevent NPE during initial background/padding component building
		when(spacingService.createSpacing(anyInt())).thenReturn(Component.empty());

		assertThrows(IllegalStateException.class, () -> composer.compose(defaultSkin, "Overly long dialogue text..."));
	}

	@Test
	void testDialogueProducesExpectedFivePartComponent() {
		String text = "Test dialogue";
		int textWidth = 66;
		int backgroundWidth = 256;
		int leftPadding = 12;
		int trailingSpace = backgroundWidth - leftPadding - textWidth; // 178

		when(widthService.measure(text)).thenReturn(textWidth);

		TextComponent bgComponent = defaultSkin.getBackgroundGlyph().createComponent();
		TextComponent neg256 = Component.text("\uE108");
		TextComponent pos12 = Component.text("\uE113\uE112");

		// Updated to include skin.getLineFont(0) applied by delegation
		TextComponent textComp = Component.text(text).font(defaultSkin.getLineFont(0));
		TextComponent pos178 = Component.text("\uE117\uE115\uE114\uE111");

		when(spacingService.createSpacing(-backgroundWidth)).thenReturn(neg256);
		when(spacingService.createSpacing(leftPadding)).thenReturn(pos12);
		when(spacingService.createSpacing(trailingSpace)).thenReturn(pos178);

		Component result = composer.compose(defaultSkin, text);

		assertEquals(5, result.children().size());
		assertEquals(bgComponent, result.children().get(0));
		assertEquals(neg256, result.children().get(1));
		assertEquals(pos12, result.children().get(2));
		assertEquals(textComp, result.children().get(3));
		assertEquals(pos178, result.children().get(4));
	}

	@Test
	void shortAndLongerMessageProduceEqualTotalLogicalWidth() {
		String shortText = "Hi";
		String longText = "Welcome to TomBlock Adventure!";

		when(widthService.measure(shortText)).thenReturn(10);
		when(widthService.measure(longText)).thenReturn(150);

		when(spacingService.createSpacing(anyInt())).thenAnswer(invocation -> {
			int width = invocation.getArgument(0);
			return Component.text("[space:" + width + "]");
		});

		// Short Text: -256 + 12 + 10 + 234 = 0 net movement after background
		composer.compose(defaultSkin, shortText);
		verify(spacingService).createSpacing(256 - 12 - 10); // 234 trailing space

		// Long Text: -256 + 12 + 150 + 94 = 0 net movement after background
		composer.compose(defaultSkin, longText);
		verify(spacingService).createSpacing(256 - 12 - 150); // 94 trailing space
	}

	// --- Multiline Tests ---

	@Test
	void nullLineListIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> composer.compose(threeLineSkin, (List<String>) null));
	}

	@Test
	void emptyLineListIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> composer.compose(threeLineSkin, List.of()));
	}

	@Test
	void listContainingNullIsRejected() {
		List<String> linesWithNull = Arrays.asList("First line", null, "Third line");
		assertThrows(IllegalArgumentException.class, () -> composer.compose(threeLineSkin, linesWithNull));
	}

	@Test
	void fourLinesRejectedByThreeLineSkin() {
		List<String> fourLines = List.of("Line 1", "Line 2", "Line 3", "Line 4");
		assertThrows(IllegalArgumentException.class, () -> composer.compose(threeLineSkin, fourLines));
	}

	@Test
	void emptyStringWithinValidListIsAccepted() {
		when(widthService.measure("")).thenReturn(0);
		when(widthService.measure("Line 1")).thenReturn(40);
		when(widthService.measure("Line 3")).thenReturn(40);

		when(spacingService.createSpacing(anyInt())).thenReturn(Component.empty());

		assertDoesNotThrow(() -> composer.compose(threeLineSkin, List.of("Line 1", "", "Line 3")));
	}

	@Test
	void individuallyOversizedLineIsRejected() {
		// Usable width = 256 - 12 - 14 = 230
		when(widthService.measure("Valid Line")).thenReturn(100);
		when(widthService.measure("Oversized Line")).thenReturn(231);

		// Fix: Stub spacing service to prevent NPE during initial background/padding setup
		when(spacingService.createSpacing(anyInt())).thenReturn(Component.empty());

		List<String> lines = List.of("Valid Line", "Oversized Line");
		assertThrows(IllegalStateException.class, () -> composer.compose(threeLineSkin, lines));
	}

	@Test
	void eachLineReceivesCorrespondingSkinFont() {
		when(widthService.measure(anyString())).thenReturn(50);

		// Fix: Return non-empty placeholder components to prevent Adventure from flattening empty nodes
		when(spacingService.createSpacing(anyInt())).thenAnswer(invocation -> {
			int width = invocation.getArgument(0);
			return Component.text("[space:" + width + "]");
		});

		List<String> lines = List.of("Line 1", "Line 2", "Line 3");
		Component result = composer.compose(threeLineSkin, lines);

		// Assembly structure with non-empty spacing children:
		// [0] Background, [1] -256, [2] +12, [3] Line1, [4] Reset1 (-50), [5] Line2, [6] Reset2 (-50), [7] Line3, [8] Trailing (+194)
		assertEquals(Key.key("tomblock", "dialogue_line_1"), result.children().get(3).style().font());
		assertEquals(Key.key("tomblock", "dialogue_line_2"), result.children().get(5).style().font());
		assertEquals(Key.key("tomblock", "dialogue_line_3"), result.children().get(7).style().font());
	}

	@Test
	void negativeSpacingResetsCursorAfterLinesOneAndTwo() {
		when(widthService.measure("Line 1")).thenReturn(50);
		when(widthService.measure("Line 2")).thenReturn(60);
		when(widthService.measure("Line 3")).thenReturn(70);

		when(spacingService.createSpacing(anyInt())).thenReturn(Component.empty());

		List<String> lines = List.of("Line 1", "Line 2", "Line 3");
		composer.compose(threeLineSkin, lines);

		verify(spacingService).createSpacing(-50);
		verify(spacingService).createSpacing(-60);
	}

	@Test
	void finalLineReceivesTrailingSpacing() {
		when(widthService.measure("Line 1")).thenReturn(50);
		when(widthService.measure("Line 2")).thenReturn(60);
		when(widthService.measure("Line 3")).thenReturn(70);

		when(spacingService.createSpacing(anyInt())).thenReturn(Component.empty());

		List<String> lines = List.of("Line 1", "Line 2", "Line 3");
		composer.compose(threeLineSkin, lines);

		// Expected trailing space = 256 (bgWidth) - 12 (leftPadding) - 70 (lastLineWidth) = 174
		verify(spacingService).createSpacing(174);
	}

	@Test
	void finishedComponentRetainsLogicalWidthEqualToBackgroundWidth() {
		when(widthService.measure("Line 1")).thenReturn(40);
		when(widthService.measure("Line 2")).thenReturn(80);

		when(spacingService.createSpacing(anyInt())).thenAnswer(invocation -> {
			int width = invocation.getArgument(0);
			return Component.text("[space:" + width + "]");
		});

		DialogueHudSkin twoLineSkin = new DialogueHudSkin(
				"TWO_LINE_SKIN",
				backgroundGlyph,
				12,
				14,
				List.of(
						Key.key("tomblock", "dialogue_line_1"),
						Key.key("tomblock", "dialogue_line_2")
				)
		);

		composer.compose(twoLineSkin, List.of("Line 1", "Line 2"));

		// Initial offsets
		verify(spacingService).createSpacing(-256);
		verify(spacingService).createSpacing(12);

		// Intermediate cursor reset after line 1
		verify(spacingService).createSpacing(-40);

		// Final trailing space: 256 - 12 - 80 = 164
		verify(spacingService).createSpacing(164);
	}
}