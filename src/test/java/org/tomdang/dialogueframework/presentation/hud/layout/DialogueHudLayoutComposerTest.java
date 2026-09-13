package org.tomdang.dialogueframework.presentation.hud.layout;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.hud.glyph.HudGlyph;
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.hud.text.HudTextWidthService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DialogueHudLayoutComposerTest {

	private HudSpacingService spacingService;
	private HudTextWidthService widthService;
	private DialogueHudLayoutComposer composer;
	private DialogueHudSkin skin;

	@BeforeEach
	void setUp() {
		spacingService = mock(HudSpacingService.class);
		widthService = mock(HudTextWidthService.class);
		composer = new DialogueHudLayoutComposer(spacingService, widthService);
		skin = new DialogueHudSkin(
				"BLACKSMITH_BOX",
				new HudGlyph(Key.key("tomblock", "dialogue"), "\uE001", 256),
				12,
				14,
				List.of(
						Key.key("tomblock", "dialogue_line_1"),
						Key.key("tomblock", "dialogue_line_2"),
						Key.key("tomblock", "dialogue_line_3")
				),
				Key.key("tomblock", "dialogue_speaker"),
				8,
				10
		);
		when(spacingService.createSpacing(anyInt())).thenAnswer(invocation ->
				Component.text("[space:" + invocation.<Integer>getArgument(0) + "]")
		);
	}

	@Test
	void nullDependenciesAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudLayoutComposer(null, widthService));
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudLayoutComposer(spacingService, null));
	}

	@Test
	void invalidArgumentsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> composer.compose(null, List.of("Hello"), "Blacksmith"));
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, (List<String>) null, "Blacksmith"));
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, List.of(), "Blacksmith"));
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, List.of("Hello"), null));
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, List.of("Hello"), "   "));

		List<String> linesWithNull = new ArrayList<>();
		linesWithNull.add("Hello");
		linesWithNull.add(null);
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, linesWithNull, "Blacksmith"));
	}

	@Test
	void emptyDialogueTextIsAccepted() {
		when(widthService.measure(anyString())).thenReturn(0);
		assertDoesNotThrow(() -> composer.compose(skin, "", "Blacksmith"));
	}

	@Test
	void tooManyLinesAreRejected() {
		assertThrows(IllegalArgumentException.class, () ->
				composer.compose(skin, List.of("One", "Two", "Three", "Four"), "Blacksmith")
		);
	}

	@Test
	void oversizedDialogueLineIsRejected() {
		when(widthService.measure("Blacksmith")).thenReturn(50);
		when(widthService.measure("Too wide")).thenReturn(231);

		assertThrows(IllegalStateException.class, () -> composer.compose(skin, "Too wide", "Blacksmith"));
	}

	@Test
	void oversizedSpeakerNameIsRejectedUsingMeasuredPixelWidth() {
		when(widthService.measure("A very wide speaker")).thenReturn(239);

		assertThrows(IllegalStateException.class, () -> composer.compose(skin, "Hello", "A very wide speaker"));
	}

	@Test
	void speakerUsesDedicatedFontAndCursorReturnsToBackgroundLeftEdge() {
		when(widthService.measure("Blacksmith")).thenReturn(52);
		when(widthService.measure("Hello")).thenReturn(25);

		Component result = composer.compose(skin, "Hello", "Blacksmith");

		assertEquals(Key.key("tomblock", "dialogue_speaker"), result.children().get(3).style().font());
		verify(spacingService).createSpacing(8);
		verify(spacingService).createSpacing(-(8 + 52));
	}

	@Test
	void dialogueLinesRetainTheirCorrespondingFonts() {
		when(widthService.measure("Blacksmith")).thenReturn(52);
		when(widthService.measure("One")).thenReturn(18);
		when(widthService.measure("Two")).thenReturn(19);

		Component result = composer.compose(skin, List.of("One", "Two"), "Blacksmith");

		assertEquals(Key.key("tomblock", "dialogue_line_1"), result.children().get(6).style().font());
		assertEquals(Key.key("tomblock", "dialogue_line_2"), result.children().get(8).style().font());
	}

	@Test
	void finalSpacingRestoresLogicalBackgroundWidth() {
		when(widthService.measure("Blacksmith")).thenReturn(52);
		when(widthService.measure("Hello")).thenReturn(25);

		composer.compose(skin, "Hello", "Blacksmith");

		verify(spacingService).createSpacing(-256);
		verify(spacingService).createSpacing(12);
		verify(spacingService).createSpacing(256 - 12 - 25);
	}
}
