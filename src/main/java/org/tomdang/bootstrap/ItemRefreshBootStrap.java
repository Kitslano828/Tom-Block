package org.tomdang.bootstrap;

import lombok.Getter;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.CustomItemStackFactory;
import org.tomdang.customitemframework.CustomItemStackUpdater;
import org.tomdang.customitemframework.refresh.PlayerInventoryItemRefreshService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatSnapshotFactory;

public final class ItemRefreshBootStrap {

	@Getter
	private final PlayerInventoryItemRefreshService playerInventoryItemRefreshService;

	public ItemRefreshBootStrap(CustomItemResolver customItemResolver,
	                           CustomItemStackFactory customItemStackFactory,
	                           PlayerStatsService playerStatsService) {
		if (customItemResolver == null) throw new IllegalArgumentException("customItemResolver cannot be null");
		if (customItemStackFactory == null) throw new IllegalArgumentException("customItemStackFactory cannot be null");
		if (playerStatsService == null) throw new IllegalArgumentException("playerStatsService cannot be null");

		PlayerStatSnapshotFactory statSnapshotFactory = new PlayerStatSnapshotFactory(playerStatsService);
		CustomItemStackUpdater itemStackUpdater = new CustomItemStackUpdater(
				customItemResolver,
				customItemStackFactory
		);
		playerInventoryItemRefreshService = new PlayerInventoryItemRefreshService(
				statSnapshotFactory,
				itemStackUpdater
		);
	}
}
