package org.tomdang.bootstrap;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.mining.MiningRegenerationService;
import org.tomdang.mining.MiningService;
import org.tomdang.customabilityframework.activeability.ActiveAbilityService;
import org.tomdang.mining.customminingability.miningspreadability.MiningSpreadAbility;
import org.tomdang.mining.miningblock.MiningBlockRegistry;
import org.tomdang.mining.mininglevel.MiningLevel;
import org.tomdang.mining.miningstats.MiningFortune;
import org.tomdang.mining.miningtool.MiningToolCreator;
import org.tomdang.mining.miningtool.MiningToolRegistry;
import org.tomdang.mining.miningtool.MiningToolResolver;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.player.playerresource.PlayerStatsService;

public class MiningBootstrap {

	@Getter
	private final MiningToolRegistry miningToolRegistry;
	@Getter
	private final MiningService miningService;
	@Getter
	private final MiningToolCreator miningToolCreator;

	public MiningBootstrap(TomBlock instance, NamespacedKey customIDKey, CustomItemRegistry customItemRegistry,
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


		MiningBlockRegistry miningBlockRegistry = new MiningBlockRegistry();
		miningToolCreator = new MiningToolCreator(customIDKey);
		miningToolRegistry = new MiningToolRegistry(miningToolCreator, customItemRegistry, customAbilityRegistry);
		MiningToolResolver miningToolResolver = new MiningToolResolver(
				customIDKey,
				miningToolRegistry,
				customItemRegistry
		);
		MiningLevel miningLevel = new MiningLevel(playerActionBarService);
		MiningFortune miningFortune = new MiningFortune();
		MiningRegenerationService miningRegenerationService = new MiningRegenerationService(instance);
		this.miningService = new MiningService(
				playerProfileService,
				miningBlockRegistry,
				miningLevel,
				miningToolResolver,
				miningFortune,
				miningRegenerationService,
				playerActionBarService,
				playerStatsService,
				activeAbilityService
		);

	}
}
