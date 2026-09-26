package org.tomdang.player.skill;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

/** In-game feedback for earned XP, kept separate from progression mutations. */
public final class SkillProgressPresenter {
	private final SkillProgressNotificationSink notifications;

	public SkillProgressPresenter(SkillProgressNotificationSink notifications) {
		if (notifications == null) throw new IllegalArgumentException("notifications cannot be null");
		this.notifications = notifications;
	}

	public void showAward(Player player, SkillAwardResult result) {
		if (player == null || result == null) throw new IllegalArgumentException("Player and award result are required");
		if (result.leveledUp()) {
			player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_3, 1.5f, 2.0f);
		}
		notifications.show(player, result);
	}
}
