package org.tomdang.bootstrap;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.TomBlock;
import org.tomdang.combat.CombatService;
import org.bukkit.Bukkit;
import org.tomdang.combat.attackspeed.AttackReadinessCalculator;
import org.tomdang.combat.attackspeed.AttackReadinessDamageScaler;
import org.tomdang.combat.attackspeed.PlayerAttackReadinessService;
import org.tomdang.combat.attackspeed.PlayerAttackIndicatorService;
import org.tomdang.combat.attackspeed.AttackRecoveryCalculator;
import org.tomdang.combat.attackspeed.HeldItemCombatResolver;
import org.tomdang.combat.configuration.CombatTimingConfiguration;
import org.tomdang.combat.configuration.CombatTimingConfigurationLoader;
import org.tomdang.combat.combo.ComboTimingCalculator;
import org.tomdang.combat.combo.ComboTimingConfiguration;
import org.tomdang.combat.combo.ComboTimingConfigurationLoader;
import org.tomdang.combat.combo.ConsecutiveChargedHitTracker;
import org.tomdang.combat.combo.milestone.ComboMilestoneObserver;
import org.tomdang.combat.combo.requirement.ComboRequirement;
import org.tomdang.combat.combo.requirement.ComboRequirementEvaluator;
import org.tomdang.combat.damage.PlayerDamageCalculator;
import org.tomdang.combat.hit.PlayerCombatHitPublisher;
import org.tomdang.combat.customcombatability.AbilityDamageService;
import org.tomdang.combat.customcombatability.abilities.MagicBoltAbility;
import org.tomdang.combat.customcombatability.abilities.WindDashAbility;
import org.tomdang.combat.weapons.WeaponCreator;
import org.tomdang.combat.weapons.WeaponRegistry;
import org.tomdang.combat.weapons.configuration.WeaponConfigurationLoader;
import org.tomdang.combat.weapons.configuration.WeaponDefinition;
import org.tomdang.combat.weapons.configuration.WeaponDefinitionRegistrar;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.combat.CombatDamageType;
import org.tomdang.customitemframework.combat.CombatWeightClass;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class CombatBootStrap {

	@Getter
	private final CombatService combatService;
	@Getter
	private final WeaponRegistry weaponRegistry;
	@Getter
	private final WeaponCreator weaponCreator;
	@Getter
	private final PlayerAttackReadinessService playerAttackReadinessService;
	@Getter
	private final PlayerAttackIndicatorService playerAttackIndicatorService;
	@Getter
	private final PlayerCombatHitPublisher playerCombatHitPublisher;
	@Getter
	private final ConsecutiveChargedHitTracker consecutiveChargedHitTracker;


	public CombatBootStrap(TomBlock instance, WeaponCreator weaponCreator,
						   CustomItemRegistry customItemRegistry, CustomItemResolver customItemResolver,
						   CustomAbilityRegistry customAbilityRegistry,
						   PlayerProfileService playerProfileService, PlayerStatsService playerStatsService, PlayerResourceService playerResourceService,
						   CustomMobResolver customMobResolver, CustomMobHealthService customMobHealthService
	) {



		AbilityDamageService abilityDamageService = new AbilityDamageService(customMobHealthService);
		Component magicBoltDescription = Component.text("Shoots a magic bolt", NamedTextColor.GRAY);
		MagicBoltAbility magicBoltAbility = new MagicBoltAbility(instance, "MAGIC_BOLT_ABILITY",
				"Magic Bolt", 20, AbilityTrigger.RIGHT_CLICK, 40.0, 60, magicBoltDescription,
				abilityDamageService
		);
		customAbilityRegistry.registerAbility(magicBoltAbility);

		Component windDashDescription = Component.text("Dash Forward!", NamedTextColor.GRAY);
		WindDashAbility windDashAbility = new WindDashAbility("WIND_DASH_ABILITY", "Wind Dash", 0,
				instance, AbilityTrigger.RIGHT_CLICK, 10,windDashDescription);
		customAbilityRegistry.registerAbility(windDashAbility);

		this.weaponCreator = weaponCreator;
		weaponRegistry = new WeaponRegistry(weaponCreator, customItemRegistry);

		WeaponConfigurationLoader weaponConfigurationLoader = new WeaponConfigurationLoader();
		List<WeaponDefinition> weaponDefinitions;
		try (InputStream weaponConfigurationStream = instance.getResource("items/weapons.yml")) {
			if (weaponConfigurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain weapons.yml");
			}
			weaponDefinitions = weaponConfigurationLoader.loadDefinitions(
					new InputStreamReader(weaponConfigurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled weapons.yml resource", exception);
		}
		WeaponDefinitionRegistrar weaponDefinitionRegistrar = new WeaponDefinitionRegistrar(
				weaponRegistry,
				customAbilityRegistry
		);
		weaponDefinitionRegistrar.registerWeaponDefinitions(weaponDefinitions);
		CombatTimingConfiguration timingConfiguration = loadTimingConfiguration(instance);
		HeldItemCombatResolver heldItemCombatResolver = new HeldItemCombatResolver(
				customItemResolver, timingConfiguration);
		playerAttackReadinessService = new PlayerAttackReadinessService(
				new AttackReadinessCalculator(),
				Bukkit::getCurrentTick
		);
		playerAttackIndicatorService = new PlayerAttackIndicatorService(
				instance,
				heldItemCombatResolver,
				playerStatsService,
				new AttackRecoveryCalculator()
		);
		playerAttackIndicatorService.start();
		playerCombatHitPublisher = new PlayerCombatHitPublisher(instance.getLogger());
		ComboTimingConfiguration comboTimingConfiguration = loadComboTimingConfiguration(instance);
		consecutiveChargedHitTracker = new ConsecutiveChargedHitTracker(
				new ComboTimingCalculator(comboTimingConfiguration),
				Bukkit::getCurrentTick
		);
		playerCombatHitPublisher.register(consecutiveChargedHitTracker);
		ComboRequirement divansDrillTestRequirement = new ComboRequirement(
				3,
				Optional.of(CombatWeightClass.HEAVY),
				Optional.of(CombatDamageType.BLUNT),
				Optional.of("SUPER_PICKAXE")
		);
		ComboMilestoneObserver divansDrillTestMilestone = new ComboMilestoneObserver(
				consecutiveChargedHitTracker,
				new ComboRequirementEvaluator(),
				divansDrillTestRequirement,
				(context, progress) -> context.attacker().sendMessage(
						Component.text("COMBO COMPLETE", NamedTextColor.GOLD)
								.decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)
				),
				true
		);
		playerCombatHitPublisher.register(divansDrillTestMilestone);

		combatService = new CombatService(playerProfileService,
				customMobResolver,
				playerStatsService,
				playerResourceService,
				customMobHealthService,
				new PlayerDamageCalculator(),
				playerAttackReadinessService,
				new AttackReadinessDamageScaler(),
				heldItemCombatResolver,
				playerCombatHitPublisher
		);
	}

	private CombatTimingConfiguration loadTimingConfiguration(TomBlock instance) {
		try (InputStream configurationStream = instance.getResource("combat/combat.yml")) {
			if (configurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain combat.yml");
			}
			return new CombatTimingConfigurationLoader().load(
					new InputStreamReader(configurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled combat.yml resource", exception);
		}
	}

	private ComboTimingConfiguration loadComboTimingConfiguration(TomBlock instance) {
		try (InputStream configurationStream = instance.getResource("combat/combat.yml")) {
			if (configurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain combat.yml");
			}
			return new ComboTimingConfigurationLoader().load(
					new InputStreamReader(configurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled combat.yml resource", exception);
		}
	}

}
