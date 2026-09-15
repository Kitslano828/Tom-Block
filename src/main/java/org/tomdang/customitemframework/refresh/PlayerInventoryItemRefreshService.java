package org.tomdang.customitemframework.refresh;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.tomdang.customitemframework.CustomItemStackUpdater;
import org.tomdang.customitemframework.lore.ItemLoreContext;
import org.tomdang.player.stats.PlayerStatSnapshotFactory;

public final class PlayerInventoryItemRefreshService {

	private final PlayerStatSnapshotFactory statSnapshotFactory;
	private final CustomItemStackUpdater itemStackUpdater;

	public PlayerInventoryItemRefreshService(PlayerStatSnapshotFactory statSnapshotFactory,
	                                         CustomItemStackUpdater itemStackUpdater) {
		if (statSnapshotFactory == null) throw new IllegalArgumentException("statSnapshotFactory cannot be null");
		if (itemStackUpdater == null) throw new IllegalArgumentException("itemStackUpdater cannot be null");
		this.statSnapshotFactory = statSnapshotFactory;
		this.itemStackUpdater = itemStackUpdater;
	}

	public PlayerInventoryItemRefreshResult refresh(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		PlayerInventory inventory = player.getInventory();
		if (inventory == null) throw new IllegalStateException("player inventory cannot be null");
		ItemStack[] contents = inventory.getContents();
		if (contents == null) throw new IllegalStateException("player inventory contents cannot be null");

		ItemLoreContext context = new ItemLoreContext(statSnapshotFactory.create(player));
		int updated = 0;
		int skipped = 0;
		int failed = 0;

		for (ItemStack itemStack : contents) {
			if (itemStack == null || isAir(itemStack)) {
				skipped++;
				continue;
			}

			try {
				if (itemStackUpdater.update(itemStack, context)) {
					updated++;
				} else {
					skipped++;
				}
			} catch (RuntimeException exception) {
				failed++;
			}
		}

		return new PlayerInventoryItemRefreshResult(contents.length, updated, skipped, failed);
	}

	private boolean isAir(ItemStack itemStack) {
		Material material = itemStack.getType();
		return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR;
	}
}
