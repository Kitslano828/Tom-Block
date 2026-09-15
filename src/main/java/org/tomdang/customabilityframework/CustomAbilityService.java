package org.tomdang.customabilityframework;

import org.bukkit.entity.Player;
import org.tomdang.customabilityframework.abilitycooldown.AbilityCooldownCalculator;
import org.tomdang.customabilityframework.abilitycooldown.AbilityCooldownService;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customabilityframework.source.AbilitySource;
import org.tomdang.customabilityframework.source.AbilitySourceProvider;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatType;

public class CustomAbilityService {

	private final AbilitySourceProvider abilitySourceProvider;
	private final PlayerResourceService playerResourceService;
	private final AbilityCooldownService abilityCooldownService;
	private final PlayerStatsService playerStatsService;
	private final AbilityCooldownCalculator abilityCooldownCalculator;

	public CustomAbilityService(AbilitySourceProvider abilitySourceProvider, PlayerResourceService playerResourceService,
								AbilityCooldownService abilityCooldownService, PlayerStatsService playerStatsService, AbilityCooldownCalculator abilityCooldownCalculator) {
		if (abilitySourceProvider == null) throw new IllegalArgumentException("abilitySourceProvider cannot be null");
		if (playerResourceService == null) throw new IllegalArgumentException("playerResourceService cannot be null");
		if (abilityCooldownService == null) throw new IllegalArgumentException("abilityCooldownService cannot be null");
		if (playerStatsService == null) throw new IllegalArgumentException("playerStatsService cannot be null");
		if (abilityCooldownCalculator == null) throw new IllegalArgumentException("abilityCooldownCalculator cannot be null");
		this.abilitySourceProvider = abilitySourceProvider;
		this.playerResourceService = playerResourceService;
		this.abilityCooldownService = abilityCooldownService;
		this.playerStatsService = playerStatsService;
		this.abilityCooldownCalculator = abilityCooldownCalculator;
	}

	public void triggerAbility(Player player, AbilityTrigger abilityTrigger) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		if (abilityTrigger == null) throw new IllegalArgumentException("abilityTrigger cannot be null");

		for (AbilitySource source : abilitySourceProvider.getAbilitySources(player)) {
			CustomAbility ability = source.ability();
			if (abilityTrigger != ability.getAbilityTrigger()) continue;
			AbilityExecutionContext abilityExecutionContext = new AbilityExecutionContext(source.sourceItem(), player);
			if (!ability.canActivate(abilityExecutionContext)) continue;

			if (abilityCooldownService.isAbilityOnCooldown(player, source.sourceId())) {
				player.sendMessage(ability.getAbilityName() + " is on cooldown!");
				continue;
			}

			if (!playerResourceService.spendEnergy(player, ability.getEnergyCost())) {
				player.sendMessage("You don't have enough energy to use this ability!");
				continue;
			}

			double abilityHaste = playerStatsService.getTotalStat(player, PlayerStatType.ABILITY_HASTE);
			long effectiveCooldown = abilityCooldownCalculator.calculate(ability.getCooldownInTicks(), abilityHaste);

			abilityCooldownService.startAbilityCooldown(player, source.sourceId(), effectiveCooldown);
			ability.execute(abilityExecutionContext);
		}
	}

}
