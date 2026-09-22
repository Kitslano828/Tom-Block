package org.tomdang.bootstrap;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemStackFactory;
import org.tomdang.mining.MiningRegenerationService;
import org.tomdang.mining.MiningService;
import org.tomdang.mining.MiningProgressService;
import org.tomdang.customabilityframework.activeability.ActiveAbilityService;
import org.tomdang.mining.configuration.miningblock.MiningBlockConfigurationLoader;
import org.tomdang.mining.configuration.miningblock.MiningBlockDefinition;
import org.tomdang.mining.configuration.miningblock.MiningBlockDefinitionRegistrar;
import org.tomdang.mining.configuration.miningtool.MiningToolConfigurationLoader;
import org.tomdang.mining.configuration.miningtool.MiningToolDefinition;
import org.tomdang.mining.configuration.miningtool.MiningToolDefinitionRegistrar;
import org.tomdang.mining.customminingability.miningspreadability.MiningSpreadAbility;
import org.tomdang.mining.miningblock.MiningBlockRegistry;
import org.tomdang.mining.miningstats.MiningFortune;
import org.tomdang.mining.miningtool.MiningToolCreator;
import org.tomdang.mining.miningtool.MiningToolRegistry;
import org.tomdang.mining.miningtool.MiningToolResolver;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MiningBootstrap {

	@Getter
	private final MiningToolRegistry miningToolRegistry;
	@Getter
	private final MiningService miningService;
	@Getter
	private final MiningProgressService miningProgressService;
	@Getter
	private final MiningToolCreator miningToolCreator;
	@Getter
	private final MiningBlockRegistry miningBlockRegistry;

	public MiningBootstrap(TomBlock instance, NamespacedKey customIDKey, MiningToolCreator miningToolCreator,
						   CustomItemRegistry customItemRegistry, CustomItemStackFactory customItemStackFactory,
	                       PlayerActionBarService playerActionBarService, PlayerProfileService playerProfileService,
	                       PlayerStatsService playerStatsService, ActiveAbilityService activeAbilityService,
	                       CustomAbilityRegistry customAbilityRegistry
	) {
		Component miningSpreadDescription = Component.text("Mines nearby mining blocks ", NamedTextColor.GRAY);
		MiningSpreadAbility miningSpreadAbility = new MiningSpreadAbility("MINING_SPREAD_ABILITY"
				, "Mining Spread", 10, instance, AbilityTrigger.RIGHT_CLICK, 200,
				miningSpreadDescription, activeAbilityService
		);
		customAbilityRegistry.registerAbility(miningSpreadAbility);


		miningBlockRegistry = new MiningBlockRegistry();
		MiningBlockConfigurationLoader miningBlockConfigurationLoader = new MiningBlockConfigurationLoader();
		List<MiningBlockDefinition> miningBlockDefinitions;
		try (InputStream miningBlockConfigurationStream = instance.getResource("mining/mining-blocks.yml")) {
			if (miningBlockConfigurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain mining-blocks.yml");
			}
			miningBlockDefinitions = miningBlockConfigurationLoader.loadDefinitions(
					new InputStreamReader(miningBlockConfigurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled mining-blocks.yml resource", exception);
		}
		new MiningBlockDefinitionRegistrar(miningBlockRegistry, customItemRegistry)
				.registerDefinitions(miningBlockDefinitions);

		this.miningToolCreator = miningToolCreator;
		miningToolRegistry = new MiningToolRegistry(miningToolCreator, customItemRegistry);

		MiningToolConfigurationLoader miningToolConfigurationLoader = new MiningToolConfigurationLoader();
		List<MiningToolDefinition> miningToolDefinitions;
		try (InputStream miningToolConfigurationStream = instance.getResource("mining/mining-tools.yml")) {
			if (miningToolConfigurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain mining-tools.yml");
			}
			miningToolDefinitions = miningToolConfigurationLoader.loadDefinitions(
					new InputStreamReader(miningToolConfigurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled mining-tools.yml resource", exception);
		}

		MiningToolDefinitionRegistrar miningToolDefinitionRegistrar = new MiningToolDefinitionRegistrar(
				miningToolRegistry,
				customAbilityRegistry
		);
		miningToolDefinitionRegistrar.registerMiningToolDefinitions(miningToolDefinitions);

		MiningToolResolver miningToolResolver = new MiningToolResolver(
				customIDKey,
				miningToolRegistry,
				customItemRegistry
		);
		org.tomdang.player.skill.SkillProgressionService skillProgression = new org.tomdang.player.skill.SkillProgressionService();
		org.tomdang.player.skill.SkillProgressPresenter skillPresenter = new org.tomdang.player.skill.SkillProgressPresenter(playerActionBarService);
		MiningFortune miningFortune = new MiningFortune();
		MiningRegenerationService miningRegenerationService = new MiningRegenerationService(instance);
		this.miningService = new MiningService(
				playerProfileService,
				miningBlockRegistry,
				skillProgression,
				skillPresenter,
				miningToolResolver,
				miningFortune,
				miningRegenerationService,
				playerActionBarService,
				playerStatsService,
				activeAbilityService,
				customItemStackFactory
		);
		this.miningProgressService = new MiningProgressService(instance, miningBlockRegistry,
				miningToolResolver, playerProfileService, playerStatsService);

	}
}
