package org.tomdang.dialogueframework.presentation.hud.animation;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.TomBlock;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.hud.DialogueDisplayState;
import org.tomdang.dialogueframework.presentation.hud.DialogueDisplayStateRegistry;
import org.tomdang.dialogueframework.presentation.hud.DialogueTextAnimationService;
import org.tomdang.dialogueframework.presentation.hud.DialogueTextAnimator;
import org.tomdang.dialogueframework.session.DialogueSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BukkitDialogueTextAnimator implements DialogueTextAnimator {

	private final TomBlock instance;
	private final DialogueTextAnimationService dialogueTextAnimationService;
	private final DialogueDisplayStateRegistry dialogueDisplayStateRegistry;
	private final int charactersPerStep;
	private final long periodTicks;
	private final long keepAlivePeriodTicks;

	private final Map<UUID, BukkitTask> bukkitTaskMap = new HashMap<>();
	private final Map<UUID, Long> lastRefreshTickMap = new HashMap<>();

	public BukkitDialogueTextAnimator(TomBlock instance, DialogueTextAnimationService dialogueTextAnimationService,
									  DialogueDisplayStateRegistry dialogueDisplayStateRegistry, int charactersPerStep,
									  long periodTicks, long keepAlivePeriodTicks) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		if (dialogueTextAnimationService == null) throw new IllegalArgumentException("dialogueTextAnimationService cannot be null");
		if (dialogueDisplayStateRegistry == null) throw new IllegalArgumentException("dialogueDisplayStateRegistry cannot be null");
		if (charactersPerStep <= 0) throw new IllegalArgumentException("charactersPerStep cannot be 0 or below");
		if (periodTicks <= 0) throw new IllegalArgumentException("periodTicks cannot be 0 or below");
		if (keepAlivePeriodTicks <= 0) throw new IllegalArgumentException("keepAlivePeriodTicks cannot be 0 or below");

		this.instance = instance;
		this.dialogueTextAnimationService = dialogueTextAnimationService;
		this.dialogueDisplayStateRegistry = dialogueDisplayStateRegistry;
		this.charactersPerStep = charactersPerStep;
		this.periodTicks = periodTicks;
		this.keepAlivePeriodTicks = keepAlivePeriodTicks;
	}

	@Override
	public void start(Player player, DialogueSession session) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (session == null) throw new IllegalArgumentException("session cannot be null");

		UUID playerUUID = player.getUniqueId();
		if (!playerUUID.equals(session.getPlayerUUID())) throw new IllegalStateException("Player UUIDs do not match");
		cancel(playerUUID);

		DialogueDisplayState displayState = dialogueDisplayStateRegistry.getState(playerUUID);
		if (displayState == null) throw new IllegalStateException("displayState is null");

		DialogueNode node = session.getCurrentNode();
		if (node == null) throw new IllegalStateException("session's node is null");
		String nodeID = node.getNodeID();

		if (!displayState.getDisplayNodeID().equals(nodeID)) throw new IllegalStateException("Node IDs do not match");

		lastRefreshTickMap.put(playerUUID, (long) instance.getServer().getCurrentTick());

		BukkitTask task = instance.getServer().getScheduler().runTaskTimer(instance, () -> {
			if (!player.isOnline()) {
				cancel(playerUUID);
				return;
			}

			DialogueDisplayState currentState = dialogueDisplayStateRegistry.getState(playerUUID);

			if (currentState == null) {
				cancel(playerUUID);
				return;
			}

			DialogueNode currentNode = session.getCurrentNode();
			if (currentNode == null) {
				cancel(playerUUID);
				return;
			}

			if (!currentState.getDisplayNodeID().equals(currentNode.getNodeID())) {
				cancel(playerUUID);
				return;
			}

			if (!currentNode.getNodeID().equals(nodeID)) {
				cancel(playerUUID);
				return;
			}

			if (currentState.isFullyRevealed()) {
				long currentTick = instance.getServer().getCurrentTick();
				long lastRefreshTick = lastRefreshTickMap.getOrDefault(playerUUID, currentTick);
				if (currentTick - lastRefreshTick >= keepAlivePeriodTicks) {
					dialogueTextAnimationService.refresh(player, session);
					lastRefreshTickMap.put(playerUUID, currentTick);
				}
				return;
			}

			DialogueDisplayState updatedState = dialogueTextAnimationService.revealCharacters(player, session, charactersPerStep);
			if (updatedState.isFullyRevealed()) {
				lastRefreshTickMap.put(playerUUID, (long) instance.getServer().getCurrentTick());
			}

		}, this.periodTicks, this.periodTicks);

		bukkitTaskMap.put(playerUUID, task);

	}

	@Override
	public void cancel(UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		BukkitTask task = bukkitTaskMap.remove(playerUUID);
		if (task != null) task.cancel();
		lastRefreshTickMap.remove(playerUUID);
	}
}
