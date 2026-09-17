package org.tomdang.player.skill;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.tomdang.player.playeractionbar.PlayerActionBarService;

import java.util.Locale;

/** In-game feedback for earned XP, kept separate from progression mutations. */
public final class SkillProgressPresenter {
	private final PlayerActionBarService actionBar;

	public SkillProgressPresenter(PlayerActionBarService actionBar) {
		if (actionBar == null) throw new IllegalArgumentException("actionBar cannot be null");
		this.actionBar = actionBar;
	}

	public void showAward(Player player, SkillAwardResult result) {
		if (player == null || result == null) throw new IllegalArgumentException("Player and award result are required");
		String name = result.skill().name().toLowerCase(Locale.ROOT);
		SkillProgress after = result.after();
		if (result.leveledUp()) {
			player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_3, 1.5f, 2.0f);
			actionBar.showTemporaryMessage(player, Component.text(result.skill().name() + " LEVEL UP")
					.color(NamedTextColor.AQUA).decoration(TextDecoration.BOLD, true), 40);
			player.sendMessage("§3<" + result.skill().name() + "> Level " + result.before().level()
					+ " → " + after.level() + " (" + result.levelsGained() + " gained)");
			player.sendMessage("§3" + result.skill().rewardStat().getDisplayName() + ": +"
					+ org.tomdang.player.stats.PlayerStatValueFormatter.format(result.skill().rewardAt(result.before().level()))
					+ " → +" + org.tomdang.player.stats.PlayerStatValueFormatter.format(result.skill().rewardAt(after.level())));
		} else {
			Component text = Component.text(after.maxLevel() ? name + " MAX LEVEL"
					: after.xpIntoLevel() + " / " + after.xpNeededForNextLevel() + " " + name + " XP")
					.color(NamedTextColor.DARK_AQUA);
			actionBar.showTemporaryMessage(player, text, 40);
		}
	}
}
