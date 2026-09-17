package org.tomdang.player.playerdata;

import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.stats.PlayerStatType;

import java.io.File;

public class PlayerProfileStorage {

	private final File file;
	private final YamlConfiguration data;
	
	public PlayerProfileStorage(File file) {
		this.file = file;
		this.data = YamlConfiguration.loadConfiguration(file);
	}

	// for first time joiners
	public void createPlayerProfile(PlayerProfile player) {
		String path = "players." + player.getUuid();
		data.createSection(path);
		data.set(path + ".mining-xp", 0L);
		data.set(path + ".combat-xp", 0L);
		data.set(path + ".skill-reward-version", 2);
		saveStats(path, player);
	}

	// Does this happen when a player leave the game / disconnect?
	public void savePlayerProfile(PlayerProfile player) {
		String path = "players." + player.getUuid();
		data.set(path + ".mining-xp", player.getMiningXP());
		data.set(path + ".combat-xp", player.getCombatXP());
		data.set(path + ".mining-level", null);
		data.set(path + ".combat-level", null);
		data.set(path + ".skill-xp-version", null);
		data.set(path + ".skill-reward-version", 2);
		saveStats(path, player);
	} // I would assume that data.save(file) would be in Main file

	// This should happen when a player joins the game after a restart / or a crash
	public void loadPlayerProfile(PlayerProfile player) {
		String path = "players." + player.getUuid();
		loadStats(path, player);
		if (data.getInt(path + ".skill-reward-version", 0) >= 2) {
			player.restoreSkillXp(data.getLong(path + ".mining-xp", 0),
					data.getLong(path + ".combat-xp", 0));
		} else {
			// Old profiles mixed earned rewards into these base stats. There is no
			// reliable way to separate them from manually edited values; reset only
			// these two stats and both skills. Other stored stats are preserved.
			player.restoreSkillXp(0, 0);
			player.getStats().reset(PlayerStatType.MINING_FORTUNE);
			player.getStats().reset(PlayerStatType.STRENGTH);
		}
	}

	private void saveStats(String playerPath, PlayerProfile player) {
		for (PlayerStatType statType : PlayerStatType.values()) {
			data.set(playerPath + "." + statType.getStorageKey(), player.getStats().get(statType));
		}
	}

	private void loadStats(String playerPath, PlayerProfile player) {
		for (PlayerStatType statType : PlayerStatType.values()) {
			double value = data.getDouble(
					playerPath + "." + statType.getStorageKey(),
					statType.getDefaultValue()
			);
			player.getStats().set(statType, value);
		}
	}

	public boolean storageContainsPlayer(String path) {
		return data.contains(path);
	}

	public void saveFile()  {
		try {
			data.save(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
