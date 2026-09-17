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
		data.set(path + ".skill-xp-version", 1);
		saveStats(path, player);
	}

	// Does this happen when a player leave the game / disconnect?
	public void savePlayerProfile(PlayerProfile player) {
		String path = "players." + player.getUuid();
		data.set(path + ".mining-xp", player.getMiningXP());
		data.set(path + ".combat-xp", player.getCombatXP());
		data.set(path + ".mining-level", null);
		data.set(path + ".combat-level", null);
		data.set(path + ".skill-xp-version", 1);
		saveStats(path, player);
	} // I would assume that data.save(file) would be in Main file

	// This should happen when a player joins the game after a restart / or a crash
	public void loadPlayerProfile(PlayerProfile player) {
		String path = "players." + player.getUuid();
		loadStats(path, player);
		long miningXp = data.getLong(path + ".mining-xp", 0);
		long combatXp = data.getLong(path + ".combat-xp", 0);
		if (data.contains(path + ".skill-xp-version") || !data.contains(path)) {
			player.restoreSkillXp(miningXp, combatXp);
		} else {
			player.restoreLegacySkillXp(miningXp, data.getInt(path + ".mining-level", 1),
					combatXp, data.getInt(path + ".combat-level", 1));
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
