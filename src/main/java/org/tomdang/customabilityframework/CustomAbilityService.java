package org.tomdang.customabilityframework;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customabilityframework.abilitycooldown.AbilityCooldownService;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.player.playerresource.PlayerResourceService;

public class CustomAbilityService {

	private final CustomItemResolver customItemResolver;
	private final PlayerResourceService playerResourceService;
	private final AbilityCooldownService abilityCooldownService;

	public CustomAbilityService(CustomItemResolver customItemResolver, PlayerResourceService playerResourceService,
								AbilityCooldownService abilityCooldownService) {
		this.customItemResolver = customItemResolver;
		this.playerResourceService = playerResourceService;
		this.abilityCooldownService = abilityCooldownService;
	}

	public void triggerAbility(Player player, AbilityTrigger abilityTrigger) {
		ItemStack heldItem = player.getInventory().getItemInMainHand();
		CustomItem customItem = customItemResolver.getCustomItem(heldItem);

		if (customItem != null) {
			for (CustomAbility ability : customItem.getCustomAbilities()) {
				if (abilityTrigger == ability.getAbilityTrigger()) {
					if (abilityCooldownService.isAbilityOnCooldown(player, ability)) {
						player.sendMessage(ability.getAbilityName() + " is on cooldown!");
					} else {
						if (playerResourceService.spendEnergy(player, ability.getEnergyCost())) {
							AbilityExecutionContext abilityExecutionContext = new AbilityExecutionContext(customItem, player);
							abilityCooldownService.startAbilityCooldown(player, ability);
							ability.execute(abilityExecutionContext);
						} else {
							player.sendMessage("You don't have enough energy to use this ability!");
						}
					}
				}
			}
		}

	}

}
