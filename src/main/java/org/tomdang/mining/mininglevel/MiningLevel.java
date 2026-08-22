package org.tomdang.mining.mininglevel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playeractionbar.PlayerActionBarService;

public class MiningLevel {
	private final int baseXP = 20;
	private final PlayerActionBarService playerActionBarService;

	public MiningLevel(PlayerActionBarService playerActionBarService) {
		this.playerActionBarService = playerActionBarService;
	}

	public void playerMiningLevelUp(Player player, PlayerProfile playerProfile) {
		if (playerProfile.getMiningXP() >= getNextMiningLevel(playerProfile)) {
			int previousFortune = (int)playerProfile.getMiningFortune();
			int previousLvl = playerProfile.getMiningLVL();
			int currentLvl = playerProfile.getMiningLVL();
			// to deal with skipping levels
			while (playerProfile.getMiningXP() >= ((int)(baseXP * (Math.pow(currentLvl, 1.5))) + ((int)(baseXP * (Math.pow(playerProfile.getMiningLVL(), 1.5))) / 2 ))) {
				increaseMiningFortune(playerProfile);
				currentLvl++;
			}
			playerProfile.setMiningLVL(currentLvl);
			showLevelUpMessage(player, playerProfile, previousLvl, previousFortune);
		}
	}

	public void showLevelUpMessage(Player player, PlayerProfile playerProfile, int previousLevel, double previousFortune) {
		player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_3,1.5f,2.0f);
		Component text = Component.text("MINING LEVEL UP")
				.color(NamedTextColor.AQUA).decoration(TextDecoration.BOLD, true);
		playerActionBarService.showTemporaryMessage(player, text, 40);
		player.sendMessage("§3<MINING>----------------------------------------");
		player.sendMessage("§3<MINING>                 Mining LEVEL UP          ");
		player.sendMessage("§3<MINING>             Mining Level:   " + previousLevel + " ---> " + playerProfile.getMiningLVL());
		player.sendMessage("§3<MINING>             Mining Fortune: " + previousFortune + " ---> " + (int)playerProfile.getMiningFortune());
		player.sendMessage("§3<MINING>----------------------------------------");
	}

	// this one is exclusively for leveling up only
	private void increaseMiningFortune(PlayerProfile player) {
		player.increaseMiningFortune(4);
	}

	// Intentionally making leveling up slow for longer progression requirement
	public int getNextMiningLevel(PlayerProfile player) {
		return ((int)(baseXP * (Math.pow(player.getMiningLVL(), 1.5)))) + ((int)(baseXP * (Math.pow(player.getMiningLVL(), 1.5))) / 2);
	}

	public void updatePlayerMiningXP(int amount, PlayerProfile player) {
		player.increaseMiningXP(amount);
	}
}
