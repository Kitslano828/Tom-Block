package org.tomdang.combat.combatlevel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.playeractionbar.PlayerActionBarService;

public class CombatLevel {
	private final int baseXP = 20;

	public CombatLevel() {

	}

	public void playerCombatLevelUp(EntityDeathEvent event, PlayerProfile player, PlayerActionBarService playerActionBarService) {
		if (player.getCombatXP() >= getNextCombatLevel(player)) {
			int previousLevel = player.getCombatLvl();
			int currentLvl = player.getCombatLvl();
			double previousStrength = player.getStrength();
			double newStrength = previousStrength;
			while (player.getCombatXP() >= ((int)(baseXP * (Math.pow(currentLvl, 1.5))) + ((int)(baseXP * (Math.pow(player.getCombatLvl(), 1.5))) / 2 ))) {
				currentLvl++;
				newStrength += 2;
			}
			player.setCombatLvl(currentLvl);
			player.setStrength(newStrength);
			showLevelUpMessage(event, player, previousLevel, previousStrength, playerActionBarService);
		}
	}

	public void showLevelUpMessage(EntityDeathEvent event, PlayerProfile playerProfile, int previousLevel,
	                               double previousStrength, PlayerActionBarService playerActionBarService) {
		Player player = event.getEntity().getKiller();
		Component text = Component.text("COMBAT LEVEL UP").color(NamedTextColor.AQUA).decoration(TextDecoration.BOLD, true);
		playerActionBarService.showTemporaryMessage(player, text, 40);
		player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_3,1.5f,2.0f);
		player.sendMessage("§3<COMBAT>----------------------------------------");
		player.sendMessage("§3<COMBAT>                 COMBAT LEVEL UP          ");
		player.sendMessage("§3<COMBAT>             Combat Level:   " + previousLevel + " ---> " + playerProfile.getCombatLvl());
		player.sendMessage("§3<COMBAT>             Strength    : " + previousStrength + " ---> " + playerProfile.getStrength());
		player.sendMessage("§3<COMBAT>----------------------------------------");
	}

	public int getNextCombatLevel(PlayerProfile player) {
		return ((int)(baseXP * (Math.pow(player.getCombatLvl(), 1.5)))) + ((int)(baseXP * (Math.pow(player.getCombatLvl(), 1.5))) / 2);
	}

	public void updatePlayerCombatXP(int amount, PlayerProfile playerProfile) {
		playerProfile.increaseCombatXP(amount);
	}
}
