package org.tomdang.customabilityframework.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.CustomAbilityService;

public class PlayerInteractListener implements Listener {

	private final CustomAbilityService customAbilityService;

	public PlayerInteractListener(CustomAbilityService customAbilityService) {
		this.customAbilityService = customAbilityService;
	}

	// 1. Handles interacting with Air (or Blocks)
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		// Prevent the event from firing twice (once for main hand, once for off hand)
		if (event.getHand() != EquipmentSlot.HAND) return;

		Action action = event.getAction();

		// Check if the player right-clicked the air
		if (action == Action.RIGHT_CLICK_AIR) {
			triggerRightClick(event.getPlayer());
		}
	}

	// 2. Handles interacting with Mobs/Entities
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		// Prevent dual-firing from off hand
		if (event.getHand() != EquipmentSlot.HAND) return;

		// This triggers when right-clicking any entity (LivingEntity, Villager, Monster, etc.)
		triggerRightClick(event.getPlayer());
	}

	// Helper method to keep your trigger logic clean and unified
	private void triggerRightClick(Player player) {
		AbilityTrigger abilityTrigger = AbilityTrigger.RIGHT_CLICK;
		customAbilityService.triggerAbility(player, abilityTrigger);
	}

}
