package org.tomdang.dialogueframework.presentation.hud.renderer;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.hud.DialogueHudRenderer;
import org.tomdang.dialogueframework.presentation.hud.DialogueVisibleLineService;
import org.tomdang.dialogueframework.presentation.hud.layout.DialogueHudLayoutComposer;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;

import java.util.List;

public class ActionBarDialogueHudRenderer implements DialogueHudRenderer {

	private final DialogueVisibleLineService dialogueVisibleLineService;
	private final DialogueHudLayoutComposer dialogueHudLayoutComposer;

	public ActionBarDialogueHudRenderer(DialogueVisibleLineService dialogueVisibleLineService, DialogueHudLayoutComposer dialogueHudLayoutComposer) {
		if (dialogueVisibleLineService == null) throw new IllegalArgumentException("dialogueVisibleLineService cannot be null");
		if (dialogueHudLayoutComposer == null) throw new IllegalArgumentException("dialogueHudLayoutComposer cannot be null");

		this.dialogueVisibleLineService = dialogueVisibleLineService;
		this.dialogueHudLayoutComposer = dialogueHudLayoutComposer;
	}

	@Override
	public void render(Player player, DialogueThemeDefinition definition, DialogueHudSkin hudSkin, DialogueNode node, int revealedCharacterCount) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		if (definition == null) throw new IllegalArgumentException("definition cannot be null");
		if (hudSkin == null) throw new IllegalArgumentException("hudSkin cannot be null");
		if (node == null) throw new IllegalArgumentException("node cannot be null");
		if (revealedCharacterCount < 0) throw new IllegalArgumentException("revealedCharacterCount cannot below 0");

		int nodeTextLength = node.getDialogueText().length();
		if (revealedCharacterCount > nodeTextLength) throw new IllegalStateException("revealedCharacterCount is higher than Node text length");

		String completeText = node.getDialogueText();
		List<String> visibleLines =  dialogueVisibleLineService.prepare(hudSkin, completeText, revealedCharacterCount);



		player.sendActionBar(dialogueHudLayoutComposer.compose(hudSkin, visibleLines));

	}

	@Override
	public void clear(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		Component text = Component.empty();
		player.sendActionBar(text);

	}
}
