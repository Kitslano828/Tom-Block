package org.tomdang.dialogueframework.presentation.hud.layout;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorState;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorStyle;
import org.tomdang.hud.glyph.HudGlyph;
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.hud.text.HudTextWidthService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
				10,
				TextColor.fromHexString("#D8D8D8"),
				TextColor.fromHexString("#FFF1D0"),
				Map.of(
						DialogueHudIndicatorState.CONTINUE,
						new DialogueHudIndicatorStyle("»", Key.key("tomblock", "dialogue_speaker"), TextColor.color(0xFFF1D0), 10),
						DialogueHudIndicatorState.CHOICE_REQUIRED,
						new DialogueHudIndicatorStyle("?", Key.key("tomblock", "dialogue_speaker"), TextColor.color(0xFFF1D0), 10)
				)
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
		assertThrows(IllegalArgumentException.class, () -> composer.compose(null, List.of("Hello"), "Blacksmith", DialogueHudIndicatorState.HIDDEN));
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, (List<String>) null, "Blacksmith", DialogueHudIndicatorState.HIDDEN));
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, List.of(), "Blacksmith", DialogueHudIndicatorState.HIDDEN));
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, List.of("Hello"), null, DialogueHudIndicatorState.HIDDEN));
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, List.of("Hello"), "   ", DialogueHudIndicatorState.HIDDEN));
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, List.of("Hello"), "Blacksmith", null));

		List<String> linesWithNull = new ArrayList<>();
		linesWithNull.add("Hello");
		linesWithNull.add(null);
		assertThrows(IllegalArgumentException.class, () -> composer.compose(skin, linesWithNull, "Blacksmith", DialogueHudIndicatorState.HIDDEN));
	}

	@Test
	void emptyDialogueTextIsAccepted() {
		when(widthService.measure(anyString())).thenReturn(0);
		assertDoesNotThrow(() -> composer.compose(skin, "", "Blacksmith", DialogueHudIndicatorState.HIDDEN));
	}

	@Test
	void tooManyLinesAreRejected() {
		assertThrows(IllegalArgumentException.class, () ->
				composer.compose(skin, List.of("One", "Two", "Three", "Four"), "Blacksmith", DialogueHudIndicatorState.HIDDEN)
		);
	}

	@Test
	void oversizedDialogueLineIsRejected() {
		when(widthService.measure("Blacksmith")).thenReturn(50);
		when(widthService.measure("Too wide")).thenReturn(231);

		assertThrows(IllegalStateException.class, () -> composer.compose(skin, "Too wide", "Blacksmith", DialogueHudIndicatorState.HIDDEN));
	}

	@Test
	void oversizedSpeakerNameIsRejectedUsingMeasuredPixelWidth() {
		when(widthService.measure("A very wide speaker")).thenReturn(239);

		assertThrows(IllegalStateException.class, () -> composer.compose(skin, "Hello", "A very wide speaker", DialogueHudIndicatorState.HIDDEN));
	}

	@Test
	void speakerUsesDedicatedFontAndCursorReturnsToBackgroundLeftEdge() {
		when(widthService.measure("Blacksmith")).thenReturn(52);
		when(widthService.measure("Hello")).thenReturn(25);

		Component result = composer.compose(skin, "Hello", "Blacksmith", DialogueHudIndicatorState.HIDDEN);

		assertEquals(Key.key("tomblock", "dialogue_speaker"), result.children().get(3).style().font());
		assertEquals(TextColor.color(0xFFF1D0), result.children().get(3).style().color());
		verify(spacingService).createSpacing(8);
		verify(spacingService).createSpacing(-(8 + 52));
	}

	@Test
	void dialogueLinesRetainTheirCorrespondingFonts() {
		when(widthService.measure("Blacksmith")).thenReturn(52);
		when(widthService.measure("One")).thenReturn(18);
		when(widthService.measure("Two")).thenReturn(19);

		Component result = composer.compose(skin, List.of("One", "Two"), "Blacksmith", DialogueHudIndicatorState.HIDDEN);

		assertEquals(Key.key("tomblock", "dialogue_line_1"), result.children().get(6).style().font());
		assertEquals(Key.key("tomblock", "dialogue_line_2"), result.children().get(8).style().font());
		assertEquals(TextColor.color(0xD8D8D8), result.children().get(6).style().color());
		assertEquals(TextColor.color(0xD8D8D8), result.children().get(8).style().color());
	}

	@Test
	void finalSpacingRestoresLogicalBackgroundWidth() {
		when(widthService.measure("Blacksmith")).thenReturn(52);
		when(widthService.measure("Hello")).thenReturn(25);

		composer.compose(skin, "Hello", "Blacksmith", DialogueHudIndicatorState.HIDDEN);

		verify(spacingService).createSpacing(-256);
		verify(spacingService).createSpacing(12);
		verify(spacingService).createSpacing(256 - 12 - 25);
	}

	@Test
	void continueIndicatorUsesConfiguredStyleAndRightAlignment() {
		when(widthService.measure("Blacksmith")).thenReturn(52);
		when(widthService.measure("Hello")).thenReturn(25);
		when(widthService.measure("»")).thenReturn(6);

		Component result = composer.compose(skin, "Hello", "Blacksmith", DialogueHudIndicatorState.CONTINUE);
		Component indicator = result.children().get(result.children().size() - 2);

		assertEquals(Component.text("»")
				.font(Key.key("tomblock", "dialogue_speaker"))
				.color(TextColor.color(0xFFF1D0)), indicator);
		verify(spacingService).createSpacing(-(10 + 6));
		verify(spacingService).createSpacing(10);
	}

	@Test
	void choiceIndicatorUsesStyleMappedToChoiceRequiredState() {
		when(widthService.measure("Blacksmith")).thenReturn(52);
		when(widthService.measure("Hello")).thenReturn(25);
		when(widthService.measure("?")).thenReturn(5);

		Component result = composer.compose(skin, "Hello", "Blacksmith", DialogueHudIndicatorState.CHOICE_REQUIRED);
		Component indicator = result.children().get(result.children().size() - 2);

		assertEquals(Component.text("?")
				.font(Key.key("tomblock", "dialogue_speaker"))
				.color(TextColor.color(0xFFF1D0)), indicator);
		verify(spacingService).createSpacing(-(10 + 5));
	}
}
