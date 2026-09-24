package org.tomdang.dialogueframework.presentation.hud.renderer;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.hud.DialogueVisibleLineService;
import org.tomdang.dialogueframework.presentation.hud.layout.DialogueHudLayoutComposer;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorState;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePage;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.hud.glyph.HudGlyph;
import org.tomdang.player.playeractionbar.PlayerActionBarService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActionBarDialogueHudRendererTest {

	private DialogueVisibleLineService visibleLineService;
	private DialogueHudLayoutComposer layoutComposer;
	private PlayerActionBarService actionBarService;
	private ActionBarDialogueHudRenderer renderer;
	private Player player;
	private DialogueHudSkin skin;
	private DialogueNode node;
	private DialoguePage page;
	private DialogueThemeDefinition theme;

	@BeforeEach
	void setUp() {
		visibleLineService = mock(DialogueVisibleLineService.class);
		layoutComposer = mock(DialogueHudLayoutComposer.class);
		actionBarService = mock(PlayerActionBarService.class);
		renderer = new ActionBarDialogueHudRenderer(visibleLineService, layoutComposer, actionBarService);
		player = mock(Player.class);
		skin = mock(DialogueHudSkin.class);
		HudGlyph background = mock(HudGlyph.class);
		when(background.getPixelWidth()).thenReturn(256);
		when(skin.getBackgroundGlyph()).thenReturn(background);
		node = mock(DialogueNode.class);
		page = mock(DialoguePage.class);
		theme = new DialogueThemeDefinition("BLACKSMITH_THEME", "Blacksmith", "BLACKSMITH_BOX");
	}

	@Test
	void nullVisibleLineServiceIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new ActionBarDialogueHudRenderer(null, layoutComposer, actionBarService));
	}

	@Test
	void nullLayoutComposerIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new ActionBarDialogueHudRenderer(visibleLineService, null, actionBarService));
		assertThrows(IllegalArgumentException.class, () -> new ActionBarDialogueHudRenderer(visibleLineService, layoutComposer, null));
	}

	@Test
	void nullRenderArgumentsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> renderer.render(null, theme, skin, node, page, 0, DialogueHudIndicatorState.HIDDEN));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(player, null, skin, node, page, 0, DialogueHudIndicatorState.HIDDEN));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(player, theme, null, node, page, 0, DialogueHudIndicatorState.HIDDEN));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(player, theme, skin, null, page, 0, DialogueHudIndicatorState.HIDDEN));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(player, theme, skin, node, null, 0, DialogueHudIndicatorState.HIDDEN));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(player, theme, skin, node, page, 0, null));
	}

	@Test
	void negativeRevealCountIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> renderer.render(player, theme, skin, node, page, -1, DialogueHudIndicatorState.HIDDEN));
	}

	@Test
	void renderPreparesComposesAndSendsVisibleLines() {
		String completeText = "Welcome to my forge.";
		int revealedCharacterCount = 9;
		List<String> visibleLines = List.of("Welcome t", "");
		Component composedHud = Component.text("composed dialogue HUD");

		when(node.getDialogueText()).thenReturn(completeText);
		when(visibleLineService.prepare(page, completeText, revealedCharacterCount)).thenReturn(visibleLines);
		when(layoutComposer.compose(skin, visibleLines, "Blacksmith", DialogueHudIndicatorState.CONTINUE)).thenReturn(composedHud);

		renderer.render(player, theme, skin, node, page, revealedCharacterCount, DialogueHudIndicatorState.CONTINUE);

		verify(visibleLineService).prepare(page, completeText, revealedCharacterCount);
		verify(layoutComposer).compose(skin, visibleLines, "Blacksmith", DialogueHudIndicatorState.CONTINUE);
		verify(actionBarService).setOverlay(player, "dialogue", composedHud, 256);
	}

	@Test
	void clearRemovesOnlyTheDialogueLayer() {
		renderer.clear(player);
		verify(actionBarService).clearOverlay(player, "dialogue");
	}

	@Test
	void clearRejectsNullPlayer() {
		assertThrows(IllegalArgumentException.class, () -> renderer.clear(null));
	}
}
