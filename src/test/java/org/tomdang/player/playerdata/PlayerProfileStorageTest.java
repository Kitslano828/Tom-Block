package org.tomdang.player.playerdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.skill.SkillXpCurve;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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

	@Test
	void oldSkillFormatResetsSkillsAndEmbeddedStatsButKeepsOtherStats() {
		File file = temporaryDirectory.resolve("legacy-player.yml").toFile();
		UUID playerId = UUID.randomUUID();
		String path = "players." + playerId;
		YamlConfiguration legacy = new YamlConfiguration();
		legacy.set(path + ".mining-xp", 0);
		legacy.set(path + ".mining-level", 10);
		legacy.set(path + ".combat-xp", 0);
		legacy.set(path + ".combat-level", 8);
		legacy.set(path + ".mining-fortune", 36.0);
		legacy.set(path + ".strength", 14.0);
		legacy.set(path + ".defense", 23.0);
		assertDoesNotThrow(() -> legacy.save(file));
		PlayerProfile profile = new PlayerProfile(playerId);
		PlayerProfileStorage storage = new PlayerProfileStorage(file);
		storage.loadPlayerProfile(profile);
		assertEquals(0, profile.getMiningLVL());
		assertEquals(0, profile.getCombatLvl());
		assertEquals(0, profile.getMiningXP());
		assertEquals(0.0, profile.getMiningFortune());
		assertEquals(0.0, profile.getStrength());
		assertEquals(23.0, profile.getDefense());
		storage.savePlayerProfile(profile);
		storage.saveFile();
		YamlConfiguration migrated = YamlConfiguration.loadConfiguration(file);
		assertEquals(2, migrated.getInt(path + ".skill-reward-version"));
		assertEquals(false, migrated.contains(path + ".mining-level"));
		assertEquals(false, migrated.contains(path + ".skill-xp-version"));
		PlayerProfile reloaded = new PlayerProfile(playerId);
		new PlayerProfileStorage(file).loadPlayerProfile(reloaded);
		assertEquals(profile.getMiningXP(), reloaded.getMiningXP());
		assertEquals(profile.getMiningFortune(), reloaded.getMiningFortune());
	}

	@Test
	void previousXpOnlyFormatAlsoResetsInsteadOfTransferringBonuses() {
		File file = temporaryDirectory.resolve("xp-only-player.yml").toFile();
		UUID playerId = UUID.randomUUID();
		String path = "players." + playerId;
		YamlConfiguration previous = new YamlConfiguration();
		previous.set(path + ".mining-xp", 999L);
		previous.set(path + ".combat-xp", 999L);
		previous.set(path + ".skill-xp-version", 1);
		previous.set(path + ".mining-fortune", 27.0);
		previous.set(path + ".strength", 9.0);
		assertDoesNotThrow(() -> previous.save(file));
		PlayerProfileStorage storage = new PlayerProfileStorage(file);
		PlayerProfile first = new PlayerProfile(playerId);
		storage.loadPlayerProfile(first);
		assertEquals(0, first.getMiningXP());
		assertEquals(0, first.getCombatXP());
		assertEquals(0.0, first.getMiningFortune());
		assertEquals(0.0, first.getStrength());
		storage.savePlayerProfile(first);
		storage.saveFile();
		PlayerProfile second = new PlayerProfile(playerId);
		new PlayerProfileStorage(file).loadPlayerProfile(second);
		assertEquals(0, second.getMiningXP());
		assertEquals(0.0, second.getMiningFortune());
	}

	@Test
	void currentFormatPersistsXpAndBaseStatsWithoutLegacyReset() {
		File file = temporaryDirectory.resolve("current-player.yml").toFile();
		UUID id = UUID.randomUUID();
		PlayerProfile profile = new PlayerProfile(id);
		profile.setMiningLVL(5);
		profile.setCombatLvl(3);
		profile.setMiningFortune(7);
		PlayerProfileStorage writer = new PlayerProfileStorage(file);
		writer.savePlayerProfile(profile);
		writer.saveFile();
		PlayerProfile restored = new PlayerProfile(id);
		new PlayerProfileStorage(file).loadPlayerProfile(restored);
		assertEquals(SkillXpCurve.totalXpForLevel(5), restored.getMiningXP());
		assertEquals(3, restored.getCombatLvl());
		assertEquals(7, restored.getMiningFortune());
	}
}
