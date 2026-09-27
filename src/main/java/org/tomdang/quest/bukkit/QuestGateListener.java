package org.tomdang.quest.bukkit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.tomdang.quest.progress.QuestProgressService;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.gameplay.event.type.ActorInteracted;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.quest.gate.QuestGateDefinition;
import org.tomdang.quest.gate.QuestGateRegistry;
import org.tomdang.player.playeractionbar.PlayerActionBarService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** Per-player soft boundary; incomplete players blink back while completed players pass through. */
public final class QuestGateListener implements Listener {
	private final QuestProgressService quests;
	private final QuestGateRegistry gates;
	private final DialogueController dialogueController;
	private final DialogueSessionService dialogueSessions;
	private final Map<CooldownKey, Long> lastMessage = new HashMap<>();
	private final GameplayEventBus gameplayEvents;
	private final PlayerActionBarService hudMessages;

	public QuestGateListener(QuestProgressService quests, QuestGateRegistry gates,
	                         DialogueController dialogueController, DialogueSessionService dialogueSessions,
	                         GameplayEventBus gameplayEvents, PlayerActionBarService hudMessages) {
		if (quests == null || gates == null || dialogueController == null || dialogueSessions == null || gameplayEvents == null)
			throw new IllegalArgumentException("Quest gate dependencies are required");
		this.quests = quests;
		this.gates = gates;
		this.dialogueController = dialogueController;
		this.dialogueSessions = dialogueSessions;
		this.gameplayEvents = gameplayEvents;
		this.hudMessages = java.util.Objects.requireNonNull(hudMessages);
	}

	@EventHandler(ignoreCancelled = true) public void onMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		if (to == null || sameBlock(event.getFrom(), to)) return;
		if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
		for (QuestGateDefinition gate : gates.all()) {
			if (!gate.world().equals(to.getWorld().getName()) || quests.progress(
					event.getPlayer().getUniqueId(), gate.questId()).isPresent() || !isBlockedSide(gate, to)) continue;
			event.getPlayer().addPotionEffect(new PotionEffect(
					PotionEffectType.BLINDNESS, gate.blinkTicks(), 0, false, false, false));
			event.setTo(returnLocation(gate, to));
			showPrompt(event, gate);
			return;
		}
	}

	private void showPrompt(PlayerMoveEvent event, QuestGateDefinition gate) {
		if (gate.dialogueId() == null && gate.promptMessage() == null) return;
		CooldownKey key = new CooldownKey(event.getPlayer().getUniqueId(), gate.id());
		long now = System.currentTimeMillis();
		if (now - lastMessage.getOrDefault(key, 0L) < gate.messageCooldownMillis()) return;
		lastMessage.put(key, now);
		if (gate.promptMessage() != null) {
			hudMessages.showTemporaryMessage(event.getPlayer(), Component.text(gate.promptMessage(), NamedTextColor.GOLD), 40);
			return;
		}
		if (dialogueSessions.getActiveSession(event.getPlayer().getUniqueId()) == null) {
			gameplayEvents.publish(new ActorInteracted(event.getPlayer().getUniqueId(), gate.actorId(), null));
			dialogueController.startDialogue(event.getPlayer(), gate.dialogueId(), DialogueContext.system(gate.actorId()));
		}
	}

	/** Projects the player onto the arrival side of the gate without changing their view direction. */
	private static Location returnLocation(QuestGateDefinition gate, Location crossedAt) {
		double tangentX = -gate.normalZ(), tangentZ = gate.normalX();
		double dx = crossedAt.getX() - gate.centerX();
		double dz = crossedAt.getZ() - gate.centerZ();
		double lateral = Math.max(-gate.halfWidth(), Math.min(gate.halfWidth(), dx * tangentX + dz * tangentZ));
		return new Location(
				crossedAt.getWorld(),
				gate.centerX() + tangentX * lateral + gate.normalX() * gate.returnDistance(),
				crossedAt.getY(),
				gate.centerZ() + tangentZ * lateral + gate.normalZ() * gate.returnDistance(),
				crossedAt.getYaw(),
				crossedAt.getPitch()
		);
	}

	private static boolean isBlockedSide(QuestGateDefinition gate, Location location) {
		if (location.getY() < gate.minimumY() || location.getY() > gate.maximumY()) return false;
		double dx = location.getX() - gate.centerX();
		double dz = location.getZ() - gate.centerZ();
		double forward = dx * gate.normalX() + dz * gate.normalZ();
		double lateral = dx * -gate.normalZ() + dz * gate.normalX();
		return forward < gate.blockedSideMargin() && Math.abs(lateral) <= gate.halfWidth();
	}

	private static boolean sameBlock(Location first, Location second) {
		return first.getBlockX() == second.getBlockX() && first.getBlockY() == second.getBlockY()
				&& first.getBlockZ() == second.getBlockZ();
	}

	private record CooldownKey(UUID playerId, String gateId) {}
}
