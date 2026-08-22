package org.tomdang.bootstrap;

import lombok.Getter;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customabilityframework.CustomAbilityService;
import org.tomdang.customabilityframework.abilitycooldown.AbilityCooldownService;
import org.tomdang.customabilityframework.activeability.ActiveAbilityService;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.player.playerresource.PlayerResourceService;

public class AbilityBootStrap {

	@Getter
	private final ActiveAbilityService activeAbilityService;
	@Getter
	private final CustomAbilityRegistry customAbilityRegistry;
	@Getter
	private final CustomAbilityService customAbilityService;


	public AbilityBootStrap(CustomItemResolver customItemResolver, PlayerResourceService playerResourceService) {
		activeAbilityService = new ActiveAbilityService();
		customAbilityRegistry = new CustomAbilityRegistry();
		AbilityCooldownService abilityCooldownService = new AbilityCooldownService();
		customAbilityService = new CustomAbilityService(
				customItemResolver,
				playerResourceService,
				abilityCooldownService
		);

	}

}
