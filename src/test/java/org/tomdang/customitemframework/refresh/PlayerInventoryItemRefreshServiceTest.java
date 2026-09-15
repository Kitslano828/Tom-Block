package org.tomdang.customitemframework.refresh;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.tomdang.customitemframework.CustomItemStackUpdater;
import org.tomdang.customitemframework.lore.ItemLoreContext;
import org.tomdang.player.stats.PlayerStatSnapshot;
import org.tomdang.player.stats.PlayerStatSnapshotFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerInventoryItemRefreshServiceTest {

	@Test
	void createsOneSnapshotAndRefreshesEveryNonEmptySlot() {
		PlayerStatSnapshotFactory snapshotFactory = mock(PlayerStatSnapshotFactory.class);
		CustomItemStackUpdater updater = mock(CustomItemStackUpdater.class);
		PlayerInventoryItemRefreshService service = new PlayerInventoryItemRefreshService(snapshotFactory, updater);
		Player player = mock(Player.class);
		PlayerInventory inventory = mock(PlayerInventory.class);
		PlayerStatSnapshot snapshot = PlayerStatSnapshot.defaults();
		ItemStack updatedItem = item(Material.DIAMOND_BOOTS);
		ItemStack ordinaryItem = item(Material.STICK);
		ItemStack air = item(Material.AIR);

		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getContents()).thenReturn(new ItemStack[]{updatedItem, ordinaryItem, null, air});
		when(snapshotFactory.create(player)).thenReturn(snapshot);
		when(updater.update(same(updatedItem), any(ItemLoreContext.class))).thenReturn(true);
		when(updater.update(same(ordinaryItem), any(ItemLoreContext.class))).thenReturn(false);

		PlayerInventoryItemRefreshResult result = service.refresh(player);

		assertEquals(new PlayerInventoryItemRefreshResult(4, 1, 3, 0), result);
		verify(snapshotFactory).create(player);
		verify(updater).update(same(updatedItem), any(ItemLoreContext.class));
		verify(updater).update(same(ordinaryItem), any(ItemLoreContext.class));
		verify(updater, never()).update(same(air), any(ItemLoreContext.class));
		ArgumentCaptor<ItemLoreContext> contexts = ArgumentCaptor.forClass(ItemLoreContext.class);
		verify(updater, org.mockito.Mockito.times(2)).update(any(ItemStack.class), contexts.capture());
		assertSame(contexts.getAllValues().getFirst(), contexts.getAllValues().getLast());
	}

	@Test
	void oneBrokenItemDoesNotPreventLaterItemsFromRefreshing() {
		PlayerStatSnapshotFactory snapshotFactory = mock(PlayerStatSnapshotFactory.class);
		CustomItemStackUpdater updater = mock(CustomItemStackUpdater.class);
		PlayerInventoryItemRefreshService service = new PlayerInventoryItemRefreshService(snapshotFactory, updater);
		Player player = mock(Player.class);
		PlayerInventory inventory = mock(PlayerInventory.class);
		ItemStack brokenItem = item(Material.STONE);
		ItemStack validItem = item(Material.DIAMOND_SWORD);

		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getContents()).thenReturn(new ItemStack[]{brokenItem, validItem});
		when(snapshotFactory.create(player)).thenReturn(PlayerStatSnapshot.defaults());
		when(updater.update(same(brokenItem), any(ItemLoreContext.class))).thenThrow(new IllegalStateException("broken"));
		when(updater.update(same(validItem), any(ItemLoreContext.class))).thenReturn(true);

		assertEquals(new PlayerInventoryItemRefreshResult(2, 1, 0, 1), service.refresh(player));
		verify(updater).update(same(validItem), any(ItemLoreContext.class));
	}

	@Test
	void invalidDependenciesAndPlayerStateAreRejected() {
		PlayerStatSnapshotFactory snapshotFactory = mock(PlayerStatSnapshotFactory.class);
		CustomItemStackUpdater updater = mock(CustomItemStackUpdater.class);
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerInventoryItemRefreshService(null, updater));
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerInventoryItemRefreshService(snapshotFactory, null));

		PlayerInventoryItemRefreshService service = new PlayerInventoryItemRefreshService(snapshotFactory, updater);
		assertThrows(IllegalArgumentException.class, () -> service.refresh(null));

		Player playerWithoutInventory = mock(Player.class);
		assertThrows(IllegalStateException.class, () -> service.refresh(playerWithoutInventory));

		Player playerWithoutContents = mock(Player.class);
		PlayerInventory inventory = mock(PlayerInventory.class);
		when(playerWithoutContents.getInventory()).thenReturn(inventory);
		when(inventory.getContents()).thenReturn(null);
		assertThrows(IllegalStateException.class, () -> service.refresh(playerWithoutContents));
	}

	private ItemStack item(Material material) {
		ItemStack itemStack = mock(ItemStack.class);
		when(itemStack.getType()).thenReturn(material);
		return itemStack;
	}
}
