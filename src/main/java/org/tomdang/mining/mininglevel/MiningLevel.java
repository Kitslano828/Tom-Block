package org.tomdang.mining.mininglevel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.player.skill.SkillXpCurve;

public class MiningLevel {
	private final PlayerActionBarService playerActionBarService;

	public MiningLevel(PlayerActionBarService playerActionBarService) {
		if (playerActionBarService == null) throw new IllegalArgumentException("playerActionBarService is required");
		this.playerActionBarService = playerActionBarService;
	}

	/** XP and rewards have already changed in PlayerProfile; this only presents the level-up. */
	public void playerMiningLevelUp(Player player, PlayerProfile profile, int previousLevel, double previousFortune) {
		if (profile.getMiningLVL() <= previousLevel) return;
		player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_3, 1.5f, 2.0f);
		playerActionBarService.showTemporaryMessage(player, Component.text("MINING LEVEL UP")
				.color(NamedTextColor.AQUA).decoration(TextDecoration.BOLD, true), 40);
		player.sendMessage("§3<MINING>----------------------------------------");
		player.sendMessage("§3<MINING>                 Mining LEVEL UP");
		player.sendMessage("§3<MINING>             Mining Level: " + previousLevel + " ---> " + profile.getMiningLVL());
		player.sendMessage("§3<MINING>             Mining Fortune: " + previousFortune + " ---> " + profile.getMiningFortune());
		player.sendMessage("§3<MINING>----------------------------------------");
	}

	/** Cumulative XP threshold for the next level; capped at level 100. */
	public long getNextMiningLevel(PlayerProfile profile) {
		return SkillXpCurve.totalXpForLevel(Math.min(profile.getMiningLVL() + 1, SkillXpCurve.MAX_LEVEL));
	}

	public void updatePlayerMiningXP(int amount, PlayerProfile profile) { profile.increaseMiningXP(amount); }
}
