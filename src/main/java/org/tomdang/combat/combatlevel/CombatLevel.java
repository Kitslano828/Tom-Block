package org.tomdang.combat.combatlevel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.player.skill.SkillXpCurve;

public class CombatLevel {
	/** XP and rewards have already changed in PlayerProfile; this only presents the level-up. */
	public void playerCombatLevelUp(EntityDeathEvent event, PlayerProfile profile, int previousLevel,
	                                double previousStrength, PlayerActionBarService actionBar) {
		if (profile.getCombatLvl() <= previousLevel) return;
		Player player = event.getEntity().getKiller();
		if (player == null) return;
		actionBar.showTemporaryMessage(player, Component.text("COMBAT LEVEL UP")
				.color(NamedTextColor.AQUA).decoration(TextDecoration.BOLD, true), 40);
		player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_3, 1.5f, 2.0f);
		player.sendMessage("§3<COMBAT>----------------------------------------");
		player.sendMessage("§3<COMBAT>                 COMBAT LEVEL UP");
		player.sendMessage("§3<COMBAT>             Combat Level: " + previousLevel + " ---> " + profile.getCombatLvl());
		player.sendMessage("§3<COMBAT>             Strength: " + previousStrength + " ---> " + profile.getStrength());
		player.sendMessage("§3<COMBAT>----------------------------------------");
	}

	/** Cumulative XP threshold for the next level; capped at level 100. */
	public long getNextCombatLevel(PlayerProfile profile) {
		return SkillXpCurve.totalXpForLevel(Math.min(profile.getCombatLvl() + 1, SkillXpCurve.MAX_LEVEL));
	}

	public void updatePlayerCombatXP(int amount, PlayerProfile profile) { profile.increaseCombatXP(amount); }
}
