package org.tomdang.dialogueframework.presentation.hud;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorState;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePage;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;

public interface DialogueHudRenderer {

	public void render(Player player, DialogueThemeDefinition definition, DialogueHudSkin hudSkin, DialogueNode node, DialoguePage page, int revealedCharacterCount, DialogueHudIndicatorState dialogueHudIndicatorState);

	public void clear(Player player);

}
