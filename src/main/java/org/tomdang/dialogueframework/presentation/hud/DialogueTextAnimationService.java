package org.tomdang.dialogueframework.presentation.hud;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;

import java.util.UUID;

public class DialogueTextAnimationService {

	private final DialogueDisplayStateRegistry dialogueDisplayStateRegistry;
	private final DialogueThemeRegistry dialogueThemeRegistry;
	private final DialogueHudRenderer dialogueHudRenderer;
	private final DialogueHudSkinRegistry dialogueHudSkinRegistry;

	public DialogueTextAnimationService(DialogueDisplayStateRegistry dialogueDisplayStateRegistry,
										DialogueThemeRegistry dialogueThemeRegistry,
	                               		DialogueHudRenderer dialogueHudRenderer, DialogueHudSkinRegistry dialogueHudSkinRegistry)
	{
		if (dialogueThemeRegistry == null) throw new IllegalArgumentException("DialogueThemeRegistry cannot be null");
		if (dialogueDisplayStateRegistry == null) throw new IllegalArgumentException("dialogueDisplayStateRegistry cannot be null");
		if (dialogueHudRenderer == null) throw new IllegalArgumentException("dialogueHudRenderer cannot be null");
		if (dialogueHudSkinRegistry == null) throw new IllegalArgumentException("dialogueHudSkinRegistry cannot be null");

		this.dialogueThemeRegistry = dialogueThemeRegistry;
		this.dialogueDisplayStateRegistry = dialogueDisplayStateRegistry;
		this.dialogueHudRenderer = dialogueHudRenderer;
		this.dialogueHudSkinRegistry = dialogueHudSkinRegistry;
	}

	public DialogueDisplayState revealCharacters(Player player, DialogueSession session, int charactersToBeRevealed) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (session == null) throw new IllegalArgumentException("session cannot be null");
		if (charactersToBeRevealed <= 0) throw new IllegalArgumentException("Amount To Be Revealed must be greater than 0");

		UUID playerUUID = player.getUniqueId();
		if (!playerUUID.equals(session.getPlayerUUID())) throw new IllegalStateException("Player UUIDS do not match");

		DialogueNode node = session.getCurrentNode();
		if (node == null) throw new IllegalStateException("Node cannot be null");

		DialogueDisplayState displayState = dialogueDisplayStateRegistry.getState(playerUUID);
		if (displayState == null) throw new IllegalStateException("Player Display state does not exist");

		if (!displayState.getDisplayNodeID().equals(node.getNodeID())) throw new IllegalStateException("Not matching display Node IDs");

		String sourceID = session.getDialogueContext().sourceID();
		DialogueThemeDefinition themeDefinition = dialogueThemeRegistry.resolveThemeForSource(sourceID);
		if (themeDefinition == null) throw new IllegalStateException(sourceID + " does not exist");

		String skinID = themeDefinition.hudSkinID();
		DialogueHudSkin hudSkin = dialogueHudSkinRegistry.lookupSkin(skinID);
		if (hudSkin == null) throw new IllegalStateException(skinID + " does not exist");

		int currentNodeLength = getTotalNodeLength(node);
		displayState.revealCharacters(charactersToBeRevealed, currentNodeLength);
		dialogueHudRenderer.render(player, themeDefinition, hudSkin, node, displayState.getRevealedCharacterCount());
		return displayState;
	}

	public DialogueDisplayState revealAll(Player player, DialogueSession session) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (session == null) throw new IllegalArgumentException("session cannot be null");

		UUID playerUUID = player.getUniqueId();
		if (!playerUUID.equals(session.getPlayerUUID())) throw new IllegalStateException("Player UUIDS do not match");

		DialogueNode node = session.getCurrentNode();
		if (node == null) throw new IllegalStateException("Node cannot be null");

		DialogueDisplayState displayState = dialogueDisplayStateRegistry.getState(playerUUID);
		if (displayState == null) throw new IllegalStateException("Player Display state does not exist");

		if (!displayState.getDisplayNodeID().equals(node.getNodeID())) throw new IllegalStateException("Not matching display Node IDs");

		String sourceID = session.getDialogueContext().sourceID();
		DialogueThemeDefinition themeDefinition = dialogueThemeRegistry.resolveThemeForSource(sourceID);
		if (themeDefinition == null) throw new IllegalStateException(sourceID + " does not exist");

		String skinID = themeDefinition.hudSkinID();
		DialogueHudSkin hudSkin = dialogueHudSkinRegistry.lookupSkin(skinID);
		if (hudSkin == null) throw new IllegalStateException(skinID + " does not exist");

		int currentNodeLength = getTotalNodeLength(node);
		displayState.revealAll(currentNodeLength);
		dialogueHudRenderer.render(player, themeDefinition, hudSkin, node, displayState.getRevealedCharacterCount());
		return displayState;
	}

	public void refresh(Player player, DialogueSession session) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (session == null) throw new IllegalArgumentException("session cannot be null");

		UUID playerUUID = player.getUniqueId();
		if (!playerUUID.equals(session.getPlayerUUID())) throw new IllegalStateException("Player UUIDS do not match");

		DialogueNode node = session.getCurrentNode();
		if (node == null) throw new IllegalStateException("Node cannot be null");

		DialogueDisplayState displayState = dialogueDisplayStateRegistry.getState(playerUUID);
		if (displayState == null) throw new IllegalStateException("Player Display state does not exist");

		if (!displayState.getDisplayNodeID().equals(node.getNodeID())) throw new IllegalStateException("Not matching display Node IDs");

		String sourceID = session.getDialogueContext().sourceID();
		DialogueThemeDefinition themeDefinition = dialogueThemeRegistry.resolveThemeForSource(sourceID);
		if (themeDefinition == null) throw new IllegalStateException(sourceID + " does not exist");

		String skinID = themeDefinition.hudSkinID();
		DialogueHudSkin hudSkin = dialogueHudSkinRegistry.lookupSkin(skinID);
		if (hudSkin == null) throw new IllegalStateException(skinID + " does not exist");

		dialogueHudRenderer.render(player, themeDefinition, hudSkin, node, displayState.getRevealedCharacterCount());
	}

	private int getTotalNodeLength(DialogueNode node) {
		return node.getDialogueText().length();
	}

}
