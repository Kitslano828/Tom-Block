package org.tomdang.region.edit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.override.RegionOverrideState;
import org.tomdang.region.position.BlockPosition;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegionBrushListenerTest {
	@Test
	void mapsBrushClicksToIncludeExcludeAndClear() {
		assertApplied(Action.LEFT_CLICK_BLOCK, false, RegionOverrideState.INCLUSION);
		assertApplied(Action.RIGHT_CLICK_BLOCK, false, RegionOverrideState.EXCLUSION);
		assertApplied(Action.RIGHT_CLICK_BLOCK, true, RegionOverrideState.NONE);
	}

	@Test
	void ignoresOffHandAndOrdinaryItems() {
		RegionBrushItemService brushItems = mock(RegionBrushItemService.class);
		RegionEditingService editing = mock(RegionEditingService.class);
		RegionBrushListener listener = new RegionBrushListener(
				brushItems, editing, new BukkitBlockPositionAdapter());
		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		when(event.getHand()).thenReturn(EquipmentSlot.OFF_HAND);

		listener.onBrushUse(event);

		verify(editing, never()).apply(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
	}

	@Test
	void cancelsBrushUseButRequiresPermissionAndSession() {
		RegionBrushItemService brushItems = mock(RegionBrushItemService.class);
		RegionEditingService editing = mock(RegionEditingService.class);
		RegionBrushListener listener = new RegionBrushListener(
				brushItems, editing, new BukkitBlockPositionAdapter());
		PlayerInteractEvent event = event(Action.LEFT_CLICK_BLOCK, false, brushItems);
		Player player = event.getPlayer();
		when(player.hasPermission("tomblock.admin.region.edit")).thenReturn(false);

		listener.onBrushUse(event);

		verify(event).setCancelled(true);
		verify(editing, never()).apply(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
	}

	private void assertApplied(Action action, boolean sneaking, RegionOverrideState expected) {
		RegionBrushItemService brushItems = mock(RegionBrushItemService.class);
		RegionEditingService editing = mock(RegionEditingService.class);
		RegionBrushListener listener = new RegionBrushListener(
				brushItems, editing, new BukkitBlockPositionAdapter());
		PlayerInteractEvent event = event(action, sneaking, brushItems);
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		BlockPosition position = new BlockPosition("world", 4, 70, 6);
		when(player.hasPermission("tomblock.admin.region.edit")).thenReturn(true);
		when(editing.session(playerId)).thenReturn(Optional.of(mock(RegionEditSession.class)));
		when(editing.apply(playerId, position, expected)).thenReturn(
				new RegionEditAction("VILLAGE", position, RegionOverrideState.NONE, expected));

		listener.onBrushUse(event);

		verify(event).setCancelled(true);
		verify(editing).apply(playerId, position, expected);
	}

	private PlayerInteractEvent event(Action action, boolean sneaking, RegionBrushItemService brushItems) {
		World world = mock(World.class);
		when(world.getName()).thenReturn("world");
		Block block = mock(Block.class);
		when(block.getLocation()).thenReturn(new Location(world, 4, 70, 6));
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		when(player.isSneaking()).thenReturn(sneaking);
		ItemStack item = mock(ItemStack.class);
		when(brushItems.isBrush(item)).thenReturn(true);
		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		when(event.getHand()).thenReturn(EquipmentSlot.HAND);
		when(event.getItem()).thenReturn(item);
		when(event.getClickedBlock()).thenReturn(block);
		when(event.getAction()).thenReturn(action);
		when(event.getPlayer()).thenReturn(player);
		return event;
	}
}
