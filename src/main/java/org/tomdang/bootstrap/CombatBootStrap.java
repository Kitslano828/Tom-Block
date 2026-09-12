package org.tomdang.bootstrap;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.tomdang.TomBlock;
import org.tomdang.combat.CombatService;
import org.tomdang.combat.combatlevel.CombatLevel;
import org.tomdang.combat.customcombatability.AbilityDamageService;
import org.tomdang.combat.customcombatability.abilities.MagicBoltAbility;
import org.tomdang.combat.customcombatability.abilities.WindDashAbility;
import org.tomdang.combat.weapons.WeaponCreator;
import org.tomdang.combat.weapons.WeaponRegistry;
import org.tomdang.combat.weapons.WeaponResolver;
import org.tomdang.combat.weapons.configuration.WeaponConfigurationLoader;
import org.tomdang.combat.weapons.configuration.WeaponDefinition;
import org.tomdang.combat.weapons.configuration.WeaponDefinitionRegistrar;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customitemframework.CustomItemRegistry;
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

public class CombatBootStrap {

	@Getter
	private final CombatLevel combatLevel;
	@Getter
	private final CombatService combatService;
	@Getter
	private final WeaponRegistry weaponRegistry;
	@Getter
	private final WeaponCreator weaponCreator;


	public CombatBootStrap(TomBlock instance, NamespacedKey customItemIDKey, WeaponCreator weaponCreator,
						   CustomItemRegistry customItemRegistry, CustomAbilityRegistry customAbilityRegistry,
						   PlayerProfileService playerProfileService, PlayerStatsService playerStatsService, PlayerResourceService playerResourceService,
						   CustomMobResolver customMobResolver, CustomMobHealthService customMobHealthService
	) {

		combatLevel = new CombatLevel();


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
		try (InputStream weaponConfigurationStream = instance.getResource("weapons.yml")) {
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

		WeaponResolver weaponResolver = new WeaponResolver(
				customItemIDKey,
				customItemRegistry,
				weaponRegistry
		);

		combatService = new CombatService(playerProfileService,
				weaponResolver,
				customMobResolver,
				playerStatsService,
				playerResourceService,
				customMobHealthService
		);
	}

}
