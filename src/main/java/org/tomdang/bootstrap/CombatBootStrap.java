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
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;

public class CombatBootStrap {

	@Getter
	private final CombatLevel combatLevel;
	@Getter
	private final CombatService combatService;
	@Getter
	private final WeaponRegistry weaponRegistry;
	@Getter
	private final WeaponCreator weaponCreator;


	public CombatBootStrap(TomBlock instance, NamespacedKey customItemIDKey, CustomItemRegistry customItemRegistry, CustomAbilityRegistry customAbilityRegistry,
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
		WindDashAbility windDashAbility = new WindDashAbility("WIND_DASH_ABILITY", "Wind Dash", 10,
				instance, AbilityTrigger.RIGHT_CLICK, 10,windDashDescription);
		customAbilityRegistry.registerAbility(windDashAbility);

		weaponCreator = new WeaponCreator(customItemIDKey);
		weaponRegistry = new WeaponRegistry(weaponCreator, customItemRegistry, customAbilityRegistry);
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
