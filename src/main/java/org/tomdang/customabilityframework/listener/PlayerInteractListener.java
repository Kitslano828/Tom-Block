package org.tomdang.customabilityframework.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.tomdang.customabilityframework.CustomAbilityService;
import org.tomdang.customabilityframework.trigger.AbilityTriggerResolver;

public class PlayerInteractListener implements Listener {

	private final CustomAbilityService customAbilityService;
	private final AbilityTriggerResolver abilityTriggerResolver;

	public PlayerInteractListener(CustomAbilityService customAbilityService,
	                              AbilityTriggerResolver abilityTriggerResolver) {
		if (customAbilityService == null) throw new IllegalArgumentException("customAbilityService cannot be null");
		if (abilityTriggerResolver == null) throw new IllegalArgumentException("abilityTriggerResolver cannot be null");
		this.customAbilityService = customAbilityService;
		this.abilityTriggerResolver = abilityTriggerResolver;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;

		Player player = event.getPlayer();
		abilityTriggerResolver.resolveInteraction(event.getAction(), player.isSneaking())
				.ifPresent(trigger -> customAbilityService.triggerAbility(player, trigger));
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;

		Player player = event.getPlayer();
		abilityTriggerResolver.resolveInteraction(Action.RIGHT_CLICK_AIR, player.isSneaking())
				.ifPresent(trigger -> customAbilityService.triggerAbility(player, trigger));
	}

	@EventHandler
	public void onPlayerDamageEntity(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player player)) return;

		abilityTriggerResolver.resolveInteraction(Action.LEFT_CLICK_AIR, player.isSneaking())
				.ifPresent(trigger -> customAbilityService.triggerAbility(player, trigger));
	}

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		abilityTriggerResolver.resolveSneakChange(event.isSneaking())
				.ifPresent(trigger -> customAbilityService.triggerAbility(event.getPlayer(), trigger));
	}

}
