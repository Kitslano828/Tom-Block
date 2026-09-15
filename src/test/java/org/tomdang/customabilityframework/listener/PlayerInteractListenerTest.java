package org.tomdang.customabilityframework.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.CustomAbilityService;
import org.tomdang.customabilityframework.trigger.AbilityTriggerResolver;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PlayerInteractListenerTest {

	@Test
	void nullDependenciesAreRejected() {
		CustomAbilityService service = mock(CustomAbilityService.class);
		AbilityTriggerResolver resolver = new AbilityTriggerResolver();

		assertThrows(IllegalArgumentException.class, () -> new PlayerInteractListener(null, resolver));
		assertThrows(IllegalArgumentException.class, () -> new PlayerInteractListener(service, null));
	}

	@Test
	void interactionUsesActionAndSneakingState() {
		CustomAbilityService service = mock(CustomAbilityService.class);
		PlayerInteractListener listener = listener(service);
		Player player = mock(Player.class);
		when(player.isSneaking()).thenReturn(true);
		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		when(event.getHand()).thenReturn(EquipmentSlot.HAND);
		when(event.getAction()).thenReturn(Action.LEFT_CLICK_BLOCK);
		when(event.getPlayer()).thenReturn(player);

		listener.onPlayerInteract(event);

		verify(service).triggerAbility(player, AbilityTrigger.SNEAK_LEFT_CLICK);
	}

	@Test
	void offHandInteractionIsIgnored() {
		CustomAbilityService service = mock(CustomAbilityService.class);
		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		when(event.getHand()).thenReturn(EquipmentSlot.OFF_HAND);

		listener(service).onPlayerInteract(event);

		verifyNoInteractions(service);
	}

	@Test
	void entityInteractionResolvesRightClick() {
		CustomAbilityService service = mock(CustomAbilityService.class);
		Player player = mock(Player.class);
		PlayerInteractEntityEvent event = mock(PlayerInteractEntityEvent.class);
		when(event.getHand()).thenReturn(EquipmentSlot.HAND);
		when(event.getPlayer()).thenReturn(player);

		listener(service).onPlayerInteractEntity(event);

		verify(service).triggerAbility(player, AbilityTrigger.RIGHT_CLICK);
	}

	@Test
	void playerDamageResolvesLeftClickAndNonPlayerDamageIsIgnored() {
		CustomAbilityService service = mock(CustomAbilityService.class);
		Player player = mock(Player.class);
		EntityDamageByEntityEvent playerEvent = mock(EntityDamageByEntityEvent.class);
		when(playerEvent.getDamager()).thenReturn(player);
		when(player.isSneaking()).thenReturn(false);
		EntityDamageByEntityEvent entityEvent = mock(EntityDamageByEntityEvent.class);
		when(entityEvent.getDamager()).thenReturn(mock(Entity.class));

		PlayerInteractListener listener = listener(service);
		listener.onPlayerDamageEntity(playerEvent);
		listener.onPlayerDamageEntity(entityEvent);

		verify(service).triggerAbility(player, AbilityTrigger.LEFT_CLICK);
	}

	@Test
	void sneakTriggersOnlyWhenCrouchingStarts() {
		CustomAbilityService service = mock(CustomAbilityService.class);
		Player player = mock(Player.class);
		PlayerToggleSneakEvent startEvent = mock(PlayerToggleSneakEvent.class);
		when(startEvent.isSneaking()).thenReturn(true);
		when(startEvent.getPlayer()).thenReturn(player);
		PlayerToggleSneakEvent stopEvent = mock(PlayerToggleSneakEvent.class);
		when(stopEvent.isSneaking()).thenReturn(false);
		when(stopEvent.getPlayer()).thenReturn(player);

		PlayerInteractListener listener = listener(service);
		listener.onPlayerToggleSneak(startEvent);
		listener.onPlayerToggleSneak(stopEvent);

		verify(service).triggerAbility(player, AbilityTrigger.SNEAK);
		verify(service, never()).triggerAbility(player, null);
	}

	private PlayerInteractListener listener(CustomAbilityService service) {
		return new PlayerInteractListener(service, new AbilityTriggerResolver());
	}
}
