package org.tomdang.dialogueframework.presentation.hud.renderer;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.hud.DialogueVisibleLineService;
import org.tomdang.dialogueframework.presentation.hud.layout.DialogueHudLayoutComposer;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class  ActionBarDialogueHudRendererTest {

	private DialogueVisibleLineService visibleLineService;
	private DialogueHudLayoutComposer layoutComposer;
	private ActionBarDialogueHudRenderer renderer;
	private Player player;
	private DialogueHudSkin skin;
	private DialogueNode node;
	private DialogueThemeDefinition theme;

	@BeforeEach
	void setUp() {
		visibleLineService = mock(DialogueVisibleLineService.class);
		layoutComposer = mock(DialogueHudLayoutComposer.class);
		renderer = new ActionBarDialogueHudRenderer(visibleLineService, layoutComposer);
		player = mock(Player.class);
		skin = mock(DialogueHudSkin.class);
		node = mock(DialogueNode.class);
		theme = new DialogueThemeDefinition("BLACKSMITH_THEME", "Blacksmith", "BLACKSMITH_BOX");
	}

	@Test
	void nullVisibleLineServiceIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new ActionBarDialogueHudRenderer(null, layoutComposer)
		);
	}

	@Test
	void nullLayoutComposerIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new ActionBarDialogueHudRenderer(visibleLineService, null)
		);
	}

	@Test
	void nullRenderArgumentsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> renderer.render(null, theme, skin, node, 0));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(player, null, skin, node, 0));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(player, theme, null, node, 0));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(player, theme, skin, null, 0));
	}

	@Test
	void negativeRevealCountIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> renderer.render(player, theme, skin, node, -1)
		);
	}

	@Test
	void revealCountBeyondNodeTextLengthIsRejected() {
		when(node.getDialogueText()).thenReturn("Hello");

		assertThrows(
				IllegalStateException.class,
				() -> renderer.render(player, theme, skin, node, 6)
		);
	}

	@Test
	void renderPreparesComposesAndSendsVisibleLines() {
		String completeText = "Welcome to my forge.";
		int revealedCharacterCount = 9;
		List<String> visibleLines = List.of("Welcome t", "");
		Component composedHud = Component.text("composed dialogue HUD");
		when(node.getDialogueText()).thenReturn(completeText);
		when(visibleLineService.prepare(skin, completeText, revealedCharacterCount)).thenReturn(visibleLines);
		when(layoutComposer.compose(skin, visibleLines)).thenReturn(composedHud);

		renderer.render(player, theme, skin, node, revealedCharacterCount);

		verify(visibleLineService).prepare(skin, completeText, revealedCharacterCount);
		verify(layoutComposer).compose(skin, visibleLines);
		verify(player).sendActionBar(composedHud);
	}

	@Test
	void clearSendsAnEmptyActionBar() {
		renderer.clear(player);

		verify(player).sendActionBar(Component.empty());
	}

	@Test
	void clearRejectsNullPlayer() {
		assertThrows(IllegalArgumentException.class, () -> renderer.clear(null));
	}
}
