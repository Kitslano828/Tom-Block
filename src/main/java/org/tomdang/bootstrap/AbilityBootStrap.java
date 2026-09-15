package org.tomdang.bootstrap;

import lombok.Getter;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customabilityframework.CustomAbilityService;
import org.tomdang.customabilityframework.abilitycooldown.AbilityCooldownService;
import org.tomdang.customabilityframework.activeability.ActiveAbilityService;
import org.tomdang.customabilityframework.source.CompositeAbilitySourceProvider;
import org.tomdang.customabilityframework.source.EquippedArmorAbilitySourceProvider;
import org.tomdang.customabilityframework.source.HeldItemAbilitySourceProvider;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.player.playerresource.PlayerResourceService;

import java.util.List;

public class AbilityBootStrap {

	@Getter
	private final ActiveAbilityService activeAbilityService;
	@Getter
	private final CustomAbilityRegistry customAbilityRegistry;
	@Getter
	private final CustomAbilityService customAbilityService;


	public AbilityBootStrap(CustomItemResolver customItemResolver, CustomArmorService customArmorService,
	                       PlayerResourceService playerResourceService) {
		activeAbilityService = new ActiveAbilityService();
		customAbilityRegistry = new CustomAbilityRegistry();
		AbilityCooldownService abilityCooldownService = new AbilityCooldownService();
		CompositeAbilitySourceProvider abilitySourceProvider = new CompositeAbilitySourceProvider(List.of(
				new HeldItemAbilitySourceProvider(customItemResolver),
				new EquippedArmorAbilitySourceProvider(customArmorService)
		));
		customAbilityService = new CustomAbilityService(
				abilitySourceProvider,
				playerResourceService,
				abilityCooldownService
		);

	}

}
