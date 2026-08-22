package org.tomdang.dialogueframework.presentation.hud;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;

public interface DialogueHudRenderer {

	public void render(Player player, DialogueThemeDefinition definition, DialogueHudSkin hudSkin, DialogueNode node, int revealedCharacterCount);

	public void clear(Player player);

}
