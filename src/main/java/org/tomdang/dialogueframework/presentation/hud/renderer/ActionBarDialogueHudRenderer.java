package org.tomdang.dialogueframework.presentation.hud.renderer;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.hud.DialogueHudRenderer;
import org.tomdang.dialogueframework.presentation.hud.DialogueVisibleLineService;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorState;
import org.tomdang.dialogueframework.presentation.hud.layout.DialogueHudLayoutComposer;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePage;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.player.playeractionbar.PlayerActionBarService;

import java.util.List;

public class ActionBarDialogueHudRenderer implements DialogueHudRenderer {

	private final DialogueVisibleLineService dialogueVisibleLineService;
	private final DialogueHudLayoutComposer dialogueHudLayoutComposer;
	private final PlayerActionBarService actionBarService;
	private static final String DIALOGUE_LAYER = "dialogue";

	public ActionBarDialogueHudRenderer(DialogueVisibleLineService dialogueVisibleLineService,
	                                  DialogueHudLayoutComposer dialogueHudLayoutComposer,
	                                  PlayerActionBarService actionBarService) {
		if (dialogueVisibleLineService == null) throw new IllegalArgumentException("dialogueVisibleLineService cannot be null");
		if (dialogueHudLayoutComposer == null) throw new IllegalArgumentException("dialogueHudLayoutComposer cannot be null");
		if (actionBarService == null) throw new IllegalArgumentException("actionBarService cannot be null");

		this.dialogueVisibleLineService = dialogueVisibleLineService;
		this.dialogueHudLayoutComposer = dialogueHudLayoutComposer;
		this.actionBarService = actionBarService;
	}

	@Override
	public void render(Player player, DialogueThemeDefinition definition, DialogueHudSkin hudSkin, DialogueNode node, DialoguePage page, int revealedCharacterCount, DialogueHudIndicatorState dialogueHudIndicatorState) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		if (definition == null) throw new IllegalArgumentException("definition cannot be null");
		if (hudSkin == null) throw new IllegalArgumentException("hudSkin cannot be null");
		if (node == null) throw new IllegalArgumentException("node cannot be null");
		if (revealedCharacterCount < 0) throw new IllegalArgumentException("revealedCharacterCount cannot below 0");
		if (page == null) throw new IllegalArgumentException("page cannot be null");
		if (dialogueHudIndicatorState == null) throw new IllegalArgumentException("dialogueHudIndicatorState cannot be null");

		String completeText = node.getDialogueText();
		List<String> visibleLines =  dialogueVisibleLineService.prepare(page, completeText, revealedCharacterCount);



		actionBarService.setOverlay(player, DIALOGUE_LAYER,
				dialogueHudLayoutComposer.compose(hudSkin, visibleLines, definition.speakerName(), dialogueHudIndicatorState),
				hudSkin.getBackgroundGlyph().getPixelWidth());

	}

	@Override
	public void clear(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		actionBarService.clearOverlay(player, DIALOGUE_LAYER);

	}
}
