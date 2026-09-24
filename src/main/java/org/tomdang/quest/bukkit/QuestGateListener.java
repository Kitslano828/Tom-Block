package org.tomdang.quest.bukkit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.tomdang.quest.progress.QuestProgressService;
import org.tomdang.quest.progress.QuestSignal;
import org.tomdang.quest.definition.QuestObjectiveType;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.session.DialogueSessionService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-player soft boundary; incomplete players blink back while completed players pass through. */
public final class QuestGateListener implements Listener {
	private static final String WORLD = "world";
	private static final double CENTER_X = -1173.614;
	private static final double CENTER_Z = 255.624;
	private static final double NORMAL_X = 0.380;
	private static final double NORMAL_Z = 0.925;
	private static final double TANGENT_X = -NORMAL_Z;
	private static final double TANGENT_Z = NORMAL_X;
	private static final double HALF_WIDTH = 12.0;
	private static final double BLOCKED_SIDE_MARGIN = -0.35;
	private static final double RETURN_DISTANCE = 3.5;
	private static final int MIN_Y = 60;
	private static final int MAX_Y = 78;
	private static final int BLINK_TICKS = 8;
	private static final long MESSAGE_COOLDOWN_MS = 2500;

	private final QuestProgressService quests;
	private final DialogueController dialogueController;
	private final DialogueSessionService dialogueSessions;
	private final Map<UUID, Long> lastMessage = new HashMap<>();

	public QuestGateListener(QuestProgressService quests,
	                         DialogueController dialogueController, DialogueSessionService dialogueSessions) {
		if (quests == null || dialogueController == null || dialogueSessions == null)
			throw new IllegalArgumentException("Quest gate dependencies are required");
		this.quests = quests;
		this.dialogueController = dialogueController;
		this.dialogueSessions = dialogueSessions;
	}

	@EventHandler(ignoreCancelled = true) public void onMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		if (to == null || sameBlock(event.getFrom(), to) || !WORLD.equals(to.getWorld().getName())) return;
		if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
		if (quests.isCompleted(event.getPlayer().getUniqueId(), QuestPlayerConnectionListener.INTRO_QUEST)) return;
		if (!isBlockedSide(to)) return;
		event.getPlayer().addPotionEffect(new PotionEffect(
				PotionEffectType.BLINDNESS, BLINK_TICKS, 0, false, false, false));
		event.setTo(returnLocation(to));
		long now = System.currentTimeMillis();
		if (now - lastMessage.getOrDefault(event.getPlayer().getUniqueId(), 0L) >= MESSAGE_COOLDOWN_MS) {
			lastMessage.put(event.getPlayer().getUniqueId(), now);
			if (dialogueSessions.getActiveSession(event.getPlayer().getUniqueId()) == null) {
				quests.signal(event.getPlayer().getUniqueId(), QuestSignal.one(
						QuestObjectiveType.INTERACT_WITH_ACTOR, "CRITTER_HUNTER_WILL"));
				dialogueController.startDialogue(event.getPlayer(), "CRITTER_HUNTER_WILL_INTRO",
						DialogueContext.system("CRITTER_HUNTER_WILL"));
			}
		}
	}

	/** Projects the player onto the arrival side of the gate without changing their view direction. */
	private static Location returnLocation(Location crossedAt) {
		double dx = crossedAt.getX() - CENTER_X;
		double dz = crossedAt.getZ() - CENTER_Z;
		double lateral = Math.max(-HALF_WIDTH, Math.min(HALF_WIDTH, dx * TANGENT_X + dz * TANGENT_Z));
		return new Location(
				crossedAt.getWorld(),
				CENTER_X + TANGENT_X * lateral + NORMAL_X * RETURN_DISTANCE,
				crossedAt.getY(),
				CENTER_Z + TANGENT_Z * lateral + NORMAL_Z * RETURN_DISTANCE,
				crossedAt.getYaw(),
				crossedAt.getPitch()
		);
	}

	private static boolean isBlockedSide(Location location) {
		if (location.getY() < MIN_Y || location.getY() > MAX_Y) return false;
		double dx = location.getX() - CENTER_X;
		double dz = location.getZ() - CENTER_Z;
		double forward = dx * NORMAL_X + dz * NORMAL_Z;
		double lateral = dx * TANGENT_X + dz * TANGENT_Z;
		return forward < BLOCKED_SIDE_MARGIN && Math.abs(lateral) <= HALF_WIDTH;
	}

	private static boolean sameBlock(Location first, Location second) {
		return first.getBlockX() == second.getBlockX() && first.getBlockY() == second.getBlockY()
				&& first.getBlockZ() == second.getBlockZ();
	}
}
