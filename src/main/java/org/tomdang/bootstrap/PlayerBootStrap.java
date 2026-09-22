package org.tomdang.bootstrap;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.tomdang.TomBlock;
import org.tomdang.customarmorframework.CustomArmorResolver;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.customarmorframework.stats.ArmorStatModifierProvider;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.stats.HeldItemStatModifierProvider;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playeractionbar.ActionBarRegistry;
import org.tomdang.player.playeractionbar.ActionBarSuppressionService;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.player.playerdata.PlayerProfileStorage;
import org.tomdang.player.playerdata.PlayerProfileRepository;
import org.tomdang.player.playerdata.PostgresConfiguration;
import org.tomdang.player.playerdata.PostgresPlayerProfileRepository;
import org.tomdang.player.playerdata.MigratingPlayerProfileRepository;
import org.tomdang.player.playerdata.PostgresDatabase;
import org.tomdang.player.counter.InMemoryPlayerCounterRepository;
import org.tomdang.player.counter.PlayerCounterService;
import org.tomdang.player.counter.PostgresPlayerCounterRepository;
import org.tomdang.player.counter.CounterDefinitionConfigurationLoader;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.playerhealthdisplay.PlayerHealthDisplayService;
import org.tomdang.player.playerresource.PlayerResourceRegenerationService;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.modifier.PlayerStatModifierCalculator;
import org.tomdang.player.stats.modifier.PlayerStatModifierProviderRegistry;
import org.tomdang.player.skill.SkillStatModifierProvider;
import org.tomdang.player.stats.rule.PlayerStatRuleRegistry;
import org.tomdang.player.stats.modifier.cap.PlayerStatCapModifierProviderRegistry;
import org.tomdang.island.PrivateIslandService;
import org.tomdang.island.PostgresPrivateIslandRepository;
import org.tomdang.island.InMemoryPrivateIslandRepository;
import org.tomdang.customitemframework.stats.HeldItemStatCapModifierProvider;
import org.tomdang.customarmorframework.stats.ArmorStatCapModifierProvider;

import java.io.File;

public class PlayerBootStrap {

	@Getter
	private final PlayerProfileService playerProfileService;
	@Getter
	private final PlayerProfileRepository playerProfileStorage;
	@Getter
	private final PlayerCounterService playerCounterService;
	private final PostgresDatabase postgresDatabase;
	@Getter
	private final PrivateIslandService privateIslandService;
	@Getter
	private final PlayerStatsService playerStatsService;
	@Getter
	private final PlayerResourceService playerResourceService;
	@Getter
	private final CustomArmorService customArmorService;

	@Getter
	private final PlayerActionBarService playerActionBarService;
	private final PlayerResourceRegenerationService playerResourceRegenerationService;

	@Getter
	private final ActionBarSuppressionService actionBarSuppressionService;
	@Getter
	private final PlayerStatModifierProviderRegistry statModifierProviderRegistry;
	@Getter
	private final PlayerStatCapModifierProviderRegistry statCapModifierProviderRegistry;

	public PlayerBootStrap(TomBlock instance, File playerFile, CustomArmorResolver customArmorResolver,
	                      CustomItemResolver customItemResolver, PlayerStatRuleRegistry statRuleRegistry) {
		if (statRuleRegistry == null) throw new IllegalArgumentException("statRuleRegistry cannot be null");
		playerProfileService = new PlayerProfileService();
		PlayerProfileStorage yamlStorage = new PlayerProfileStorage(playerFile);
		File databaseFile = new File(instance.getDataFolder(), "database.yml");
		if (!databaseFile.isFile()) instance.saveResource("database.yml", false);
		PostgresConfiguration database = PostgresConfiguration.from(
				YamlConfiguration.loadConfiguration(databaseFile), System.getenv());
		if (database.enabled()) {
			postgresDatabase = new PostgresDatabase(database);
			PostgresPlayerProfileRepository postgres = new PostgresPlayerProfileRepository(postgresDatabase);
			playerProfileStorage = new MigratingPlayerProfileRepository(
					postgres, yamlStorage, instance.getLogger()::info);
			playerCounterService = new PlayerCounterService(
					new PostgresPlayerCounterRepository(postgresDatabase.dataSource()));
			privateIslandService = new PrivateIslandService(new PostgresPrivateIslandRepository(postgresDatabase.dataSource()));
			instance.getLogger().info("Player profiles use PostgreSQL; legacy YAML profiles import on first join.");
		} else {
			postgresDatabase = null;
			playerProfileStorage = yamlStorage;
			playerCounterService = new PlayerCounterService(new InMemoryPlayerCounterRepository());
			privateIslandService = new PrivateIslandService(new InMemoryPrivateIslandRepository());
			instance.getLogger().info("Player profiles use legacy YAML storage (PostgreSQL disabled). ");
			instance.getLogger().warning("Expandable player counters are in-memory and will reset on restart.");
		}
		var counterDefinitions = new CounterDefinitionConfigurationLoader().load(
				instance.getResource("counter-definitions.yml"));
		counterDefinitions.forEach(playerCounterService::register);
		instance.getLogger().info("Registered " + counterDefinitions.size() + " expandable counter definitions.");
		customArmorService = new CustomArmorService(customArmorResolver);
		ArmorStatModifierProvider armorStatModifierProvider = new ArmorStatModifierProvider(customArmorService);
		HeldItemStatModifierProvider heldItemStatModifierProvider = new HeldItemStatModifierProvider(customItemResolver);
		statModifierProviderRegistry = new PlayerStatModifierProviderRegistry();
		statModifierProviderRegistry.register("armor", armorStatModifierProvider);
		statModifierProviderRegistry.register("held-item", heldItemStatModifierProvider);
		statModifierProviderRegistry.register("skills", new SkillStatModifierProvider(playerProfileService));
		PlayerStatModifierCalculator playerStatModifierCalculator = new PlayerStatModifierCalculator(statRuleRegistry);
		statCapModifierProviderRegistry = new PlayerStatCapModifierProviderRegistry();
		statCapModifierProviderRegistry.register("armor", new ArmorStatCapModifierProvider(customArmorService));
		statCapModifierProviderRegistry.register("held-item", new HeldItemStatCapModifierProvider(customItemResolver));
		playerStatsService = new PlayerStatsService(
				playerProfileService,
				statModifierProviderRegistry,
				playerStatModifierCalculator,
				statCapModifierProviderRegistry
		);
		PlayerHealthDisplayService 	playerHealthDisplayService = new PlayerHealthDisplayService(playerProfileService, playerStatsService);
		playerResourceService = new PlayerResourceService(playerProfileService, playerStatsService, playerHealthDisplayService);
		ActionBarRegistry actionBarRegistry = new ActionBarRegistry(playerStatsService);

		actionBarSuppressionService = new ActionBarSuppressionService();

		playerActionBarService = new PlayerActionBarService(
				instance,
				playerProfileService,
				customArmorResolver,
				actionBarRegistry,
				actionBarSuppressionService
		);


		playerResourceRegenerationService = new PlayerResourceRegenerationService(
				instance,
				playerStatsService,
				playerResourceService);

	}

	public void start() {
		playerActionBarService.scheduleActionBarUpdate();
		playerResourceRegenerationService.start();
	}

	public void shutDown() {
		try {
			for (Player p : Bukkit.getOnlinePlayers()) {
				PlayerProfile player = playerProfileService.getPlayerProfileFromMap(p.getUniqueId());
				playerProfileStorage.savePlayerProfile(player);
			}
			playerProfileStorage.flush();
		} finally {
			try {
				playerProfileStorage.close();
			} finally {
				if (postgresDatabase != null) postgresDatabase.close();
			}
		}
	}

}
