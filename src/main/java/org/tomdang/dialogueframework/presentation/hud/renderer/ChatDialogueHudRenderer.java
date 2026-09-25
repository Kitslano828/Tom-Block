package org.tomdang.dialogueframework.presentation.hud.renderer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.hud.DialogueHudRenderer;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorState;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePage;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Temporary Phase 0 presentation used while the custom HUD renderer is rebuilt.
 * The existing animation and pagination state can continue to advance, but chat
 * receives a page only after that page is fully revealed. This avoids emitting
 * one chat message for every typewriter-animation frame.
 */
public final class ChatDialogueHudRenderer implements DialogueHudRenderer {
	private final Map<UUID, String> lastRenderedPage = new HashMap<>();

	@Override
	public void render(Player player, DialogueThemeDefinition definition, DialogueHudSkin hudSkin,
	                   DialogueNode node, DialoguePage page, int revealedCharacterCount,
	                   DialogueHudIndicatorState dialogueHudIndicatorState) {
		if (player == null || definition == null || hudSkin == null || node == null || page == null
				|| dialogueHudIndicatorState == null) {
			throw new IllegalArgumentException("Dialogue render arguments cannot be null");
		}
		if (revealedCharacterCount < 0) {
			throw new IllegalArgumentException("revealedCharacterCount cannot be negative");
		}
		if (revealedCharacterCount < page.getEndingIndex()) return;

		String pageText = String.join(" ", page.getLines()).trim();
		String renderKey = node.getNodeID() + ":" + page.getBeginningIndex() + ":" + pageText;
		if (renderKey.equals(lastRenderedPage.put(player.getUniqueId(), renderKey))) return;

		Component speaker = Component.text(definition.speakerName() + ": ", NamedTextColor.GOLD)
				.decoration(TextDecoration.BOLD, true);
		player.sendMessage(speaker.append(Component.text(pageText, NamedTextColor.WHITE)));
	}

	@Override
	public void clear(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		lastRenderedPage.remove(player.getUniqueId());
	}
}
