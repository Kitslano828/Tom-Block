package org.tomdang.player.playerdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.stats.PlayerStatType;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerProfileStorageTest {

	@TempDir
	Path temporaryDirectory;

	@Test
	void newProfileUsesStableLegacyKeysForEveryRegisteredStat() {
		File file = temporaryDirectory.resolve("new-player.yml").toFile();
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());
		String playerPath = "players." + profile.getUuid();
		PlayerProfileStorage storage = new PlayerProfileStorage(file);

		storage.createPlayerProfile(profile);
		storage.saveFile();
		YamlConfiguration savedData = YamlConfiguration.loadConfiguration(file);

		for (PlayerStatType statType : PlayerStatType.values()) {
			assertEquals(
					statType.getDefaultValue(),
					savedData.getDouble(playerPath + "." + statType.getStorageKey()),
					0.000001,
					statType.name()
			);
		}
	}

	@Test
	void migratedStatsRoundTripThroughExistingYamlFormatIntoStatBlock() {
		File file = temporaryDirectory.resolve("players.yml").toFile();
		UUID playerId = UUID.randomUUID();
		PlayerProfile savedProfile = new PlayerProfile(playerId);
		savedProfile.setStrength(37.5);
		savedProfile.setDefense(18.5);
		savedProfile.setMiningFortune(22.5);
		savedProfile.setMaximumHealth(175);
		savedProfile.setMaximumEnergy(140);

		PlayerProfileStorage writer = new PlayerProfileStorage(file);
		writer.savePlayerProfile(savedProfile);
		writer.saveFile();

		PlayerProfile loadedProfile = new PlayerProfile(playerId);
		PlayerProfileStorage reader = new PlayerProfileStorage(file);
		reader.loadPlayerProfile(loadedProfile);

		assertEquals(37.5, loadedProfile.getStrength(), 0.000001);
		assertEquals(37.5, loadedProfile.getStats().get(PlayerStatType.STRENGTH), 0.000001);
		assertEquals(18.5, loadedProfile.getDefense(), 0.000001);
		assertEquals(22.5, loadedProfile.getMiningFortune(), 0.000001);
		assertEquals(175, loadedProfile.getMaximumHealth(), 0.000001);
		assertEquals(140, loadedProfile.getMaximumEnergy(), 0.000001);
	}

	@Test
	void absentStatKeysUseFrameworkDefaults() {
		File file = temporaryDirectory.resolve("players-with-missing-stats.yml").toFile();
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());

		new PlayerProfileStorage(file).loadPlayerProfile(profile);

		assertEquals(PlayerStatType.STRENGTH.getDefaultValue(), profile.getStrength(), 0.000001);
		assertEquals(PlayerStatType.DEFENSE.getDefaultValue(), profile.getDefense(), 0.000001);
		assertEquals(PlayerStatType.MINING_FORTUNE.getDefaultValue(), profile.getMiningFortune(), 0.000001);
		assertEquals(PlayerStatType.MAX_HEALTH.getDefaultValue(), profile.getMaximumHealth(), 0.000001);
		assertEquals(PlayerStatType.MAX_ENERGY.getDefaultValue(), profile.getMaximumEnergy(), 0.000001);
	}
}
